//package com.autogarage.presentation.ui.adapter
//
//import android.view.LayoutInflater
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.ListAdapter
//import androidx.recyclerview.widget.RecyclerView
//import android.view.ViewGroup
//import com.autogarage.domain.model.Customer
//
//// ✅ OPTIMIZATION: Use DiffUtil for efficient list updates
//class CustomerAdapter(
//    private val onItemClick: (Customer) -> Unit
//) : ListAdapter<Customer, CustomerAdapter.ViewHolder>(CustomerDiffCallback()) {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val binding = ItemCustomerBinding.inflate(
//            LayoutInflater.from(parent.context),
//            parent,
//            false
//        )
//        // ViewHolder creation
//        return ViewHolder(binding, onItemClick)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//
//    class ViewHolder(
//        private val binding: ItemCustomerBinding,
//        private val onItemClick: (Customer) -> Unit
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        fun bind(customer: Customer) {
//            binding.apply {
//                // Customer name
//                tvCustomerName.text = customer.name
//
//                // Phone number
//                tvCustomerPhone.text = customer.phone
//
//                // Email (optional)
//                if (customer.email != null) {
//                    tvCustomerEmail.text = customer.email
//                    tvCustomerEmail.visibility = android.view.View.VISIBLE
//                } else {
//                    tvCustomerEmail.visibility = android.view.View.GONE
//                }
//
//                // Total spent
//                tvTotalSpent.text = "₹${String.format("%.2f", customer.totalSpent)}"
//
//                // Loyalty points
//                if (customer.loyaltyPoints > 0) {
//                    tvLoyaltyPoints.text = "${customer.loyaltyPoints} pts"
//                    tvLoyaltyPoints.visibility = android.view.View.VISIBLE
//                } else {
//                    tvLoyaltyPoints.visibility = android.view.View.GONE
//                }
//
//                // Avatar initial
//                tvAvatar.text = customer.name.take(1).uppercase()
//
//                // Click listener
//                root.setOnClickListener {
//                    onItemClick(customer)
//                }
//            }
//        }
//    }
//}
//
//// ✅ OPTIMIZATION: Efficient diff calculation
//class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
//    override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean {
//        return oldItem.id == newItem.id
//    }
//
//    override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean {
//        return oldItem == newItem
//    }
//}
