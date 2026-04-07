package site.addzero.kcloud.plugins.hostconfig.api.tag

import jakarta.validation.Valid
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
import site.addzero.kcloud.plugins.hostconfig.service.TagService

@RestController
@RequestMapping("/api/v1")
class TagController(
    private val tagService: TagService,
) {

    @GetMapping("/devices/{deviceId}/tags")
    fun listTags(
        @PathVariable deviceId: Long,
        @RequestParam(name = "o", defaultValue = "0") offset: Int,
        @RequestParam(name = "s", defaultValue = "42") size: Int,
    ): PageResponse<TagResponse> = tagService.listTags(deviceId, offset, size)

    @GetMapping("/tags/{tagId}")
    fun getTag(@PathVariable tagId: Long): TagResponse = tagService.getTag(tagId)

    @PostMapping("/devices/{deviceId}/tags")
    @ResponseStatus(HttpStatus.CREATED)
    fun createTag(
        @PathVariable deviceId: Long,
        @Valid @RequestBody request: TagCreateRequest,
    ): TagResponse = tagService.createTag(deviceId, request)

    @PutMapping("/tags/{tagId}")
    fun updateTag(
        @PathVariable tagId: Long,
        @Valid @RequestBody request: TagUpdateRequest,
    ): TagResponse = tagService.updateTag(tagId, request)

    @PutMapping("/tags/{tagId}/value-texts")
    fun replaceValueTexts(
        @PathVariable tagId: Long,
        @Valid @RequestBody request: ReplaceTagValueTextsRequest,
    ): List<TagValueTextResponse> = tagService.replaceValueTexts(tagId, request)

    @PutMapping("/tags/{tagId}/position")
    fun updateTagPosition(
        @PathVariable tagId: Long,
        @Valid @RequestBody request: TagPositionUpdateRequest,
    ): TagResponse = tagService.updateTagPosition(tagId, request)

    @DeleteMapping("/tags/{tagId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteTag(@PathVariable tagId: Long) {
        tagService.deleteTag(tagId)
    }
}
