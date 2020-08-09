package com.example.opencvjavaexample;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "OpenCV Java Example";

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "static initializer: OpenCV is not loaded!");
        } else {
            Log.d(TAG, "static initializer: OpenCV is loaded successfully!");
        }
    }

//    앱 권한 요청
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = new String[] { Manifest.permission.READ_EXTERNAL_STORAGE };

    private static final int REQ_CODE_SELECT_IMAGE = 200;

    private ImageView imageView;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // Android 6.0(API 수준 23) 이상
            if (!hasPermissions(PERMISSIONS)) {
                requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE);  // 권한 요청
            }
        }

        imageView = (ImageView) findViewById(R.id.imageView);
    }

//    앱에 이미 권한이 부여되었는지 확인
    private boolean hasPermissions(String[] permissions) {
        int result;

        for (String permission : permissions) {
            result = ContextCompat.checkSelfPermission(this, permission);

            if (result == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        bitmap.recycle();
        bitmap = null;

        super.onDestroy();
    }

    public void onButtonClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String path = getImagePathFromURI(data.getData());

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeFile(path, options);

                    if (bitmap != null) {
                        detectEdge();
                        imageView.setImageBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void detectEdge() {
        Mat src = new Mat();
        Utils.bitmapToMat(bitmap, src);

        Mat edge = new Mat();
        Imgproc.Canny(src, edge, 50, 150);

        Utils.matToBitmap(edge, bitmap);

        src.release();
        edge.release();
    }

    private String getImagePathFromURI(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(data, proj, null, null, null);
        if (cursor == null) {
            return data.getPath();
        } else {
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imgPath = cursor.getString(idx);
            cursor.close();
            return imgPath;
        }
    }
}