package com.example.uthcare;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private final List<Integer> bannerList;

    public BannerAdapter(List<Integer> bannerList) {
        this.bannerList = bannerList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView imageView = new ImageView(parent.getContext());

        // Chiều rộng = MATCH_PARENT, chiều cao = MATCH_PARENT để fit với ViewPager2 item
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Giữ nguyên tỉ lệ ảnh, toàn bộ ảnh hiển thị (không crop chữ)
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        return new BannerViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        if (bannerList == null || bannerList.isEmpty()) return;
        int realPos = position % bannerList.size();
        holder.imageView.setImageResource(bannerList.get(realPos));
    }

    @Override
    public int getItemCount() {
        if (bannerList == null || bannerList.isEmpty()) return 0;
        return Integer.MAX_VALUE; // để chạy vô hạn
    }

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
        }
    }
}
