package com.example.myapplication.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.example.myapplication.R;
import com.example.myapplication.widget.ZoomableImageView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.widget.ZoomableImageView;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.net.Uri;
import androidx.core.content.FileProvider;
import android.graphics.BitmapFactory;


public class AdjustActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private Button btnCancel;
    private Button btnConfirm;
    private SeekBar seekbarBrightness;
    private SeekBar seekbarContrast;
    private TextView textBrightnessValue;
    private TextView textContrastValue;
    private Uri imageUri;

    // 图片数据
    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片

    // 当前调节值
    private int currentBrightness = 0;
    private int currentContrast = 0;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjust);

        initViews();
        loadImage();
        // 设置SeekBar监听器
        setupSeekBarListeners();
        // 设置按钮监听器
        setupButtonListeners();
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
        seekbarBrightness = findViewById(R.id.seekbar_brightness);
        seekbarContrast = findViewById(R.id.seekbar_contrast);
        textBrightnessValue = findViewById(R.id.text_brightness_value);
        textContrastValue = findViewById(R.id.text_contrast_value);
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

                            Toast.makeText(AdjustActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(AdjustActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


//    private void loadImage() {
//        // 从Intent获取图片
//        Bundle extras = getIntent().getExtras();
//        if (extras != null && extras.containsKey("image")) {
//            originalBitmap = (Bitmap) extras.getParcelable("image");
//            if (originalBitmap != null) {
//                // 创建副本用于编辑
//                currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
//                imageView.setImageBitmap(currentBitmap);
//            }
//        }
//        if (originalBitmap == null) {
//            Toast.makeText(this, "未找到图片", Toast.LENGTH_SHORT).show();
//        }
//    }

    // 亮度、对比度调节监听
    private void setupSeekBarListeners() {
        // 亮度调节监听
        seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将progress转换为实际亮度值（-100到100）
                currentBrightness = progress - 100;

                // 更新右边的数值显示
                textBrightnessValue.setText(String.valueOf(currentBrightness));

                // 实时更新图片预览效果
                applyAdjustments();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // 对比度调节监听
        seekbarContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 将progress转换为实际对比度值（-50到150）
                currentContrast = progress - 50;

                // 更新右边的数值显示
                textContrastValue.setText(String.valueOf(currentContrast));

                // 实时更新图片预览效果
                applyAdjustments();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    // 取消、确定按钮监听
    private void setupButtonListeners() {
        // 取消按钮 - 恢复原图并返回
        btnCancel.setOnClickListener(v -> {

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

                    Toast.makeText(this, "已应用调节", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "图片保存失败", Toast.LENGTH_SHORT).show();
                }
            }
            finish();
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
     * 应用亮度和对比度调节
     * 同时调节两个参数，避免多次重复计算
     */
    private void applyAdjustments() {
        if (originalBitmap == null) {
            return;
        }

        // 使用ColorMatrix同时应用亮度和对比度调节
        currentBitmap = adjustImageBrightnessContrast(
                originalBitmap,
                currentBrightness,
                currentContrast
        );

        // 更新ImageView显示
        imageView.setImageBitmap(currentBitmap);
    }

    /**
     * 调节图片亮度和对比度
     * @param bitmap 原始图片
     * @param brightness 亮度值（-100到100）
     * @param contrast 对比度值（-50到150）
     * @return 调节后的图片
     */
    private Bitmap adjustImageBrightnessContrast(Bitmap bitmap, int brightness, int contrast) {
        // 创建新的Bitmap
        Bitmap adjustedBitmap = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                bitmap.getConfig()
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();

        // 创建ColorMatrix
        ColorMatrix colorMatrix = new ColorMatrix();

        // 调节亮度
        // 亮度调节：通过改变颜色矩阵的平移量实现
        float brightnessValue = brightness * 255f / 100f;  // 转换为0-255范围
        colorMatrix.set(new float[] {
                1, 0, 0, 0, brightnessValue,
                0, 1, 0, 0, brightnessValue,
                0, 0, 1, 0, brightnessValue,
                0, 0, 0, 1, 0
        });

        // 调节对比度
        // 对比度调节：通过缩放因子实现
        // contrast范围：-50到150，转换为缩放因子
        // 对比度=0时，缩放因子=1（无变化）
        // 对比度>0时，增强对比度；对比度<0时，降低对比度
        float contrastFactor = (contrast + 100f) / 100f;
        float translate = (1f - contrastFactor) * 127.5f;  // 中心点偏移

        ColorMatrix contrastMatrix = new ColorMatrix();
        contrastMatrix.set(new float[] {
                contrastFactor, 0, 0, 0, translate,
                0, contrastFactor, 0, 0, translate,
                0, 0, contrastFactor, 0, translate,
                0, 0, 0, 1, 0
        });

        // 合并两个矩阵：先应用对比度，再应用亮度
        colorMatrix.postConcat(contrastMatrix);

        // 应用ColorMatrix到画笔
        ColorMatrixColorFilter colorFilter = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(colorFilter);

        // 绘制调节后的图片
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return adjustedBitmap;
    }

    /**
     * 单独调节亮度（备用方法）
     * @param bitmap 原始图片
     * @param brightness 亮度值（-100到100）
     * @return 调节后的图片
     */
    private Bitmap adjustBrightness(Bitmap bitmap, int brightness) {
        Bitmap adjustedBitmap = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                bitmap.getConfig()
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();

        // 亮度调节：改变RGB通道的偏移量
        float value = brightness * 255f / 100f;
        ColorMatrix colorMatrix = new ColorMatrix(new float[] {
                1, 0, 0, 0, value,
                0, 1, 0, 0, value,
                0, 0, 1, 0, value,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return adjustedBitmap;
    }

    /**
     * 单独调节对比度（备用方法）
     * @param bitmap 原始图片
     * @param contrast 对比度值（-50到150）
     * @return 调节后的图片
     */
    private Bitmap adjustContrast(Bitmap bitmap, int contrast) {
        Bitmap adjustedBitmap = Bitmap.createBitmap(
                bitmap.getWidth(),
                bitmap.getHeight(),
                bitmap.getConfig()
        );

        Canvas canvas = new Canvas(adjustedBitmap);
        Paint paint = new Paint();

        // 对比度调节：通过缩放RGB通道实现
        float factor = (contrast + 100f) / 100f;
        float translate = (1f - factor) * 127.5f;

        ColorMatrix colorMatrix = new ColorMatrix(new float[] {
                factor, 0, 0, 0, translate,
                0, factor, 0, 0, translate,
                0, 0, factor, 0, translate,
                0, 0, 0, 1, 0
        });

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        return adjustedBitmap;
    }


}












