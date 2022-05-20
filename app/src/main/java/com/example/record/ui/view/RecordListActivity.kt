package com.example.record.ui.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.record.database.AppDatabase
import com.example.record.database.AudioRecord
import com.example.record.databinding.ActivityRecordListBinding
import com.example.record.ui.adapter.RecordAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RecordListActivity : AppCompatActivity(), RecordAdapter.MyOnClickListener {

    lateinit var binding: ActivityRecordListBinding
    lateinit var adapter: RecordAdapter
    lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "audioRecord").build()
        binding.recordList.layoutManager = LinearLayoutManager(this)
        adapter = RecordAdapter(this@RecordListActivity)
        binding.recordList.adapter = adapter
        getAllRecords()
    }

    private fun getAllRecords(){
        GlobalScope.launch(Dispatchers.IO) {
            var list = db.audioDAO().getAllRecord()
            adapter.updateRecordList(list)
        }
    }

    override fun onItemClick(record: AudioRecord) {
        var intent = Intent(this, RecordPlayerActivity::class.java)
        intent.putExtra("recordName", record.fileName)
        intent.putExtra("filePath", record.filePath)
        startActivity(intent)
    }

}