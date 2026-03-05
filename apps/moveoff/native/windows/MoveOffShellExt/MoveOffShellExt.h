#pragma once

#include <windows.h>
#include <shlobj.h>
#include <string>
#include <vector>

// {YOUR-GUID-HERE-REPLACE-THIS}
// 使用 guidgen 生成新的 GUID
const CLSID CLSID_MoveOffContextMenu =
    {0x12345678, 0x1234, 0x1234, {0x12, 0x34, 0x12, 0x34, 0x56, 0x78, 0x90, 0xAB}};

// 文件同步状态
enum class FileSyncStatus {
    UNKNOWN = -1,
    SYNCED = 0,
    SYNCING = 1,
    PENDING_UPLOAD = 2,
    PENDING_DOWNLOAD = 3,
    CONFLICT = 4,
    ERROR = 5
};

// 上下文菜单扩展类
class CMoveOffContextMenu : public IShellExtInit, public IContextMenu {
public:
    CMoveOffContextMenu();
    ~CMoveOffContextMenu();

    // IUnknown
    IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);
    IFACEMETHODIMP_(ULONG) AddRef();
    IFACEMETHODIMP_(ULONG) Release();

    // IShellExtInit
    IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pdtobj, HKEY hkeyProgID);

    // IContextMenu
    IFACEMETHODIMP QueryContextMenu(HMENU hmenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);
    IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);
    IFACEMETHODIMP GetCommandString(UINT_PTR idCmd, UINT uFlags, UINT* pwReserved, LPSTR pszName, UINT cchMax);

private:
    LONG m_cRef;
    std::vector<std::wstring> m_selectedFiles;
    FileSyncStatus m_status;

    // 与主应用通信
    FileSyncStatus GetFileStatus(const std::wstring& path);
    void TriggerSync(const std::wstring& path);
    void ShowInApp(const std::wstring& path);
    void ResolveConflict(const std::wstring& path);

    // 菜单项 ID
    static const UINT CMD_SYNC_NOW = 0;
    static const UINT CMD_SHOW_IN_APP = 1;
    static const UINT CMD_RESOLVE_CONFLICT = 2;
    static const UINT CMD_SHARE_LINK = 3;
};

// 类工厂
class CClassFactory : public IClassFactory {
public:
    // IUnknown
    IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);
    IFACEMETHODIMP_(ULONG) AddRef();
    IFACEMETHODIMP_(ULONG) Release();

    // IClassFactory
    IFACEMETHODIMP CreateInstance(IUnknown* pUnkOuter, REFIID riid, void** ppv);
    IFACEMETHODIMP LockServer(BOOL fLock);
};
