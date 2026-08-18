package com.pmlp.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.tv.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.RenderersFactory
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import com.pmlp.edutask.ui.EventosSharedViewModel
import com.pmlp.tv.ui.TVHomeScreen
import com.pmlp.tv.ui.theme.EdutaskTheme
import androidx.media3.common.PlaybackParameters

class MainActivitytv : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializePlayer()

        setContent {
            val viewModel: EventosSharedViewModel = viewModel()
            EdutaskTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    TVHomeScreen(viewModel = viewModel)
                }
            }
        }
    }

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }

        exoPlayer?.let { player ->
            if (player.isPlaying) return

            val uri = "android.resource://${packageName}/${R.raw.himno}"
            player.setMediaItem(MediaItem.fromUri(uri))

            // REMOVER o COMENTAR esta línea para dejar que ExoPlayer gestione la velocidad según el reloj del archivo:
            // player.playbackParameters = PlaybackParameters(1.0f, 1.0f)

            player.repeatMode = Player.REPEAT_MODE_ALL
            player.prepare()
            player.playWhenReady = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.stop()
        exoPlayer?.clearMediaItems()
        exoPlayer?.release()
        exoPlayer = null
    }
}