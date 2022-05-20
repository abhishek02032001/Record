package com.example.record.ui.view

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.SeekBar
import com.example.record.R
import com.example.record.databinding.ActivityRecordPlayerBinding
import com.example.record.services.MusicService

class RecordPlayerActivity : AppCompatActivity(), ServiceConnection {

    lateinit var binding: ActivityRecordPlayerBinding
    private lateinit var runnable: Runnable
    private lateinit var handler: Handler
    private var delay: Long = 1000L
    var isPlaying: Boolean = true
    var musicService: MusicService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)

        setUpNameOfRecord()

        binding.action.setOnClickListener {
            if (isPlaying) {
                pauseMediaPlayer()
            } else {
                resumeMediaPlayer()
            }
        }

        musicService?.mediaPlayer?.setOnCompletionListener {
            handleOnComplete()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p2) {
                    musicService?.mediaPlayer?.seekTo(p1)
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        binding.forward10.setOnClickListener {
            increaseDuration()
        }

        binding.backward10.setOnClickListener {
            decreaseDuration()
        }

        binding.share.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(intent.getStringExtra("filePath")))
            startActivity(Intent.createChooser(shareIntent, "Share Your Recordings"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        musicService?.mediaPlayer?.stop()
        musicService?.mediaPlayer?.release()
        handler.removeCallbacks(runnable)
    }

    private fun handleOnComplete() {
        isPlaying = false
        binding.action.setImageResource(R.drawable.ic_play)
        handler.removeCallbacks(runnable)
    }

    private fun setUpSeekbar() {
        binding.seekBar.max = musicService?.mediaPlayer?.duration!!
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            binding.seekBar.progress = musicService?.mediaPlayer!!.currentPosition
            handler.postDelayed(runnable, delay)
        }
    }

    private fun pauseMediaPlayer() {
        isPlaying = false
        binding.action.setImageResource(R.drawable.ic_play)
        musicService?.mediaPlayer?.pause()
        handler.removeCallbacks(runnable)
    }

    private fun resumeMediaPlayer() {
        isPlaying = true
        binding.action.setImageResource(R.drawable.ic_pause)
        musicService?.mediaPlayer?.start()
        handler.postDelayed(runnable, 0)
    }

    private fun setUpMediaPlayer() {
        val filePath = intent.getStringExtra("filePath")
        musicService?.mediaPlayer = MediaPlayer()
        musicService?.mediaPlayer!!.apply {
            setDataSource(filePath)
            prepare()
        }
    }

    private fun setUpNameOfRecord() {
        val name = intent.getStringExtra("recordName")
        binding.recordName.text = name
    }

    private fun increaseDuration() {
        musicService?.mediaPlayer?.seekTo(musicService?.mediaPlayer?.currentPosition!!.plus(10000))
        binding.seekBar.progress = musicService?.mediaPlayer!!.currentPosition
    }

    private fun decreaseDuration() {
        musicService?.mediaPlayer?.seekTo(musicService?.mediaPlayer?.currentPosition!!.minus(10000))
        binding.seekBar.progress = musicService?.mediaPlayer!!.currentPosition
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        setUpMediaPlayer()
        setUpSeekbar()
        resumeMediaPlayer()
        musicService?.showNotification()
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
        musicService = null
    }
}