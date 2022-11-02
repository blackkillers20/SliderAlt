package com.example.imageslideralt.Resouces;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageslideralt.R;
import com.example.imageslideralt.Ultis.ImageUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public class CameraAdapter extends RecyclerView.Adapter<CameraAdapter.ViewHolder> {
    private List<File> ListImages;

    public interface OnItemClickListener
    {
        void onClick(View view, int position);
    }

    private OnItemClickListener listener;

    public void setListener(OnItemClickListener listener)
    {
        this.listener = listener;
    }

    public CameraAdapter(List<File> ListImages)
    {
        this.ListImages = ListImages;
    }

    public void setListImages(List<File> list) {this.ListImages = list;}


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.list_images_row, null);
        return new ViewHolder(view, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            holder.Bind(ListImages.get(position));
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return ListImages.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        public ImageView imageView;
        public TextView imageName;
        private Context context;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            this.context = context;
            imageView = itemView.findViewById(R.id.imageView);
            imageName = itemView.findViewById(R.id.imgName);
            itemView.setOnClickListener(this);
        }


        public void Bind(File image) throws MalformedURLException, URISyntaxException {
            ImageUtils.from(context).load(image.getAbsolutePath()).apply(imageView);
            imageName.setText(image.getName());
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }
}
