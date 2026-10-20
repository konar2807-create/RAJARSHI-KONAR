package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val blocksJson: String,
    val lastModified: Long,
    val createdAt: Long,
    val authorName: String = "You",
    val authorEmail: String = "user@wordcraft.cloud",
    val isStarred: Boolean = false,
    val themeColorHex: String = "#185ABD",
    val category: String = "General",
    val syncStatus: String = "SYNCED",
    val cloudRevision: Int = 1,
    val wordCount: Int = 0,
    val pageCount: Int = 1,
    val collaboratorCount: Int = 1,
    val watermarkText: String = "",
    val marginType: String = "NORMAL"
)

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val blockId: String?,
    val selectedText: String,
    val authorName: String,
    val authorColorHex: String,
    val content: String,
    val timestamp: Long,
    val resolved: Boolean,
    val repliesJson: String
)

@Entity(tableName = "document_versions")
data class VersionEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val versionNumber: Int,
    val label: String,
    val timestamp: Long,
    val authorName: String,
    val summary: String,
    val blocksSnapshotJson: String
)
