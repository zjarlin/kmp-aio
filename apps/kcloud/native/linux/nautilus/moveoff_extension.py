#!/usr/bin/env python3
"""
MoveOff Nautilus (GNOME Files) Extension

安装:
    cp moveoff_extension.py ~/.local/share/nautilus-python/extensions/
    或
    sudo cp moveoff_extension.py /usr/share/nautilus-python/extensions/

依赖:
    python-nautilus, python-gi
"""

import os
import json
import socket
from urllib.parse import unquote
from gi.repository import Nautilus, GObject, Gio, GLib

SOCKET_PATH = os.path.expanduser("~/.moveoff/ipc.sock")


class IPCClient:
    """Unix Domain Socket IPC 客户端"""

    def __init__(self, socket_path=SOCKET_PATH):
        self.socket_path = socket_path

    def send_request(self, action, path):
        """发送请求到主应用"""
        try:
            sock = socket.socket(socket.AF_UNIX, socket.SOCK_STREAM)
            sock.settimeout(2.0)
            sock.connect(self.socket_path)

            request = json.dumps({"action": action, "path": path}) + "\n"
            sock.send(request.encode('utf-8'))

            response = sock.recv(4096).decode('utf-8').strip()
            sock.close()

            return json.loads(response)
        except Exception as e:
            print(f"IPC Error: {e}")
            return None


class MoveOffExtension(GObject.GObject, Nautilus.MenuProvider, Nautilus.InfoProvider):
    """MoveOff Nautilus 扩展"""

    def __init__(self):
        self.ipc = IPCClient()
        self.sync_root = os.path.expanduser("~/MoveOff")

    def get_file_items(self, files):
        """生成右键菜单项"""
        if not files:
            return

        # 检查是否在同步目录内
        file_path = unquote(files[0].get_uri()[7:])  # file://
        if not file_path.startswith(self.sync_root):
            return

        menu = Nautilus.MenuItem(
            name="MoveOff::Menu",
            label="MoveOff",
            tip="MoveOff 文件同步"
        )

        submenu = Nautilus.Menu()
        menu.set_submenu(submenu)

        # 立即同步
        sync_item = Nautilus.MenuItem(
            name="MoveOff::Sync",
            label="立即同步",
            tip="立即同步选中的文件"
        )
        sync_item.connect("activate", self.on_sync, files)
        submenu.append_item(sync_item)

        # 在应用中显示
        show_item = Nautilus.MenuItem(
            name="MoveOff::Show",
            label="在 MoveOff 中显示",
            tip="在 MoveOff 应用中显示此文件"
        )
        show_item.connect("activate", self.on_show, files)
        submenu.append_item(show_item)

        # 获取共享链接
        link_item = Nautilus.MenuItem(
            name="MoveOff::Link",
            label="获取共享链接",
            tip="获取文件的共享链接"
        )
        link_item.connect("activate", self.on_get_link, files)
        submenu.append_item(link_item)

        return menu,

    def on_sync(self, menu, files):
        """触发同步"""
        for file in files:
            path = unquote(file.get_uri()[7:])
            self.ipc.send_request("TRIGGER_SYNC", path)

    def on_show(self, menu, files):
        """在应用中显示"""
        if files:
            path = unquote(files[0].get_uri()[7:])
            self.ipc.send_request("SHOW_IN_APP", path)

    def on_get_link(self, menu, files):
        """获取共享链接"""
        if files:
            path = unquote(files[0].get_uri()[7:])
            response = self.ipc.send_request("GET_SHARE_LINK", path)
            if response and "link" in response:
                # 复制到剪贴板
                self._copy_to_clipboard(response["link"])

    def _copy_to_clipboard(self, text):
        """复制文本到剪贴板"""
        try:
            import subprocess
            subprocess.run(["xclip", "-selection", "clipboard"],
                          input=text.encode(), check=True)
        except:
            pass

    def update_file_info(self, file):
        """更新文件信息（用于显示状态图标）"""
        # Nautilus 的图标覆盖需要通过其他方式实现
        # 这里只是预留接口
        pass
