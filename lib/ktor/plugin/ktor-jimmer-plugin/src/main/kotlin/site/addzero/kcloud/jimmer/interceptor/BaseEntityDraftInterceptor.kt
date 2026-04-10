package site.addzero.kcloud.jimmer.interceptor

import org.babyfish.jimmer.kt.isLoaded
import org.babyfish.jimmer.sql.DraftInterceptor
import org.koin.core.annotation.Single
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntity
import site.addzero.kcloud.jimmer.model.entity.base.BaseEntityDraft

@Single
class BaseEntityDraftInterceptor : DraftInterceptor<BaseEntity, BaseEntityDraft> {
    override fun beforeSave(draft: BaseEntityDraft, original: BaseEntity?) {
        if (!isLoaded(draft, BaseEntity::updatedAt)) {
            draft.updatedAt = System.currentTimeMillis()
        }

        if (original === null) {
            if (!isLoaded(draft, BaseEntity::createdAt)) {
                draft.createdAt = System.currentTimeMillis()
            }
        }
    }
}
