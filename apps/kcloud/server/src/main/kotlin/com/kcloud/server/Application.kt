package com.kcloud.server

import com.kcloud.createKCloudRuntime

fun main() {
    val runtime = createKCloudRuntime()
    runtime.startServer(wait = true)
}
