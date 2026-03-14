#pragma once

#include <windows.h>
#include <shlobj.h>
#include <string>

// 定义不同的图标覆盖标识符
// {C3D4E5F6-A7B8-4C9D-8E0F-1A2B3C4D5E6F} - 已同步（绿色勾）
const CLSID CLSID_KCloudSyncedOverlay =
    {0xC3D4E5F6, 0xA7B8, 0x4C9D, {0x8E, 0x0F, 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F}};

// {D4E5F6A7-B8C9-4D0E-9F1A-2B3C4D5E6F7A} - 同步中（蓝色箭头）
const CLSID CLSID_KCloudSyncingOverlay =
    {0xD4E5F6A7, 0xB8C9, 0x4D0E, {0x9F, 0x1A, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x7A}};

// {E5F6A7B8-C9D0-4E1F-AF2B-3C4D5E6F7A8B} - 冲突（红色感叹号）
const CLSID CLSID_KCloudConflictOverlay =
    {0xE5F6A7B8, 0xC9D0, 0x4E1F, {0xAF, 0x2B, 0x3C, 0x4D, 0x5E, 0x6F, 0x7A, 0x8B}};

/**
 * KCloud 图标覆盖处理器
 * 实现 IShellIconOverlayIdentifier 接口
 */
class KCloudIconOverlay : public IShellIconOverlayIdentifier {
public:
    enum class OverlayType {
        SYNCED,     // 绿色勾
        SYNCING,    // 蓝色箭头
        CONFLICT    // 红色感叹号
    };

    KCloudIconOverlay(OverlayType type);

    // IUnknown
    IFACEMETHODIMP QueryInterface(REFIID riid, void** ppv);
    IFACEMETHODIMP_(ULONG) AddRef();
    IFACEMETHODIMP_(ULONG) Release();

    // IShellIconOverlayIdentifier
    IFACEMETHODIMP IsMemberOf(LPCWSTR pwszPath, DWORD dwAttrib);
    IFACEMETHODIMP GetOverlayInfo(LPWSTR pwszIconFile, int cchMax, int* pIndex, DWORD* pdwFlags);
    IFACEMETHODIMP GetPriority(int* pPriority);

protected:
    ~KCloudIconOverlay();

private:
    LONG m_cRef;
    OverlayType m_type;

    // 通过 IPC 查询文件状态
    std::string GetFileStatus(const std::wstring& path);
    bool IsInSyncDirectory(const std::wstring& path);
};
