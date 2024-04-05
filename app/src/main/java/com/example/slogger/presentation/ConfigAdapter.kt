package com.example.slogger.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slogger.R
import org.w3c.dom.Text

class ConfigAdapter (val configList:MutableList<ConfigItem>)
    : RecyclerView.Adapter<ConfigAdapter.ViewHolder>(){

    var onItemClick: ((ConfigItem) -> Unit)? = null

    override fun getItemCount(): Int {
        return configList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item = configList[position]
        holder.textView.text = "${item.name}: ${item.value}"

        holder.textView.setOnClickListener {
            onItemClick?.invoke(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var textView: TextView;

        init {
            textView = itemView.findViewById(R.id.itemTextView)
        }
    }
}

data class ConfigItem(var tag: String, var name: String, var value: String) {
}