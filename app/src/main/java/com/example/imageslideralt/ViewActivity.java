package com.example.imageslideralt;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.imageslideralt.R;
import com.example.imageslideralt.Ultis.ImageUtils;
import com.example.imageslideralt.databinding.ActivityMainBinding;
import com.example.imageslideralt.databinding.ActivityViewBinding;

import java.io.File;

public class ViewActivity extends AppCompatActivity {

    private ActivityViewBinding binding;
    ImageView imageView;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        imageView = findViewById(R.id.imgSlider);


        super.onCreate(savedInstanceState);
        binding = ActivityViewBinding.inflate(this.getLayoutInflater());
        setContentView(binding.getRoot());
        imageFile = (File) getIntent().getSerializableExtra("ImageFile");
        binding.imgSlider.setImageURI(Uri.fromFile(imageFile));

        ImageUtils.ImageFiles imageFiles =  ImageUtils.from(this).files();
        binding.slideNext.setOnClickListener(v -> {
            imageFiles.Next(imageFile).onCursor((cursor)->{
                imageFile = cursor;
            }).apply(binding.imgSlider);
        });

        binding.slidePre.setOnClickListener(v -> {
            imageFiles.Previous(imageFile).onCursor((cursor)->{
                imageFile = cursor;
            }).apply(binding.imgSlider);
        });
    }
}