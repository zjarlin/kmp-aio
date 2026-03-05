#include "MoveOffShellExt.h"
#include <strsafe.h>
#include <shlwapi.h>

#pragma comment(lib, "shlwapi.lib")

// 全局变量
HINSTANCE g_hInst = NULL;
LONG g_cDllRef = 0;

// DLL 入口点
BOOL APIENTRY DllMain(HMODULE hModule, DWORD dwReason, LPVOID lpReserved) {
    switch (dwReason) {
    case DLL_PROCESS_ATTACH:
        g_hInst = hModule;
        DisableThreadLibraryCalls(hModule);
        break;
    }
    return TRUE;
}

// 导出函数
STDAPI DllGetClassObject(REFCLSID rclsid, REFIID riid, void** ppv) {
    if (!IsEqualCLSID(rclsid, CLSID_MoveOffContextMenu)) {
        return CLASS_E_CLASSNOTAVAILABLE;
    }

    CClassFactory* pClassFactory = new CClassFactory();
    if (!pClassFactory) {
        return E_OUTOFMEMORY;
    }

    HRESULT hr = pClassFactory->QueryInterface(riid, ppv);
    pClassFactory->Release();
    return hr;
}

STDAPI DllCanUnloadNow(void) {
    return (g_cDllRef > 0) ? S_FALSE : S_OK;
}

STDAPI DllRegisterServer(void) {
    HRESULT hr;
    WCHAR szModule[MAX_PATH];

    if (!GetModuleFileName(g_hInst, szModule, ARRAYSIZE(szModule))) {
        return HRESULT_FROM_WIN32(GetLastError());
    }

    // 注册组件
    hr = RegisterServer(szModule, CLSID_MoveOffContextMenu,
                        L"MoveOff Context Menu Extension",
                        L"Apartment");
    if (SUCCEEDED(hr)) {
        // 注册到所有文件的上下文菜单
        hr = RegisterShellExtContextMenuHandler(
            L"*",
            CLSID_MoveOffContextMenu,
            L"MoveOffExt");
    }

    if (SUCCEEDED(hr)) {
        // 注册到文件夹的上下文菜单
        hr = RegisterShellExtContextMenuHandler(
            L"Directory",
            CLSID_MoveOffContextMenu,
            L"MoveOffExt");
    }

    // 刷新 shell
    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, NULL, NULL);

    return hr;
}

STDAPI DllUnregisterServer(void) {
    HRESULT hr = UnregisterServer(CLSID_MoveOffContextMenu);

    if (SUCCEEDED(hr)) {
        hr = UnregisterShellExtContextMenuHandler(L"*", L"MoveOffExt");
    }

    if (SUCCEEDED(hr)) {
        hr = UnregisterShellExtContextMenuHandler(L"Directory", L"MoveOffExt");
    }

    SHChangeNotify(SHCNE_ASSOCCHANGED, SHCNF_IDLIST, NULL, NULL);

    return hr;
}

// CMoveOffContextMenu 实现

CMoveOffContextMenu::CMoveOffContextMenu() : m_cRef(1), m_status(FileSyncStatus::UNKNOWN) {
    InterlockedIncrement(&g_cDllRef);
}

CMoveOffContextMenu::~CMoveOffContextMenu() {
    InterlockedDecrement(&g_cDllRef);
}

IFACEMETHODIMP CMoveOffContextMenu::QueryInterface(REFIID riid, void** ppv) {
    static const QITAB qit[] = {
        QITABENT(CMoveOffContextMenu, IShellExtInit),
        QITABENT(CMoveOffContextMenu, IContextMenu),
        {0},
    };
    return QISearch(this, qit, riid, ppv);
}

IFACEMETHODIMP_(ULONG) CMoveOffContextMenu::AddRef() {
    return InterlockedIncrement(&m_cRef);
}

IFACEMETHODIMP_(ULONG) CMoveOffContextMenu::Release() {
    ULONG cRef = InterlockedDecrement(&m_cRef);
    if (cRef == 0) {
        delete this;
    }
    return cRef;
}

