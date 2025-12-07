package com.example.myapplication.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import android.graphics.Canvas;
import java.util.List;
import java.util.ArrayList;

public class PasterActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private FrameLayout imageContainer;
    private Button btnCancel;
    private Button btnConfirm;
    private ImageButton btnPicture1;
    private ImageButton btnPicture2;
    private ImageButton btnPicture3;
    private ImageButton btnPicture4;
    private ImageButton btnPicture5;
    private ImageButton btnPicture6;
    private ImageButton btnPicture7;

    // 图片数据
    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片

    private Uri imageUri;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paster);

        initViews();
        loadImage();
        setupListeners();
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);

        btnPicture1 = findViewById(R.id.btn_picture1);
        btnPicture2 = findViewById(R.id.btn_picture2);
        btnPicture3 = findViewById(R.id.btn_picture3);
        btnPicture4 = findViewById(R.id.btn_picture4);
        btnPicture5 = findViewById(R.id.btn_picture5);
        btnPicture6 = findViewById(R.id.btn_picture6);
        btnPicture7 = findViewById(R.id.btn_picture7);

        imageContainer = findViewById(R.id.image_container);
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

                            Toast.makeText(PasterActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(PasterActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }



    private void setupListeners() {
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        btnConfirm.setOnClickListener(v -> {
            confirmAndSave();
        });

        btnPicture1.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic1);
        });
        btnPicture2.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic2);
        });
        btnPicture3.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic3);
        });
        btnPicture4.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic4);
        });
        btnPicture5.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic5);
        });
        btnPicture6.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic6);
        });
        btnPicture7.setOnClickListener(v -> {
            addStickerToImage(R.drawable.pic7);
        });
    }

    /**
     * 添加贴纸到图片上
     */
    private void addStickerToImage(int stickerRes) {
        ImageView stickerView = new ImageView(this);
        // 设置贴纸样式
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(150, 150);
        stickerView.setLayoutParams(params);
        stickerView.setImageResource(stickerRes);
        stickerView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // 设置初始位置（屏幕中央）
        stickerView.post(() -> {
            float centerX = (imageContainer.getWidth() - stickerView.getWidth()) / 2f;
            float centerY = (imageContainer.getHeight() - stickerView.getHeight()) / 2f;
            stickerView.setX(centerX);
            stickerView.setY(centerY);
        });
        // 设置拖动功能
        setupStickerTouch(stickerView);
        // 添加到容器并记录
        imageContainer.addView(stickerView);

        Toast.makeText(this, "贴纸已添加", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置贴纸的触摸事件，支持拖动移动
     */
    private void setupStickerTouch(ImageView stickerView) {
        stickerView.setOnTouchListener(new View.OnTouchListener() {
            private float lastTouchX, lastTouchY;
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 记录初始触摸位置
                        lastTouchX = event.getRawX();
                        lastTouchY = event.getRawY();
                        dX = v.getX() - lastTouchX;
                        dY = v.getY() - lastTouchY;

                        // 设置选中状态
                        setStickerSelected(stickerView, true);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // 拖动移动贴纸
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // 限制在容器范围内
                        newX = Math.max(0, Math.min(newX, imageContainer.getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, imageContainer.getHeight() - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        v.performClick();
                        return true;
                }
                return false;
            }
        });

        // 点击事件
        stickerView.setOnClickListener(v -> {
            setStickerSelected(stickerView, true);
        });
    }

    private void setStickerSelected(ImageView stickerView, boolean selected) {
//        // 先清除其他贴纸的选中状态
//        for (ImageView sticker : activeStickerViews) {
//            sticker.setBackgroundResource(0);
//        }

        if (selected) {
            // 设置选中边框
            stickerView.setBackgroundResource(R.drawable.text_selected_border);
        }
    }

    /*** 确认并保存贴纸到图片 */
    private void confirmAndSave() {
        if (originalBitmap != null) {
            Bitmap resultBitmap = drawStickersOnBitmap();
            Uri newImageUri = saveBitmapToUri(resultBitmap);

            if (newImageUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_image_uri", newImageUri.toString());
                setResult(RESULT_OK, resultIntent);

                Toast.makeText(this, "贴纸已应用", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /*** 将贴纸绘制到Bitmap上 */
    private Bitmap drawStickersOnBitmap() {
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        // TODO: 将贴纸绘制到Bitmap上
        // 计算图片在ImageView中的实际显示比例
        float imageViewWidth = imageView.getWidth();
        float imageViewHeight = imageView.getHeight();
        float bitmapWidth = mutableBitmap.getWidth();
        float bitmapHeight = mutableBitmap.getHeight();

        // 计算图片的显示缩放比例（保持宽高比）
        float displayScale = Math.min(imageViewWidth / bitmapWidth, imageViewHeight / bitmapHeight);

        // 计算图片在ImageView中的实际显示区域
        float actualImageWidth = bitmapWidth * displayScale;
        float actualImageHeight = bitmapHeight * displayScale;

        // 计算图片在ImageView中的偏移量（居中显示）
        float offsetX = (imageViewWidth - actualImageWidth) / 2f;
        float offsetY = (imageViewHeight - actualImageHeight) / 2f;

        // 遍历所有贴纸
        for (int i = 0; i < imageContainer.getChildCount(); i++) {
            View child = imageContainer.getChildAt(i);

            if (child instanceof ImageView && child != imageView) {
                ImageView stickerView = (ImageView) child;
                Drawable drawable = stickerView.getDrawable();

                if (drawable != null) {
                    // 获取贴纸在屏幕上的位置和大小
                    float stickerScreenX = stickerView.getX();
                    float stickerScreenY = stickerView.getY();
                    float stickerScreenWidth = stickerView.getWidth();
                    float stickerScreenHeight = stickerView.getHeight();

                    // 转换为相对于实际图片显示区域的坐标
                    float relativeX = (stickerScreenX - offsetX) / displayScale;
                    float relativeY = (stickerScreenY - offsetY) / displayScale;

                    // 关键修改：保持贴纸原始宽高比
                    // 获取贴纸原始尺寸
                    int intrinsicWidth = drawable.getIntrinsicWidth();
                    int intrinsicHeight = drawable.getIntrinsicHeight();

                    // 计算贴纸在原图上应该显示的尺寸，保持原始宽高比
                    float stickerScale = stickerScreenWidth / displayScale / intrinsicWidth;
                    float finalWidth = intrinsicWidth * stickerScale;
                    float finalHeight = intrinsicHeight * stickerScale;

                    // 只绘制在图片区域内的贴纸
                    if (relativeX < bitmapWidth && relativeY < bitmapHeight &&
                            relativeX + finalWidth > 0 && relativeY + finalHeight > 0) {

                        // 计算在原图上的绘制区域
                        int left = Math.max(0, (int) relativeX);
                        int top = Math.max(0, (int) relativeY);
                        int right = Math.min(mutableBitmap.getWidth(), (int) (relativeX + finalWidth));
                        int bottom = Math.min(mutableBitmap.getHeight(), (int) (relativeY + finalHeight));

                        // 保存Canvas状态
                        canvas.save();

                        // 处理旋转和缩放变换
                        float centerX = (left + right) / 2f;
                        float centerY = (top + bottom) / 2f;

                        if (stickerView.getRotation() != 0) {
                            canvas.rotate(stickerView.getRotation(), centerX, centerY);
                        }

                        if (stickerView.getScaleX() != 1f || stickerView.getScaleY() != 1f) {
                            // 保持原始宽高比的缩放
                            float avgScale = (stickerView.getScaleX() + stickerView.getScaleY()) / 2f;
                            canvas.scale(avgScale, avgScale, centerX, centerY);
                        }

                        // 绘制贴纸，保持原始宽高比
                        drawable.setBounds(left, top, right, bottom);
                        drawable.draw(canvas);

                        // 恢复Canvas状态
                        canvas.restore();
                    }
                }
            }
        }

        return mutableBitmap;
    }
    /*** 将Bitmap保存为临时文件并返回Uri */
    private Uri saveBitmapToUri(Bitmap bitmap) {
        try {
            File tempDir = new File(getCacheDir(), "edited_images");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            String fileName = "text_" + System.currentTimeMillis() + ".jpg";
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

}
