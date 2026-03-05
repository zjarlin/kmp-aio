import Cocoa
import FinderSync

// MARK: - XPC Protocol
@objc protocol MoveOffXPCProtocol {
    func getFileStatus(_ path: String, reply: @escaping (Int) -> Void)
    func triggerSync(_ path: String)
    func showInApp(_ path: String)
}

// MARK: - Finder Sync Provider
class FinderSync: FIFinderSync {

    var xpcConnection: NSXPCConnection?
    var syncedFolderURL: URL?

    // 状态图标
    let syncedBadge = NSImage(named: NSImage.statusAvailableName)
    let syncingBadge = NSImage(named: NSImage.statusPartiallyAvailableName)
    let conflictBadge = NSImage(named: NSImage.statusUnavailableName)
    let pendingBadge = NSImage(named: NSImage.statusNoneName)

    override init() {
        super.init()

        NSLog("MoveOff FinderSync init")

        // 设置监控的目录
        setupSyncedFolder()

        // 建立 XPC 连接
        setupXPCConnection()
    }

    // MARK: - Setup

    func setupSyncedFolder() {
        // 从用户默认值读取同步目录
        if let syncPath = UserDefaults.standard.string(forKey: "moveoff_sync_path") {
            syncedFolderURL = URL(fileURLWithPath: syncPath)
        } else {
            // 默认使用 ~/MoveOff
            let home = FileManager.default.homeDirectoryForCurrentUser
            syncedFolderURL = home.appendingPathComponent("MoveOff")
        }

        if let url = syncedFolderURL {
            FIFinderSyncController.default().directoryURLs = [url]
            NSLog("Monitoring directory: \(url.path)")
        }
    }

    func setupXPCConnection() {
        let connection = NSXPCConnection(machServiceName: "site.addzero.moveoff.xpc", options: [])
        connection.remoteObjectInterface = NSXPCInterface(with: MoveOffXPCProtocol.self)
        connection.resume()
        xpcConnection = connection
    }

    // MARK: - Badge Management

    override func badgeIdentifier(for url: URL) -> String {
        guard let syncedFolder = syncedFolderURL else { return "" }

        // 只处理同步目录下的文件
        let path = url.path
        guard path.hasPrefix(syncedFolder.path) else { return "" }

        // 异步获取状态，这里返回默认状态
        // 实际状态通过 requestBadgeIdentifier(for:) 更新
        return getBadgeIdentifierForPath(path)
    }

    func getBadgeIdentifierForPath(_ path: String) -> String {
        // 从 XPC 获取文件状态
        guard let proxy = xpcConnection?.remoteObjectProxy as? MoveOffXPCProtocol else {
            return ""
        }

        var badgeId = ""
        let semaphore = DispatchSemaphore(value: 0)

        proxy.getFileStatus(path) { status in
            badgeId = self.badgeIdentifierForStatus(status)
            semaphore.signal()
        }

        _ = semaphore.wait(timeout: .now() + 0.1)
        return badgeId
    }

    func badgeIdentifierForStatus(_ status: Int) -> String {
        switch status {
        case 0: return "synced"
        case 1: return "syncing"
        case 2: return "pending_upload"
        case 3: return "pending_download"
        case 4: return "conflict"
        case 5: return "error"
        default: return ""
        }
    }

    // MARK: - Menu Items

    override func menu(for menuKind: FIMenuKind) -> NSMenu {
        let menu = NSMenu(title: "MoveOff")

        // 获取选中的文件
        let controller = FIFinderSyncController.default()
        guard let urls = controller.selectedItemURLs(), !urls.isEmpty else {
            return menu
        }

        let firstURL = urls[0]
        let status = getFileStatus(firstURL.path)

        // 根据状态添加不同的菜单项
        switch menuKind {
        case .contextualMenuForContainer,
             .contextualMenuForItems:
            // 上下文菜单
            addContextMenuItems(to: menu, for: urls, status: status)

        case .toolbarItemMenu:
            // 工具栏菜单
            addToolbarMenuItems(to: menu, for: urls)

        default:
            break
        }

        return menu
    }

