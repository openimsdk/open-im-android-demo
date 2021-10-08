package cn.mtjsoft.inputview.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.mtjsoft.inputview.R
import cn.mtjsoft.inputview.entity.EmojiEntry
import cn.mtjsoft.inputview.iml.AdapterItemClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter

class EmojiAdapter(
    private val context: Context,
    private val data: List<EmojiEntry>,
    private val clickListener: AdapterItemClickListener
) :
    RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmojiAdapter.EmojiViewHolder = EmojiViewHolder(
        LayoutInflater.from(context)
            .inflate(R.layout.item_emoji, parent, false)
    )

    fun getData(): List<EmojiEntry> = data

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(
        holder: EmojiAdapter.EmojiViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        Glide.with(context).load(Uri.parse("file:///android_asset/" + data[position].src))
            .transform(FitCenter()).into(holder.imageView)
        holder.itemLayout.setOnClickListener {
            clickListener.onItemClick(it, position)
        }
    }

    inner class EmojiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemLayout: RelativeLayout = view.findViewById(R.id.itemLayout)
        var imageView: ImageView = view.findViewById(R.id.iv_item)
    }
}