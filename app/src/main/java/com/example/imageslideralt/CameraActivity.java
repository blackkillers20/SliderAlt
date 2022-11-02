package com.example.imageslideralt;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import com.example.imageslideralt.Resouces.CameraAdapter;
import com.example.imageslideralt.Resouces.ImageEntity;
import com.example.imageslideralt.Ultis.ImageUtils;
import com.example.imageslideralt.Ultis.PermissionUtils;
import com.example.imageslideralt.Ultis.ViewUtils;
import com.example.imageslideralt.databinding.ActivityCameraBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private CameraAdapter cameraAdapter;
    private File captureImageFile;
    private LoaderManager loaderManager;
    private ActivityCameraBinding binding;
    private List<File> fileList = new ArrayList<File>();
    private CameraAdapter CameraAdapter;
    private RecyclerView recyclerView;
    FirebaseFirestore firestore;

    private final LoaderManager.LoaderCallbacks<List<File>> loaderCallbacks = new LoaderManager.LoaderCallbacks<List<File>>() {
        @NonNull
        @Override
        public Loader<List<File>> onCreateLoader(int id, @Nullable Bundle args) {
            return new AsyncTaskLoader<List<File>>(CameraActivity.this) {
                @Override
                public void onStartLoading() {
                    forceLoad();
                }

                @Nullable
                @Override
                public List<File> loadInBackground() {
                    return ImageUtils.from(CameraActivity.this).files().getFiles();
                }
            };
        }

        @Override
        public void onLoadFinished(@NonNull Loader<List<File>> loader, List<File> data) {
            fileList = data;
            cameraAdapter = new CameraAdapter(fileList);
            LoadRecycleView();
            cameraAdapter.notifyItemChanged(fileList.size());

        }

        @Override
        public void onLoaderReset(@NonNull Loader<List<File>> loader) {
            cameraAdapter.setListImages(null);
        }
    };


    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    CollectionReference collectionReference = firestore.collection("Slider");
                    // get captured image
//                    assert result.getData() != null;
//                    imageDatabase.getImageDAO().insert(new ImageEntity(capturedImageFile.getName(), capturedImageFile.toURI().getPath()));
                    ImageEntity imageEntity = new ImageEntity(captureImageFile.toURI().getPath());
                    collectionReference.add(imageEntity);
                    ViewUtils.showSnackbar(CameraActivity.this, "Successful!");

                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        loaderManager = LoaderManager.getInstance(CameraActivity.this);
        loaderManager.initLoader(101, null, loaderCallbacks);
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        firestore = FirebaseFirestore.getInstance();

    }
    @Override
    public void onResume() {

        super.onResume();
        loaderManager.restartLoader(101, null, loaderCallbacks);
    }

    private void LoadRecycleView()
    {
        recyclerView = findViewById(R.id.recyclerImages);
        cameraAdapter.setListener((view, position)->{
            Intent intent = new Intent(this, ViewActivity.class);
            intent.putExtra("ImageFile", fileList.get(position));
            startActivity(intent);
        });
        recyclerView.setAdapter(cameraAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, RecyclerView.VERTICAL));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.camera, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        PermissionUtils.checkPermission(this, "android.permission.CAMERA", new PermissionUtils.PermissionAskListener(){

            @Override
            public void onPermissionAsk() {
                PermissionUtils.requestPermission(CameraActivity.this, "android.permission.CAMERA", 100 );
            }

            @Override
            public void onPermissionPreviouslyDenied() {
                PermissionUtils.requestPermission(CameraActivity.this, "android.permission.CAMERA", 100 );
            }

            @Override
            public void onPermissionDisabled() {
                ViewUtils.showToast(CameraActivity.this, "PERMISSION DISABLED");
            }

            @Override
            public void onPermissionGranted() {
                captureImageFile = new File(getFilesDir(), ImageUtils.DEFAULT_IMAGE_NAME);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        FileProvider.getUriForFile(CameraActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                captureImageFile));
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cameraLauncher.launch(intent);
            }
        });

        return super.onOptionsItemSelected(item);
    }

}