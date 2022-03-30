package com.turkcell.turkcellodev

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.turkcell.turkcellodev.databinding.RecyclerItemBinding

class ImageAdapter(
    private val imageClick: ImageClick,
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Image>() {
        override fun areItemsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Image, newItem: Image): Boolean {
            return oldItem == newItem
        }
    }

    private val recyclerDiffUtil = AsyncListDiffer(this, diffUtil)

    var imageList: List<Image>
        get() = recyclerDiffUtil.currentList
        set(value) = recyclerDiffUtil.submitList(value)

    class ImageViewHolder(val binding: RecyclerItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding =
            RecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val currentData = imageList[position]

        holder.binding.imageView.setImageURI(currentData.uri)
        holder.binding.imageView.setOnClickListener {
            imageClick.onImageClick(currentData)
        }
    }

    override fun getItemCount(): Int = imageList.size
}