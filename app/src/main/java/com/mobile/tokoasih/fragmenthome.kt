package com.mobile.tokoasih

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import com.mobile.tokoasih.databinding.FragmentHomeBinding

class fragmenthome : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var player: ExoPlayer
    private lateinit var playPauseImageView: ImageView
    private lateinit var seekBar: SeekBar
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        playPauseImageView = binding.playPauseImageView
        seekBar = binding.seekBar

        initializePlayer()

        playPauseImageView.setOnClickListener {
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }

        return view
    }

    private fun initializePlayer() {
        try {
            player = ExoPlayer.Builder(requireContext().applicationContext).build()
            binding.playerView.player = player

            val rawRes = resources.getIdentifier("video2", "raw", requireContext().packageName)
            if (rawRes != 0) {
                val videoUri = Uri.parse("android.resource://${requireContext().packageName}/$rawRes")
                val mediaItem = MediaItem.fromUri(videoUri)
                player.setMediaItem(mediaItem)
                player.prepare()
            } else {
                Log.e("Error Video", "Video tidak ditemukan")
            }

            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playPauseImageView.setImageResource(
                        if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    )
                }
            })

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        seekBar.max = player.duration.toInt()
                    }
                }

                override fun onPositionDiscontinuity(reason: Int) {
                    seekBar.progress = player.currentPosition.toInt()
                }
            })

            handler.post(object : Runnable {
                override fun run() {
                    seekBar.progress = player.currentPosition.toInt()
                    handler.postDelayed(this, 1000)
                }
            })

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        player.seekTo(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

        } catch (e: Exception) {
            Log.e("ExoPlayer Init Error", "Error initializing ExoPlayer: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }
}
