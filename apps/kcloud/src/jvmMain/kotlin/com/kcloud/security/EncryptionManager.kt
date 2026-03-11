package com.kcloud.security

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.security.*
import java.security.spec.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.crypto.*
import javax.crypto.spec.*

private val logger: Logger = Logger.getLogger("com.kcloud.security.EncryptionManager")

/**
 * 加密配置
 *
 * @param algorithm 加密算法
 * @param keySize 密钥长度（位）
 * @param ivSize IV 长度（字节）
 * @param saltSize 盐长度（字节）
 * @param iterations PBKDF2 迭代次数
 * @param tagLength GCM 认证标签长度（位）
 */
data class EncryptionConfig(
    val algorithm: String = "AES",
    val keySize: Int = 256,
    val ivSize: Int = 12,  // GCM 推荐 12 字节
    val saltSize: Int = 32,
    val iterations: Int = 100000,
    val tagLength: Int = 128
) {
    companion object {
        val DEFAULT = EncryptionConfig()
    }
}

/**
 * 加密头部信息 - 存储在加密文件开头
 *
 * 格式: [magic(4)] [version(1)] [salt(32)] [iv(12)] [encryptedFileName(?)]
 * - magic: "MOEN" (MoveOff ENcrypted)
 * - version: 当前格式版本 (1)
 * - salt: PBKDF2 盐
 * - iv: AES-GCM IV
 */
