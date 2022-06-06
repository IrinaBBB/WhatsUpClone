package ru.irinavb.whatsupclone.activities.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import ru.irinavb.whatsupclone.R
import ru.irinavb.whatsupclone.activities.listeners.ChatClickListener
import ru.irinavb.whatsupclone.util.populateImage

class ChatsAdapter(private val chats: ArrayList<String>) :
    RecyclerView.Adapter<ChatsAdapter.ChatsViewHolder>() {

    private var clickListener: ChatClickListener? = null

    fun setOnClickListener(listener: ChatClickListener) {
        clickListener = listener
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder {
        return ChatsViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
        holder.bind(chats[position], clickListener)
    }

    override fun getItemCount(): Int = chats.size

    fun updateChat(updatedChat: ArrayList<String>) {
        chats.clear()
        chats.addAll(updatedChat)
        notifyDataSetChanged()
    }

    class ChatsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var chatImageView = view.findViewById<ImageView>(R.id.chat_image_view)
        private var chatName = view.findViewById<TextView>(R.id.chat_text_view)
        private var chatCardView = view.findViewById<CardView>(R.id.chat_card_view)

        fun bind(chatId: String, listener: ChatClickListener?) {
            populateImage(chatImageView.context, "", chatImageView, R.drawable.default_user)
            chatName.text = chatId
        }
    }
}