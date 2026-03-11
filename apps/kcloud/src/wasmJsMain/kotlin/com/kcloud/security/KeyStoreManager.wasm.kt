package com.kcloud.security

/**
 * WebAssembly 平台占位实现。
 *
 * 当前版本暂不支持安全密钥存储。
 */
actual class KeyStoreManager {
    actual fun storeKey(keyId: String, keyData: ByteArray): Boolean {
        return false
    }

    actual fun retrieveKey(keyId: String): ByteArray? {
        return null
    }

    actual fun deleteKey(keyId: String): Boolean {
        return false
    }

    actual fun hasKey(keyId: String): Boolean {
        return false
    }
}
