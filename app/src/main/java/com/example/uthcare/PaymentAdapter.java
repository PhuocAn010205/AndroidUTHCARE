package com.example.uthcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DecimalFormat;
import java.util.List;
import android.util.Log;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {
    private Context context;
    private List<CartItem> paymentItems;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private static final String BASE_URL = "http://10.0.2.2:3000"; // Hoặc 192.168.2.11 nếu cần

    public PaymentAdapter(Context context, List<CartItem> paymentItems) {
        this.context = context;
        this.paymentItems = paymentItems;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        CartItem item = paymentItems.get(position);
        holder.tvName.setText(item.getProductName());
        holder.tvPrice.setText(decimalFormat.format(item.getPrice()) + "đ");
        holder.tvQuantity.setText("Số lượng: " + item.getQuantity());

        String thumbnailUrl = item.getThumbnailUrl();
        String fullThumbnailUrl = thumbnailUrl;
        if (thumbnailUrl != null && !thumbnailUrl.startsWith("http")) {
            fullThumbnailUrl = BASE_URL + thumbnailUrl; // Chỉ thêm BASE_URL nếu không phải URL đầy đủ
        }
        Log.d("PaymentAdapter", "Loading thumbnail with URL: " + fullThumbnailUrl);

        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder);
        Glide.with(context)
                .load(fullThumbnailUrl)
                .apply(options)
                .into(holder.ivThumb);
    }

    @Override
    public int getItemCount() {
        return paymentItems.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb;
        TextView tvName, tvPrice, tvQuantity;

        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumb = itemView.findViewById(R.id.iv_payment_thumb);
            tvName = itemView.findViewById(R.id.tv_payment_name);
            tvPrice = itemView.findViewById(R.id.tv_payment_price);
            tvQuantity = itemView.findViewById(R.id.tv_payment_quantity);
        }
    }
}