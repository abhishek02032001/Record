package com.example.record.ui.view

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.example.record.R
import com.example.record.common.Constants
import com.example.record.common.Timer
import com.example.record.database.AppDatabase
import com.example.record.database.AudioRecord
import com.example.record.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), Timer.onTimerClickListener {

    private lateinit var binding: ActivityMainBinding
    private val permissionList = arrayOf(Manifest.permission.RECORD_AUDIO)
    private lateinit var recorder: MediaRecorder
    private var permissionGranted: Boolean = false
    private var isRecording: Boolean = false
    private var isPause: Boolean = false
    lateinit var timer: Timer
    lateinit var fileName: String
    lateinit var dirPath: String
    private lateinit var bottomSheetBehaviour: BottomSheetBehavior<LinearLayout>
    private lateinit var db: AppDatabase
    private var duration: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkRequiredPermission()

        db = Room.databaseBuilder(this, AppDatabase::class.java, "audioRecord").build()

        bottomSheetBehaviour = BottomSheetBehavior.from(binding.middle.bottomSheet)
        bottomSheetBehaviour.peekHeight = 0
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

        binding.playPause.setOnClickListener {
            handleRecordClick()
        }
        binding.cancel.setOnClickListener {
            showConfirmationDialog()
        }
        binding.done.setOnClickListener {
            doneCLickHandler()
        }

        binding.middle.cancelBottomSheet.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            bottomSheetCancel()
        }

        binding.middle.saveRecord.setOnClickListener {
            saveRecording()
        }

        binding.bottomSheetBackground.setOnClickListener {
            bottomSheetCancel()
        }

        binding.list.setOnClickListener {
            val intent = Intent(this, RecordListActivity::class.java)
            startActivity(intent)
        }

        timer = Timer(this)
    }

    private fun saveRecording() {
        val newFileName = binding.middle.fileNameInput.text.toString()
        resetRecorder()
        if (newFileName != fileName) {
            var newFile = File("$dirPath$newFileName.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)
        }

        var filePath = "$dirPath$newFileName.mp3"
        var timeStamp = Date().time

        var audioObj = AudioRecord(newFileName, filePath, timeStamp, duration)
        bottomSheetCancel()
        GlobalScope.launch(Dispatchers.IO) {
            db.audioDAO().insertRecord(audioObj)
        }
    }

    private fun doneCLickHandler() {
        pauseRecorder()
        bottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        binding.bottomSheetBackground.visibility = View.VISIBLE
        binding.middle.fileNameInput.setText(fileName)
    }

    private fun bottomSheetCancel() {
        hideKeyBoard(binding.root)
        binding.bottomSheetBackground.visibility = View.GONE
        Handler(Looper.getMainLooper()).postDelayed({
            bottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 1000)
    }

    private fun hideKeyBoard(view: View) {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun checkRequiredPermission() {
        permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            permissionList[0]
        ) == PackageManager.PERMISSION_GRANTED
        if (!permissionGranted)
            askPermission()
    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(
            this,
            permissionList,
            Constants.REQUEST_PERMISSION_CONSTANT
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_PERMISSION_CONSTANT)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private fun handleRecordClick() {
        if (!permissionGranted) {
            askPermission()
            return
        }
        when {
            isRecording -> pauseRecorder()
            isPause -> resumeRecorder()
            else -> startRecorder()
        }
    }

    private fun startRecorder() {
        recorder = MediaRecorder()

        dirPath = "${externalCacheDir?.absolutePath}/"
        val dateFormat = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = dateFormat.format(Date())
        fileName = "audio_record_$date"

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch (e: IOException) {
            }

            start()
            timer.start()
        }
        binding.waveform.visibility = View.VISIBLE
        binding.list.visibility = View.GONE
        binding.done.visibility = View.VISIBLE
        binding.playPause.setImageResource(R.drawable.ic_pause)
        binding.action.text = "Recording"
        isRecording = true
    }

    private fun resumeRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.resume()
        }
        binding.playPause.setImageResource(R.drawable.ic_pause)
        binding.action.text = "Recording"
        isRecording = true
        isPause = false
        timer.start()
    }

    private fun pauseRecorder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            recorder.pause()
        }
        binding.playPause.setImageResource(R.drawable.ic_play)
        binding.action.text = "Paused"
        isRecording = false
        isPause = true
        timer.pause()
    }

    private fun resetRecorder() {
        if(this::recorder.isInitialized){
            recorder.stop()
            recorder.release()
        }
        binding.playPause.setImageResource(R.drawable.ic_record)
        binding.done.visibility = View.GONE
        binding.list.visibility = View.VISIBLE
        binding.action.text = "Start"
        binding.time.text = "00:00:00"
        isRecording = false
        isPause = false
        binding.waveform.amplitudes.clear()
        binding.waveform.spikes.clear()
        binding.waveform.visibility = View.INVISIBLE
        timer.stop()
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Alert Dialog")
            setMessage("Are you sure you want to cancel the recording?")
            setPositiveButton("Yes") { _, _ ->
                resetRecorder()
            }
            setNegativeButton("No") { _, _ ->
                //Do Nothing
            }
        }
        builder.show()
    }

    override fun onTimeClick(duration: String) {
        binding.time.text = duration
        this.duration = duration.dropLast(3)
        binding.waveform.addAmplitude(recorder.maxAmplitude.toFloat())
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            setTitle("Record")
            setMessage("Are you sure you want to exit the recording app?")
            setPositiveButton("Yes") { _, _ ->
                finishAffinity()
            }
            setNegativeButton("No") { _, _ ->
                // Do Nothing
            }
        }
        builder.show()
    }

}