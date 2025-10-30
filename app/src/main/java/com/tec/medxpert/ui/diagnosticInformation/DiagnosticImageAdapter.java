package com.tec.medxpert.ui.diagnosticInformation;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tec.medxpert.R;

import java.util.List;

public class DiagnosticImageAdapter extends RecyclerView.Adapter<DiagnosticImageAdapter.ImageViewHolder> {

    private final List<String> imageUrls;
    private final Context context;

    public DiagnosticImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_diagnostic_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String url = imageUrls.get(position);
        Glide.with(context)
                .load(url)
                .placeholder(new ColorDrawable(context.getResources().getColor(R.color.blue)))
                .into(holder.imgDiagnostic);

        holder.imgDiagnostic.setOnClickListener(v -> {
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.dialog_image_fullscreen);

            dialog.getWindow().setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );

            ImageView imgFullscreen = dialog.findViewById(R.id.imgFullscreen);
            ImageView btnClose = dialog.findViewById(R.id.btnClose);

            if (imgFullscreen != null) {
                Glide.with(context)
                        .load(url)
                        .placeholder(new ColorDrawable(context.getResources().getColor(R.color.blue)))
                        .error(new ColorDrawable(context.getResources().getColor(R.color.red)))
                        .into(imgFullscreen);
            } else {
                Log.e("DiagnosticImageAdapter", "ImageView not found in dialog layout.");
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(view -> dialog.dismiss());
            } else {
                Log.e("DiagnosticImageAdapter", "Close button not found in dialog layout.");
            }

            dialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imgDiagnostic;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgDiagnostic = itemView.findViewById(R.id.imgDiagnostic);
        }
    }
}
