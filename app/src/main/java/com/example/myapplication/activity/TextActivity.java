package com.example.myapplication.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.myapplication.R;
import com.example.myapplication.widget.ZoomableImageView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import android.text.TextUtils;
import android.view.View;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.example.myapplication.widget.ZoomableImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TextActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private FrameLayout imageContainer; // 图片容器
    private Button btnCancel, btnConfirm; // 取消、确定

    // 字体按钮
    private Button btnSongTypeface;
    private Button btnBoldface;
    private Button btnRegularScript;

    private EditText textEdit; // 文字编辑框
    private TextView overlayTextView; // 文字显示框

    private Uri imageUri;
    private Bitmap originalBitmap;  // 原始图片
    private Bitmap currentBitmap;   // 当前调节后的图片

    // 文字样式参数
    private int currentTextSize = 30;
    private int currentTextColor = Color.BLACK;
    private float currentRotation = 0f;
    private float currentScaleX = 1f;
    private float currentScaleY = 1f;

    private SeekBar seekbarTextSize; // 字号
    private TextView textSizeValue; // 字号值

    private boolean isTextSelected = true;

    private LinearLayout colorPalette;
    // 添加手势检测器
    private ScaleGestureDetector scaleDetector;
    private RotationGestureDetector rotationDetector;
    private Typeface currentTypeface = Typeface.DEFAULT; // 默认字体



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text);

        initViews();
        loadImage();
        createColorPalette();
        setupListeners();
        setupGestureDetectors(); // 添加手势检测器初始化
    }

    private void initViews() {
        imageView = findViewById(R.id.image_view);
        imageContainer = findViewById(R.id.image_container);
        btnCancel = findViewById(R.id.btn_cancel);
        btnConfirm = findViewById(R.id.btn_confirm);
        textEdit = findViewById(R.id.text_edit);
        seekbarTextSize = findViewById(R.id.seekbar_text_size);
        textSizeValue = findViewById(R.id.text_size_value);
        colorPalette = findViewById(R.id.color_palette);
        // 字体按钮初始化
        btnSongTypeface = findViewById(R.id.btn_song_typeface);
        btnBoldface = findViewById(R.id.btn_boldface);
        btnRegularScript = findViewById(R.id.btn_regular_script);
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
                            Toast.makeText(TextActivity.this, "图片加载成功", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理资源
                        }
                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            Toast.makeText(TextActivity.this, "图片加载失败", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*** 设置监听器 */
    private void setupListeners() {
        // 监听输入框文字变化，实时更新图片上的文字
        textEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    updateOverlayText(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 字体切换监听器
        btnSongTypeface.setOnClickListener(v -> {
            setTextTypeface(Typeface.SERIF);
            Toast.makeText(this, "已切换到宋体", Toast.LENGTH_SHORT).show();
        });

        btnBoldface.setOnClickListener(v -> {
            setTextTypeface(Typeface.DEFAULT_BOLD);
            Toast.makeText(this, "已切换到黑体", Toast.LENGTH_SHORT).show();
        });

        btnRegularScript.setOnClickListener(v -> {
            // 楷体需要使用系统字体或自定义字体
            Typeface kaiti = Typeface.create(Typeface.SERIF, Typeface.ITALIC);
            setTextTypeface(kaiti);
            Toast.makeText(this, "已切换到楷体", Toast.LENGTH_SHORT).show();
        });
        // 字号调节 - 符合项目要求12-36号可调节[1]
        seekbarTextSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTextSize = progress + 12; // 范围12-36
                textSizeValue.setText(String.valueOf(currentTextSize));
                overlayTextView.setTextSize(currentTextSize);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        // 取消按钮
        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        // 确认按钮
        btnConfirm.setOnClickListener(v -> {
            confirmAndSave();
        });

        // 点击容器空白区域取消文字选中
        imageContainer.setOnClickListener(v -> {
            if (isTextSelected) {
                setTextSelected(false);
            }
        });
    }

    /**
     * 设置文字字体
     * @param typeface 字体类型
     */
    private void setTextTypeface(Typeface typeface) {
        if (overlayTextView != null) {
            overlayTextView.setTypeface(typeface);
        }
    }

    /*** 确认并保存文字到图片 */
    private void confirmAndSave() {
        String text = textEdit.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "请输入文字", Toast.LENGTH_SHORT).show();
            return;
        }
        // 将文字绘制到Bitmap上
        Bitmap resultBitmap = drawTextOnBitmap();
        if (resultBitmap != null) {
            // 保存并返回Uri
            Uri newImageUri = saveBitmapToUri(resultBitmap);
            if (newImageUri != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("new_image_uri", newImageUri.toString());
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "文字已添加", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*** 更新图片上叠加的文字，符合项目要求：支持文字内容自定义输入，支持换行 */
    private void updateOverlayText(String text) {
        try {
            if (overlayTextView == null) {
                // 如果TextView还未创建，动态创建
                createOverlayTextView();
            }
            if (text == null || text.trim().isEmpty()) {
                // 输入为空时隐藏TextView
                overlayTextView.setVisibility(View.INVISIBLE);
            } else {
                // 设置文字内容
                overlayTextView.setText(text);
                overlayTextView.setVisibility(View.VISIBLE);
                // 首次显示时，设置到屏幕中央
                if (overlayTextView.getParent() == null) {
                    imageContainer.addView(overlayTextView);
                }
                // 确保布局完成后再设置位置
                if (overlayTextView.getX() == 0 && overlayTextView.getY() == 0) {
                    overlayTextView.post(() -> {
                        setTextViewToCenter();
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "文字显示异常", Toast.LENGTH_SHORT).show();
        }
    }

    /*** 创建叠加的TextView */
    private void createOverlayTextView() {
        if (overlayTextView == null) {
            overlayTextView = new TextView(this);

            // 设置文字样式
            overlayTextView.setTextSize(currentTextSize);
            overlayTextView.setTextColor(currentTextColor);
            overlayTextView.setTypeface(currentTypeface); // 应用当前字体
            overlayTextView.setPadding(16, 8, 16, 8);

            // 设置布局参数
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            overlayTextView.setLayoutParams(params);
            // 设置初始文本（用于测试显示）
            overlayTextView.setText("请输入文字");
            // 设置可见性
            overlayTextView.setVisibility(View.VISIBLE);
            // 关键：将TextView添加到容器中
            imageContainer.addView(overlayTextView);
            setTextSelected(true);
            // 设置初始位置（屏幕中央）
            overlayTextView.post(() -> {
                setTextViewToCenter();
            });
            // 添加移动功能支持
            setupTextViewTouch();
        }
    }

    /*** 自定义旋转手势检测器 */
    private static class RotationGestureDetector {

        public interface OnRotationGestureListener {
            boolean onRotation(RotationGestureDetector rotationDetector);
        }

        private OnRotationGestureListener listener;
        private float previousAngle;
        private float currentAngle;

        public RotationGestureDetector(OnRotationGestureListener listener) {
            this.listener = listener;
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (event.getPointerCount() == 2) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        // 记录初始角度
                        previousAngle = getAngle(event);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        // 计算角度变化
                        currentAngle = getAngle(event);
                        float deltaAngle = currentAngle - previousAngle;

                        // 处理角度跨越180°的情况
                        if (deltaAngle > 180) {
                            deltaAngle -= 360;
                        } else if (deltaAngle < -180) {
                            deltaAngle += 360;
                        }

                        if (listener != null && Math.abs(deltaAngle) > 1) {
                            boolean handled = listener.onRotation(this);
                            previousAngle = currentAngle;
                            return handled;
                        }
                        break;
                }
            }
            return false;
        }

        private float getAngle(MotionEvent event) {
            float deltaX = event.getX(0) - event.getX(1);
            float deltaY = event.getY(0) - event.getY(1);
            return (float) Math.toDegrees(Math.atan2(deltaY, deltaX));
        }
        public float getAngle() {
            return currentAngle - previousAngle;
        }
    }

    /*** 设置手势识别器，支持缩放、旋转 */
    private void setupGestureDetectors() {
        // 缩放手势检测器
        scaleDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (isTextSelected && overlayTextView != null) {
                    float scaleFactor = detector.getScaleFactor();

                    // 缩放文字大小，限制范围0.5-3.0倍
                    currentScaleX *= scaleFactor;
                    currentScaleY *= scaleFactor;
                    currentScaleX = Math.max(0.5f, Math.min(3.0f, currentScaleX));
                    currentScaleY = Math.max(0.5f, Math.min(3.0f, currentScaleY));

                    overlayTextView.setScaleX(currentScaleX);
                    overlayTextView.setScaleY(currentScaleY);

                    return true;
                }
                return false;
            }
        });

        // 旋转手势检测器（自定义实现）
        rotationDetector = new RotationGestureDetector(new RotationGestureDetector.OnRotationGestureListener() {
            @Override
            public boolean onRotation(RotationGestureDetector rotationDetector) {
                if (isTextSelected && overlayTextView != null) {
                    currentRotation += rotationDetector.getAngle();
                    // 确保角度在0-360°范围内
                    currentRotation = currentRotation % 360f;
                    if (currentRotation < 0) {
                        currentRotation += 360f;
                    }
                    overlayTextView.setRotation(currentRotation);
                    return true;
                }
                return false;
            }
        });
    }

    /*** 设置TextView的触摸事件，支持拖动移动 */
    private void setupTextViewTouch() {
        if (overlayTextView != null) {
            overlayTextView.setOnTouchListener(new View.OnTouchListener() {
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

                            // 设置为选中状态
                            setTextSelected(true);
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            // 单指拖动移动
                            float newX = event.getRawX() + dX;
                            float newY = event.getRawY() + dY;

                            // 限制在容器范围内，避免移出屏幕
                            newX = Math.max(0, Math.min(newX, imageContainer.getWidth() - v.getWidth()));
                            newY = Math.max(0, Math.min(newY, imageContainer.getHeight() - v.getHeight()));

                            // 应用新位置
                            v.setX(newX);
                            v.setY(newY);
                            return true;

                        case MotionEvent.ACTION_UP:
                            // 调用performClick()支持无障碍访问
                            v.performClick();
                            return true;
                    }
                    return false;
                }
            });

            // 设置点击监听器来处理performClick()调用
            overlayTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 处理点击逻辑
                    setTextSelected(true);
                }
            });
        }
    }


    /*** 将TextView设置到屏幕中央 */
    private void setTextViewToCenter() {
        if (overlayTextView != null && imageContainer != null) {
            int containerWidth = imageContainer.getWidth();
            int containerHeight = imageContainer.getHeight();
            int textWidth = overlayTextView.getWidth();
            int textHeight = overlayTextView.getHeight();

            if (containerWidth > 0 && containerHeight > 0) {
                float centerX = (containerWidth - textWidth) / 2f;
                float centerY = (containerHeight - textHeight) / 2f;

                overlayTextView.setX(Math.max(0, centerX));
                overlayTextView.setY(Math.max(0, centerY));
            }
        }
    }

    /*** 将文字绘制到Bitmap上 */
    private Bitmap drawTextOnBitmap() {
        if (originalBitmap == null || overlayTextView.getVisibility() != View.VISIBLE) {
            return originalBitmap;
        }
        // 创建可变的Bitmap副本
        Bitmap mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);

        // 计算正确的缩放比例
        float scaleX = (float) mutableBitmap.getWidth() / imageView.getWidth();
        float scaleY = (float) mutableBitmap.getHeight() / imageView.getHeight();

        // 创建画笔
        Paint paint = new Paint();
        paint.setColor(currentTextColor);
        paint.setTextSize(currentTextSize * 2); // 根据屏幕密度调整
