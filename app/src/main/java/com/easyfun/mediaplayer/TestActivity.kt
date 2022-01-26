package com.easyfun.mediaplayer

import android.media.AudioManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceControl
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.github.mminng.media.renderer.Renderer
import com.github.mminng.media.renderer.SurfaceRenderView

class TestActivity : AppCompatActivity() {

    val player: MediaPlayer = MediaPlayer()
    private val surfaceView: SurfaceView by lazy {
        findViewById(R.id.surfaceView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                player.setDisplay(holder)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }
        })
        player.setOnPreparedListener {
            player.start()
        }
        player.setDataSource("https://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4")
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