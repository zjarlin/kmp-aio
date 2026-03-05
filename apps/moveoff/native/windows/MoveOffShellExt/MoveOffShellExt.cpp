#include "MoveOffShellExt.h"
#include <shlwapi.h>
#include <shellapi.h>
#include <json/json.h>  // 需要 vcpkg 安装 jsoncpp

#pragma comment(lib, "shlwapi.lib")
#pragma comment(lib, "shell32.lib")

// 命名管道名称
const wchar_t* IPCClient::PIPE_NAME = L"\\\\.\\pipe\\MoveOff";

// IPC 客户端实现
IPCClient::IPCClient() : hPipe(INVALID_HANDLE_VALUE) {
}

IPCClient::~IPCClient() {
    Disconnect();
}

bool IPCClient::Connect() {
    hPipe = CreateFile(
        PIPE_NAME,
        GENERIC_READ | GENERIC_WRITE,
        0,
        NULL,
        OPEN_EXISTING,
        0,
        NULL
    );
    
    if (hPipe == INVALID_HANDLE_VALUE) {
        // 尝试连接到 TCP 回环地址（备用方案）
        return false;
    }
    
    DWORD dwMode = PIPE_READMODE_MESSAGE;
    SetNamedPipeHandleState(hPipe, &dwMode, NULL, NULL);
    
    return true;
}

void IPCClient::Disconnect() {
    if (hPipe != INVALID_HANDLE_VALUE) {
        CloseHandle(hPipe);
        hPipe = INVALID_HANDLE_VALUE;
    }
}

std::string IPCClient::SendRequest(const std::string& jsonRequest) {
    if (!Connect()) {
        return "{\"error\": \"无法连接到 MoveOff 应用\"}";
    }
    
    DWORD written;
    WriteFile(hPipe, jsonRequest.c_str(), jsonRequest.length(), &written, NULL);
    
    char buffer[4096];
    DWORD read;
    BOOL success = ReadFile(hPipe, buffer, sizeof(buffer) - 1, &read, NULL);
    
    Disconnect();
    
    if (success && read > 0) {
        buffer[read] = '\0';
        return std::string(buffer);
    }
    
    return "{\"error\": \"读取响应失败\"}";
}

// Shell 扩展实现
MoveOffContextMenu::MoveOffContextMenu() : m_cRef(1) {
}

MoveOffContextMenu::~MoveOffContextMenu() {
}

IFACEMETHODIMP MoveOffContextMenu::QueryInterface(REFIID riid, void** ppv) {
    static const QITAB qit[] = {
        QITABENT(MoveOffContextMenu, IShellExtInit),
        QITABENT(MoveOffContextMenu, IContextMenu),
        { 0 }
    };
    return QISearch(this, qit, riid, ppv);
}

IFACEMETHODIMP_(ULONG) MoveOffContextMenu::AddRef() {
    return InterlockedIncrement(&m_cRef);
}

IFACEMETHODIMP_(ULONG) MoveOffContextMenu::Release() {
    ULONG cRef = InterlockedDecrement(&m_cRef);
    if (cRef == 0) {
        delete this;
    }
    return cRef;
}

IFACEMETHODIMP MoveOffContextMenu::Initialize(LPCITEMIDLIST pidlFolder, 
                                               LPDATAOBJECT pdtobj, 
                                               HKEY hkeyProgID) {
    if (pdtobj == NULL) {
        return E_INVALIDARG;
    }
    
    FORMATETC fmt = { CF_HDROP, NULL, DVASPECT_CONTENT, -1, TYMED_HGLOBAL };
    STGMEDIUM stg = { TYMED_HGLOBAL };
    
    if (FAILED(pdtobj->GetData(&fmt, &stg))) {
        return E_FAIL;
    }
    
    HDROP hDrop = static_cast<HDROP>(GlobalLock(stg.hGlobal));
    if (hDrop == NULL) {
        ReleaseStgMedium(&stg);
        return E_FAIL;
    }
    
    UINT fileCount = DragQueryFile(hDrop, 0xFFFFFFFF, NULL, 0);
    for (UINT i = 0; i < fileCount; i++) {
        wchar_t path[MAX_PATH];
        if (DragQueryFile(hDrop, i, path, MAX_PATH)) {
            m_selectedFiles.push_back(path);
        }
    }
    
    GlobalUnlock(stg.hGlobal);
    ReleaseStgMedium(&stg);
    
    return S_OK;
}

