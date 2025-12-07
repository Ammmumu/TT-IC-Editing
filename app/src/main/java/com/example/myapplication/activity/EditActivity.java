package com.example.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.widget.ZoomableImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.BitmapFactory;

public class EditActivity extends AppCompatActivity {

    private static final String TAG = "EditActivity";
    private ZoomableImageView imageView; // 缩放
    private Bitmap currentBitmap;  // 统一使用Bitmap管理图片
//    private ImageButton btnBack; // 返回按钮
    private Button btnBack; // 返回按钮
    private Button btnSave; // 保存按钮

    // 底部功能按钮
    private ImageButton btnCrop; // 裁剪按钮
    private ImageButton btnRotate; // 旋转按钮
    private ImageButton btnOverturn; // 翻转按钮
    private ImageButton btnAdjust; // 调节按钮
    private ImageButton btnText; // 文字按钮
    private ImageButton btnPaster; // 贴纸按钮

    private Uri imageUri; // 图片URI
    private Uri currentImageUri;
    private boolean isSaving = false; // 防止重复保存

    private static final int REQUEST_CODE_ADJUST = 1001;
    // 存储权限请求启动器
    private ActivityResultLauncher<String> storagePermissionLauncher;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        initViews();
        initLaunchers();
        loadImage();
        setupClickListeners();
    }

    /*** 初始化页面 */
    private void initViews() {
        imageView = findViewById(R.id.image_view);
        btnBack = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.btn_save);

        btnCrop = findViewById(R.id.btn_crop);
        btnRotate = findViewById(R.id.btn_rotate);
        btnOverturn = findViewById(R.id.btn_overturn);
        btnAdjust = findViewById(R.id.btn_adjust);
        btnText = findViewById(R.id.btn_text);
        btnPaster = findViewById(R.id.btn_paster);
    }

    /*** 初始化权限请求启动器 */
    private void initLaunchers() {
        storagePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限授予，执行保存
                        performSave();
                    } else {
                        // 权限被拒绝，显示提示对话框
                        showStoragePermissionDeniedDialog();
                    }
                }
        );
    }

    /*** 加载图片 */