data class EncryptionHeader(
    val version: Byte = 1,
    val salt: ByteArray,
    val iv: ByteArray,
    val encryptedFileName: ByteArray? = null
) {
    companion object {
        val MAGIC = byteArrayOf(0x4D, 0x4F, 0x45, 0x4E) // "MOEN"
        const val CURRENT_VERSION: Byte = 1

        fun readFrom(input: InputStream): EncryptionHeader? {
            val dataIn = DataInputStream(input)

            // 读取 magic
            val magic = ByteArray(4)
            if (dataIn.read(magic) != 4 || !magic.contentEquals(MAGIC)) {
                logger.severe("Invalid magic number, not a MoveOff encrypted file")
                return null
            }

            // 读取版本
            val version = dataIn.readByte()
            if (version != CURRENT_VERSION) {
                logger.severe("Unsupported encryption version: $version")
                return null
            }

            // 读取 salt (32 bytes)
            val salt = ByteArray(32)
            dataIn.readFully(salt)

            // 读取 iv (12 bytes)
            val iv = ByteArray(12)
            dataIn.readFully(iv)

            // 读取文件名长度（如果有）
            val fileNameLength = dataIn.readInt()
            val encryptedFileName = if (fileNameLength > 0) {
                ByteArray(fileNameLength).also { dataIn.readFully(it) }
            } else null

            return EncryptionHeader(version, salt, iv, encryptedFileName)
        }
    }

    fun writeTo(output: OutputStream) {
        val dataOut = DataOutputStream(output)
        dataOut.write(MAGIC)
        dataOut.writeByte(version.toInt())
        dataOut.write(salt)
        dataOut.write(iv)
        dataOut.writeInt(encryptedFileName?.size ?: 0)
        encryptedFileName?.let { dataOut.write(it) }
        dataOut.flush()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptionHeader) return false
        return version == other.version &&
                salt.contentEquals(other.salt) &&
                iv.contentEquals(other.iv) &&
                encryptedFileName?.contentEquals(other.encryptedFileName ?: byteArrayOf()) == true
    }

    override fun hashCode(): Int {
        var result = version.toInt()
        result = 31 * result + salt.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + (encryptedFileName?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * 加密管理器 - 处理端到端加密
 *
 * ## 安全设计
 * - 使用 AES-256-GCM 进行认证加密
 * - 使用 PBKDF2WithHmacSHA256 从密码派生密钥
 * - 每个文件使用随机生成的 salt 和 IV
 * - 加密文件头部包含所有必要的解密信息
 */
class EncryptionManager(
    private val config: EncryptionConfig = EncryptionConfig.DEFAULT
) {
    private val cipher: Cipher = Cipher.getInstance("${config.algorithm}/GCM/NoPadding")

    /**
     * 从密码派生密钥
     */
    fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            config.iterations,
            config.keySize
        )
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, config.algorithm)
    }

    /**
     * 生成随机字节数组
     */
    fun generateRandomBytes(size: Int): ByteArray {
        return ByteArray(size).apply {
            SecureRandom().nextBytes(this)
        }
    }

    /**
     * 加密文件
     *
     * @param inputFile 输入文件
     * @param outputFile 输出文件
     * @param password 加密密码
     * @param originalFileName 原始文件名（可选，用于存储在加密文件中）
     * @return 加密结果
     */
    suspend fun encryptFile(
        inputFile: File,
        outputFile: File,
        password: String,
        originalFileName: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // 生成随机 salt 和 IV
            val salt = generateRandomBytes(config.saltSize)
            val iv = generateRandomBytes(config.ivSize)

            // 派生密钥
            val key = deriveKey(password, salt)

            // 加密文件名（可选）
            val encryptedFileName = originalFileName?.let { name ->
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(config.tagLength, iv))
                cipher.doFinal(name.toByteArray(Charsets.UTF_8))
            }

            // 写入头部
            val header = EncryptionHeader(
                salt = salt,
                iv = iv,
                encryptedFileName = encryptedFileName
            )

            FileOutputStream(outputFile).use { fos ->
                // 写入头部
                header.writeTo(fos)

                // 初始化加密器
                cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(config.tagLength, iv))

                // 使用 CipherOutputStream 进行流式加密
                CipherOutputStream(fos, cipher).use { cos ->
                    inputFile.inputStream().use { input ->
                        input.copyTo(cos, bufferSize = 8192)
                    }
                }
            }

            logger.info("File encrypted successfully: ${inputFile.name} -> ${outputFile.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to encrypt file: ${inputFile.name}", e)
            Result.failure(e)
        }
    }

    /**
     * 解密文件
     *
     * @param inputFile 加密文件
     * @param outputFile 输出文件
     * @param password 解密密码
     * @return 解密结果，包含原始文件名（如果有）
     */
    suspend fun decryptFile(
        inputFile: File,
        outputFile: File,
        password: String
    ): Result<String?> = withContext(Dispatchers.IO) {
        try {
            FileInputStream(inputFile).use { fis ->
                // 读取头部
                val header = EncryptionHeader.readFrom(fis)
                    ?: return@withContext Result.failure(IllegalArgumentException("Invalid encrypted file format"))

                // 派生密钥
                val key = deriveKey(password, header.salt)

                // 解密文件名
                val originalFileName = header.encryptedFileName?.let { encrypted ->
                    try {
                        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(config.tagLength, header.iv))
                        String(cipher.doFinal(encrypted), Charsets.UTF_8)
                    } catch (e: Exception) {
                        logger.warning("Failed to decrypt file name, password might be wrong")
                        null
                    }
                }

                // 初始化解密器
                cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(config.tagLength, header.iv))

                // 确保输出目录存在
                outputFile.parentFile?.mkdirs()

                // 使用 CipherInputStream 进行流式解密
                CipherInputStream(fis, cipher).use { cis ->
                    outputFile.outputStream().use { output ->
                        cis.copyTo(output, bufferSize = 8192)
                    }
                }

                logger.info("File decrypted successfully: ${inputFile.name} -> ${outputFile.name}")
                Result.success(originalFileName)
            }
        } catch (e: AEADBadTagException) {
            logger.log(Level.SEVERE, "Authentication failed, file may be tampered or password is wrong", e)
            Result.failure(SecurityException("解密失败：认证标签不匹配，密码错误或文件被篡改", e))
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Failed to decrypt file: ${inputFile.name}", e)
            Result.failure(e)
        }
    }

    /**
     * 检查文件是否是 MoveOff 加密格式
     */
    fun isEncryptedFile(file: File): Boolean {
        return try {
            FileInputStream(file).use { fis ->
                val magic = ByteArray(4)
                fis.read(magic)
                magic.contentEquals(EncryptionHeader.MAGIC)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 加密数据块（小数据量）
     */
    fun encryptData(data: ByteArray, password: String): ByteArray {
        val salt = generateRandomBytes(config.saltSize)
        val iv = generateRandomBytes(config.ivSize)
        val key = deriveKey(password, salt)

        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(config.tagLength, iv))
        val encrypted = cipher.doFinal(data)

        // 组合: [salt][iv][encrypted]
        return salt + iv + encrypted
    }

    /**
     * 解密数据块（小数据量）
     */
    fun decryptData(encryptedData: ByteArray, password: String): ByteArray {
        val salt = encryptedData.copyOfRange(0, config.saltSize)
        val iv = encryptedData.copyOfRange(config.saltSize, config.saltSize + config.ivSize)
        val encrypted = encryptedData.copyOfRange(config.saltSize + config.ivSize, encryptedData.size)

        val key = deriveKey(password, salt)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(config.tagLength, iv))
        return cipher.doFinal(encrypted)
    }
}

