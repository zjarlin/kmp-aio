package site.addzero.notes

import site.addzero.notes.data.ApiNoteDataSource
import site.addzero.notes.data.MultiSourceNoteRepository
import site.addzero.notes.data.NoteRepository
import site.addzero.notes.model.DataSourceType

actual object PlatformRepositoryFactory {
    actual fun createNoteRepository(): NoteRepository {
        val local = ApiNoteDataSource(
            type = DataSourceType.SQLITE,
            sourceKey = "sqlite"
        )
        val remote = ApiNoteDataSource(
            type = DataSourceType.POSTGRES,
            sourceKey = "postgres"
        )
        return MultiSourceNoteRepository(local = local, remote = remote)
    }
}
