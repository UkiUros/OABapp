package rs.fourexample.redred.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import rs.fourexample.redred.R
import rs.fourexample.redred.data.BloodRequest
import rs.fourexample.redred.databinding.ItemBloodRequestBinding
import rs.fourexample.redred.helper.calculateTimeAgo

class RequestAdapter :
    ListAdapter<BloodRequest, RequestAdapter.RequestViewHolder>(RequestDiffCallback()) {

    interface IRequestCallback {
        fun onItemClick(bloodRequest: BloodRequest)
    }

    var callback: IRequestCallback? = null

    inner class RequestViewHolder(val binding: ItemBloodRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder =
        RequestViewHolder(
            ItemBloodRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = getItem(position)

        if (request.userPhoto?.isNotEmpty() == true) {
            Picasso.get()
                .load(request.userPhoto)
                .into(holder.binding.imageViewProfile)
        }

        holder.itemView.setOnClickListener {
            callback?.onItemClick(getItem(holder.adapterPosition))
        }

        holder.binding.textViewName.text = request.userName
        holder.binding.textViewBloodType.text = request.bloodType?.typeValue
        holder.binding.textViewDescription.text = request.description
        holder.binding.textViewTimestamp.text = calculateTimeAgo(request.timestamp)
        holder.binding.textViewSubtitle.text =
            holder.itemView.context.getString(
                R.string.BloodRequest_Item_Location,
                request.bloodType?.typeValue ?: "",
                request.address
            )

        // todo add supported blood types since some blood types can be donated to others
    }

}

class RequestDiffCallback : DiffUtil.ItemCallback<BloodRequest>() {
    override fun areItemsTheSame(oldItem: BloodRequest, newItem: BloodRequest): Boolean {
        return oldItem.requestId == newItem.requestId
    }

    override fun areContentsTheSame(oldItem: BloodRequest, newItem: BloodRequest): Boolean {
        return oldItem == newItem
    }
}
