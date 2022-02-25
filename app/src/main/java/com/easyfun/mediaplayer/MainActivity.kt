package com.easyfun.mediaplayer

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.github.mminng.media.controller.DefaultController
import com.github.mminng.media.PlayerView
import com.github.mminng.media.player.DefaultMediaPlayer
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
    private val setSpeed: Button by lazy {
        findViewById(R.id.setSpeed)
    }
    private val playerContent: FrameLayout by lazy {
        findViewById(R.id.player_content)
    }
    val controllerView: DefaultController by lazy {
        DefaultController(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        val player = DefaultMediaPlayer()
//        val player = Ijk_Player()
        val player = Exo_Player(this)
        playerView.setPlayer(player)
        playerView.setController(controllerView)
//        controllerView.setCoverViewEnable(true)
//        controllerView.setTopControllerVisibility(View.VISIBLE)
        controllerView.setCoverPlayButtonResource(R.drawable.ic_action_paused)
        controllerView.setTitle("好莱坞往事")
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
//        playerView.setDataSource("http://ips.ifeng.com/video19.ifeng.com/video09/2014/06/16/1989823-102-086-0009.mp4")
        playerView.setDataSource("https://vfx.mtime.cn/Video/2022/01/14/mp4/220114181259659149.mp4")
        playerView.prepare()
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
//                Toast.makeText(this@MainActivity, "准备就绪", Toast.LENGTH_SHORT).show()
            }
            started {
//                Toast.makeText(this@MainActivity, "播放", Toast.LENGTH_SHORT).show()
            }
            paused {
//                Toast.makeText(this@MainActivity, "暂停", Toast.LENGTH_SHORT).show()
            }
            screenChanged {
//                Toast.makeText(this@MainActivity, if (it) "全屏" else "小屏", Toast.LENGTH_SHORT).show()
                if (it) {
                    controllerView.setTopControllerVisibility(View.VISIBLE)
                } else {
                    controllerView.setTopControllerVisibility(View.INVISIBLE)
                }
            }
            completion {
//                Toast.makeText(this@MainActivity, "播放完成", Toast.LENGTH_SHORT).show()
            }
            error {
//                Toast.makeText(this@MainActivity, "播放错误$it", Toast.LENGTH_SHORT).show()
            }
        }
//        playerContent.setOnKeyListener(object : View.OnKeyListener {
//            override fun onKey(view: View?, keyCode: Int, event: KeyEvent?): Boolean {
//                if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_DOWN) {
//                    Toast.makeText(this@MainActivity, "setOnKeyListener", Toast.LENGTH_SHORT).show()
//                    return true
//                }
//                return false
//            }
//        })
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