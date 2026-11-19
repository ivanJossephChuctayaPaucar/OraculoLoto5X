package com.oraculoloto5x.app.pdf

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

private const val PREFS_PDF = "oraculo_pdf_prefs"
private const val KEY_TREE_URI = "pdf_tree_uri"

object NativePdfManager {

    private val renderMutex = Mutex()

    // ── Persist & read the SAF tree URI ──────────────────────────────────────

    fun saveTreeUri(context: Context, uri: Uri) {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        context.getSharedPreferences(PREFS_PDF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TREE_URI, uri.toString())
            .apply()
    }

    fun getTreeUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences(PREFS_PDF, Context.MODE_PRIVATE)
            .getString(KEY_TREE_URI, null) ?: return null
        return Uri.parse(uriStr)
    }

    fun hasTreeUri(context: Context): Boolean = getTreeUri(context) != null

    // ── Open ParcelFileDescriptor (Bundled assets Priority 1, SAF Priority 2) ──

    fun openParcelFileDescriptor(context: Context, fileName: String): ParcelFileDescriptor? {
        // 1. Try bundled assets / cacheDir storage
        try {
            val booksDir = File(context.cacheDir, "books")
            if (!booksDir.exists()) booksDir.mkdirs()
            val localFile = File(booksDir, fileName)

            if (!localFile.exists() || localFile.length() == 0L) {
                // Copy from bundled assets to cacheDir for efficient memory management
                try {
                    context.assets.open("books/$fileName").use { input ->
                        localFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (_: Exception) {
                    // Not in assets or error
                }
            }

            if (localFile.exists() && localFile.length() > 0L) {
                return ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }
        } catch (_: Exception) {}

        // 2. Try SAF URI fallback
        val safUri = findFileUri(context, fileName)
        if (safUri != null) {
            try {
                return context.contentResolver.openFileDescriptor(safUri, "r")
            } catch (_: Exception) {}
        }

        return null
    }

    // ── Find a file inside SAF tree by expected name (case-insensitive) ────────

    fun findFileUri(context: Context, expectedFileName: String): Uri? {
        val treeUri = getTreeUri(context) ?: return null
        return findInTree(context, treeUri, expectedFileName)
    }

    private fun findInTree(context: Context, treeUri: Uri, fileName: String): Uri? {
        val treeDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, treeDocId)

        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
                ),
                null, null, null
            )
            while (cursor != null && cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val name = cursor.getString(1) ?: ""
                val mime = cursor.getString(2) ?: ""

                if (name.equals(fileName, ignoreCase = true)) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                }
                // Recurse into subdirectories
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    val found = findInSubdir(context, treeUri, docId, fileName)
                    if (found != null) return found
                }
            }
            null
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    private fun findInSubdir(context: Context, treeUri: Uri, parentDocId: String, fileName: String): Uri? {
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(
                childrenUri,
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME
                ),
                null, null, null
            )
            while (cursor != null && cursor.moveToNext()) {
                val docId = cursor.getString(0)
                val name = cursor.getString(1) ?: ""
                if (name.equals(fileName, ignoreCase = true)) {
                    return DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                }
            }
            null
        } catch (e: Exception) {
            null
        } finally {
            cursor?.close()
        }
    }

    // ── Count how many of the 5 books are available ───────────────────────────

    fun countFoundBooks(context: Context): Int {
        val expected = listOf(
            "The_Life_You_Were_Born_to_Live_-_Dan_Millman.pdf",
            "_David_Phillips__The_Complete_Book_of_NumerologyBookFi.pdf",
            "Cheiro's Book of Numbers_text.pdf",
            "numberstheiroccu1911west.pdf",
            "gyhn.pdf"
        )
        return expected.count { hasBook(context, it) }
    }

    fun hasBook(context: Context, fileName: String): Boolean {
        // Check cacheDir / asset
        val localFile = File(File(context.cacheDir, "books"), fileName)
        if (localFile.exists() && localFile.length() > 0L) return true
        try {
            context.assets.open("books/$fileName").use { return true }
        } catch (_: Exception) {}
        return findFileUri(context, fileName) != null
    }

    // ── Render a page as Bitmap (thread-safe, dynamic scale max 1440px) ──────

    suspend fun renderPage(
        context: Context,
        fileName: String,
        pageIndex: Int,
        viewWidthPx: Int
    ): Bitmap? = withContext(Dispatchers.IO) {
        renderMutex.withLock {
            var pfd: ParcelFileDescriptor? = null
            var pdfRenderer: PdfRenderer? = null
            var page: PdfRenderer.Page? = null
            return@withLock try {
                pfd = openParcelFileDescriptor(context, fileName) ?: return@withLock null
                pdfRenderer = PdfRenderer(pfd)
                val safeIndex = pageIndex.coerceIn(0, pdfRenderer.pageCount - 1)
                page = pdfRenderer.openPage(safeIndex)

                // Escala dinámica limitada a un ancho máximo de 1440 px para proteger la RAM
                val dynamicScale = (1440f / page.width.toFloat()).coerceIn(1.0f, 2.0f)
                val width = (page.width * dynamicScale).toInt().coerceAtLeast(600)
                val height = (page.height * dynamicScale).toInt()

                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

                // Fondo cálido pergamino
                Canvas(bitmap).drawColor(Color.parseColor("#F5EFE6"))

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            } catch (e: Exception) {
                null
            } finally {
                page?.close()
                pdfRenderer?.close()
                pfd?.close()
            }
        }
    }

    /** Total page count for a given file */
    suspend fun getPageCount(context: Context, fileName: String): Int = withContext(Dispatchers.IO) {
        var pfd: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        return@withContext try {
            pfd = openParcelFileDescriptor(context, fileName) ?: return@withContext 0
            pdfRenderer = PdfRenderer(pfd)
            pdfRenderer.pageCount
        } catch (e: Exception) {
            0
        } finally {
            pdfRenderer?.close()
            pfd?.close()
        }
    }
}
