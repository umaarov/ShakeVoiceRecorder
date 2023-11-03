package uz.umarov.shakevoicerecorder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AudioListAdapter(private val audioRecordings: List<AudioRecording>) :
    RecyclerView.Adapter<AudioListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAudioFileName: TextView = itemView.findViewById(R.id.tvAudioFileName)
        val btnPlayAudio: Button = itemView.findViewById(R.id.btnPlayAudio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_audio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val audioRecording = audioRecordings[position]
        holder.tvAudioFileName.text = audioRecording.fileName

        holder.btnPlayAudio.setOnClickListener {
            // Handle audio playback for the selected item
            // You can use audioRecording.filePath to play the audio
        }
    }

    override fun getItemCount(): Int {
        return audioRecordings.size
    }
}
