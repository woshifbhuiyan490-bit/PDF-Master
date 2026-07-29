package com.example.data.repository

import com.example.data.db.AnnotationEntity
import com.example.data.db.BookmarkEntity
import com.example.data.db.PdfDao
import com.example.data.db.RecentPdfEntity
import kotlinx.coroutines.flow.Flow

class PdfRepository(private val pdfDao: PdfDao) {
    val allRecents: Flow<List<RecentPdfEntity>> = pdfDao.getAllRecents()
    val favoriteRecents: Flow<List<RecentPdfEntity>> = pdfDao.getFavoriteRecents()
    val allBookmarks: Flow<List<BookmarkEntity>> = pdfDao.getAllBookmarks()
    val allAnnotations: Flow<List<AnnotationEntity>> = pdfDao.getAllAnnotations()

    fun getRecentsByTag(tag: String): Flow<List<RecentPdfEntity>> = pdfDao.getRecentsByTag(tag)

    fun getBookmarksForPdf(pdfUri: String): Flow<List<BookmarkEntity>> {
        return pdfDao.getBookmarksForPdf(pdfUri)
    }

    fun getAnnotationsForPdf(pdfUri: String): Flow<List<AnnotationEntity>> {
        return pdfDao.getAnnotationsForPdf(pdfUri)
    }

    suspend fun getRecentByUri(uri: String): RecentPdfEntity? {
        return pdfDao.getRecentByUri(uri)
    }

    suspend fun addOrUpdateRecent(recent: RecentPdfEntity) {
        pdfDao.insertOrUpdateRecent(recent)
    }

    suspend fun toggleFavorite(uri: String, isFavorite: Boolean) {
        pdfDao.toggleFavorite(uri, isFavorite)
    }

    suspend fun updateTag(uri: String, tag: String) {
        pdfDao.updateTag(uri, tag)
    }

    suspend fun addReadingTime(uri: String, additionalMs: Long) {
        pdfDao.addReadingTime(uri, additionalMs)
    }

    suspend fun deleteRecent(recent: RecentPdfEntity) {
        pdfDao.deleteRecent(recent)
    }

    suspend fun deleteRecentByUri(uri: String) {
        pdfDao.deleteRecentByUri(uri)
    }

    suspend fun clearAllRecents() {
        pdfDao.clearAllRecents()
    }

    suspend fun addBookmark(bookmark: BookmarkEntity) {
        pdfDao.insertBookmark(bookmark)
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) {
        pdfDao.updateBookmark(bookmark)
    }

    suspend fun deleteBookmark(bookmark: BookmarkEntity) {
        pdfDao.deleteBookmark(bookmark)
    }

    suspend fun deleteBookmarkById(id: Int) {
        pdfDao.deleteBookmarkById(id)
    }

    suspend fun addAnnotation(annotation: AnnotationEntity) {
        pdfDao.insertAnnotation(annotation)
    }

    suspend fun deleteAnnotation(annotation: AnnotationEntity) {
        pdfDao.deleteAnnotation(annotation)
    }

    suspend fun deleteAnnotationById(id: Int) {
        pdfDao.deleteAnnotationById(id)
    }
}
