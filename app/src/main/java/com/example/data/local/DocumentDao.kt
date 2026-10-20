package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents ORDER BY lastModified DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentByIdDirect(id: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(doc: DocumentEntity)

    @Update
    suspend fun updateDocument(doc: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)

    @Query("UPDATE documents SET isStarred = :isStarred WHERE id = :id")
    suspend fun updateStarred(id: String, isStarred: Boolean)

    @Query("UPDATE documents SET syncStatus = :status, cloudRevision = cloudRevision + 1 WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: String)

    // Comments
    @Query("SELECT * FROM comments WHERE documentId = :docId ORDER BY timestamp ASC")
    fun getCommentsForDocument(docId: String): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)

    @Query("UPDATE comments SET resolved = :resolved WHERE id = :commentId")
    suspend fun updateCommentResolved(commentId: String, resolved: Boolean)

    @Query("UPDATE comments SET repliesJson = :repliesJson WHERE id = :commentId")
    suspend fun updateCommentReplies(commentId: String, repliesJson: String)

    @Query("DELETE FROM comments WHERE id = :commentId")
    suspend fun deleteComment(commentId: String)

    // Versions
    @Query("SELECT * FROM document_versions WHERE documentId = :docId ORDER BY versionNumber DESC")
    fun getVersionsForDocument(docId: String): Flow<List<VersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: VersionEntity)
}
