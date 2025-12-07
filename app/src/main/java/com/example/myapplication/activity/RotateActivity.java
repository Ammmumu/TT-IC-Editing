package com.example.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.myapplication.R;
import com.example.myapplication.widget.ZoomableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RotateActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private Button btnCancel;
    private Button btnConfirm;
    private ImageButton btnClockwise90;
    private ImageButton btnAnticlockwise90;
    private ImageButton btnRotate180;
    private Uri imageUri;

    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotate);

        initViews();
        loadImage();
        setupButtonListeners();
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnClockwise90 = findViewById(R.id.btn_clockwise90);
        btnAnticlockwise90 = findViewById(R.id.btn_anticlockwise90);
        btnRotate180 = findViewById(R.id.btn_rotate180);

    }

    private void loadImage() {
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);

            // 使用 Glide 加载图片并转换为 Bitmap
            Glide.with(this)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            // 保存原始图片
                            originalBitmap = resource;

                            // 创建可编辑的副本
                            currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);

                            // 显示图片
                            imageView.setImageBitmap(currentBitmap);

                            Toast.makeText(RotateActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(RotateActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupButtonListeners() {
        // 取消按钮 - 恢复原图并返回
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        // 确认按钮 - 保存调节后的图片并返回
        btnConfirm.setOnClickListener(v -> {
            // TODO: 将调节后的图片返回给上一个Activity
            if (currentBitmap != null) {
                // 将调节后的Bitmap保存为临时文件并转换为Uri
                Uri newImageUri = saveBitmapToUri(currentBitmap);

                if (newImageUri != null) {
                    // 创建Intent返回Uri
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_image_uri", newImageUri.toString());
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(this, "已应用旋转", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 顺时针90度
        btnClockwise90.setOnClickListener(v -> {
            performRotation(90);
        });

        // 逆时针90度
        btnAnticlockwise90.setOnClickListener(v -> {
            performRotation(-90);
        });

        // 旋转180度
        btnRotate180.setOnClickListener(v -> {
            performRotation(180);
        });
    }

    /**
     * 执行旋转操作
     * @param degrees 旋转角度，正数为顺时针，负数为逆时针
     */
    private void performRotation(float degrees) {
        if (currentBitmap != null) {
            Bitmap rotatedBitmap = rotateBitmap(currentBitmap, degrees);

            // 释放旧的Bitmap
            if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }

            currentBitmap = rotatedBitmap;
            imageView.setImageBitmap(currentBitmap);

            // 提供操作反馈
            String direction = degrees > 0 ? "顺时针" : (degrees < 0 ? "逆时针" : "");
            Toast.makeText(this, "已" + direction + "旋转" + Math.abs(degrees) + "°",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 旋转Bitmap
     * @param bitmap 原始图片
     * @param degrees 旋转角度
     * @return 旋转后的图片
     */
    private Bitmap rotateBitmap(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);

        return Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
        );
    }


    /**
     * 将Bitmap保存为临时文件并返回Uri
     */
    private Uri saveBitmapToUri(Bitmap bitmap) {
        try {
            // 创建临时文件
            File tempDir = new File(getCacheDir(), "edited_images");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            // 生成唯一文件名
            String fileName = "edited_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(tempDir, fileName);

            // 将Bitmap写入文件
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            // 使用FileProvider获取Uri（Android 7.0以上必须使用）
            Uri imageUri = FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    imageFile
            );

            return imageUri;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



}