IFACEMETHODIMP CMoveOffContextMenu::Initialize(
    LPCITEMIDLIST pidlFolder,
    LPDATAOBJECT pdtobj,
    HKEY hkeyProgID) {

    if (!pdtobj) {
        return E_INVALIDARG;
    }

    // 获取选中的文件
    FORMATETC fmt = {CF_HDROP, NULL, DVASPECT_CONTENT, -1, TYMED_HGLOBAL};
    STGMEDIUM stg = {TYMED_HGLOBAL};

    if (FAILED(pdtobj->GetData(&fmt, &stg))) {
        return E_FAIL;
    }

    HDROP hDrop = static_cast<HDROP>(GlobalLock(stg.hGlobal));
    if (!hDrop) {
        ReleaseStgMedium(&stg);
        return E_FAIL;
    }

    UINT nFiles = DragQueryFile(hDrop, 0xFFFFFFFF, NULL, 0);
    for (UINT i = 0; i < nFiles; i++) {
        WCHAR szPath[MAX_PATH];
        if (DragQueryFile(hDrop, i, szPath, ARRAYSIZE(szPath))) {
            m_selectedFiles.push_back(szPath);
        }
    }

    GlobalUnlock(stg.hGlobal);
    ReleaseStgMedium(&stg);

    // 获取第一个文件的状态
    if (!m_selectedFiles.empty()) {
        m_status = GetFileStatus(m_selectedFiles[0]);
    }

    return S_OK;
}

IFACEMETHODIMP CMoveOffContextMenu::QueryContextMenu(
    HMENU hmenu,
    UINT indexMenu,
    UINT idCmdFirst,
    UINT idCmdLast,
    UINT uFlags) {

    if (uFlags & CMF_DEFAULTONLY) {
        return MAKE_HRESULT(SEVERITY_SUCCESS, 0, 0);
    }

    // 创建子菜单
    HMENU hSubMenu = CreatePopupMenu();
    if (!hSubMenu) {
        return E_FAIL;
    }

    UINT idCmd = idCmdFirst;

    // 添加菜单项
    InsertMenu(hSubMenu, 0, MF_BYPOSITION | MF_STRING, idCmd + CMD_SYNC_NOW, L"立即同步");

    if (m_status == FileSyncStatus::CONFLICT) {
        InsertMenu(hSubMenu, 1, MF_BYPOSITION | MF_STRING, idCmd + CMD_RESOLVE_CONFLICT, L"解决冲突...");
    }

    InsertMenu(hSubMenu, 2, MF_BYPOSITION | MF_STRING, idCmd + CMD_SHOW_IN_APP, L"在 MoveOff 中显示");

    if (m_status == FileSyncStatus::SYNCED) {
        InsertMenu(hSubMenu, 3, MF_BYPOSITION | MF_STRING, idCmd + CMD_SHARE_LINK, L"获取共享链接");
    }

    // 添加分隔线
    InsertMenu(hmenu, indexMenu, MF_BYPOSITION | MF_SEPARATOR, 0, NULL);

    // 添加子菜单到上下文菜单
    MENUITEMINFO mii = {sizeof(mii)};
    mii.fMask = MIIM_SUBMENU | MIIM_STRING | MIIM_ID;
    mii.wID = idCmd;
    mii.hSubMenu = hSubMenu;

    // 根据状态设置菜单标题
    std::wstring menuTitle = GetStatusTitle(m_status);
    mii.dwTypeData = const_cast<LPWSTR>(menuTitle.c_str());

    InsertMenuItem(hmenu, indexMenu + 1, TRUE, &mii);

    // 添加分隔线
    InsertMenu(hmenu, indexMenu + 2, MF_BYPOSITION | MF_SEPARATOR, 0, NULL);

    return MAKE_HRESULT(SEVERITY_SUCCESS, 0, idCmd - idCmdFirst + 5);
}

IFACEMETHODIMP CMoveOffContextMenu::InvokeCommand(LPCMINVOKECOMMANDINFO pici) {
    if (!pici || pici->cbSize < sizeof(CMINVOKECOMMANDINFO)) {
        return E_INVALIDARG;
    }

    if (HIWORD(pici->lpVerb)) {
        return E_INVALIDARG;
    }

    UINT idCmd = LOWORD(pici->lpVerb);

    if (m_selectedFiles.empty()) {
        return E_FAIL;
    }

    const std::wstring& path = m_selectedFiles[0];

    switch (idCmd) {
    case CMD_SYNC_NOW:
        TriggerSync(path);
        break;
    case CMD_SHOW_IN_APP:
        ShowInApp(path);
        break;
    case CMD_RESOLVE_CONFLICT:
        ResolveConflict(path);
        break;
    case CMD_SHARE_LINK:
        // 复制共享链接到剪贴板
        if (OpenClipboard(NULL)) {
            EmptyClipboard();
            // TODO: 获取实际的共享链接
            const wchar_t* link = L"https://moveoff.example.com/s/xxx";
            HGLOBAL hMem = GlobalAlloc(GMEM_MOVEABLE, (wcslen(link) + 1) * sizeof(wchar_t));
            if (hMem) {
                memcpy(GlobalLock(hMem), link, (wcslen(link) + 1) * sizeof(wchar_t));
                GlobalUnlock(hMem);
                SetClipboardData(CF_UNICODETEXT, hMem);
            }
            CloseClipboard();
        }
        break;
    default:
        return E_INVALIDARG;
    }

    return S_OK;
}

