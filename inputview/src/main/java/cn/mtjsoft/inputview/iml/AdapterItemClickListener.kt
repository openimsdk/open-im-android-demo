package cn.mtjsoft.inputview.iml

import android.view.View

interface AdapterItemClickListener {
    fun onItemClick(view: View, position: Int)
}