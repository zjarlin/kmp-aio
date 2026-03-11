#include "MoveOffIconOverlay.h"
#include "../MoveOffShellExt/MoveOffShellExt.h"  // 复用 IPCClient
#include <shlwapi.h>
#include <json/json.h>

#pragma comment(lib, "shlwapi.lib")

MoveOffIconOverlay::MoveOffIconOverlay(OverlayType type)
    : m_cRef(1), m_type(type) {
}

MoveOffIconOverlay::~MoveOffIconOverlay() {
}

IFACEMETHODIMP MoveOffIconOverlay::QueryInterface(REFIID riid, void** ppv) {
    if (IsEqualIID(riid, IID_IUnknown) || IsEqualIID(riid, IID_IShellIconOverlayIdentifier)) {
        *ppv = this;
        AddRef();
        return S_OK;
    }
    *ppv = NULL;
    return E_NOINTERFACE;
}

IFACEMETHODIMP_(ULONG) MoveOffIconOverlay::AddRef() {
    return InterlockedIncrement(&m_cRef);
}

IFACEMETHODIMP_(ULONG) MoveOffIconOverlay::Release() {
    ULONG cRef = InterlockedDecrement(&m_cRef);
    if (cRef == 0) {
        delete this;
    }
    return cRef;
}

IFACEMETHODIMP MoveOffIconOverlay::IsMemberOf(LPCWSTR pwszPath, DWORD dwAttrib) {
    // 检查是否在同步目录内
    if (!IsInSyncDirectory(pwszPath)) {
        return S_FALSE;
    }

    // 查询文件状态
    std::string status = GetFileStatus(pwszPath);

    // 根据类型判断是否匹配
    switch (m_type) {
        case OverlayType::SYNCED:
            return (status == "SYNCED") ? S_OK : S_FALSE;
        case OverlayType::SYNCING:
            return (status == "SYNCING" || status.find("PENDING") != std::string::npos) ? S_OK : S_FALSE;
        case OverlayType::CONFLICT:
            return (status == "CONFLICT") ? S_OK : S_FALSE;
    }

    return S_FALSE;
}

IFACEMETHODIMP MoveOffIconOverlay::GetOverlayInfo(LPWSTR pwszIconFile, int cchMax, int* pIndex, DWORD* pdwFlags) {
    // 获取 DLL 路径
    HMODULE hModule = GetModuleHandleW(L"MoveOffIconOverlay.dll");
    if (hModule == NULL) {
        return E_FAIL;
    }

    GetModuleFileNameW(hModule, pwszIconFile, cchMax);

    // 根据类型设置图标索引
    switch (m_type) {
        case OverlayType::SYNCED:
            *pIndex = 0;  // 绿色勾
            break;
        case OverlayType::SYNCING:
            *pIndex = 1;  // 蓝色箭头
            break;
        case OverlayType::CONFLICT:
            *pIndex = 2;  // 红色感叹号
            break;
    }

    *pdwFlags = ISIOI_ICONFILE | ISIOI_ICONINDEX;
    return S_OK;
}

IFACEMETHODIMP MoveOffIconOverlay::GetPriority(int* pPriority) {
    // 优先级：冲突 > 同步中 > 已同步
    switch (m_type) {
        case OverlayType::CONFLICT:
            *pPriority = 0;
            break;
        case OverlayType::SYNCING:
            *pPriority = 1;
            break;
        case OverlayType::SYNCED:
            *pPriority = 2;
            break;
    }
    return S_OK;
}

std::string MoveOffIconOverlay::GetFileStatus(const std::wstring& path) {
    // 使用 IPCClient 查询状态
    // 简化实现，实际应该连接到主应用
    return "SYNCED";
}

bool MoveOffIconOverlay::IsInSyncDirectory(const std::wstring& path) {
    wchar_t syncRoot[MAX_PATH];
    ExpandEnvironmentStringsW(L"%USERPROFILE%\\MoveOff", syncRoot, MAX_PATH);
    return wcsncmp(path.c_str(), syncRoot, wcslen(syncRoot)) == 0;
}
