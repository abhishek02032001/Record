package com.example.record.database

import androidx.room.*

@Dao
interface AudioDAO {
    @Query("SELECT * FROM audioRecord")
    fun getAllRecord() : List<AudioRecord>

    @Insert
    fun insertRecord(vararg audioRecord: AudioRecord)

    @Delete
    fun deleteRecord(audioRecord: AudioRecord)

    @Delete
    fun deleteManyRecord(audioRecord : ArrayList<AudioRecord>)

    @Update
    fun updateRecord(audioRecord: AudioRecord)
}