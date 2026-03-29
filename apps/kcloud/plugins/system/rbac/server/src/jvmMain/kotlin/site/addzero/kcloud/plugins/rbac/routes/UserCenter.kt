package site.addzero.kcloud.plugins.rbac.routes

import org.koin.mp.KoinPlatform
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import site.addzero.kcloud.plugins.rbac.UserProfileService
import site.addzero.kcloud.system.api.UserProfileDto
import site.addzero.kcloud.system.api.UserProfileUpdateRequest

/**
 * 用户中心服务端路由定义，同时作为客户端 API 生成源。
 */
/**
 * 读取当前桌面账号资料。
 */
@GetMapping("/api/system/user/profile")
fun getCurrentUserProfile(): UserProfileDto {
    return service().readCurrentProfile()
}

/**
 * 保存当前桌面账号资料。
 */
@PutMapping("/api/system/user/profile")
fun saveCurrentUserProfile(
    @RequestBody request: UserProfileUpdateRequest,
): UserProfileDto {
    return service().saveCurrentProfile(request)
}

private fun service(): UserProfileService {
    return KoinPlatform.getKoin().get()
}
