package com.nothingsound.widget

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: SoundListAdapter

    private val soundStoppedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            adapter.clearPlayingState()
        }
    }

    private val pickAudio = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { promptForNameAndSave(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.sound_list)
        adapter = SoundListAdapter()
        listView.adapter = adapter

        findViewById<View>(R.id.btn_add_sound).setOnClickListener {
            pickAudio.launch("audio/*")
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
        ContextCompat.registerReceiver(
            this,
            soundStoppedReceiver,
            IntentFilter(SoundPlayService.ACTION_SOUND_STOPPED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(soundStoppedReceiver)
    }

    private fun promptForNameAndSave(uri: Uri) {
        val input = EditText(this).apply { hint = getString(R.string.hint_name_sound) }

        AlertDialog.Builder(this, R.style.Theme_NothingSound)
            .setTitle(R.string.hint_name_sound)
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val label = input.text.toString().ifBlank { "custom sound" }
                val savedPath = copyToInternalStorage(uri)
                if (savedPath != null) {
                    SoundRepository.addUserSound(this, label, savedPath)
                    SoundWidgetProvider.refreshAll(this)
                    adapter.refresh()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun copyToInternalStorage(uri: Uri): String? {
        return try {
            val soundsDir = File(filesDir, "sounds").apply { mkdirs() }
            val outFile = File(soundsDir, "sound_${System.currentTimeMillis()}.audio")
            contentResolver.openInputStream(uri)?.use { input ->
                outFile.outputStream().use { output -> input.copyTo(output) }
            }
            outFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private inner class SoundListAdapter : BaseAdapter() {
        private var items: List<SoundItem> = emptyList()
        private var playingId: String? = null

        fun refresh() {
            items = SoundRepository.getAll(this@MainActivity)
                .filterNot { (it.isAddTile || it.resName != null) } // show only user-added sounds
            notifyDataSetChanged()
        }

        fun clearPlayingState() {
            playingId = null
            notifyDataSetChanged()
        }

        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity)
                .inflate(R.layout.item_sound_row, parent, false)
            val item = items[position]

            view.findViewById<TextView>(R.id.row_label).text = item.label

            val playBtn = view.findViewById<ImageView>(R.id.row_play_stop)
            val isPlaying = playingId == item.id
            playBtn.setImageResource(if (isPlaying) R.drawable.ic_stop_circle else R.drawable.ic_play_circle)

            // Play/Stop sound when the play button or row is clicked
            val togglePlayback = {
                if (playingId == item.id) {
                    // Stop
                    val stopIntent = Intent(this@MainActivity, SoundPlayService::class.java).apply {
                        action = "STOP"
                    }
                    startService(stopIntent)
                    playingId = null
                } else {
                    // Play
                    val playIntent = Intent(this@MainActivity, SoundPlayService::class.java).apply {
                        putExtra(SoundPlayService.EXTRA_SOUND_ID, item.id)
                    }
                    startService(playIntent)
                    playingId = item.id
                }
                notifyDataSetChanged()
            }

            view.setOnClickListener { togglePlayback() }
            playBtn.setOnClickListener { togglePlayback() }

            // Remove sound only when the delete button is clicked
            view.findViewById<View>(R.id.row_delete).setOnClickListener {
                if (playingId == item.id) playingId = null
                SoundRepository.removeSound(this@MainActivity, item.id)
                SoundWidgetProvider.refreshAll(this@MainActivity)
                refresh()
            }
            return view
        }
    }
}
