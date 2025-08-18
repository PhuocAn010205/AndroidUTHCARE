package com.example.uthcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orders;
    private OnOrderActionListener listener;

    public OrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orders = orders;
    }

    public void setOnOrderActionListener(OnOrderActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderId.setText("Mã đơn hàng: " + order.getOrderId());
        holder.tvTotalAmount.setText("Tổng tiền: " + order.getTotalAmount());
        holder.tvStatus.setText("Trạng thái: " + order.getStatus());
        holder.tvCreatedAt.setText("Ngày đặt: " + order.getCreatedAt());

        holder.btnEditAddress.setOnClickListener(v -> listener.onEditAddress(order));
        holder.btnCancel.setOnClickListener(v -> listener.onCancelOrder(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvTotalAmount, tvStatus, tvCreatedAt;
        Button btnEditAddress, btnCancel;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvTotalAmount = itemView.findViewById(R.id.tv_total_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            btnEditAddress = itemView.findViewById(R.id.btn_edit_address);
            btnCancel = itemView.findViewById(R.id.btn_cancel);
        }
    }

    public interface OnOrderActionListener {
        void onEditAddress(Order order);
        void onCancelOrder(Order order);
    }
}