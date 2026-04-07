package site.addzero.kcloud.plugins.hostconfig.routes.tag

import org.koin.core.annotation.Single
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import site.addzero.kcloud.plugins.hostconfig.api.common.PageResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.ReplaceTagValueTextsRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagCreateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagPositionUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagResponse
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagUpdateRequest
import site.addzero.kcloud.plugins.hostconfig.api.tag.TagValueTextResponse
import site.addzero.kcloud.plugins.hostconfig.service.TagService

/**
 * 宿主配置里的点位详情与值文本路由。
 */
@Single
@RestController
@RequestMapping("/api/host-config/v1")
class TagController(
    private val tagService: TagService,
) {
    /** 分页读取设备点位列表，兼容旧前端的分页参数格式。 */
    @GetMapping("/devices/{deviceId}/tags")
    fun listTags(
        @PathVariable deviceId: Long,
        @RequestParam(name = "offset", defaultValue = "0") offset: Int,
        @RequestParam(name = "size", defaultValue = "42") size: Int,
    ): PageResponse<TagResponse> = tagService.listTags(deviceId, offset, size)

    /** 读取单个点位详情，用于右侧编辑表单或抽屉。 */
    @GetMapping("/tags/{tagId}")
    fun getTag(
        @PathVariable tagId: Long,
    ): TagResponse = tagService.getTag(tagId)

    /** 在指定设备下新增点位，并做地址与缩放校验。 */
    @PostMapping("/devices/{deviceId}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTag(
        @PathVariable deviceId: Long,
        @RequestBody request: TagCreateRequest,
    ): TagResponse = tagService.createTag(deviceId, request)

    /** 更新点位详情，保持旧宿主配置的字段集合。 */
    @PutMapping("/tags/{tagId}")
    fun updateTag(
        @PathVariable tagId: Long,
        @RequestBody request: TagUpdateRequest,
    ): TagResponse = tagService.updateTag(tagId, request)

    /** 全量替换枚举值文本，方便前端一次提交整组映射。 */
    @PutMapping("/tags/{tagId}/value-texts")
    fun replaceValueTexts(
        @PathVariable tagId: Long,
        @RequestBody request: ReplaceTagValueTextsRequest,
    ): List<TagValueTextResponse> = tagService.replaceValueTexts(tagId, request)

    /** 调整点位顺序，或者把点位移动到另一台设备。 */
    @PutMapping("/tags/{tagId}/position")
    fun updateTagPosition(
        @PathVariable tagId: Long,
        @RequestBody request: TagPositionUpdateRequest,
    ): TagResponse = tagService.updateTagPosition(tagId, request)

    /** 删除点位，并依赖数据库级联清理点位值文本。 */
    @DeleteMapping("/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(
        @PathVariable tagId: Long,
    ) {
        tagService.deleteTag(tagId)
    }
}
