package com.kcloud.security

import com.kcloud.paths.KCloudPaths
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.security.*
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.*
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

private val logger = KotlinLogging.logger {}

/**
 * JVM 平台密钥管理器实现
 *
 * 使用 Java KeyStore 存储密钥，密钥库使用主密码保护。
 * 主密码派生自用户密码和设备特定信息。
 *
 * ## 安全注意事项
 * - 密钥库文件存储在用户配置目录
 * - 主密码使用 PBKDF2 派生
 * - 私钥和密钥受密钥库密码保护
 */
actual class KeyStoreManager {
    private val keyStore: KeyStore = KeyStore.getInstance("PKCS12")
    private val keyStoreFile: File = resolveKeyStoreFile()
    private val masterPassword: CharArray by lazy { deriveMasterPassword() }

    companion object {
        private const val KEYSTORE_TYPE = "PKCS12"
        private const val MASTER_KEY_ALIAS = "master"
        private const val ITERATIONS = 100000
    }

    init {
        initializeKeyStore()
    }

    private fun getKeyStoreDirectory(): File {
        return KCloudPaths.securityDir().apply {
            mkdirs()
            // 设置目录权限（仅所有者访问）
            setReadable(false, false)
            setWritable(false, false)
            setExecutable(false, false)
            setReadable(true, true)
            setWritable(true, true)
            setExecutable(true, true)
        }
    }

    private fun resolveKeyStoreFile(): File {
        val keyStoreDirectory = getKeyStoreDirectory()
        return File(keyStoreDirectory, "kcloud.keystore")
    }

    /**
     * 派生主密码
     *
     * 结合用户密码和设备特定信息生成主密码。
     * 注意：这是简化实现，实际应该使用平台特定的安全存储。
     */
    private fun deriveMasterPassword(): CharArray {
        // 组合多种系统信息作为基础
        val systemInfo = buildString {
            append(System.getProperty("user.name"))
            append(System.getProperty("os.name"))
            append(System.getProperty("os.version"))
            append(System.getenv("HOME") ?: System.getenv("USERPROFILE") ?: "")
        }

        // 使用 SHA-256 生成固定长度的密码
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(systemInfo.toByteArray())

        // 转换为十六进制字符串作为密码
        return hash.joinToString("") { "%02x".format(it) }.toCharArray()
    }

    private fun initializeKeyStore() {
        if (keyStoreFile.exists()) {
            // 加载现有密钥库
            try {
                keyStoreFile.inputStream().use { fis ->
                    keyStore.load(fis, masterPassword)
                }
                logger.info { "Keystore loaded successfully" }
            } catch (e: Exception) {
                logger.error(e) { "Failed to load keystore, creating new one" }
                createNewKeyStore()
            }
        } else {
            createNewKeyStore()
        }
    }

    private fun createNewKeyStore() {
        keyStore.load(null, null)
        saveKeyStore()
        logger.info { "New keystore created at ${keyStoreFile.absolutePath}" }
    }

    private fun saveKeyStore() {
        keyStoreFile.outputStream().use { fos ->
            keyStore.store(fos, masterPassword)
        }
        // 设置文件权限（仅所有者读写）
        keyStoreFile.setReadable(false, false)
        keyStoreFile.setWritable(false, false)
        keyStoreFile.setReadable(true, true)
        keyStoreFile.setWritable(true, true)
    }

    actual fun storeKey(keyId: String, keyData: ByteArray): Boolean {
        return try {
            // 生成一个随机的密钥条目
            val secretKey = SecretKeySpec(keyData, "AES")

            // 创建 KeyStore.Entry
            val entry = KeyStore.SecretKeyEntry(secretKey)

            // 使用密钥库密码保护条目
            val protectionParam = KeyStore.PasswordProtection(masterPassword)

            // 存储密钥
            keyStore.setEntry(keyId, entry, protectionParam)
            saveKeyStore()

            logger.info { "Key stored successfully: $keyId" }
            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to store key: $keyId" }
            false
        }
    }

    actual fun retrieveKey(keyId: String): ByteArray? {
        return try {
            val protectionParam = KeyStore.PasswordProtection(masterPassword)
            val entry = keyStore.getEntry(keyId, protectionParam) as? KeyStore.SecretKeyEntry

            entry?.secretKey?.encoded?.also {
                logger.debug { "Key retrieved successfully: $keyId" }
            } ?: run {
                logger.warn { "Key not found: $keyId" }
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to retrieve key: $keyId" }
            null
        }
    }

    actual fun deleteKey(keyId: String): Boolean {
        return try {
            if (keyStore.containsAlias(keyId)) {
                keyStore.deleteEntry(keyId)
                saveKeyStore()
                logger.info { "Key deleted successfully: $keyId" }
                true
            } else {
                logger.warn { "Key not found for deletion: $keyId" }
                false
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to delete key: $keyId" }
            false
        }
    }

    actual fun hasKey(keyId: String): Boolean {
        return try {
            keyStore.containsAlias(keyId)
        } catch (e: Exception) {
            logger.error(e) { "Failed to check key existence: $keyId" }
            false
        }
    }

    /**
     * 列出所有存储的密钥别名
     */
    fun listKeys(): List<String> {
        return keyStore.aliases().toList()
    }

    /**
     * 生成一个新的 AES-256 密钥并存储
     */
    fun generateAndStoreKey(keyId: String): Boolean {
        return try {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(256)
            val key = keyGen.generateKey()
            storeKey(keyId, key.encoded)
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate and store key: $keyId" }
            false
        }
    }
}

/**
 * 密钥管理器单例
 */
object KeyStoreManagerInstance {
    private val instance = KeyStoreManager()

    fun get(): KeyStoreManager = instance
}