    func addContextMenuItems(to menu: NSMenu, for urls: [URL], status: Int) {
        // 同步状态
        let statusItem = NSMenuItem(
            title: statusTitle(for: status),
            action: nil,
            keyEquivalent: ""
        )
        statusItem.isEnabled = false
        menu.addItem(statusItem)

        menu.addItem(NSMenuItem.separator())

        // 立即同步
        let syncItem = NSMenuItem(
            title: "立即同步",
            action: #selector(syncNow(_:)),
            keyEquivalent: ""
        )
        syncItem.target = self
        menu.addItem(syncItem)

        // 解决冲突
        if status == 4 { // CONFLICT
            let resolveItem = NSMenuItem(
                title: "解决冲突...",
                action: #selector(resolveConflict(_:)),
                keyEquivalent: ""
            )
            resolveItem.target = self
            menu.addItem(resolveItem)
        }

        menu.addItem(NSMenuItem.separator())

        // 在 MoveOff 中显示
        let showItem = NSMenuItem(
            title: "在 MoveOff 中显示",
            action: #selector(showInApp(_:)),
            keyEquivalent: ""
        )
        showItem.target = self
        menu.addItem(showItem)

        // 共享链接
        if status == 0 { // SYNCED
            let shareItem = NSMenuItem(
                title: "获取共享链接",
                action: #selector(getShareLink(_:)),
                keyEquivalent: ""
            )
            shareItem.target = self
            menu.addItem(shareItem)
        }
    }

    func addToolbarMenuItems(to menu: NSMenu, for urls: [URL]) {
        let syncItem = NSMenuItem(
            title: "同步选中项",
            action: #selector(syncNow(_:)),
            keyEquivalent: ""
        )
        syncItem.target = self
        menu.addItem(syncItem)

        let showItem = NSMenuItem(
            title: "在 MoveOff 中打开",
            action: #selector(showInApp(_:)),
            keyEquivalent: ""
        )
        showItem.target = self
        menu.addItem(showItem)
    }

    // MARK: - Actions

    @objc func syncNow(_ sender: AnyObject?) {
        guard let urls = FIFinderSyncController.default().selectedItemURLs(),
              let firstURL = urls.first else { return }

        guard let proxy = xpcConnection?.remoteObjectProxy as? MoveOffXPCProtocol else {
            return
        }

        proxy.triggerSync(firstURL.path)

        // 显示通知
        showNotification(title: "MoveOff", message: "已开始同步 \(firstURL.lastPathComponent)")
    }

    @objc func resolveConflict(_ sender: AnyObject?) {
        guard let urls = FIFinderSyncController.default().selectedItemURLs(),
              let firstURL = urls.first else { return }

        guard let proxy = xpcConnection?.remoteObjectProxy as? MoveOffXPCProtocol else {
            return
        }

        proxy.showInApp(firstURL.path)
    }

    @objc func showInApp(_ sender: AnyObject?) {
        guard let urls = FIFinderSyncController.default().selectedItemURLs(),
              let firstURL = urls.first else { return }

        guard let proxy = xpcConnection?.remoteObjectProxy as? MoveOffXPCProtocol else {
            return
        }

        proxy.showInApp(firstURL.path)
    }

    @objc func getShareLink(_ sender: AnyObject?) {
        guard let urls = FIFinderSyncController.default().selectedItemURLs(),
              let firstURL = urls.first else { return }

        // TODO: 获取共享链接
        let pasteboard = NSPasteboard.general
        pasteboard.clearContents()
        pasteboard.setString("https://moveoff.example.com/s/xxx", forType: .string)

        showNotification(title: "MoveOff", message: "共享链接已复制到剪贴板")
    }

    // MARK: - Helpers

    func getFileStatus(_ path: String) -> Int {
        guard let proxy = xpcConnection?.remoteObjectProxy as? MoveOffXPCProtocol else {
            return -1
        }

        var status = -1
        let semaphore = DispatchSemaphore(value: 0)

        proxy.getFileStatus(path) { s in
            status = s
            semaphore.signal()
        }

        _ = semaphore.wait(timeout: .now() + 0.5)
        return status
    }

    func statusTitle(for status: Int) -> String {
        switch status {
        case 0: return "✓ 已同步"
        case 1: return "⟳ 同步中..."
        case 2: return "↑ 等待上传"
        case 3: return "↓ 等待下载"
        case 4: return "⚠ 冲突"
        case 5: return "✕ 错误"
        default: return "MoveOff"
        }
    }

    func showNotification(title: String, message: String) {
        let notification = NSUserNotification()
        notification.title = title
        notification.informativeText = message
        NSUserNotificationCenter.default.deliver(notification)
    }
}
