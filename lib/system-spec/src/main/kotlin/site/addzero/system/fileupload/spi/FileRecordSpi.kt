package site.addzero.system.fileupload.spi

import site.addzero.system.common.dto.PageQuery
import site.addzero.system.common.dto.PageResult
import site.addzero.system.fileupload.dto.*

/**
 * 文件记录服务SPI
 * 管理文件元数据记录，与业务系统关联
 */
interface FileRecordSpi {

    /**
     * 创建文件记录
     * @param record 文件记录
     * @return 创建后的记录
     */
    fun create(record: FileRecordCreateRequest): FileRecordDTO

    /**
     * 根据ID获取文件记录
     */
    fun getById(id: String): FileRecordDTO?

    /**
     * 根据业务键获取文件记录
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    fun getByBizKey(bizType: String, bizId: String): List<FileRecordDTO>

    /**
     * 分页查询文件记录
     */
    fun page(query: FileRecordQuery): PageResult<FileRecordDTO>

    /**
     * 更新文件记录
     */
    fun update(id: String, request: FileRecordUpdateRequest): FileRecordDTO

    /**
     * 绑定业务关联
     * @param fileId 文件ID
     * @param bizType 业务类型
     * @param bizId 业务ID
     */
    fun bindBiz(fileId: String, bizType: String, bizId: String)

    /**
     * 解除业务绑定
     */
    fun unbindBiz(fileId: String, bizType: String, bizId: String)

    /**
     * 删除文件记录（软删除）
     */
    fun delete(id: String)

    /**
     * 批量删除文件记录
     */
    fun deleteBatch(ids: List<String>)

    /**
     * 获取存储桶使用统计
     */
    fun getStorageStats(bucket: String): StorageStats
}
