#pragma once

#include <windows.h>
#include <shlobj.h>
#include <string>
#include <vector>

// {B8A4C3D2-E5F6-4A7B-8C9D-0E1F2A3B4C5D} - 生成新的 GUID 替换此值
const CLSID CLSID_KCloudContextMenu = 
    {0xB8A4C3D2, 0xE5F6, 0x4A7B, {0x8C, 0x9D, 0x0E, 0x1F, 0x2A, 0x3B, 0x4C, 0x5D}};

// IPC 客户端类
class IPCClient {
public:
    IPCClient();
    ~IPCClient();
    
    bool Connect();
    void Disconnect();
    std::string SendRequest(const std::string& jsonRequest);
    
private:
    HANDLE hPipe;
    static const wchar_t* PIPE_NAME;
};

// Shell 扩展类
class KCloudContextMenu : public IShellExtInit, public IContextMenu {
public:
    KCloudContextMenu();
    
    // IUnknown
    IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);
    IFACEMETHODIMP_(ULONG) AddRef();
    IFACEMETHODIMP_(ULONG) Release();
    
    // IShellExtInit
    IFACEMETHODIMP Initialize(LPCITEMIDLIST pidlFolder, LPDATAOBJECT pdtobj, HKEY hkeyProgID);
    
    // IContextMenu
    IFACEMETHODIMP QueryContextMenu(HMENU hmenu, UINT indexMenu, UINT idCmdFirst, UINT idCmdLast, UINT uFlags);
    IFACEMETHODIMP InvokeCommand(LPCMINVOKECOMMANDINFO pici);
    IFACEMETHODIMP GetCommandString(UINT_PTR idCmd, UINT uType, UINT* pReserved, CHAR* pszName, UINT cchMax);

protected:
    ~KCloudContextMenu();

private:
    LONG m_cRef;
    std::vector<std::wstring> m_selectedFiles;
    IPCClient m_ipcClient;
    
    // 辅助方法
    std::string GetFileStatus(const std::wstring& path);
    void SyncFile(const std::wstring& path);
    void ShowInApp(const std::wstring& path);
    void ResolveConflict(const std::wstring& path);
    
    // 菜单项 ID
    enum {
        ID_SYNC_NOW = 0,
        ID_SHOW_IN_APP,
        ID_RESOLVE_CONFLICT,
        ID_GET_SHARE_LINK,
        ID_MAX
    };
};