IFACEMETHODIMP MoveOffContextMenu::QueryContextMenu(HMENU hmenu, 
                                                    UINT indexMenu, 
                                                    UINT idCmdFirst, 
                                                    UINT idCmdLast, 
                                                    UINT uFlags) {
    if (uFlags & CMF_DEFAULTONLY) {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, 0);
    }
    
    if (m_selectedFiles.empty()) {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, 0);
    }
    
    // 检查是否在 MoveOff 同步目录内
    wchar_t syncRoot[MAX_PATH];
    ExpandEnvironmentStringsW(L"%USERPROFILE%\\MoveOff", syncRoot, MAX_PATH);
    
    bool inSyncDir = false;
    for (const auto& file : m_selectedFiles) {
        if (wcsncmp(file.c_str(), syncRoot, wcslen(syncRoot)) == 0) {
            inSyncDir = true;
            break;
        }
    }
    
    if (!inSyncDir) {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, 0);
    }
    
    // 创建菜单
    HMENU hSubMenu = CreatePopupMenu();
    
    InsertMenu(hSubMenu, 0, MF_BYPOSITION | MF_STRING, idCmdFirst + ID_SYNC_NOW, L"MoveOff - 立即同步");
    InsertMenu(hSubMenu, 1, MF_BYPOSITION | MF_STRING, idCmdFirst + ID_SHOW_IN_APP, L"MoveOff - 在应用中显示");
    InsertMenu(hSubMenu, 2, MF_BYPOSITION | MF_SEPARATOR, 0, NULL);
    
    // 检查是否有冲突
    bool hasConflict = false;
    for (const auto& file : m_selectedFiles) {
        std::string status = GetFileStatus(file);
        if (status.find("CONFLICT") != std::string::npos) {
            hasConflict = true;
            break;
        }
    }
    
    if (hasConflict) {
        InsertMenu(hSubMenu, 3, MF_BYPOSITION | MF_STRING, idCmdFirst + ID_RESOLVE_CONFLICT, L"MoveOff - 解决冲突");
    }
    
    InsertMenu(hSubMenu, 4, MF_BYPOSITION | MF_STRING, idCmdFirst + ID_GET_SHARE_LINK, L"MoveOff - 获取共享链接");
    
    // 插入主菜单项
    MENUITEMINFO mii = { sizeof(mii) };
    mii.fMask = MIIM_SUBMENU | MIIM_STRING | MIIM_ID;
    mii.wID = idCmdFirst + ID_MAX;
    mii.hSubMenu = hSubMenu;
    mii.dwTypeData = const_cast<LPWSTR>(L"MoveOff");
    
    InsertMenuItem(hmenu, indexMenu, TRUE, &mii);
    
    return MAKE_HRESULT(SEVERITY_SUCCESS, 0, ID_MAX + 1);
}

IFACEMETHODIMP MoveOffContextMenu::InvokeCommand(LPCMINVOKECOMMANDINFO pici) {
    if (HIWORD(pici->lpVerb) != 0) {
        return E_INVALIDARG;
    }
    
    UINT idCmd = LOWORD(pici->lpVerb);
    if (idCmd >= ID_MAX) {
        return E_INVALIDARG;
    }
    
    if (m_selectedFiles.empty()) {
        return E_FAIL;
    }
    
    const std::wstring& path = m_selectedFiles[0];
    
    switch (idCmd) {
        case ID_SYNC_NOW:
            SyncFile(path);
            break;
        case ID_SHOW_IN_APP:
            ShowInApp(path);
            break;
        case ID_RESOLVE_CONFLICT:
            ResolveConflict(path);
            break;
        case ID_GET_SHARE_LINK:
            // TODO: 实现获取共享链接
            break;
    }
    
    return S_OK;
}

