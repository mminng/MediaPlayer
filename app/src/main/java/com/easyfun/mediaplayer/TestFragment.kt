package com.easyfun.mediaplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mminng.media.controller.DefaultController
import com.github.mminng.media.player.DefaultMediaPlayer
import com.github.mminng.media.PlayerView
import com.github.mminng.media.renderer.RenderMode

/**
 * Created by zh on 2022/2/15.
 */
class TestFragment : Fragment() {

    private lateinit var rootView: View

    private val playerView: PlayerView by lazy {
        rootView.findViewById(R.id.player_view)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_test, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val controllerView = DefaultController(requireContext())
        val player = DefaultMediaPlayer()
        playerView.setPlayer(player)
        playerView.setRenderMode(RenderMode.DEFAULT)
        playerView.setController(controllerView)
        playerView.setDataSource("https://vfx.mtime.cn/Video/2021/12/23/mp4/211223160113712148.mp4")
        playerView.prepare()
        playerView.setOnPlayerListener {
            screenChanged {
            }
        }
        controllerView.setTitle("https://vfx.mtime.cn/Video/2021/12/23/mp4/211223160113712148.mp4-https://vfx.mtime.cn/Video/2021/12/23/mp4/211223160113712148.mp4")
    }

    override fun onResume() {
        super.onResume()
        playerView.start()
    }

    override fun onStop() {
        super.onStop()
        playerView.pause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerView.release()
    }

}