IFACEMETHODIMP CMoveOffContextMenu::GetCommandString(
    UINT_PTR idCmd,
    UINT uFlags,
    UINT* pwReserved,
    LPSTR pszName,
    UINT cchMax) {

    if (uFlags == GCS_HELPTEXT) {
        switch (idCmd) {
        case CMD_SYNC_NOW:
            StringCchCopy(reinterpret_cast<LPWSTR>(pszName), cchMax, L"立即同步文件到云端");
            break;
        case CMD_SHOW_IN_APP:
            StringCchCopy(reinterpret_cast<LPWSTR>(pszName), cchMax, L"在 MoveOff 应用中显示文件");
            break;
        default:
            return E_INVALIDARG;
        }
        return S_OK;
    }

    return E_INVALIDARG;
}

// 与主应用通信

FileSyncStatus CMoveOffContextMenu::GetFileStatus(const std::wstring& path) {
    // TODO: 通过 IPC 与主应用通信获取文件状态
    // 暂时返回 UNKNOWN
    return FileSyncStatus::UNKNOWN;
}

void CMoveOffContextMenu::TriggerSync(const std::wstring& path) {
    // TODO: 通过 IPC 触发同步
    // 使用命名管道或 TCP 与主应用通信
}

void CMoveOffContextMenu::ShowInApp(const std::wstring& path) {
    // TODO: 通过 IPC 打开主窗口
}

void CMoveOffContextMenu::ResolveConflict(const std::wstring& path) {
    // TODO: 通过 IPC 打开冲突解决窗口
}

std::wstring GetStatusTitle(FileSyncStatus status) {
    switch (status) {
    case FileSyncStatus::SYNCED: return L"MoveOff (已同步)";
    case FileSyncStatus::SYNCING: return L"MoveOff (同步中...)";
    case FileSyncStatus::PENDING_UPLOAD: return L"MoveOff (等待上传)";
    case FileSyncStatus::PENDING_DOWNLOAD: return L"MoveOff (等待下载)";
    case FileSyncStatus::CONFLICT: return L"MoveOff (冲突)";
    case FileSyncStatus::ERROR: return L"MoveOff (错误)";
    default: return L"MoveOff";
    }
}

// CClassFactory 实现

IFACEMETHODIMP CClassFactory::QueryInterface(REFIID riid, void** ppv) {
    if (IsEqualIID(riid, IID_IUnknown) || IsEqualIID(riid, IID_IClassFactory)) {
        *ppv = static_cast<IClassFactory*>(this);
        AddRef();
        return S_OK;
    }
    *ppv = NULL;
    return E_NOINTERFACE;
}

IFACEMETHODIMP_(ULONG) CClassFactory::AddRef() {
    return InterlockedIncrement(&g_cDllRef);
}

IFACEMETHODIMP_(ULONG) CClassFactory::Release() {
    ULONG cRef = InterlockedDecrement(&g_cDllRef);
    if (cRef == 0) {
        // 不删除，因为这是静态对象
    }
    return cRef;
}

IFACEMETHODIMP CClassFactory::CreateInstance(IUnknown* pUnkOuter, REFIID riid, void** ppv) {
    if (pUnkOuter != NULL) {
        return CLASS_E_NOAGGREGATION;
    }

    CMoveOffContextMenu* pExt = new CMoveOffContextMenu();
    if (!pExt) {
        return E_OUTOFMEMORY;
    }

    HRESULT hr = pExt->QueryInterface(riid, ppv);
    pExt->Release();
    return hr;
}

IFACEMETHODIMP CClassFactory::LockServer(BOOL fLock) {
    if (fLock) {
        InterlockedIncrement(&g_cDllRef);
    } else {
        InterlockedDecrement(&g_cDllRef);
    }
    return S_OK;
}

// 注册表操作函数

