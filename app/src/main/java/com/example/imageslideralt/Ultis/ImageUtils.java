package com.example.imageslideralt.Ultis;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ImageUtils {
    public static String DEFAULT_IMAGE_NAME = "JPEG_" + System.currentTimeMillis() + ".jpg";

    public interface ImageCallback {
        void onImageLoaded(Bitmap bitmap);
    }

    private Context context;
    private String uri;

    private ImageUtils(Context context) {
        this.context = context;
    }

    /**
     * Initialize URI path. Must be using before using other methods beside {@link ImageUtils#from(Context)}.
     *
     * @param uri
     */
    public ImageUtils load(String uri) {
        this.uri = uri;
        return this;
    }

    public ImageUtils onFile(Consumer<File> consumer)
    {
        File file = new File(this.uri);
        consumer.accept(file);
        return this;
    }

    public ImageUtils onDetails(Consumer<String> consumer)
    {
        consumer.accept(this.uri);
        return this;
    }

    /**
     * Initialize context. Must be using before using any other methods.
     *
     * @param context
     * @return ImageUtils
     */
    public static ImageUtils from(Context context) {
        return new ImageUtils(context);
    }

    public ImageFiles files() {
        List<File> imageFiles = new ArrayList<>();
        for (File file : Objects.requireNonNull(context.getFilesDir().listFiles())) {
            if (file.isFile() && (file.getName().endsWith(".jpg") && file.getName().startsWith("JPEG_"))) {
                imageFiles.add(file);
            }
        }
        return new ImageFiles(imageFiles);
    }

    /**
     * Apply image from URI to ImageView.
     * <p>
     * Note: Using this method before or without {@link ImageUtils#asFile()} will load image from URI first provided by
     * {@link ImageUtils#load(String)}.
     * </p>
     * <p>
     * In case you want to use local image, invoke {@link ImageUtils#asFile()} first before using this method.
     * </p>
     *
     * @param imageView ImageView to apply image to.
     */
    public ImageUtils apply(ImageView imageView) {
        imageView.setImageURI(Uri.parse(uri));
        return this;
    }



    /**
     * Apply image from URI first provided by {@link ImageUtils#load(String)} to ImageView.
     *
     * @param imageView
     * @return Name of the image
     */
    public String applyAndGetName(ImageView imageView) {
        imageView.setImageURI(Uri.parse(uri));
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    /**
     * Load image from URI and write to a file.
     * <p>
     * Note: Using this method before {@link ImageUtils#apply(ImageView)} will cause it to load image from External URL.
     * </p>
     * <p>
     * In case you want to use local image, invoke this first before using {@link ImageUtils#apply(ImageView)}.
     * </p>
     */
    public ImageUtils asFile() {
        new Thread(() -> {
            String newUri = write(DEFAULT_IMAGE_NAME, uri);
            if (newUri != null) {
                this.uri = newUri;
            }
        }).start();
        return this;
    }

    public ImageUtils asFile(Runnable before ,Runnable after) throws InterruptedException {
        Thread[] threads = {
                new Thread(() -> {
                    before.run();
                    String newUri = write(DEFAULT_IMAGE_NAME, uri);
                    if (newUri != null) {
                        this.uri = newUri;
                    }
                }),
                new Thread(after)

    };
        for (Thread thread : threads)
        {
            thread.start();

        }

        for (Thread thread : threads)
        {
            thread.join();
        }

        return this;
    }

    public ImageBitmap asBitmap() throws IOException {

        return new ImageBitmap(context, BitmapFactory
                .decodeFile(uri));
    }

    public ImageBitmap asConnectionBitmap() throws IOException, InterruptedException {
        RunnableResult<Bitmap> bitmapRunnableResult = new RunnableResult<Bitmap>() {
            private Bitmap bitmap;
            @Override
            public Bitmap getValue() {
                return bitmap;
            }

            @Override
            public void run() {
                try {
                    bitmap = new HttpUtils().downloadToBitmap(new URL(uri));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        Thread thread = new Thread(bitmapRunnableResult);
        thread.start();
        thread.join();
        return new ImageBitmap(context, bitmapRunnableResult.getValue());

    }

    public ImageBitmap setBitmap(Bitmap bitmap) throws IOException {
        return new ImageBitmap(context, bitmap);
    }

    /**
     * Apply image to ImageView after saving it into the device.
     * <p>
     * In case you want to use external URL image, invoke {@link ImageUtils#apply(ImageView)}
     * right after {@link ImageUtils#load(String)}.
     * </p>
     *
     * @param imageView ImageView to apply image to.
     */

    public ImageUtils into(ImageView imageView) {
        return asFile().apply(imageView);
    }



    /**
     * Download and write image to file.
     * @param fileName
     * @param uri
     * @return
     */
    private String write(String fileName, String uri) {
        try {
            // download and return path
            return new HttpUtils().download(context, new URL(uri), fileName).getPath();
        } catch (Exception error) {
            error.printStackTrace();
            return null;
        }
    }

    public static class ImageFiles {
        private File cursor;
        private final List<File> files;

        public ImageFiles(File... files) {
            this.files = Arrays.asList(files);
        }

        public ImageFiles(List<File> files) {
            this.files = files;
        }

        public List<File> getFiles() {
            return files;
        }

        public ImageFiles apply(ImageView imageView)
        {
            imageView.setImageURI(Uri.fromFile(this.cursor));
            return this;
        }

        public ImageFiles onCursor(Consumer<File> fileConsumer)
        {
            fileConsumer.accept(this.cursor);
            return this;
        }

        public ImageFiles Next(File file)
        {
            int position = files.indexOf(file);
            if (position == files.size() - 1)
            {
                position = 0;
                this.cursor = files.get(position);

            }
            else if (files.size() == 1)
            {
                position = 0;
                this.cursor = files.get(position);
            }
            else
            {
                this.cursor = files.get(position + 1);
            }

            return this;
        }
        public ImageFiles Previous(File file)
        {
            int position = files.indexOf(file);
            if (position == 0)
            {
                position = files.size() - 1;
                this.cursor = files.get(position);

            }
            else if (files.size() == 1)
            {
                position = 0;
                this.cursor = files.get(position);
            }
            else
            {
                this.cursor = files.get(position - 1);
            }

            return this;
        }

        public void forEach(Consumer<File> consumer) {
            files.forEach(consumer);
        }

        public CompletableFuture<Boolean> delete(File file) {
            return CompletableFuture.supplyAsync(file::delete);
        }
    }
    public static class ImageBitmap {
        private final Bitmap bitmap;
        private final Context context;
        public ImageBitmap(Context context, Bitmap bitmap) {
            this.bitmap = bitmap;
            this.context = context;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public ImageBitmap on(Consumer<Bitmap> iterator) {
            iterator.accept(bitmap);
            return this;
        }
        public ImageBitmap on(Consumer<Bitmap> iterator, Consumer<Throwable> error) {
            try {
                iterator.accept(bitmap);
            } catch (Throwable throwable) {
                error.accept(throwable);
            }
            return this;
        }

        public ImageBitmap asFile() throws IOException {
            FileOutputStream outputStream = new FileOutputStream(new File(context.getFilesDir(), DEFAULT_IMAGE_NAME));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
            return this;
        }
        public ImageBitmap apply(ImageView imageView) {
            imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 500, 500, false));
            return this;
        }
    }
}
