package com.example.uthcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.DecimalFormat;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<CartItem> cartItems;
    private OnItemActionListener listener;
    private static final String BASE_URL = "http://10.0.2.2:3000"; // Thêm host
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public interface OnItemActionListener {
        void onItemDelete(CartItem cartItem);
        void onQuantityChange(CartItem cartItem, int newQuantity);
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvPrice.setText(decimalFormat.format(item.getPrice()) + "đ");
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(item.isSelected());
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> item.setSelected(isChecked));

        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = item.getQuantity() + 1;
            item.setQuantity(newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            if (listener != null) listener.onQuantityChange(item, newQuantity);
        });

        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = Math.max(1, item.getQuantity() - 1);
            item.setQuantity(newQuantity);
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            if (listener != null) listener.onQuantityChange(item, newQuantity);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onItemDelete(item);
        });

        String fullThumbnailUrl = BASE_URL + item.getThumbnailUrl();
        Glide.with(context)
                .load(fullThumbnailUrl)
                .placeholder(R.drawable.placeholder)
                .into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSelect;
        ImageView ivThumb;
        TextView tvName, tvPrice, tvQuantity;
        ImageButton btnIncrease, btnDecrease, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSelect = itemView.findViewById(R.id.cb_select);
            ivThumb = itemView.findViewById(R.id.iv_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}