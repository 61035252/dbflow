package com.raizlabs.dbflow5.provider

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import com.raizlabs.dbflow5.annotation.provider.ContentProvider
import com.raizlabs.dbflow5.annotation.provider.ContentType
import com.raizlabs.dbflow5.annotation.provider.ContentUri
import com.raizlabs.dbflow5.annotation.provider.Notify
import com.raizlabs.dbflow5.annotation.provider.NotifyMethod
import com.raizlabs.dbflow5.annotation.provider.PathSegment
import com.raizlabs.dbflow5.annotation.provider.TableEndpoint
import com.raizlabs.dbflow5.getContentValuesKey

@ContentProvider(authority = TestContentProvider.AUTHORITY, database = ContentDatabase::class,
        baseContentUri = TestContentProvider.BASE_CONTENT_URI)
object TestContentProvider {

    const val AUTHORITY = "com.raizlabs.dbflow5.test.provider"

    const val BASE_CONTENT_URI = "content://"

    private fun buildUri(vararg paths: String): Uri {
        val builder = Uri.parse(BASE_CONTENT_URI + AUTHORITY).buildUpon()
        for (path in paths) {
            builder.appendPath(path)
        }
        return builder.build()
    }

    @TableEndpoint(name = ContentProviderModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object ContentProviderModel {

        const val ENDPOINT = "ContentProviderModel"

        @JvmStatic
        @ContentUri(path = ENDPOINT,
                type = ContentType.VND_MULTIPLE + ENDPOINT)
        var CONTENT_URI = buildUri(ENDPOINT)

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#",
                type = ContentType.VND_SINGLE + ENDPOINT,
                segments = arrayOf(PathSegment(segment = 1, column = "id")))
        fun withId(id: Long): Uri {
            return buildUri(id.toString())
        }

        @JvmStatic
        @Notify(notifyMethod = NotifyMethod.INSERT, paths = arrayOf(ENDPOINT + "/#"))
        fun onInsert(contentValues: ContentValues): Array<Uri> {
            val id = contentValues.getAsLong("id")!!
            return arrayOf(withId(id))
        }

    }

    @TableEndpoint(name = NoteModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object NoteModel {

        const val ENDPOINT = "NoteModel"

        @ContentUri(path = ENDPOINT, type = ContentType.VND_MULTIPLE + ENDPOINT)
        var CONTENT_URI = buildUri(ENDPOINT)

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#", type = ContentType.VND_MULTIPLE + ENDPOINT,
                segments = arrayOf(PathSegment(column = "id", segment = 1)))
        fun withId(id: Long): Uri {
            return buildUri(ENDPOINT, id.toString())
        }

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#/#",
                type = ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
                segments = arrayOf(PathSegment(column = "id", segment = 2)))
        fun fromList(id: Long): Uri {
            return buildUri(ENDPOINT, "fromList", id.toString())
        }

        @JvmStatic
        @ContentUri(path = ENDPOINT + "/#/#",
                type = ContentType.VND_SINGLE + ContentProviderModel.ENDPOINT,
                segments = arrayOf(PathSegment(column = "id", segment = 1),
                        PathSegment(column = "isOpen", segment = 2)))
        fun withOpenId(id: Long, isOpen: Boolean): Uri {
            return buildUri(ENDPOINT, id.toString(), isOpen.toString())
        }

        @JvmStatic
        @Notify(notifyMethod = NotifyMethod.INSERT, paths = arrayOf(ENDPOINT))
        fun onInsert(contentValues: ContentValues): Array<Uri> {
            val listId = contentValues.getAsLong(getContentValuesKey(contentValues, "providerModel"))!!
            return arrayOf(ContentProviderModel.withId(listId), fromList(listId))
        }

        @JvmStatic
        @Notify(notifyMethod = NotifyMethod.INSERT, paths = arrayOf(ENDPOINT))
        fun onInsert2(contentValues: ContentValues): Uri {
            val listId = contentValues.getAsLong(getContentValuesKey(contentValues, "providerModel"))!!
            return fromList(listId)
        }

        @JvmStatic
        @Notify(notifyMethod = NotifyMethod.UPDATE, paths = arrayOf(ENDPOINT + "/#"))
        fun onUpdate(context: Context, uri: Uri): Array<Uri> {
            val noteId = java.lang.Long.valueOf(uri.pathSegments[1])!!
            val c = context.contentResolver.query(uri, arrayOf("noteModel"), null, null, null)
            c!!.moveToFirst()
            val listId = c.getLong(c.getColumnIndex("providerModel"))
            c.close()

            return arrayOf(withId(noteId), fromList(listId), ContentProviderModel.withId(listId))
        }

        @JvmStatic
        @Notify(notifyMethod = NotifyMethod.DELETE, paths = arrayOf(ENDPOINT + "/#"))
        fun onDelete(context: Context, uri: Uri): Array<Uri> {
            val noteId = java.lang.Long.valueOf(uri.pathSegments[1])!!
            val c = context.contentResolver.query(uri, null, null, null, null)
            c!!.moveToFirst()
            val listId = c.getLong(c.getColumnIndex("providerModel"))
            c.close()

            return arrayOf(withId(noteId), fromList(listId), ContentProviderModel.withId(listId))
        }
    }

    @TableEndpoint(name = TestSyncableModel.ENDPOINT, contentProvider = ContentDatabase::class)
    object TestSyncableModel {

        const val ENDPOINT = "TestSyncableModel"

        @ContentUri(path = ENDPOINT, type = "${ContentType.VND_MULTIPLE}${ENDPOINT}")
        var CONTENT_URI = buildUri(ENDPOINT)
    }
}