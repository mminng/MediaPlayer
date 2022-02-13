package com.easyfun.mediaplayer

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.DefaultController
import com.github.mminng.media.DefaultPlayer
import com.github.mminng.media.PlayerView
import com.github.mminng.media.renderer.RenderMode
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val localPath: String =
        "/storage/emulated/0/Quark/Download/bilibili_2051251897.mp4"
    private val localPath2: String =
        "/storage/emulated/0/Quark/Download/bilibili_932148655.mp4"
    private val localPath3: String =
        "/storage/emulated/0/Download/4k.mp4"
    private val localPath4: String =
        "/storage/emulated/0/Download/60fps_1080p.mp4"

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
    private val renderMode3: Button by lazy {
        findViewById(R.id.button3)
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

    private val playerContent: FrameLayout by lazy {
        findViewById(R.id.player_content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val controllerView = DefaultController(this)
        val controllerView1 = MyControllerView(this)
        val player = DefaultPlayer()
//        val player = Player(this)
        playerView.setPlayer(player)
        playerView.setController(controllerView)
        controllerView.setCoverPlayButtonResource(R.drawable.ic_action_paused)
        controllerView.setUpdateInterval(1000)
        controllerView.setMediaTitle("https://images.unsplash.com/photo-1634334181759-a965220b6a91?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHx0b3BpYy1mZWVkfDExfGJEbzQ4Y1Vod25ZfHxlbnwwfHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60")
        controllerView.setCover {
            Picasso.get()
                .load("https://images.unsplash.com/photo-1634334181759-a965220b6a91?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHx0b3BpYy1mZWVkfDExfGJEbzQ4Y1Vod25ZfHxlbnwwfHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60")
                .into(it)
        }
//        playerView.setDataSource(localPath)
//        playerView.setDataSource(localPath2)
//        playerView.setDataSource(localPath3)
//        playerView.setDataSource(localPath4)
//        playerView.setDataSource("https://v.96koo.net/common/LzQxOTAvcmVsZWFzZS8yMDIwMDczMC9ETTRCV0cyV3llL0RNNEJXRzJXeWVfODQ4XzgwMA==_19929.m3u8")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4")
        playerView.setDataSource("https://vfx.mtime.cn/Video/2022/01/14/mp4/220114181259659149.mp4")
//        playerView.prepare()
        renderMode.setOnClickListener {
            playerView.setRenderMode(RenderMode.FIT)
        }
        renderMode1.setOnClickListener {
            playerView.setRenderMode(RenderMode.FILL)
        }
        renderMode2.setOnClickListener {
            playerView.setRenderMode(RenderMode.ZOOM)
        }
        renderMode3.setOnClickListener {
            playerView.setRenderMode(RenderMode.DEFAULT)
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
        val layoutParams = playerView.layoutParams
        val contentView: ViewGroup = findViewById(Window.ID_ANDROID_CONTENT)
        playerView.setOnFullScreenModeChangedListener {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                controllerView.setTopControllerVisibility(View.INVISIBLE)
                contentView.removeView(playerView)
                playerContent.addView(playerView, layoutParams)
                setDecorVisible(this)
            } else {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                controllerView.setTopControllerVisibility(View.VISIBLE)
                playerContent.removeView(playerView)
                contentView.addView(
                    playerView, FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    )
                )
                hideStatusBar(this)
                hideNavigationBar(this)
            }
//            if (testP) {
//                testP = false
//                playerContent.removeView(playerView)
//                contentView.addView(
//                    playerView, FrameLayout.LayoutParams(
//                        FrameLayout.LayoutParams.MATCH_PARENT,
//                        FrameLayout.LayoutParams.MATCH_PARENT
//                    )
//                )
////                TransitionManager.beginDelayedTransition(contentView)
//                hideStatusBar(this@MainActivity)
//                hideNavigationBar(this@MainActivity)
//            } else {
//                testP = true
////                TransitionManager.beginDelayedTransition(contentView)
//                contentView.removeView(playerView)
//                playerContent.addView(playerView, layoutParams)
//                setDecorVisible(this@MainActivity)
//            }
        }
    }

    override fun onResume() {
        super.onResume()
        playerView.start()
    }

    override fun onStop() {
        super.onStop()
        playerView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        playerView.release()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideStatusBar(this)
            hideNavigationBar(this)
        } else {
            setDecorVisible(this)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            hideStatusBar(this)
            hideNavigationBar(this)
        }
    }

    fun hideStatusBar(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        val decorView = activity.window.decorView
        val uiOptions = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        decorView.systemUiVisibility = uiOptions
        supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.hide()
        }
    }

    fun hideNavigationBar(activity: Activity) {
        val uiOptions = activity.window.decorView.systemUiVisibility
        activity.window.decorView.systemUiVisibility =
            uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    fun setDecorVisible(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val decorView = activity.window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
        supportActionBar?.let {
            it.setShowHideAnimationEnabled(false)
            it.show()
        }
    }

}