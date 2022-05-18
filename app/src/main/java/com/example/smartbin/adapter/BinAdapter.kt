package com.example.smartbin.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartbin.databinding.ItemBinBinding
import com.example.smartbin.model.remote.Bin
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin

class BinAdapter(private val binAdapterListener: BinAdapterListener): ListAdapter<Bin, BinAdapter.BinVH>(DiffUtilItemCallback) {

    private lateinit var binding: ItemBinBinding
    private var lat: Double = 0.0
    private var lng: Double = 0.0

    object DiffUtilItemCallback : DiffUtil.ItemCallback<Bin>() {
        override fun areItemsTheSame(oldItem: Bin, newItem: Bin): Boolean {
            return oldItem.id == newItem.id

        }

        override fun areContentsTheSame(oldItem: Bin, newItem: Bin): Boolean {
            return oldItem.updatedAt == newItem.updatedAt
        }

    }

    fun setLocationData(lat: Double, lng: Double) {
        this.lat = lat
        this.lng = lng
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BinVH {
        binding = ItemBinBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BinVH(binding)
    }

    override fun onBindViewHolder(holder: BinVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BinVH(private val binding: ItemBinBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(bin: Bin) {
            binding.distance.text = "${bin.relativeDistance} km"
            binding.tvLocation.text = "${bin.name}, ${bin.location?.address}, ${bin.location?.city}, ${bin.location?.state}, ${bin.location?.country}"
            val lat2: Double = bin.location?.geoLocation?.coordinates?.get(1)!!
            val lng2: Double = bin.location?.geoLocation?.coordinates?.get(0)!!
            val distance = calculateDistance(lat, lng, lat2, lng2)
            val distanceKm = (distance/1000)
            if(distanceKm * .1f == 0.0){
                binding.distance.text = "${distance.roundToLong()} m"
            }
            else binding.distance.text = "${distance*.1f} km"
            binding.llCoin.setOnClickListener {
                binAdapterListener.onItemClick(lat2, lng2)
            }
        }

        private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val theta = lon1 - lon2
            var dist = (sin(deg2rad(lat1))
                    * sin(deg2rad(lat2))
                    + (cos(deg2rad(lat1))
                    * cos(deg2rad(lat2))
                    * cos(deg2rad(theta))))
            dist = acos(dist)
            dist = rad2deg(dist)
            dist *= 60 * 1.1515
            return dist
        }

        private fun deg2rad(deg: Double): Double {
            return deg * Math.PI / 180.0
        }

        private fun rad2deg(rad: Double): Double {
            return rad * 180.0 / Math.PI
        }
    }
}
interface BinAdapterListener {
    fun onItemClick(lat: Double, lng: Double)
}