//    private void loadImage() {
//        String uriString = getIntent().getStringExtra("image_uri");
//        if (uriString != null) {
//            imageUri = Uri.parse(uriString);
//            // 使用Glide加载图片
//            Glide.with(this)
//                    .load(imageUri)
//                    .into(imageView);
//        } else {
//            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }
    /**
     * 使用Glide加载图片并转换为Bitmap
     */
    private void loadImage() {
        String uriString = getIntent().getStringExtra("image_uri");
        if (uriString != null) {
            imageUri = Uri.parse(uriString);
            // 使用Glide加载图片并转换为Bitmap
            Glide.with(this)
                    .asBitmap()  // 关键：指定加载为Bitmap
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource,
                                                    @Nullable Transition<? super Bitmap> transition) {
                            // 成功加载，保存Bitmap并显示
                            currentBitmap = resource;
                            imageView.setImageBitmap(currentBitmap);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            // 清理时的处理
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            // 加载失败处理
                            Toast.makeText(EditActivity.this,
                                    "图片加载失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /*** 设置点击监听 */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // 保存按钮 - 完整实现
        btnSave.setOnClickListener(v -> checkStoragePermissionAndSave());

        // 裁剪按钮
        btnCrop.setOnClickListener(v -> {
            Intent intent = new Intent(this, CropActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });

        // 旋转按钮
        btnRotate.setOnClickListener(v -> {
            Intent intent = new Intent(this, RotateActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });

        // 翻转按钮
        btnOverturn.setOnClickListener(v -> {
            Intent intent = new Intent(this, OverturnActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });

        // 调节按钮
        btnAdjust.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdjustActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            // 使用startActivityForResult启动，等待返回结果
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });

        // 文字按钮
        btnText.setOnClickListener(v -> {
            Intent intent = new Intent(this, TextActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });

        // 贴纸按钮
        btnPaster.setOnClickListener(v -> {
            Intent intent = new Intent(this, PasterActivity.class);
            intent.putExtra("image_uri", imageUri.toString());
//            startActivity(intent);
            startActivityForResult(intent, REQUEST_CODE_ADJUST);
        });


    }

    /*** 接收从Activity返回的结果 */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADJUST) {
            if (resultCode == RESULT_OK && data != null) {
                // 获取调节后的图片Uri
                String uriString = data.getStringExtra("new_image_uri");
                if (uriString != null) {
                    Uri newImageUri = Uri.parse(uriString);
                    // 更新当前Uri
                    currentImageUri = newImageUri;
                    imageUri = currentImageUri;
                    // 从Uri加载图片并显示
                    loadImageFromUri(newImageUri);
                    Toast.makeText(this, "已应用", Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "已取消", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*** 从Uri加载图片并显示 */
    private void loadImageFromUri(Uri uri) {
        try {
            // 释放旧的Bitmap
            if (currentBitmap != null && !currentBitmap.isRecycled()) {
                currentBitmap.recycle();
            }

            // 从Uri加载新的Bitmap
            InputStream inputStream = getContentResolver().openInputStream(uri);
            currentBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (currentBitmap != null) {
                // 显示图片
                imageView.setImageBitmap(currentBitmap);
            } else {
                Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
        }
    }


    /*** 检查存储权限并保存 */
    private void checkStoragePermissionAndSave() {
        if (isSaving) {
            Toast.makeText(this, "正在保存中，请稍候...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Android 10 及以上使用 Scoped Storage，不需要申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            performSave();
        } else {
            // Android 9 及以下需要申请存储权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                performSave();
            } else {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    /*** 执行保存操作 */
    private void performSave() {
        isSaving = true;
        btnSave.setEnabled(false); // 禁用按钮防止重复点击
        try {
            // 从 ImageView 获取当前显示的图片
            Bitmap bitmap = getBitmapFromImageView();

            if (bitmap == null) {
                Toast.makeText(this, "保存失败：无法获取图片", Toast.LENGTH_SHORT).show();
                isSaving = false;
                btnSave.setEnabled(true);
                return;
            }
            // 保存图片到相册
            String savedPath = saveBitmapToGallery(bitmap);
            if (savedPath != null) {
                // 保存成功，显示对话框询问是否返回
                showSaveSuccessDialog(savedPath);
            } else {
                // 保存失败
                Toast.makeText(this, "保存失败，请重试", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "图片保存失败");
            }
        } catch (Exception e) {
            Log.e(TAG, "保存图片时发生异常", e);
            handleSaveException(e);
        } finally {
            isSaving = false;
            btnSave.setEnabled(true);
        }
    }

    /*** 显示保存成功对话框 */
    private void showSaveSuccessDialog(String savedPath) {
        new AlertDialog.Builder(this)
                .setTitle("保存成功")
                .setMessage("图片已保存至：" + savedPath)
                .setPositiveButton("返回主页", (dialog, which) -> {
                    finish(); // 返回主页面
                })
                .setNegativeButton("继续编辑", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    /*** 从 ImageView 获取 Bitmap */
    private Bitmap getBitmapFromImageView() {
        try {
            if (imageView.getDrawable() != null) {
                BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                return drawable.getBitmap();
            }
        } catch (Exception e) {
            Log.e(TAG, "获取Bitmap失败", e);
        }
        return null;
    }

    /*** 保存 Bitmap 到相册 */
    private String saveBitmapToGallery(Bitmap bitmap) {
        try {
            // 生成文件名
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String displayName = "IMG_EDIT_" + timeStamp + ".jpg";

            // Android 10 及以上使用 MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return saveBitmapToMediaStore(bitmap, displayName);
            } else {
                // Android 9 及以下保存到公共目录
                return saveBitmapToExternalStorage(bitmap, displayName);
            }

        } catch (Exception e) {
            Log.e(TAG, "保存图片到相册失败", e);
            return null;
        }
    }

    /*** 使用 MediaStore 保存图片 (Android 10+) */
    private String saveBitmapToMediaStore(Bitmap bitmap, String displayName) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ImageEditor");

            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (uri != null) {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.close();
                    return Environment.DIRECTORY_PICTURES + "/ImageEditor/" + displayName;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "MediaStore保存失败", e);
        }
        return null;
    }

    /*** 保存到外部存储 (Android 9-) */
    private String saveBitmapToExternalStorage(Bitmap bitmap, String displayName) {
        try {
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File imageEditorDir = new File(picturesDir, "ImageEditor");

            if (!imageEditorDir.exists()) {
                imageEditorDir.mkdirs();
            }

            File imageFile = new File(imageEditorDir, displayName);
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            // 通知媒体库更新
            MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    imageFile.getAbsolutePath(),
                    displayName,
                    "Edited by Image Editor"
            );

            return imageFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "外部存储保存失败", e);
            return null;
        }
    }

    /*** 处理保存异常 */
    private void handleSaveException(Exception e) {
        String message = "保存失败";

        if (e.getMessage() != null) {
            if (e.getMessage().contains("No space left")) {
                message = "保存失败：存储空间不足，请清理手机存储后重试";
            } else if (e.getMessage().contains("Permission denied")) {
                message = "保存失败：没有存储权限";
            } else {
                message = "保存失败：" + e.getMessage();
            }
        }

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /*** 显示存储权限被拒绝的对话框 */
    private void showStoragePermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("需要存储权限")
                .setMessage("保存图片需要存储权限，是否前往设置打开权限？")
                .setPositiveButton("去设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "无法保存图片：权限未授予", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}