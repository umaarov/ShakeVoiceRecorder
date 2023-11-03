package uz.umarov.shakevoicerecorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import uz.umarov.shakevoicerecorder.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometerSensor: Sensor
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var binding: ActivityMainBinding
    private var isRecording = false
    private var audioFilePath = ""
    private val audioRecordings = mutableListOf<AudioRecording>()
    private lateinit var adapter: AudioListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        shakeDetector = ShakeDetector(object : ShakeDetector.OnShakeListener {
            override fun onShake() {
                if (!isRecording) {
                    startRecording()
                }
            }
        })
        binding.apply {
            btnRecord.setOnClickListener {
                if (!isRecording) {
                    startRecording()
                }
            }
            btnStopPlayback.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    stopPlayback()
                }
            }
            btnStop.setOnClickListener {
                if (isRecording) {
                    stopRecording()
                }
            }

            btnPlay.setOnClickListener {
                if (!isRecording) {
                    playRecording()
                }
            }

            // Initialize RecyclerView and adapter
            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = AudioListAdapter(audioRecordings)
            recyclerView.adapter = adapter
        }
    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            shakeDetector, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(shakeDetector)
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            audioFilePath =
                "${getExternalFilesDir(Environment.DIRECTORY_MUSIC)?.absolutePath}/recording.mp3"

            mediaRecorder = MediaRecorder()
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setOutputFile(audioFilePath)
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                mediaRecorder.prepare()
                mediaRecorder.start()
                isRecording = true
                updateUI()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.release()
        isRecording = false
        updateUI()

        // Add the recorded audio to the list
        val audioRecording =
            AudioRecording(audioFilePath, "Audio Recording ${audioRecordings.size + 1}")
        audioRecordings.add(audioRecording)
        adapter.notifyDataSetChanged()
    }

    private fun updateUI() {
        binding.apply {
            if (isRecording) {
                btnRecord.visibility = View.GONE
                btnStop.visibility = View.VISIBLE
                btnPlay.visibility = View.GONE
                btnStopPlayback.visibility = View.GONE
                tvStatus.text = "Recording..."
            } else {
                btnRecord.visibility = View.VISIBLE
                btnStop.visibility = View.GONE
                btnPlay.visibility = View.VISIBLE
                btnStopPlayback.visibility = View.GONE
                tvStatus.text = "Status: Ready"
            }
        }
    }

    private fun stopPlayback() {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        updateUI()
    }

    private fun playRecording() {
        if (audioFilePath.isNotEmpty()) {
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(audioFilePath)
                mediaPlayer.prepare()
                mediaPlayer.start()
                updateUIForPlayback()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUIForPlayback() {
        binding.apply {
            btnRecord.visibility = View.GONE
            btnStop.visibility = View.GONE
            btnPlay.visibility = View.GONE
            btnStopPlayback.visibility = View.VISIBLE
            tvStatus.text = "Playing..."
        }
    }

    companion object {
        private const val RECORD_AUDIO_PERMISSION_CODE = 101
    }
}
