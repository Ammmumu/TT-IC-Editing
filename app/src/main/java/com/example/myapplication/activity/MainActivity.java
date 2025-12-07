package com.example.myapplication.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.myapplication.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button btnAlbumSelection; // 相册选取按钮
    private Button btnTakePhoto; // 相机拍照按钮
    private ImageView imageView; // 用于显示选中的图片
    private Uri currentPhotoUri; // 保存拍照的图片URI
    private ActivityResultLauncher<Intent> albumLauncher; // 相册选择器
    private ActivityResultLauncher<Uri> cameraLauncher; // 相机启动器
    private ActivityResultLauncher<String> albumPermissionLauncher; // 相册权限请求启动器
    private ActivityResultLauncher<String> cameraPermissionLauncher; // 相机权限请求启动器


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initLaunchers();
        setupClickListeners();
    }

    private void initViews() {
        btnAlbumSelection = findViewById(R.id.button_album_selection);
        btnTakePhoto = findViewById(R.id.button_take_photo);
        // imageView = findViewById(R.id.imageView); // 如果有ImageView用于预览
    }

    /*** 初始化各种启动器 */
    private void initLaunchers() {
        // 相册选择启动器
        albumLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // 相册选择成功
                            handleSelectedImage(selectedImageUri);
                        } else {
                            Toast.makeText(this, "未选择图片", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        // 相机拍照启动器
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && currentPhotoUri != null) {
                        // 拍照成功
                        handleSelectedImage(currentPhotoUri);
                    } else {
                        Toast.makeText(this, "拍照取消或失败", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 相册权限请求启动器
        albumPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限授予，打开相册
                        openAlbum();
                    } else {
                        // 权限被拒绝
                        showPermissionDeniedDialog("相册");
                    }
                }
        );

        // 相机权限请求启动器
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限授予，检查设备是否有相机
                        if (hasCamera()) {
                            openCamera();
                        } else {
                            Toast.makeText(this, "设备无相机功能", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 权限被拒绝
                        showPermissionDeniedDialog("相机");
                    }
                }
        );
    }

    /*** 设置按钮点击监听 */
    private void setupClickListeners() {
        btnAlbumSelection.setOnClickListener(v -> checkAlbumPermissionAndOpen());
        btnTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
    }

    /*** 检查相册权限并打开相册 */
    private void checkAlbumPermissionAndOpen() {
        String permission = getAlbumPermission();

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，直接打开相册
            openAlbum();
        } else {
            // 请求权限
            albumPermissionLauncher.launch(permission);
        }
    }

    /*** 检查相机权限并打开相机 */
    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // 已有权限，检查设备是否有相机
            if (hasCamera()) {
                openCamera();
            } else {
                Toast.makeText(this, "设备无相机功能", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 请求权限
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /*** 获取相册权限（适配不同Android版本） */
    private String getAlbumPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return Manifest.permission.READ_MEDIA_IMAGES;
        } else {
            return Manifest.permission.READ_EXTERNAL_STORAGE;
        }
    }

    /*** 检查设备是否有相机 */
    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    /*** 打开相册 */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        albumLauncher.launch(intent);
    }

    /*** 打开相机 */
    private void openCamera() {
        try {
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(
                        this,
                        getApplicationContext().getPackageName() + ".fileprovider",
                        photoFile
                );
                cameraLauncher.launch(currentPhotoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "创建照片文件失败", Toast.LENGTH_SHORT).show();
        }
    }

    /*** 创建图片文件 */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /*** 处理选中的图片 */
    private void handleSelectedImage(Uri imageUri) {
        // 这里处理选中的图片
        Toast.makeText(this, "图片选择成功: " + imageUri.toString(), Toast.LENGTH_SHORT).show();
        // 将图片传递到编辑界面
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra("image_uri", imageUri.toString());
        startActivity(intent);
    }

    /*** 显示权限被拒绝的对话框 */
    private void showPermissionDeniedDialog(String permissionType) {
        new AlertDialog.Builder(this)
                .setTitle("权限未授予")
                .setMessage(permissionType + "权限未打开，是否前往设置打开权限？")
                .setPositiveButton("去设置", (dialog, which) -> {
                    // 跳转到应用设置页面
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    dialog.dismiss();
                    Toast.makeText(this, "无法使用" + permissionType + "功能", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}
