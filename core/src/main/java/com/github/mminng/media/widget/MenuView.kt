package com.github.mminng.media.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.ColorInt
import com.github.mminng.media.R

/**
 * Created by zh on 2022/2/24.
 */
class MenuView<T> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val menuListview = ListView(context)
    private val menuAdapter = MenuAdapter<T>()
    private var _menuSelectedListener: ((text: String, value: T) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
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
                resources.getDimensionPixelSize(R.dimen.menu_width),
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL
            )
        )
        menuListview.selector = ColorDrawable()
        menuListview.divider = null
        menuListview.overScrollMode = OVER_SCROLL_NEVER
        menuListview.adapter = menuAdapter
        menuListview.setOnItemClickListener { _, _, position, _ ->
            menuAdapter.getData().forEachIndexed { index, menu ->
                menu.selected = index == position
            }
            menuAdapter.notifyDataSetChanged()
            _menuSelectedListener?.invoke(
                menuAdapter.getData()[position].text,
                menuAdapter.getData()[position].value
            )
            hide()
        }
    }

    fun setOnMenuSelectedListener(listener: ((text: String, value: T) -> Unit)) {
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

internal class MenuAdapter<T> : BaseAdapter() {

    private val unselectedColor: Int = Color.parseColor("#FFFFFFFF")
    private var _selectedColor: Int = Color.parseColor("#FF03DAC5")
    private var _menuData: List<Menu<T>> = listOf()

    override fun getCount(): Int = _menuData.size

    override fun getItem(position: Int): Any = _menuData[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @Suppress("UNCHECKED_CAST")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item: Menu<T> = getItem(position) as Menu<T>
        val itemView: View
        val viewHolder: MenuViewHolder
        if (convertView == null) {
            itemView = LayoutInflater.from(parent?.context)
                .inflate(R.layout.item_menu_layout, parent, false)
            viewHolder = MenuViewHolder(itemView)
            itemView.tag = viewHolder
        } else {
            itemView = convertView
            viewHolder = itemView.tag as MenuViewHolder
        }
        viewHolder.content.text = item.text
        if (item.selected) {
            viewHolder.content.setTextColor(_selectedColor)
        } else {
            viewHolder.content.setTextColor(unselectedColor)
        }
        return itemView
    }

    fun setSelectedColor(@ColorInt color: Int) {
        _selectedColor = color
        notifyDataSetChanged()
    }

    fun setData(data: List<Menu<T>>) {
        this._menuData = data
        notifyDataSetChanged()
    }

    fun getData(): List<Menu<T>> = this._menuData
}

internal class MenuViewHolder(itemView: View) {
    val content: TextView = itemView.findViewById(R.id.item_menu_content)
}

data class Menu<T>(var selected: Boolean, val text: String, val value: T)