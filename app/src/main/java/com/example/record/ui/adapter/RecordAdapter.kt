package com.example.record.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.record.database.AudioRecord
import com.example.record.databinding.RecordedItemBinding
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RecordAdapter(var listener: MyOnClickListener) : RecyclerView.Adapter<RecordAdapter.RecordViewHolder>() {

    var recordList : ArrayList<AudioRecord> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = RecordedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val currentRecord = recordList[position]
        holder.recordTitle.text = currentRecord.fileName
        holder.recordDuration.text = currentRecord.duration
        holder.recordDate.text = getDateFromTimeStamp(currentRecord.timeStamp)
    }

    override fun getItemCount(): Int {
        return recordList.size
    }

    inner class RecordViewHolder(binding: RecordedItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var linearLayout = binding.nameAndDuration
        val recordTitle = binding.recordName
        val recordDate = binding.recordDate
        var recordDuration = binding.recordDuration
        init {
            linearLayout.setOnClickListener {
                var pos = adapterPosition
                listener.onItemClick(recordList[pos])
            }
        }
    }

    private fun getDateFromTimeStamp(timeStamp:Long) : String{
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        return simpleDateFormat.format(Date(timeStamp))
    }

    fun updateRecordList(newList : List<AudioRecord>){
        recordList.clear()
        recordList.addAll(newList)
        notifyDataSetChanged()
    }

    interface MyOnClickListener{
        fun onItemClick(record:AudioRecord)
    }
}