/**
 * 加密存储包装器 - 透明加密/解密存储操作
 *
 * 包装一个 StorageClient，在上传前自动加密，下载后自动解密
 */
class EncryptedStorageClient(
    private val delegate: com.kcloud.sync.api.StorageClient,
    private val encryptionManager: EncryptionManager,
    private val passwordProvider: () -> String
) : com.kcloud.sync.api.StorageClient {

    override suspend fun testConnection(): Boolean {
        return delegate.testConnection()
    }

    override suspend fun listObjects(prefix: String?): List<com.kcloud.sync.api.RemoteObject> {
        return delegate.listObjects(prefix)
    }

    override suspend fun uploadObject(
        localPath: String,
        remotePath: String,
        progress: (Long, Long) -> Unit
    ): com.kcloud.sync.api.UploadResult {
        val originalFile = File(localPath)
        if (!originalFile.exists()) {
            return com.kcloud.sync.api.UploadResult(success = false, error = "文件不存在: $localPath")
        }

        // 创建临时加密文件
        val tempEncryptedFile = File.createTempFile("moveoff_enc_", ".tmp")

        return try {
            // 加密文件
            val password = passwordProvider()
            val encryptResult = encryptionManager.encryptFile(
                originalFile,
                tempEncryptedFile,
                password,
                originalFile.name
            )

            if (encryptResult.isFailure) {
                return com.kcloud.sync.api.UploadResult(
                    success = false,
                    error = "加密失败: ${encryptResult.exceptionOrNull()?.message}"
                )
            }

            // 上传加密后的文件（添加 .enc 后缀）
            val encryptedRemotePath = "$remotePath.enc"
            val uploadResult = delegate.uploadObject(
                tempEncryptedFile.absolutePath,
                encryptedRemotePath
            ) { transferred, total ->
                progress(transferred, total)
            }

            uploadResult
        } finally {
            // 清理临时文件
            tempEncryptedFile.delete()
        }
    }

    override suspend fun downloadObject(
        remotePath: String,
        localPath: String,
        progress: (Long, Long) -> Unit
    ): com.kcloud.sync.api.DownloadResult {
        val encryptedRemotePath = "$remotePath.enc"
        val tempEncryptedFile = File.createTempFile("moveoff_enc_", ".tmp")

        return try {
            // 下载加密文件
            val downloadResult = delegate.downloadObject(
                encryptedRemotePath,
                tempEncryptedFile.absolutePath
            ) { transferred, total ->
                progress(transferred, total)
            }

            if (!downloadResult.success) {
                // 尝试下载未加密的原始文件
                return delegate.downloadObject(remotePath, localPath, progress)
            }

            // 解密文件
            val password = passwordProvider()
            val decryptResult = encryptionManager.decryptFile(
                tempEncryptedFile,
                File(localPath),
                password
            )

            if (decryptResult.isFailure) {
                return com.kcloud.sync.api.DownloadResult(
                    success = false,
                    error = "解密失败: ${decryptResult.exceptionOrNull()?.message}"
                )
            }

            com.kcloud.sync.api.DownloadResult(success = true, bytesDownloaded = downloadResult.bytesDownloaded)
        } finally {
            tempEncryptedFile.delete()
        }
    }

    override suspend fun deleteObject(remotePath: String): Boolean {
        // 删除加密文件和可能的未加密文件
        val encryptedDeleted = delegate.deleteObject("$remotePath.enc")
        val originalDeleted = delegate.deleteObject(remotePath)
        return encryptedDeleted || originalDeleted
    }

    override suspend fun getObjectMetadata(remotePath: String): com.kcloud.sync.api.RemoteObject? {
        // 优先获取加密文件的元数据
        return delegate.getObjectMetadata("$remotePath.enc")
            ?: delegate.getObjectMetadata(remotePath)
    }
}