//        paint.setTextSize(currentTextSize * Math.min(scaleX, scaleY));
        paint.setAntiAlias(true);
        paint.setTypeface(overlayTextView.getTypeface());
        // 获取文字内容和位置
        String text = overlayTextView.getText().toString();
        float x = overlayTextView.getX() * (mutableBitmap.getWidth() / (float)imageView.getWidth());
        float y = overlayTextView.getY() * (mutableBitmap.getHeight() / (float)imageView.getHeight()) + paint.getTextSize();
        // 支持换行文字[1]
        String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], x, y + i * paint.getTextSize(), paint);
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

    /*** 设置文字选中状态 */
    private void setTextSelected(boolean selected) {
        isTextSelected = selected;
        if (selected) {
            // 显示选中边框
            overlayTextView.setBackgroundResource(R.drawable.text_selected_border);
        }
        else {
            // 恢复普通背景
            overlayTextView.setBackgroundResource(R.drawable.text_background);
        }
    }

    /*** 创建10个预选颜色按钮 */
    private void createColorPalette() {
        // 定义10种预设颜色（符合项目要求）
        int[] presetColors = {
                Color.WHITE,      // 白色
                Color.BLACK,      // 黑色
                Color.RED,        // 红色
                Color.GREEN,      // 绿色
                Color.BLUE,       // 蓝色
                Color.YELLOW,     // 黄色
                0xFFFF8800,       // 橙色
                0xFF800080,       // 紫色
                Color.GRAY,       // 灰色
                0xFFFF1493        // 深粉色
        };

        String[] colorNames = {
                "白色", "黑色", "红色", "绿色", "蓝色",
                "黄色", "橙色", "紫色", "灰色", "粉色"
        };

        for (int i = 0; i < presetColors.length; i++) {
            final int color = presetColors[i];
            final String colorName = colorNames[i];

            Button colorBtn = new Button(this);

            // 设置按钮样式
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, 80);
            params.setMargins(8, 0, 8, 0);
            colorBtn.setLayoutParams(params);
            colorBtn.setBackgroundColor(color);

            // 添加边框以便于识别
            colorBtn.setBackground(createColorButtonBackground(color));

            // 设置点击事件
            colorBtn.setOnClickListener(v -> {
                currentTextColor = color;
                overlayTextView.setTextColor(currentTextColor);
                Toast.makeText(this, "已选择" + colorName, Toast.LENGTH_SHORT).show();
            });

            colorPalette.addView(colorBtn);
        }
    }

    /*** 创建颜色按钮的背景（带边框） */
    private Drawable createColorButtonBackground(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8); // 圆角
        drawable.setStroke(2, Color.GRAY); // 边框
        return drawable;
    }














}
