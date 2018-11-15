package com.dbflow5.provider

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.ForeignKeyReference
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.contentprovider.annotation.ContentProvider
import com.dbflow5.contentprovider.annotation.ContentType
import com.dbflow5.contentprovider.annotation.ContentUri
import com.dbflow5.contentprovider.annotation.TableEndpoint

/**
 * Description:
 */
@ContentProvider(authority = ContentDatabase.AUTHORITY, database = ContentDatabase::class,
        baseContentUri = ContentDatabase.BASE_CONTENT_URI)
@Database(version = ContentDatabase.VERSION)
abstract class ContentDatabase : ContentProviderDatabase() {
    companion object {
        const val BASE_CONTENT_URI = "content://"

        const val AUTHORITY = "com.grosner.content.test.ContentDatabase"

        const val VERSION = 1
    }
}

@TableEndpoint(name = ContentProviderModel.NAME, contentProvider = ContentDatabase::class)
@Table(database = ContentDatabase::class, name = ContentProviderModel.NAME, generateContentValues = true)
class ContentProviderModel(@PrimaryKey(autoincrement = true)
                           var id: Long = 0,
                           @Column
                           var notes: String? = null,
                           @Column
                           var title: String? = null) : BaseProviderModel() {

    override val deleteUri get() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override val insertUri get() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override val updateUri get() = TestContentProvider.ContentProviderModel.CONTENT_URI

    override val queryUri get() = TestContentProvider.ContentProviderModel.CONTENT_URI

    companion object {

        const val NAME = "ContentProviderModel"

        @ContentUri(path = NAME, type = "${ContentType.VND_MULTIPLE}${NAME}")
        val CONTENT_URI = ContentUtils.buildUriWithAuthority(ContentDatabase.AUTHORITY)
    }
}

@Table(database = ContentDatabase::class, generateContentValues = true)
class NoteModel(@PrimaryKey(autoincrement = true)
                var id: Long = 0,

                @ForeignKey(references = arrayOf(ForeignKeyReference(columnName = "providerModel",
                        foreignKeyColumnName = "id")))
                var contentProviderModel: ContentProviderModel? = null,

                @Column
                var note: String? = null,

                @Column
                var isOpen: Boolean = false) : BaseProviderModel() {

    override val deleteUri get() = TestContentProvider.NoteModel.CONTENT_URI

    override val insertUri get() = TestContentProvider.NoteModel.CONTENT_URI

    override val updateUri get() = TestContentProvider.NoteModel.CONTENT_URI

    override val queryUri get() = TestContentProvider.NoteModel.CONTENT_URI
}

@Table(database = ContentDatabase::class, generateContentValues = true)
class TestSyncableModel(@PrimaryKey(autoincrement = true)
                        var id: Long = 0,
                        @Column
                        var name: String? = null) : BaseSyncableProviderModel() {

    override val deleteUri get() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override val insertUri get() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override val updateUri get() = TestContentProvider.TestSyncableModel.CONTENT_URI

    override val queryUri get() = TestContentProvider.TestSyncableModel.CONTENT_URI
}