IFACEMETHODIMP MoveOffContextMenu::GetCommandString(UINT_PTR idCmd, 
                                                    UINT uType, 
                                                    UINT* pReserved, 
                                                    CHAR* pszName, 
                                                    UINT cchMax) {
    if (idCmd >= ID_MAX) {
        return E_INVALIDARG;
    }
    
    if (uType == GCS_HELPTEXT) {
        const char* helpText = "";
        switch (idCmd) {
            case ID_SYNC_NOW:
                helpText = "立即同步选中的文件到云端";
                break;
            case ID_SHOW_IN_APP:
                helpText = "在 MoveOff 应用中显示此文件";
                break;
            case ID_RESOLVE_CONFLICT:
                helpText = "解决文件同步冲突";
                break;
            case ID_GET_SHARE_LINK:
                helpText = "获取文件的共享链接";
                break;
        }
        
        if (pici->cbSize >= sizeof(CMINVOKECOMMANDINFOEX) &&
            (pici->fMask & CMIC_MASK_UNICODE)) {
            LPCMINVOKECOMMANDINFOEX piciex = (LPCMINVOKECOMMANDINFOEX)pici;
            lstrcpynW(piciex->lpParametersW, helpTextW, cchMax);
        } else {
            lstrcpynA(pszName, helpText, cchMax);
        }
    }
    
    return S_OK;
}

std::string MoveOffContextMenu::GetFileStatus(const std::wstring& path) {
    // 转换路径为 UTF-8
    int size = WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, NULL, 0, NULL, NULL);
    std::string utf8Path(size - 1, 0);
    WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, &utf8Path[0], size, NULL, NULL);
    
    // 构建 JSON 请求
    Json::Value request;
    request["action"] = "GET_FILE_STATUS";
    request["path"] = utf8Path;
    
    Json::FastWriter writer;
    std::string jsonRequest = writer.write(request);
    
    return m_ipcClient.SendRequest(jsonRequest);
}

void MoveOffContextMenu::SyncFile(const std::wstring& path) {
    int size = WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, NULL, 0, NULL, NULL);
    std::string utf8Path(size - 1, 0);
    WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, &utf8Path[0], size, NULL, NULL);
    
    Json::Value request;
    request["action"] = "TRIGGER_SYNC";
    request["path"] = utf8Path;
    
    Json::FastWriter writer;
    std::string jsonRequest = writer.write(request);
    
    m_ipcClient.SendRequest(jsonRequest);
}

void MoveOffContextMenu::ShowInApp(const std::wstring& path) {
    int size = WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, NULL, 0, NULL, NULL);
    std::string utf8Path(size - 1, 0);
    WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, &utf8Path[0], size, NULL, NULL);
    
    Json::Value request;
    request["action"] = "SHOW_IN_APP";
    request["path"] = utf8Path;
    
    Json::FastWriter writer;
    std::string jsonRequest = writer.write(request);
    
    m_ipcClient.SendRequest(jsonRequest);
}

void MoveOffContextMenu::ResolveConflict(const std::wstring& path) {
    int size = WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, NULL, 0, NULL, NULL);
    std::string utf8Path(size - 1, 0);
    WideCharToMultiByte(CP_UTF8, 0, path.c_str(), -1, &utf8Path[0], size, NULL, NULL);
    
    Json::Value request;
    request["action"] = "RESOLVE_CONFLICT";
    request["path"] = utf8Path;
    
    Json::FastWriter writer;
    std::string jsonRequest = writer.write(request);
    
    m_ipcClient.SendRequest(jsonRequest);
}

// 类工厂
class ClassFactory : public IClassFactory {
public:
    IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv) {
        if (IsEqualIID(riid, IID_IUnknown) || IsEqualIID(riid, IID_IClassFactory)) {
            *ppv = this;
            AddRef();
            return S_OK;
        }
        *ppv = NULL;
        return E_NOINTERFACE;
    }
    
    IFACEMETHODIMP_(ULONG) AddRef() { return 1; }
    IFACEMETHODIMP_(ULONG) Release() { return 1; }
    
    IFACEMETHODIMP CreateInstance(IUnknown* pUnkOuter, REFIID riid, void** ppv) {
        if (pUnkOuter != NULL) return CLASS_E_NOAGGREGATION;
        
        MoveOffContextMenu* pExt = new (std::nothrow) MoveOffContextMenu();
        if (!pExt) return E_OUTOFMEMORY;
        
        HRESULT hr = pExt->QueryInterface(riid, ppv);
        pExt->Release();
        return hr;
    }
    
    IFACEMETHODIMP LockServer(BOOL fLock) {
        return S_OK;
    }
};

