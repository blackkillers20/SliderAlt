package com.example.imageslideralt.Resouces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.imageslideralt.R;
import com.google.android.material.slider.Slider;
import com.smarteist.autoimageslider.SliderViewAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends SliderViewAdapter<ImageViewHolder>
{

    private Context context;
    private List<ImageEntity> mSliderItems;

    public ImageAdapter(Context context, List<ImageEntity> mSliderItems) {
        this.context = context;
        this.mSliderItems = mSliderItems;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.slide_item, null);
        return new ImageViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {
        final ImageEntity data = mSliderItems.get(position);
        Glide.with(viewHolder.itemView).load(data.getImageUrl()).into(viewHolder.imageView);
//        Picasso.get().load(data.getImageUrl()).into(viewHolder.imageView);
    }

    @Override
    public int getCount() {
        return mSliderItems.size();
    }
}

class ImageViewHolder extends SliderViewAdapter.ViewHolder
{
    View itemView;
    ImageView imageView;

    public ImageViewHolder(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.idSlideimage);
        this.itemView = itemView;
    }
}