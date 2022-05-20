package com.example.record.database

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "audioRecord")
data class AudioRecord(
    var fileName: String,
    var filePath: String,
    var timeStamp: Long,
    var duration: String
){
    @PrimaryKey(autoGenerate = true)
    var id = 0
    @Ignore
    var isCheck = false
}