// DLL 导出函数
STDAPI DllGetClassObject(REFCLSID rclsid, REFIID riid, void** ppv) {
    if (IsEqualCLSID(rclsid, CLSID_MoveOffContextMenu)) {
        static ClassFactory cf;
        return cf.QueryInterface(riid, ppv);
    }
    return CLASS_E_CLASSNOTAVAILABLE;
}

STDAPI DllCanUnloadNow() {
    return S_OK;
}

STDAPI DllRegisterServer() {
    HKEY hKey;
    wchar_t clsidStr[39];
    StringFromGUID2(CLSID_MoveOffContextMenu, clsidStr, 39);
    
    // 注册 CLSID
    std::wstring clsidKey = L"CLSID\\" + std::wstring(clsidStr);
    if (RegCreateKeyExW(HKEY_CLASSES_ROOT, clsidKey.c_str(), 0, NULL, 0, 
                        KEY_WRITE, NULL, &hKey, NULL) == ERROR_SUCCESS) {
        RegSetValueExW(hKey, NULL, 0, REG_SZ, (BYTE*)L"MoveOff Context Menu", 
                       sizeof(L"MoveOff Context Menu"));
        
        HKEY hInproc;
        if (RegCreateKeyExW(hKey, L"InprocServer32", 0, NULL, 0,
                           KEY_WRITE, NULL, &hInproc, NULL) == ERROR_SUCCESS) {
            wchar_t modulePath[MAX_PATH];
            GetModuleFileNameW(NULL, modulePath, MAX_PATH);
            RegSetValueExW(hInproc, NULL, 0, REG_SZ, (BYTE*)modulePath, 
                          (wcslen(modulePath) + 1) * sizeof(wchar_t));
            RegSetValueExW(hInproc, L"ThreadingModel", 0, REG_SZ, 
                          (BYTE*)L"Apartment", sizeof(L"Apartment"));
            RegCloseKey(hInproc);
        }
        RegCloseKey(hKey);
    }
    
    // 注册到右键菜单
    std::wstring menuKey = L"*\\shellex\\ContextMenuHandlers\\MoveOff";
    if (RegCreateKeyExW(HKEY_CLASSES_ROOT, menuKey.c_str(), 0, NULL, 0,
                       KEY_WRITE, NULL, &hKey, NULL) == ERROR_SUCCESS) {
        RegSetValueExW(hKey, NULL, 0, REG_SZ, (BYTE*)clsidStr, 
                      (wcslen(clsidStr) + 1) * sizeof(wchar_t));
        RegCloseKey(hKey);
    }
    
    // 注册目录右键菜单
    std::wstring dirKey = L"Directory\\shellex\\ContextMenuHandlers\\MoveOff";
    if (RegCreateKeyExW(HKEY_CLASSES_ROOT, dirKey.c_str(), 0, NULL, 0,
                       KEY_WRITE, NULL, &hKey, NULL) == ERROR_SUCCESS) {
        RegSetValueExW(hKey, NULL, 0, REG_SZ, (BYTE*)clsidStr, 
                      (wcslen(clsidStr) + 1) * sizeof(wchar_t));
        RegCloseKey(hKey);
    }
    
    return S_OK;
}

STDAPI DllUnregisterServer() {
    wchar_t clsidStr[39];
    StringFromGUID2(CLSID_MoveOffContextMenu, clsidStr, 39);
    
    // 删除注册表项
    std::wstring clsidKey = L"CLSID\\" + std::wstring(clsidStr);
    RegDeleteTreeW(HKEY_CLASSES_ROOT, clsidKey.c_str());
    RegDeleteTreeW(HKEY_CLASSES_ROOT, L"*\\shellex\\ContextMenuHandlers\\MoveOff");
    RegDeleteTreeW(HKEY_CLASSES_ROOT, L"Directory\\shellex\\ContextMenuHandlers\\MoveOff");
    
    return S_OK;
}
