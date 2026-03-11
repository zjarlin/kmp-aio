#!/bin/bash

# MoveOff 原生扩展构建脚本
# 支持 macOS, Windows (MSYS2/MinGW), Linux

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="${SCRIPT_DIR}/build"
INSTALL_PREFIX="${SCRIPT_DIR}/install"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检测平台
detect_platform() {
    case "$(uname -s)" in
        Darwin)
            PLATFORM="macos"
            ;;
        Linux)
            PLATFORM="linux"
            ;;
        CYGWIN*|MINGW*|MSYS*)
            PLATFORM="windows"
            ;;
        *)
            log_error "不支持的平台: $(uname -s)"
            exit 1
            ;;
    esac
    log_info "检测到平台: $PLATFORM"
}

# 清理构建目录
clean() {
    log_info "清理构建目录..."
    rm -rf "$BUILD_DIR" "$INSTALL_PREFIX"
    mkdir -p "$BUILD_DIR" "$INSTALL_PREFIX"
}

# 构建 macOS Finder Extension
build_macos() {
    log_info "构建 macOS Finder Extension..."

    local macos_dir="${SCRIPT_DIR}/macos"
    local build_dir="${BUILD_DIR}/macos"

    mkdir -p "$build_dir"

    # 检查 Xcode
    if ! command -v xcodebuild &> /dev/null; then
        log_error "未找到 Xcode，无法构建 macOS 扩展"
        return 1
    fi

    # 创建 Xcode 项目（如果没有）
    if [ ! -d "${macos_dir}/MoveOffFinderExtension.xcodeproj" ]; then
        log_warn "Xcode 项目不存在，请手动创建 Finder Extension 项目"
        log_info "步骤:"
        log_info "1. 打开 Xcode"
        log_info "2. File > New > Project"
        log_info "3. 选择 'App Extension' -> 'Finder Extension'"
        log_info "4. 复制 FinderSync.swift 和 Info.plist 到项目"
        return 1
    fi

    # 构建
    xcodebuild -project "${macos_dir}/MoveOffFinderExtension.xcodeproj" \
               -scheme MoveOffFinderExtension \
               -configuration Release \
               -derivedDataPath "$build_dir" \
               build

    # 复制结果
    local app_path=$(find "$build_dir" -name "*.appex" -type d | head -1)
    if [ -n "$app_path" ]; then
        cp -R "$app_path" "${INSTALL_PREFIX}/MoveOffFinderExtension.appex"
        log_info "macOS 扩展构建完成: ${INSTALL_PREFIX}/MoveOffFinderExtension.appex"
    fi
}

# 构建 Windows Shell Extension
build_windows() {
    log_info "构建 Windows Shell Extension..."

    local windows_dir="${SCRIPT_DIR}/windows"
    local build_dir="${BUILD_DIR}/windows"

    mkdir -p "$build_dir"

    # 检查 Visual Studio 或 MinGW
    if command -v cl.exe &> /dev/null; then
        # 使用 Visual Studio
        log_info "使用 Visual Studio 构建..."

        # 查找 vcpkg
        if [ -z "$VCPKG_ROOT" ]; then
            log_warn "VCPKG_ROOT 未设置，尝试查找 vcpkg..."
            VCPKG_ROOT=$(find /c -name "vcpkg" -type d 2>/dev/null | head -1)
        fi

        if [ -n "$VCPKG_ROOT" ]; then
            # 使用 vcpkg toolchain
            cmake -S "$windows_dir/MoveOffShellExt" \
                  -B "$build_dir" \
                  -DCMAKE_TOOLCHAIN_FILE="$VCPKG_ROOT/scripts/buildsystems/vcpkg.cmake" \
                  -DCMAKE_BUILD_TYPE=Release \
                  -A x64
        else
            cmake -S "$windows_dir/MoveOffShellExt" \
                  -B "$build_dir" \
                  -DCMAKE_BUILD_TYPE=Release \
                  -A x64
        fi

        cmake --build "$build_dir" --config Release

        # 复制结果
        cp "$build_dir/Release/MoveOffShellExt.dll" "${INSTALL_PREFIX}/"
        log_info "Windows Shell 扩展构建完成: ${INSTALL_PREFIX}/MoveOffShellExt.dll"

    elif command -v x86_64-w64-mingw32-g++ &> /dev/null; then
        # 使用 MinGW
        log_info "使用 MinGW 构建..."

        # 编译 Shell Extension
        x86_64-w64-mingw32-g++ -shared -O2 \
            -I"$windows_dir/MoveOffShellExt" \
            -o "$build_dir/MoveOffShellExt.dll" \
            "$windows_dir/MoveOffShellExt/MoveOffShellExt.cpp" \
            -lshlwapi -lshell32 -lole32 -luuid \
            -Wl,--kill-at

        cp "$build_dir/MoveOffShellExt.dll" "${INSTALL_PREFIX}/"
        log_info "Windows Shell 扩展构建完成: ${INSTALL_PREFIX}/MoveOffShellExt.dll"

    else
        log_error "未找到 Visual Studio 或 MinGW，无法构建 Windows 扩展"
        return 1
    fi
}

