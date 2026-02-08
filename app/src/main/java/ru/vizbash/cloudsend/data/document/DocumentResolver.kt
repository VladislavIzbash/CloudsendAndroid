package ru.vizbash.cloudsend.data.document

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val TAG = "DocumentInfoResolver"

class DocumentResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun resolve(documentUri: String): DocumentInfo? {
        val document = DocumentFile.fromSingleUri(context, documentUri.toUri())!!

        val name = resolveName(documentUri) ?: return null

        val size = document.length()
        if (size == 0L) {
            Log.e(TAG, "Cannot read document length from $documentUri")
            return null
        }

        return DocumentInfo(name, size)
    }

    fun resolveName(documentUri: String): String? {
        val document = DocumentFile.fromSingleUri(context, documentUri.toUri())!!

        val name = document.name
        if (name == null) {
            Log.e(TAG, "Cannot read document name from $documentUri")
        }
        return name
    }
}