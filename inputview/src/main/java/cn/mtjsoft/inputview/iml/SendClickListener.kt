package cn.mtjsoft.inputview.iml

import android.view.View

/**
 * 发送接口
 *
 * @param view 点击的发送View
 * @param content 发送的内容（在回调后，输入框会清空）
 */
interface SendClickListener {
    fun onSendClick(view: View, content: String)
}