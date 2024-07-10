package com.example.stickynotes

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun soundEffects(): SoundEffectsState {
    val context = LocalContext.current
    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
    }

    val popSoundId = remember { soundPool.load(context, R.raw.pop_sound, 1) }

    fun playPopSound() {
        soundPool.play(popSoundId, 1f, 1f, 0, 0, 1f)
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPool.release()
        }
    }
    return remember { SoundEffectsState(::playPopSound) }
}
