package com.example.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.myapplication.R;
import com.example.myapplication.widget.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.Canvas;

public class CropActivity extends AppCompatActivity {

    private CropImageView imageView;
    private Button btnCancel;
    private Button btn_1_1;
    private Button btn_4_3;
    private Button btn_16_9;
    private Button btn_3_4;
    private Button btn_9_16;
    private Button btnConfirm;
    private Uri imageUri;

    // 图片数据
    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片
    // 添加裁剪相关变量
    private RectF cropRect;  // 裁剪区域
    private float currentRatio = 0f;  // 当前裁剪比例，0表示自由裁剪
    private Paint cropPaint;  // 裁剪框绘制画笔


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        initViews();
        initCropSettings();  // 初始化裁剪设置
        loadImage();
        setupClickListeners();
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        btn_1_1 = findViewById(R.id.btn_1_1);
        btn_4_3 = findViewById(R.id.btn_4_3);
        btn_16_9 = findViewById(R.id.btn_16_9);
        btn_3_4 = findViewById(R.id.btn_3_4);
        btn_9_16 = findViewById(R.id.btn_9_16);
    }

    private void initCropSettings() {
        cropPaint = new Paint();
        cropPaint.setColor(0xFFFFFFFF);  // 白色边框
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setStrokeWidth(4f);
        cropPaint.setAntiAlias(true);
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

                            Toast.makeText(CropActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(CropActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setupClickListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            performCrop();
        });

        // 设置比例按钮监听器
        btn_1_1.setOnClickListener(v -> applyCropRatio("1:1"));
        btn_4_3.setOnClickListener(v -> applyCropRatio("4:3"));
        btn_16_9.setOnClickListener(v -> applyCropRatio("16:9"));
        btn_3_4.setOnClickListener(v -> applyCropRatio("3:4"));
        btn_9_16.setOnClickListener(v -> applyCropRatio("9:16"));
    }

    /**
     * 根据选定比例计算裁剪区域
     */
    private void calculateCropRect() {
        if (currentBitmap == null || imageView == null) return;

        // 获取ImageView的实际显示尺寸
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        if (viewWidth == 0 || viewHeight == 0) {
            // 如果View还没有测量完成，延迟执行
            imageView.post(this::calculateCropRect);
            return;
        }

        if (currentRatio == 0f) {
            // 自由裁剪 - 默认裁剪整个显示区域的80%
            float margin = Math.min(viewWidth, viewHeight) * 0.1f;
            cropRect = new RectF(margin, margin,
                    viewWidth - margin, viewHeight - margin);
        } else {
            // 固定比例裁剪
            float cropWidth, cropHeight;

            if (currentRatio >= 1.0f) {
                // 横向比例
                cropHeight = Math.min(viewHeight * 0.8f, viewWidth * 0.8f / currentRatio);
                cropWidth = cropHeight * currentRatio;
            } else {
                // 纵向比例
                cropWidth = Math.min(viewWidth * 0.8f, viewHeight * 0.8f * currentRatio);
                cropHeight = cropWidth / currentRatio;
            }

            // 居中显示裁剪框
            float left = (viewWidth - cropWidth) / 2f;
            float top = (viewHeight - cropHeight) / 2f;
            cropRect = new RectF(left, top, left + cropWidth, top + cropHeight);
        }

        // 设置裁剪框到自定义ImageView
        imageView.setCropRect(cropRect);
    }


    /**
     * 执行裁剪操作 - 需要将屏幕坐标转换为Bitmap坐标
     */
    private void performCrop() {
        RectF screenCropRect = imageView.getCropRect();
        if (screenCropRect == null || currentBitmap == null) {
            Toast.makeText(this, "裁剪区域未设置", Toast.LENGTH_SHORT).show();
            return;
        }

        // 将屏幕坐标转换为Bitmap坐标
        RectF bitmapCropRect = convertScreenRectToBitmapRect(screenCropRect);

        try {
            Bitmap croppedBitmap = cropBitmap(currentBitmap, bitmapCropRect);

            if (croppedBitmap != null) {
                Uri croppedImageUri = saveBitmapToUri(croppedBitmap);

                if (croppedImageUri != null) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("new_image_uri", croppedImageUri.toString());
                    setResult(RESULT_OK, resultIntent);

                    Toast.makeText(this, "裁剪完成", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "裁剪失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 将屏幕坐标系的裁剪框转换为Bitmap坐标系
     */
    private RectF convertScreenRectToBitmapRect(RectF screenRect) {
        int bitmapWidth = currentBitmap.getWidth();
        int bitmapHeight = currentBitmap.getHeight();
        int viewWidth = imageView.getWidth();
        int viewHeight = imageView.getHeight();

        // 计算图片在View中的实际显示区域（考虑fitCenter缩放）
        float scale = Math.min((float) viewWidth / bitmapWidth, (float) viewHeight / bitmapHeight);
        float scaledWidth = bitmapWidth * scale;
        float scaledHeight = bitmapHeight * scale;

        float offsetX = (viewWidth - scaledWidth) / 2f;
        float offsetY = (viewHeight - scaledHeight) / 2f;

        // 转换坐标
        float left = (screenRect.left - offsetX) / scale;
        float top = (screenRect.top - offsetY) / scale;
        float right = (screenRect.right - offsetX) / scale;
        float bottom = (screenRect.bottom - offsetY) / scale;

        return new RectF(left, top, right, bottom);
    }

    /**
     * 裁剪Bitmap
     */
    private Bitmap cropBitmap(Bitmap sourceBitmap, RectF cropRect) {
        try {
            // 确保裁剪区域在图片范围内
            int left = Math.max(0, (int) cropRect.left);
            int top = Math.max(0, (int) cropRect.top);
            int width = Math.min(sourceBitmap.getWidth() - left, (int) cropRect.width());
            int height = Math.min(sourceBitmap.getHeight() - top, (int) cropRect.height());

            if (width <= 0 || height <= 0) {
                return null;
            }

            // 执行裁剪
            return Bitmap.createBitmap(sourceBitmap, left, top, width, height);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存Bitmap并返回Uri
     */
    private Uri saveBitmapToUri(Bitmap bitmap) {
        try {
            File tempDir = new File(getCacheDir(), "edited_images");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            String fileName = "cropped_" + System.currentTimeMillis() + ".jpg";
            File imageFile = new File(tempDir, fileName);

            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
            fos.close();

            return FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    imageFile
            );

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applyCropRatio(String ratio) {
        Toast.makeText(this, "选择比例: " + ratio, Toast.LENGTH_SHORT).show();

        switch (ratio) {
            case "1:1":
                currentRatio = 1.0f;
                break;
            case "4:3":
                currentRatio = 4.0f / 3.0f;
                break;
            case "16:9":
                currentRatio = 16.0f / 9.0f;
                break;
            case "3:4":
                currentRatio = 3.0f / 4.0f;
                break;
            case "9:16":
                currentRatio = 9.0f / 16.0f;
                break;
            default:
                currentRatio = 0f;  // 自由裁剪
                break;
        }

        // 计算并设置裁剪区域
        calculateCropRect();

        // 刷新显示
        imageView.setFixedRatio(currentRatio);
    }












}
