package com.easyfun.mediaplayer

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.github.mminng.media.DefaultPlayer
import com.github.mminng.media.PlayerView
import com.github.mminng.media.DefaultController
import com.github.mminng.media.player.state.PlayerState
import com.github.mminng.media.renderer.RenderMode

class MainActivity : AppCompatActivity() {

    private val localPath: String =
        "/storage/emulated/0/Download/Movie/bilibili_2051251897.mp4"

    private val playerView: PlayerView by lazy {
        findViewById(R.id.player_view)
    }
    private val renderMode: Button by lazy {
        findViewById(R.id.button)
    }
    private val renderMode1: Button by lazy {
        findViewById(R.id.button1)
    }
    private val renderMode2: Button by lazy {
        findViewById(R.id.button2)
    }
    private val controller: Button by lazy {
        findViewById(R.id.controller)
    }
    private val controller1: Button by lazy {
        findViewById(R.id.controller1)
    }
    private val getState: Button by lazy {
        findViewById(R.id.getState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val controllerView = DefaultController(this)
        val controllerView1 = MyControllerView(this)
        playerView.setPlayer(DefaultPlayer())
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2021/09/30/mp4/210930112954504189.mp4")
//        playerView.setDataSource(localPath)
//        playerView.setDataSource("https://v.96koo.net/common/LzQxOTAvcmVsZWFzZS8yMDIwMDczMC9ETTRCV0cyV3llL0RNNEJXRzJXeWVfODQ4XzgwMA==_19929.m3u8")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2020/09/03/mp4/200903192102416527.mp4")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2021/01/07/mp4/210107172407759182.mp4")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2021/12/05/mp4/211205092838969197.mp4")
        playerView.setDataSource("https://vfx.mtime.cn/Video/2021/12/10/mp4/211210143103622104.mp4")
        playerView.setOnFullScreenModeChangedListener {
            requestedOrientation =
                if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                }
        }
        renderMode.setOnClickListener {
            playerView.setRenderMode(RenderMode.FIT)
        }
        renderMode1.setOnClickListener {
            playerView.setRenderMode(RenderMode.FILL)
        }
        renderMode2.setOnClickListener {
            playerView.setRenderMode(RenderMode.ZOOM)
        }
        controller.setOnClickListener {
            playerView.setController(controllerView)
        }
        controller1.setOnClickListener {
            playerView.setController(controllerView1)
        }
        getState.setOnClickListener {
            Toast.makeText(
                this,
                "${playerView.getPlayerState()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
//        playerView.start()
    }

    override fun onPause() {
        super.onPause()
        playerView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerView.release()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        } else {
            showSystemUI()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        playerView.requestFocus()
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideSystemUI()
        }
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_VISIBLE
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }

}