package com.pfe.mamacareplus.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class PlaceholderAdapter : RecyclerView.Adapter<PlaceholderAdapter.PlaceholderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceholderViewHolder {
        val view = View(parent.context)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            1
        )
        return PlaceholderViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceholderViewHolder, position: Int) = Unit

    override fun getItemCount(): Int = 0

    class PlaceholderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
