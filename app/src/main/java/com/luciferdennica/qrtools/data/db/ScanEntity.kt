package com.luciferdennica.qrtools.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val format: String,
    val type: String,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val isHidden: Boolean = false,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
