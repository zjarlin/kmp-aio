cmake_minimum_required(VERSION 3.20)
project(KCloudShellExt VERSION 1.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# 查找依赖
find_package(jsoncpp CONFIG REQUIRED)

# 创建 DLL
add_library(KCloudShellExt SHARED
    KCloudShellExt.cpp
    KCloudShellExt.def
)

target_link_libraries(KCloudShellExt PRIVATE
    jsoncpp_lib
    shlwapi
    shell32
    ole32
    uuid
)

# 设置输出名称
set_target_properties(KCloudShellExt PROPERTIES
    OUTPUT_NAME "KCloudShellExt"
    PREFIX ""
)

# 安装
install(TARGETS KCloudShellExt
    RUNTIME DESTINATION bin
    LIBRARY DESTINATION lib
)
