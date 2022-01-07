package com.easyfun.mediaplayer

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import com.github.mminng.media.renderer.Renderer
import com.github.mminng.media.renderer.SurfaceRenderView

class TestActivity : AppCompatActivity() {

    val player: MediaPlayer = MediaPlayer()
    private val surfaceView: SurfaceRenderView by lazy {
        findViewById(R.id.surfaceView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        surfaceView.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        surfaceView.setCallback(object : Renderer.OnRenderCallback {
            override fun onRenderCreated(surface: Surface) {
                player.setSurface(surface)
            }

            override fun onRenderChanged(width: Int, height: Int) {
            }

            override fun onRenderDestroyed() {
            }
        })
        player.setAudioStreamType(AudioManager.STREAM_MUSIC)
//        surfaceView.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        player.setOnPreparedListener {
            player.start()
        }
        player.setOnVideoSizeChangedListener { mp, width, height ->
            surfaceView.setVideoSize(width.toFloat(), height.toFloat())
        }
        player.setDataSource("https://vfx.mtime.cn/Video/2021/12/15/mp4/211215163524157166.mp4")
        player.prepareAsync()

    }

    override fun onRestart() {
        super.onRestart()
        player.start()
    }

    override fun onStop() {
        super.onStop()
        if (player.isPlaying) {
            player.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}