HRESULT RegisterServer(
    PCWSTR pszModule,
    const CLSID& clsid,
    PCWSTR pszFriendlyName,
    PCWSTR pszThreadingModel) {

    HKEY hKey = NULL;
    HKEY hSubKey = NULL;
    HRESULT hr;

    wchar_t szCLSID[MAX_PATH];
    StringFromGUID2(clsid, szCLSID, ARRAYSIZE(szCLSID));

    // 创建 CLSID 键
    std::wstring clsidKey = L"CLSID\\";
    clsidKey += szCLSID;

    hr = HRESULT_FROM_WIN32(RegCreateKeyEx(
        HKEY_CLASSES_ROOT,
        clsidKey.c_str(),
        0, NULL,
        REG_OPTION_NON_VOLATILE,
        KEY_WRITE,
        NULL,
        &hKey,
        NULL));

    if (SUCCEEDED(hr)) {
        hr = HRESULT_FROM_WIN32(RegSetValueEx(
            hKey, NULL, 0, REG_SZ,
            reinterpret_cast<const BYTE*>(pszFriendlyName),
            (lstrlen(pszFriendlyName) + 1) * sizeof(wchar_t)));
    }

    if (SUCCEEDED(hr)) {
        hr = HRESULT_FROM_WIN32(RegCreateKeyEx(
            hKey, L"InprocServer32", 0, NULL,
            REG_OPTION_NON_VOLATILE,
            KEY_WRITE,
            NULL,
            &hSubKey,
            NULL));
    }

    if (SUCCEEDED(hr)) {
        hr = HRESULT_FROM_WIN32(RegSetValueEx(
            hSubKey, NULL, 0, REG_SZ,
            reinterpret_cast<const BYTE*>(pszModule),
            (lstrlen(pszModule) + 1) * sizeof(wchar_t)));
    }

    if (SUCCEEDED(hr)) {
        hr = HRESULT_FROM_WIN32(RegSetValueEx(
            hSubKey, L"ThreadingModel", 0, REG_SZ,
            reinterpret_cast<const BYTE*>(pszThreadingModel),
            (lstrlen(pszThreadingModel) + 1) * sizeof(wchar_t)));
    }

    if (hSubKey) RegCloseKey(hSubKey);
    if (hKey) RegCloseKey(hKey);

    return hr;
}

HRESULT UnregisterServer(const CLSID& clsid) {
    wchar_t szCLSID[MAX_PATH];
    StringFromGUID2(clsid, szCLSID, ARRAYSIZE(szCLSID));

    std::wstring clsidKey = L"CLSID\\";
    clsidKey += szCLSID;

    // 删除 InprocServer32 键
    std::wstring inprocKey = clsidKey + L"\\InprocServer32";
    RegDeleteKey(HKEY_CLASSES_ROOT, inprocKey.c_str());

    // 删除 CLSID 键
    RegDeleteKey(HKEY_CLASSES_ROOT, clsidKey.c_str());

    return S_OK;
}

HRESULT RegisterShellExtContextMenuHandler(
    PCWSTR pszFileType,
    const CLSID& clsid,
    PCWSTR pszFriendlyName) {

    HKEY hKey = NULL;
    HRESULT hr;

    wchar_t szCLSID[MAX_PATH];
    StringFromGUID2(clsid, szCLSID, ARRAYSIZE(szCLSID));

    // 创建 ShellEx\\ContextMenuHandlers 键
    std::wstring key = pszFileType;
    key += L"\\ShellEx\\ContextMenuHandlers\\";
    key += pszFriendlyName;

    hr = HRESULT_FROM_WIN32(RegCreateKeyEx(
        HKEY_CLASSES_ROOT,
        key.c_str(),
        0, NULL,
        REG_OPTION_NON_VOLATILE,
        KEY_WRITE,
        NULL,
        &hKey,
        NULL));

    if (SUCCEEDED(hr)) {
        hr = HRESULT_FROM_WIN32(RegSetValueEx(
            hKey, NULL, 0, REG_SZ,
            reinterpret_cast<const BYTE*>(szCLSID),
            (lstrlen(szCLSID) + 1) * sizeof(wchar_t)));
    }

    if (hKey) RegCloseKey(hKey);

    return hr;
}

HRESULT UnregisterShellExtContextMenuHandler(
    PCWSTR pszFileType,
    PCWSTR pszFriendlyName) {

    std::wstring key = pszFileType;
    key += L"\\ShellEx\\ContextMenuHandlers\\";
    key += pszFriendlyName;

    RegDeleteKey(HKEY_CLASSES_ROOT, key.c_str());

    return S_OK;
}
