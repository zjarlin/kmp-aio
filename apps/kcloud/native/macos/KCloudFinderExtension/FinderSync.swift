import Foundation
import FinderSync

/**
 * KCloud Finder Sync Extension
 *
 * 在 Finder 中显示文件同步状态和右键菜单
 */
class FinderSync: FIFinderSync {

    private var ipcClient: IPCClient?
    private let socketPath: String

    override init() {
        // 获取 socket 路径
        let homeDir = FileManager.default.homeDirectoryForCurrentUser
        self.socketPath = homeDir.appendingPathComponent(".kcloud/ipc.sock").path

        super.init()

        // 初始化 IPC 客户端
        self.ipcClient = IPCClient(socketPath: socketPath)

        // 设置观察的目录（同步根目录）
        if let syncRoot = getSyncRoot() {
            FIFinderSyncController.default().directoryURLs = [syncRoot]
        }

        NSLog("KCloud FinderSync 已初始化")
    }

    // MARK: - 菜单配置

    override func menu(for menuKind: FIMenuKind) -> NSMenu {
        let menu = NSMenu(title: "KCloud")

        // 获取选中的文件
        let target = FIFinderSyncController.default().targetedURL()
        let items = FIFinderSyncController.default().selectedItemURLs() ?? []

        // 检查是否在同步目录内
        guard let path = target?.path, isInSyncDirectory(path) else {
            return menu
        }

        // 添加菜单项
        menu.addItem(withTitle: "KCloud - 立即同步", action: #selector(syncNow), keyEquivalent: "")
        menu.addItem(withTitle: "KCloud - 在应用中显示", action: #selector(showInApp), keyEquivalent: "")
        menu.addItem(NSMenuItem.separator())

        // 如果有冲突，添加解决冲突菜单
        if hasConflict(items.first?.path ?? path) {
            menu.addItem(withTitle: "KCloud - 解决冲突", action: #selector(resolveConflict), keyEquivalent: "")
        }

        menu.addItem(withTitle: "KCloud - 获取共享链接", action: #selector(getShareLink), keyEquivalent: "")

        return menu
    }

    // MARK: - 状态徽章

    override func badgeIdentifier(for url: URL) -> String {
        guard isInSyncDirectory(url.path) else {
            return ""
        }

        // 查询文件状态
        if let status = getFileStatus(url.path) {
            switch status {
            case "SYNCED":
                return "Synced"
            case "SYNCING":
                return "Syncing"
            case "PENDING_UPLOAD", "PENDING_DOWNLOAD":
                return "Pending"
            case "CONFLICT":
                return "Conflict"
            case "ERROR":
                return "Error"
            default:
                return ""
            }
        }

        return ""
    }

    // MARK: - 操作回调

    @objc func syncNow() {
        let items = FIFinderSyncController.default().selectedItemURLs() ?? []
        guard let path = items.first?.path else { return }

        ipcClient?.send(message: [
            "action": "TRIGGER_SYNC",
            "path": path
        ]) { response in
            NSLog("同步响应: \(response)")
        }
    }

    @objc func showInApp() {
        let items = FIFinderSyncController.default().selectedItemURLs() ?? []
        guard let path = items.first?.path else { return }

        ipcClient?.send(message: [
            "action": "SHOW_IN_APP",
            "path": path
        ]) { response in
            NSLog("显示响应: \(response)")
        }
    }

    @objc func resolveConflict() {
        let items = FIFinderSyncController.default().selectedItemURLs() ?? []
        guard let path = items.first?.path else { return }

        ipcClient?.send(message: [
            "action": "RESOLVE_CONFLICT",
            "path": path
        ]) { response in
            NSLog("解决冲突响应: \(response)")
        }
    }

    @objc func getShareLink() {
        let items = FIFinderSyncController.default().selectedItemURLs() ?? []
        guard let path = items.first?.path else { return }

        ipcClient?.send(message: [
            "action": "GET_SHARE_LINK",
            "path": path
        ]) { response in
            NSLog("共享链接响应: \(response)")
        }
    }

    // MARK: - 辅助方法

    private func getSyncRoot() -> URL? {
        let homeDir = FileManager.default.homeDirectoryForCurrentUser
        let syncPath = homeDir.appendingPathComponent("KCloud")
        return syncPath
    }

    private func isInSyncDirectory(_ path: String) -> Bool {
        guard let syncRoot = getSyncRoot()?.path else { return false }
        return path.hasPrefix(syncRoot)
    }

    private func getFileStatus(_ path: String) -> String? {
        // 同步查询文件状态
        var status: String?
        let semaphore = DispatchSemaphore(value: 0)

        ipcClient?.send(message: [
            "action": "GET_FILE_STATUS",
            "path": path
        ]) { response in
            if let dict = response as? [String: Any] {
                status = dict["status"] as? String
            }
            semaphore.signal()
        }

        semaphore.wait(timeout: .now() + 1.0)
        return status
    }

    private func hasConflict(_ path: String) -> Bool {
        return getFileStatus(path) == "CONFLICT"
    }
}

// MARK: - IPC 客户端

class IPCClient {
    private let socketPath: String
    private var socket: Int32 = -1

    init(socketPath: String) {
        self.socketPath = socketPath
    }

    deinit {
        close()
    }

    func connect() -> Bool {
        // 创建 Unix Domain Socket
        socket = Darwin.socket(AF_UNIX, SOCK_STREAM, 0)
        guard socket >= 0 else {
            NSLog("创建 socket 失败")
            return false
        }

        var addr = sockaddr_un()
        addr.sun_family = sa_family_t(AF_UNIX)
        strncpy(&addr.sun_path.0, socketPath, MemoryLayout.size(ofValue: addr.sun_path) - 1)

        let result = withUnsafePointer(to: &addr) { ptr in
            ptr.withMemoryRebound(to: sockaddr.self, capacity: 1) { addrPtr in
                Darwin.connect(socket, addrPtr, socklen_t(MemoryLayout<sockaddr_un>.size))
            }
        }

        if result < 0 {
            NSLog("连接 socket 失败: \(errno)")
            close()
            return false
        }

        return true
    }

    func send(message: [String: String], completion: @escaping (Any?) -> Void) {
        guard connect() else {
            completion(nil)
            return
        }

        defer { close() }

        // 序列化消息
        do {
            let data = try JSONSerialization.data(withJSONObject: message)
            var jsonString = String(data: data, encoding: .utf8)!
            jsonString += "\n"

            // 发送消息
            guard let sendData = jsonString.data(using: .utf8) else {
                completion(nil)
                return
            }

            sendData.withUnsafeBytes { ptr in
                _ = Darwin.send(socket, ptr.baseAddress, sendData.count, 0)
            }

            // 接收响应
            var buffer = Data()
            let tempBuffer = UnsafeMutablePointer<UInt8>.allocate(capacity: 4096)
            defer { tempBuffer.deallocate() }

            while true {
                let bytesRead = Darwin.recv(socket, tempBuffer, 4096, 0)
                if bytesRead <= 0 { break }
                buffer.append(tempBuffer, count: bytesRead)
                if buffer.contains(10) { break } // 遇到换行符
            }

            // 解析响应
            if let responseString = String(data: buffer, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines),
               let responseData = responseString.data(using: .utf8) {
                let response = try JSONSerialization.jsonObject(with: responseData)
                completion(response)
            } else {
                completion(nil)
            }

        } catch {
            NSLog("发送消息失败: \(error)")
            completion(nil)
        }
    }

    func close() {
        if socket >= 0 {
            Darwin.close(socket)
            socket = -1
        }
    }
}
