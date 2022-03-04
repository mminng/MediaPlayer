package com.github.mminng.media.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mminng.media.R

/**
 * Created by zh on 2022/2/24.
 */
@SuppressLint("NotifyDataSetChanged")
class MenuView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val menuListview = RecyclerView(context)
    private val menuAdapter = MenuAdapter<T>()
    private var _menuSelectedListener: ((value: T) -> Unit)? = null

    init {
        layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.MATCH_PARENT,
            Gravity.END
        )
        setBackgroundColor(Color.parseColor("#CC000000"))
        visibility = GONE
        addView(
            menuListview,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL
            )
        )
        menuListview.overScrollMode = OVER_SCROLL_NEVER
        menuListview.layoutManager = LinearLayoutManager(context)
        menuListview.adapter = menuAdapter
        menuAdapter.setOnItemClickListener { data, position ->
            menuAdapter.getData().forEachIndexed { index, menu ->
                menu.selected = index == position
            }
            menuAdapter.notifyDataSetChanged()
            _menuSelectedListener?.invoke(data.value)
            hide()
        }
    }

    fun setOnMenuSelectedListener(listener: ((value: T) -> Unit)) {
        if (_menuSelectedListener === listener) return
        _menuSelectedListener = listener
    }

    fun setSelectedColor(@ColorInt color: Int) {
        menuAdapter.setSelectedColor(color)
    }

    fun setMenuData(data: List<Menu<T>>) {
        menuAdapter.setData(data)
    }

    fun getMenuData(): List<Menu<T>> {
        return menuAdapter.getData()
    }

    fun show() {
        clearAnimation()
        visibility = VISIBLE
        animation = AnimationUtils.loadAnimation(context, R.anim.right_slide_in)
    }

    fun hide() {
        clearAnimation()
        visibility = GONE
        animation = AnimationUtils.loadAnimation(context, R.anim.right_slide_out)
    }

    fun isShowing(): Boolean = visibility == VISIBLE

}

internal class MenuAdapter<T> : RecyclerView.Adapter<MenuViewHolder>() {

    private val unselectedColor: Int = Color.parseColor("#FFFFFFFF")
    private var _selectedColor: Int = Color.parseColor("#FF03DAC5")
    private var _menuData: List<Menu<T>> = listOf()
    private var _itemClick: ((data: Menu<T>, position: Int) -> Unit)? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val viewHolder = MenuViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_menu_layout, parent, false)
        )
        viewHolder.itemView.setOnClickListener {
            _itemClick?.let {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return@setOnClickListener
                }
                val item: Menu<T> = viewHolder.itemView.getTag(R.id.item_menu_data) as Menu<T>
                it.invoke(
                    item,
                    position
                )
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = _menuData[position]
        holder.itemView.setTag(R.id.item_menu_data, item)
        holder.value.text = item.text
        if (item.selected) {
            holder.value.setTextColor(_selectedColor)
        } else {
            holder.value.setTextColor(unselectedColor)
        }
    }

    override fun getItemCount() = _menuData.size

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedColor(@ColorInt color: Int) {
        _selectedColor = color
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: List<Menu<T>>) {
        this._menuData = data
        notifyDataSetChanged()
    }

    fun getData(): List<Menu<T>> = this._menuData

    fun setOnItemClickListener(listener: (data: Menu<T>, position: Int) -> Unit) {
        this._itemClick = listener
    }
}

internal class MenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val value: TextView = itemView.findViewById(R.id.item_menu_value)
}

data class Menu<T>(var selected: Boolean, val text: String, val value: T)