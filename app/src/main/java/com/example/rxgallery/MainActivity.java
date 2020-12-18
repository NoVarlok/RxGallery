package com.example.rxgallery;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private Button button;
    private ImageView imageView;
    private Bitmap bitmap = null;


    private static final int GALLERY_PERMISSION = 0;
    private Subscription subscription = null;
    ArrayList<String> paths;
    boolean showGallery = true;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.button);
        button.setOnClickListener(x -> ButtonAction());
        imageView = (ImageView) findViewById(R.id.imageView2);

        paths = new ArrayList<String>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PERMISSION);
        } else {
            GetImagePaths();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GALLERY_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GetImagePaths();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Permission to access the storage was not received", Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    void GetImagePaths() {
        Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj, // Which columns to return
                "", // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                ""); // Order-by clause (ascending by name)

        for (boolean canContinue = cursor.moveToFirst(); canContinue; canContinue = cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            paths.add(cursor.getString(columnIndex));
        }
        Collections.shuffle(paths);
        cursor.close();
    }

    @SuppressLint("SetTextI18n")
    void ButtonAction() {
        if (showGallery) {
            Toast toast = Toast.makeText(getApplicationContext(), "Start", Toast.LENGTH_LONG);
            toast.show();
            button.setText("Stop");
            Observable<String> observableImagePaths = Observable.from(paths);
            subscription = observableImagePaths.subscribeOn(Schedulers.io()).doOnNext(s -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e("Observer_DEBUG", "Failed to sleep");
                }
            }).observeOn(AndroidSchedulers.mainThread()).subscribe(imagePath -> {
                try {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(imagePath, options);
                    imageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Stop", Toast.LENGTH_LONG);
            toast.show();
            button.setText("Start");
            subscription.unsubscribe();
            imageView.setImageResource(android.R.color.transparent);
        }
        showGallery = !showGallery;
    }
}