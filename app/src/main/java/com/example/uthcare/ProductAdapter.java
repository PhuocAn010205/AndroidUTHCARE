package com.example.uthcare;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;

    // 👉 Đổi base URL tại đây khi chạy trên device thật (192.168.x.x) hoặc emulator (10.0.2.2)
    private static final String BASE_URL = "http://192.168.1.4:3000"; // Nếu dùng emulator Android

    public ProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void updateList(List<Product> newList) {
        productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getProductName());

        // Format tiền theo locale Việt Nam
        NumberFormat formatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        String formattedPrice = formatter.format(product.getPrice()) + " đ";
        holder.tvPrice.setText(formattedPrice);

        // Xử lý ảnh
        String imageUrl = product.getThumbnailUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Nếu chỉ là đường dẫn (bắt đầu bằng /), thì nối với BASE_URL
            if (!imageUrl.startsWith("http")) {
                imageUrl = BASE_URL + imageUrl;
            }

            Log.d("ImageDebug", "Loading image: " + imageUrl);

            Glide.with(context)
                    .load(imageUrl)
//                    .placeholder(R.drawable.placeholder)
//                    .error(R.drawable.image_error)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgThumb);
        } else {
            Log.w("ImageDebug", "Image URL is null or empty");
            holder.imgThumb.setImageResource(R.drawable.image_error);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;
        TextView tvName, tvPrice;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.iv_thumb);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }
    }
}
