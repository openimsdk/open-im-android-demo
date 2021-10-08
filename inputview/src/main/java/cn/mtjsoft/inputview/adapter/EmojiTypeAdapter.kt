package cn.mtjsoft.inputview.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.inputview.R
import cn.mtjsoft.inputview.iml.AdapterItemClickListener

class EmojiTypeAdapter(
    private val context: Context,
    private val data: List<String>,
    private val clickListener: AdapterItemClickListener
) :
    RecyclerView.Adapter<EmojiTypeAdapter.EmojiTypeViewHolder>() {

    private var clickPosition: Int = 0

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmojiTypeAdapter.EmojiTypeViewHolder = EmojiTypeViewHolder(
        LayoutInflater.from(context)
            .inflate(R.layout.item_type_textview, parent, false)
    )

    @SuppressLint("NotifyDataSetChanged")
    fun setClickPosition(position: Int) {
        clickPosition = position
        notifyDataSetChanged()
    }

    fun getData(): List<String> = data

    override fun onBindViewHolder(
        holder: EmojiTypeAdapter.EmojiTypeViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        holder.textView.text = data[position]
        holder.textView.setBackgroundResource(if (clickPosition == position) R.color.container_divider1 else R.color.transparent)
        holder.textView.setOnClickListener {
            setClickPosition(position)
            clickListener.onItemClick(it, position)
        }
    }

    inner class EmojiTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textView: TextView = view as TextView
    }
}