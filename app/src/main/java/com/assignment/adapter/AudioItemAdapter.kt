package com.assignment.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.assignment.ItemTouchHelperAdapter
import com.assignment.R
import com.assignment.model.AudioModel
import java.util.*


class AudioAdapter(
    private val context: Context,
    private val audioList: MutableList<AudioModel>,
    private val listener: OnItemSwipeListener
) : RecyclerView.Adapter<AudioAdapter.AudioItemViewHolder>() ,
    ItemTouchHelperAdapter {

    private var clicked = false
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AudioItemViewHolder {
        val view = LayoutInflater.from(context).inflate(
            R.layout.audio_list_item_layout,
            parent,
            false
        )
        return AudioItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioItemViewHolder, position: Int) {
        holder.bindView(audioList[position])
        holder.audioRelativeLayout.setOnClickListener {
            clicked = if (!clicked) {
                listener.playMusic(position)
                true
            } else {
                listener.stopMusic(position)
                false
            }
        }

    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    class AudioItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val audioTitle: TextView = itemView.findViewById(R.id.title)
        private val audioArtist: TextView = itemView.findViewById(R.id.artist)
        val audioRelativeLayout: RelativeLayout = itemView.findViewById(R.id.audio_ll)

        fun bindView(item: AudioModel) {
            audioArtist.text = item.audioArtist
            audioTitle.text = item.audioTitle
        }
    }

    interface OnItemSwipeListener {
        fun playMusic(position: Int)
        fun stopMusic(position: Int)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(audioList, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(audioList, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onItemDismiss(position: Int) {
    }

}