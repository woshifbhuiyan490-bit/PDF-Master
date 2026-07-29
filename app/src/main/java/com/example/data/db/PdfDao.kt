package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfDao {
    // Recent PDFs
    @Query("SELECT * FROM recent_pdfs ORDER BY lastOpenedTimestamp DESC")
    fun getAllRecents(): Flow<List<RecentPdfEntity>>

    @Query("SELECT * FROM recent_pdfs WHERE isFavorite = 1 ORDER BY lastOpenedTimestamp DESC")
    fun getFavoriteRecents(): Flow<List<RecentPdfEntity>>

    @Query("SELECT * FROM recent_pdfs WHERE tag = :tag ORDER BY lastOpenedTimestamp DESC")
    fun getRecentsByTag(tag: String): Flow<List<RecentPdfEntity>>

    @Query("SELECT * FROM recent_pdfs WHERE uri = :uri LIMIT 1")
    suspend fun getRecentByUri(uri: String): RecentPdfEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecent(recent: RecentPdfEntity)

    @Query("UPDATE recent_pdfs SET isFavorite = :isFavorite WHERE uri = :uri")
    suspend fun toggleFavorite(uri: String, isFavorite: Boolean)

    @Query("UPDATE recent_pdfs SET tag = :tag WHERE uri = :uri")
    suspend fun updateTag(uri: String, tag: String)

    @Query("UPDATE recent_pdfs SET totalReadingTimeMs = totalReadingTimeMs + :additionalMs WHERE uri = :uri")
    suspend fun addReadingTime(uri: String, additionalMs: Long)

    @Delete
    suspend fun deleteRecent(recent: RecentPdfEntity)

    @Query("DELETE FROM recent_pdfs WHERE uri = :uri")
    suspend fun deleteRecentByUri(uri: String)

    @Query("DELETE FROM recent_pdfs")
    suspend fun clearAllRecents()

    // Bookmarks
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE pdfUri = :pdfUri ORDER BY pageNumber ASC")
    fun getBookmarksForPdf(pdfUri: String): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Update
    suspend fun updateBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Int)

    // Annotations
    @Query("SELECT * FROM annotations ORDER BY timestamp DESC")
    fun getAllAnnotations(): Flow<List<AnnotationEntity>>

    @Query("SELECT * FROM annotations WHERE pdfUri = :pdfUri ORDER BY pageNumber ASC")
    fun getAnnotationsForPdf(pdfUri: String): Flow<List<AnnotationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnotation(annotation: AnnotationEntity)

    @Delete
    suspend fun deleteAnnotation(annotation: AnnotationEntity)

    @Query("DELETE FROM annotations WHERE id = :id")
    suspend fun deleteAnnotationById(id: Int)
}
