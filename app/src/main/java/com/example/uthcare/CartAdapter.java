package com.example.uthcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private final Context context;
    private final List<CartItem> cartItems;
    private OnItemActionListener actionListener;

    public interface OnItemActionListener {
        void onItemDelete(CartItem cartItem);
        void onQuantityChange(CartItem cartItem, int newQuantity);
    }

    public CartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.tvName.setText(cartItem.getProductName());
        holder.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        holder.tvPrice.setText(formatter.format(cartItem.getPrice()) + " đ");

        Glide.with(context)
                .load(cartItem.getThumbnailUrl())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.image_error)
                .into(holder.imgThumb);

        // Xử lý nút Giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() - 1;
            if (newQuantity > 0) {
                cartItem.setQuantity(newQuantity); // Cập nhật trong CartItem
                holder.tvQuantity.setText(String.valueOf(newQuantity));
                if (actionListener != null) {
                    actionListener.onQuantityChange(cartItem, newQuantity);
                }
            }
        });

        // Xử lý nút Tăng số lượng
        holder.btnIncrease.setOnClickListener(v -> {
            int newQuantity = cartItem.getQuantity() + 1;
            cartItem.setQuantity(newQuantity); // Cập nhật trong CartItem
            holder.tvQuantity.setText(String.valueOf(newQuantity));
            if (actionListener != null) {
                actionListener.onQuantityChange(cartItem, newQuantity);
            }
        });

        // Xử lý nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onItemDelete(cartItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvPrice, tvQuantity;
        Button btnDecrease, btnIncrease;
        ImageButton btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.iv_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}

