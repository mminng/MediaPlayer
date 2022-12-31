package com.easyfun.mediaplayer

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.PlayerView
import com.github.mminng.media.controller.DefaultController
import com.github.mminng.media.player.DefaultPlayer
import com.github.mminng.media.player.PlayerOrientation
import com.github.mminng.media.renderer.RenderMode
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val localPath: String =
        "/storage/emulated/0/Movies/1080p.mp4"
    private val localPath2: String =
        "/storage/emulated/0/Download/123.mp4"
    private val localPath3: String =
        "/storage/emulated/0/Download/321.mp4"

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
    private val setSpeed: Button by lazy {
        findViewById(R.id.setSpeed)
    }
    private val playerContent: FrameLayout by lazy {
        findViewById(R.id.player_content)
    }
    val controllerView: DefaultController by lazy {
        DefaultController(this)
    }
    val url =
        "https://images.unsplash.com/photo-1634334181759-a965220b6a91?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHx0b3BpYy1mZWVkfDExfGJEbzQ4Y1Vod25ZfHxlbnwwfHx8fA%3D%3D&auto=format&fit=crop&w=500&q=60"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        controllerView.onBindCover(url) { url, view ->
            Picasso.get()
                .load(url)
                .into(view)
        }
        val player = DefaultPlayer()
//        val player = DefaultIjkPlayer()
//        val player = DefaultExoPlayer(this)
        playerView.setOrientationApplySystem(true)
        playerView.setPlayer(player)
        playerView.setController(controllerView)
//        playerView.setBackNeedFinish(true)
        controllerView.setCompletionViewEnable(true)
//        controllerView.setTopControllerVisibility(View.VISIBLE)
//        controllerView.setGestureEnable(true)
//        controllerView.setGestureSeekEnable(false)
        controllerView.setCoverPlayResource(R.drawable.ic_action_paused)
        controllerView.setStyleColor(R.color.teal_200)
        controllerView.setTitle("好莱坞往事")
        controllerView.setTopControllerEnable(false)
        playerView.setFullscreen(false, PlayerOrientation.LANDSCAPE)
//        playerView.setDataSource(localPath)
//        playerView.setDataSource(localPath2)
        playerView.setDataSource(localPath3)
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2022/02/24/mp4/220224085656529169.mp4")
//        playerView.setDataSource("https://vfx.mtime.cn/Video/2019/03/21/mp4/190321153853126488.mp4")
        playerView.prepare(true)
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
        getState.setOnClickListener {
            Toast.makeText(
                this,
                "${playerView.getPlayerState()}",
                Toast.LENGTH_SHORT
            ).show()
        }
        setSpeed.setOnClickListener {
            playerView.setSpeed(2.0F)
        }
        playerView.setOnPlayerListener {
            prepared {
            }
            started {
//                Toast.makeText(this@MainActivity, "播放", Toast.LENGTH_SHORT).show()
            }
            paused {
//                Toast.makeText(this@MainActivity, "暂停", Toast.LENGTH_SHORT).show()
            }
            screenChanged {
//                Toast.makeText(this@MainActivity, if (it) "全屏" else "小屏", Toast.LENGTH_SHORT).show()
//                if (it) {
//                    controllerView.setTopControllerVisibility(View.VISIBLE)
//                } else {
//                    controllerView.setTopControllerVisibility(View.INVISIBLE)
//                }
            }
            completion {
//                controllerView.showCover()
//                Toast.makeText(this@MainActivity, "播放完成", Toast.LENGTH_SHORT).show()
            }
            error {
//                Toast.makeText(this@MainActivity, "播放错误$it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBackPressed() {
        if (playerView.canBack()) {
            super.onBackPressed()
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

}