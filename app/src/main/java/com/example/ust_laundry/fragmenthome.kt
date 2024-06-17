package com.example.ust_laundry

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.ust_laundry.databinding.FragmentHomeBinding

class fragmenthome : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var mediaController: MediaController
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var updateSeekBarRunnable: Runnable

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Initialize MediaController and VideoView
        mediaController = MediaController(requireContext())
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.playPauseButton.setImageResource(R.drawable.ic_pause)

        // Set video from raw resource
        val rawRes = resources.getIdentifier("video", "raw", requireContext().packageName)
        if (rawRes != 0) {
            val videoUri = Uri.parse("android.resource://${requireContext().packageName}/$rawRes")
            binding.videoView.setVideoURI(videoUri)
        } else {
            Log.e("Error VIdeo","Video tidak ditemukan")
        }

        // Setup VideoView listener to sync with SeekBar
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            binding.seekBar.max = mediaPlayer.duration
            mediaPlayer.start()
            updateSeekBar()
        }

        // Setup error listener for VideoView
        binding.videoView.setOnErrorListener { mp, what, extra ->
            // Tangani error di sini, misalnya dengan menampilkan pesan error
            return@setOnErrorListener true
        }

        // Setup play/pause button
        binding.playPauseButton.setOnClickListener {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
            } else {
                binding.videoView.start()
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
            }
        }

        // Setup listener for SeekBar
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    binding.videoView.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Setup runnable to update SeekBar
        updateSeekBarRunnable = object : Runnable {
            override fun run() {
                binding.seekBar.progress = binding.videoView.currentPosition
                handler.postDelayed(this, 1000)
            }
        }

        return view
    }

    private fun updateSeekBar() {
        handler.postDelayed(updateSeekBarRunnable, 1000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateSeekBarRunnable)
        binding.videoView.stopPlayback()
    }
}