# 安装 Linux 扩展
install_linux() {
    log_info "安装 Linux 文件管理器扩展..."

    local linux_dir="${SCRIPT_DIR}/linux"

    # Nautilus 扩展
    if command -v nautilus &> /dev/null; then
        local nautilus_ext_dir="$HOME/.local/share/nautilus-python/extensions"
        mkdir -p "$nautilus_ext_dir"
        cp "$linux_dir/nautilus/moveoff_extension.py" "$nautilus_ext_dir/"
        chmod +x "$nautilus_ext_dir/moveoff_extension.py"
        log_info "Nautilus 扩展已安装到: $nautilus_ext_dir"

        # 重启 Nautilus
        nautilus -q 2>/dev/null || true
        log_info "Nautilus 已重启"
    else
        log_warn "未找到 Nautilus"
    fi

    # Dolphin 扩展（KDE）
    if command -v dolphin &> /dev/null; then
        log_warn "Dolphin 扩展需要手动构建，请参考 dolphin/README.md"
    fi
}

# 显示帮助
show_help() {
    cat << EOF
MoveOff 原生扩展构建脚本

用法: $0 [选项] [命令]

命令:
    all         构建所有平台的扩展（默认）
    clean       清理构建目录
    install     安装扩展到系统

选项:
    -h, --help  显示帮助

平台特定命令:
    macos       构建 macOS Finder Extension
    windows     构建 Windows Shell Extension
    linux       安装 Linux 文件管理器扩展

环境变量:
    VCPKG_ROOT  vcpkg 安装路径（Windows 必需）

示例:
    $0                      # 构建所有
    $0 clean                # 清理
    $0 macos                # 仅构建 macOS
    $0 windows              # 仅构建 Windows
    $0 install              # 安装扩展
EOF
}

# 主函数
main() {
    local command="${1:-all}"

    case "$command" in
        -h|--help)
            show_help
            exit 0
            ;;
        clean)
            clean
            ;;
        macos)
            detect_platform
            if [ "$PLATFORM" != "macos" ]; then
                log_error "当前平台不是 macOS"
                exit 1
            fi
            clean
            build_macos
            ;;
        windows)
            detect_platform
            if [ "$PLATFORM" != "windows" ]; then
                log_error "当前平台不是 Windows"
                exit 1
            fi
            clean
            build_windows
            ;;
        linux)
            detect_platform
            if [ "$PLATFORM" != "linux" ]; then
                log_error "当前平台不是 Linux"
                exit 1
            fi
            install_linux
            ;;
        install)
            detect_platform
            case "$PLATFORM" in
                linux)
                    install_linux
                    ;;
                macos)
                    log_info "macOS 扩展需要手动安装到 Xcode 项目中"
                    ;;
                windows)
                    log_info "Windows 扩展需要手动注册: regsvr32 MoveOffShellExt.dll"
                    ;;
            esac
            ;;
        all|*)
            detect_platform
            clean
            case "$PLATFORM" in
                macos)
                    build_macos
                    ;;
                windows)
                    build_windows
                    ;;
                linux)
                    install_linux
                    ;;
            esac
            ;;
    esac

    log_info "完成！"
}

main "$@"
