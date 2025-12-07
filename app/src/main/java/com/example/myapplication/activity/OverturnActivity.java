package com.example.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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

public class OverturnActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private Button btnCancel;
    private Button btnConfirm;
    private ImageButton btnOverturnLeftRight;
    private ImageButton btnOverturnUpDown;
    private Uri imageUri;

    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overturn);

        initViews();
        loadImage();
        setupButtonListeners();
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnOverturnLeftRight = findViewById(R.id.btn_overturn_left_right);
        btnOverturnUpDown = findViewById(R.id.btn_overturn_up_down);

    }

//    private void loadImage() {
//        String uriString = getIntent().getStringExtra("image_uri");
//        if (uriString != null) {
//            imageUri = Uri.parse(uriString);
//            // 使用 Glide 或其他方式加载图片
//            Glide.with(this)
//                    .load(imageUri)
//                    .into(imageView);
//        } else {
//            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }

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

                            Toast.makeText(OverturnActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(OverturnActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 取消、确定按钮监听
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

                    Toast.makeText(this, "已应用翻转", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // 水平翻转（左右翻转）
        btnOverturnLeftRight.setOnClickListener(v -> {
            performHorizontalFlip();
        });

        // 垂直翻转（上下翻转）
        btnOverturnUpDown.setOnClickListener(v -> {
            performVerticalFlip();
        });
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



    /**
     * 执行水平翻转（左右翻转）
     */
    private void performHorizontalFlip() {
        if (currentBitmap != null) {
            Bitmap flippedBitmap = flipBitmapHorizontally(currentBitmap);

            // 释放旧的Bitmap
            if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }

            currentBitmap = flippedBitmap;
            imageView.setImageBitmap(currentBitmap);

            Toast.makeText(this, "已水平翻转", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 执行垂直翻转（上下翻转）
     */
    private void performVerticalFlip() {
        if (currentBitmap != null) {
            Bitmap flippedBitmap = flipBitmapVertically(currentBitmap);

            // 释放旧的Bitmap
            if (currentBitmap != originalBitmap && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }

            currentBitmap = flippedBitmap;
            imageView.setImageBitmap(currentBitmap);

            Toast.makeText(this, "已垂直翻转", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 水平翻转Bitmap（左右镜像）
     * @param bitmap 原始图片
     * @return 翻转后的图片
     */
    private Bitmap flipBitmapHorizontally(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(-1.0f, 1.0f); // 水平翻转：X轴缩放-1，Y轴缩放1

        return Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
        );
    }

    /**
     * 垂直翻转Bitmap（上下镜像）
     * @param bitmap 原始图片
     * @return 翻转后的图片
     */
    private Bitmap flipBitmapVertically(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.preScale(1.0f, -1.0f); // 垂直翻转：X轴缩放1，Y轴缩放-1

        return Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                false
        );
    }


}











