package com.jarvis.assistant.ui.chat

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jarvis.assistant.R
import com.jarvis.assistant.data.model.ChatTurn

class JarvisChatAdapter : RecyclerView.Adapter<JarvisChatAdapter.ChatViewHolder>() {

    private val turns = mutableListOf<ChatTurn>()

    fun submitList(newTurns: List<ChatTurn>) {
        turns.clear()
        turns.addAll(newTurns)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val ctx = parent.context
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(24, 16, 24, 16)
        }

        val userTv = TextView(ctx).apply {
            textSize = 14f
            setTextColor(Color.parseColor("#1E293B"))
        }

        val jarvisTv = TextView(ctx).apply {
            textSize = 15f
            setTextColor(Color.parseColor("#0F172A"))
            typeface = Typeface.DEFAULT_BOLD
        }

        val citationsContainer = LinearLayout(ctx).apply {
            id = R.id.layoutCitationsContainer
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        root.addView(userTv)
        root.addView(jarvisTv)
        root.addView(citationsContainer)

        return ChatViewHolder(root, userTv, jarvisTv, citationsContainer)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(turns[position], position)
    }

    override fun getItemCount(): Int = turns.size

    class ChatViewHolder(
        itemView: View,
        private val userTv: TextView,
        private val jarvisTv: TextView,
        private val citationsContainer: LinearLayout
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(turn: ChatTurn, position: Int) {
            userTv.text = turn.userTranscript
            jarvisTv.text = turn.jarvisResponse

            citationsContainer.removeAllViews()
            val validCitations = turn.citations.filter { it.url.startsWith("https://", ignoreCase = true) }
            if (validCitations.isNotEmpty()) {
                citationsContainer.visibility = View.VISIBLE
                val headerTv = TextView(itemView.context).apply {
                    text = "Verified Sources:"
                    textSize = 12f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#2563EB"))
                }
                citationsContainer.addView(headerTv)

                for (citation in validCitations) {
                    val chip = TextView(itemView.context).apply {
                        text = "🔗 ${citation.title} — ${citation.url}"
                        textSize = 12f
                        setTextColor(Color.parseColor("#0EA5E9"))
                        isClickable = true
                        isFocusable = true
                        setOnClickListener {
                            val browseIntent = Intent(Intent.ACTION_VIEW, Uri.parse(citation.url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            itemView.context.startActivity(browseIntent)
                        }
                    }
                    citationsContainer.addView(chip)
                }
            } else {
                citationsContainer.visibility = View.GONE
            }
        }
    }
}
