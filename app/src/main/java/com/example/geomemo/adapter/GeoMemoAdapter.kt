package com.example.geomemo.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.geomemo.databinding.ItemMemoBinding
import com.example.geomemo.model.GeoMemoModel

class GeoMemoAdapter(private val items: ArrayList<GeoMemoModel>) :
    RecyclerView.Adapter<GeoMemoAdapter.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    class ViewHolder(binding: ItemMemoBinding) : RecyclerView.ViewHolder(binding.root) {
        val memoItemTitle = binding.tvTitle
        val memoItemDescription = binding.tvDescription
        val memoItemImage = binding.civMemoImage
    }

    interface OnClickListener {
        fun onClick(position: Int, model: GeoMemoModel)
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.memoItemTitle.text = items[position].title
        holder.memoItemDescription.text = items[position].description
        holder.memoItemImage.setImageURI(Uri.parse(items[position].imagePath))

        holder.itemView.setOnClickListener {
            onClickListener!!.onClick(position, items[position])
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }


}