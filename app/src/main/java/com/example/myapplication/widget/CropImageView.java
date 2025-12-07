package com.example.myapplication.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CropImageView extends ZoomableImageView {

    private RectF cropRect;
    private Paint cropPaint;
    private Paint maskPaint;
    private Paint handlePaint;
    // 拖拽相关变量
    private boolean isDragging = false;
    private boolean isResizing = false;
    private int activeHandle = -1; // -1: 无, 0-3: 四个角, 4: 整体拖动
    private float lastTouchX, lastTouchY;
    private float handleRadius = 25f;
    // 最小裁剪框尺寸
    private float minCropSize = 100f;
    // 添加比例相关变量
    private float fixedRatio = 0f; // 0表示自由裁剪，其他值表示固定比例

    public CropImageView(Context context) {
        super(context);
        init();
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // 裁剪框画笔
        cropPaint = new Paint();
        cropPaint.setColor(Color.WHITE);
        cropPaint.setStyle(Paint.Style.STROKE);
        cropPaint.setStrokeWidth(4f);
        cropPaint.setAntiAlias(true);

        // 遮罩画笔
        maskPaint = new Paint();
        maskPaint.setColor(0x88000000); // 半透明黑色
        maskPaint.setStyle(Paint.Style.FILL);

        // 控制点画笔
        handlePaint = new Paint();
        handlePaint.setColor(Color.WHITE);
        handlePaint.setStyle(Paint.Style.FILL);
        handlePaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (cropRect != null && getDrawable() != null) {
            // 绘制遮罩（裁剪区域外的半透明覆盖）
            drawMask(canvas);

            // 绘制裁剪框边框
            canvas.drawRect(cropRect, cropPaint);

            // 绘制四个角的控制点
            drawCornerHandles(canvas);
        }
    }

    private void drawMask(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        // 上方遮罩
        canvas.drawRect(0, 0, width, cropRect.top, maskPaint);
        // 下方遮罩
        canvas.drawRect(0, cropRect.bottom, width, height, maskPaint);
        // 左侧遮罩
        canvas.drawRect(0, cropRect.top, cropRect.left, cropRect.bottom, maskPaint);
        // 右侧遮罩
        canvas.drawRect(cropRect.right, cropRect.top, width, cropRect.bottom, maskPaint);
    }

    private void drawCornerHandles(Canvas canvas) {
        // 四个角的控制点
        canvas.drawCircle(cropRect.left, cropRect.top, handleRadius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, handleRadius, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleRadius, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleRadius, handlePaint);

        // 绘制控制点边框
        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);
        borderPaint.setAntiAlias(true);

        canvas.drawCircle(cropRect.left, cropRect.top, handleRadius, borderPaint);
        canvas.drawCircle(cropRect.right, cropRect.top, handleRadius, borderPaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, handleRadius, borderPaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, handleRadius, borderPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (cropRect == null) {
            return super.onTouchEvent(event);
        }

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;

                // 检查是否触摸到控制点
                activeHandle = getActiveHandle(x, y);

                if (activeHandle >= 0) {
                    if (activeHandle < 4) {
                        isResizing = true;
                    } else {
                        isDragging = true;
                    }
                    return true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isResizing || isDragging) {
                    float deltaX = x - lastTouchX;
                    float deltaY = y - lastTouchY;

                    if (isResizing) {
                        // 调整裁剪框大小
                        resizeCropRect(activeHandle, deltaX, deltaY);
                    } else if (isDragging) {
                        // 移动整个裁剪框
                        moveCropRect(deltaX, deltaY);
                    }

                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
                isDragging = false;
                isResizing = false;
                activeHandle = -1;
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 获取当前触摸的控制点
     * @return -1: 无, 0: 左上, 1: 右上, 2: 左下, 3: 右下, 4: 裁剪框内部
     */
    private int getActiveHandle(float x, float y) {
        float touchRadius = handleRadius + 10f; // 增加触摸范围

        // 检查四个角的控制点
        if (isPointInCircle(x, y, cropRect.left, cropRect.top, touchRadius)) {
            return 0; // 左上角
        }
        if (isPointInCircle(x, y, cropRect.right, cropRect.top, touchRadius)) {
            return 1; // 右上角
        }
        if (isPointInCircle(x, y, cropRect.left, cropRect.bottom, touchRadius)) {
            return 2; // 左下角
        }
        if (isPointInCircle(x, y, cropRect.right, cropRect.bottom, touchRadius)) {
            return 3; // 右下角
        }

        // 检查是否在裁剪框内部（用于拖动）
        if (cropRect.contains(x, y)) {
            return 4; // 内部拖动
        }

        return -1; // 没有触摸到任何控制区域
    }

    private boolean isPointInCircle(float x, float y, float centerX, float centerY, float radius) {
        float distance = (float) Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        return distance <= radius;
    }

    /**
     * 设置固定裁剪比例
     * @param ratio 宽高比，0表示自由裁剪
     */
    public void setFixedRatio(float ratio) {
        this.fixedRatio = ratio;
    }

    /**
     * 调整裁剪框大小（保持固定比例）
     */
    private void resizeCropRect(int handle, float deltaX, float deltaY) {
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;

        if (fixedRatio == 0f) {
            // 自由裁剪模式，原有逻辑保持不变
            resizeFreeForm(handle, deltaX, deltaY);
        } else {
            // 固定比例模式
            resizeWithFixedRatio(handle, deltaX, deltaY);
        }
    }

    /**
     * 自由裁剪模式的调整
     */
    private void resizeFreeForm(int handle, float deltaX, float deltaY) {
        float left = cropRect.left;
        float top = cropRect.top;
        float right = cropRect.right;
        float bottom = cropRect.bottom;

        switch (handle) {
            case 0: // 左上角
                left = Math.min(left + deltaX, right - minCropSize);
                top = Math.min(top + deltaY, bottom - minCropSize);
                break;
            case 1: // 右上角
                right = Math.max(right + deltaX, left + minCropSize);
                top = Math.min(top + deltaY, bottom - minCropSize);
                break;
            case 2: // 左下角
                left = Math.min(left + deltaX, right - minCropSize);
                bottom = Math.max(bottom + deltaY, top + minCropSize);
                break;
            case 3: // 右下角
                right = Math.max(right + deltaX, left + minCropSize);
                bottom = Math.max(bottom + deltaY, top + minCropSize);
                break;
        }

        // 限制在View范围内
        left = Math.max(0, left);
        top = Math.max(0, top);
        right = Math.min(getWidth(), right);
        bottom = Math.min(getHeight(), bottom);

        cropRect.set(left, top, right, bottom);
    }

    /**
     * 固定比例模式的调整
     */
    private void resizeWithFixedRatio(int handle, float deltaX, float deltaY) {
        float centerX = cropRect.centerX();
        float centerY = cropRect.centerY();

        // 计算当前尺寸变化量
        float sizeDelta = 0f;

        switch (handle) {
            case 0: // 左上角 - 向内缩小或向外扩大
                sizeDelta = -Math.max(deltaX, deltaY);
                break;
            case 1: // 右上角
                sizeDelta = Math.max(deltaX, -deltaY);
                break;
            case 2: // 左下角
                sizeDelta = Math.max(-deltaX, deltaY);
                break;
            case 3: // 右下角 - 向外扩大或向内缩小
                sizeDelta = Math.max(deltaX, deltaY);
                break;
        }

        // 计算新的尺寸（保持固定比例）
        float currentWidth = cropRect.width();
        float currentHeight = cropRect.height();

        float newWidth = currentWidth + sizeDelta;
        float newHeight = newWidth / fixedRatio;

        // 确保最小尺寸
        if (newWidth < minCropSize || newHeight < minCropSize) {
            if (fixedRatio >= 1.0f) {
                newWidth = minCropSize;
                newHeight = minCropSize / fixedRatio;
            } else {
                newHeight = minCropSize;
                newWidth = minCropSize * fixedRatio;
            }
        }

        // 计算新的边界
        float newLeft = centerX - newWidth / 2f;
        float newTop = centerY - newHeight / 2f;
        float newRight = centerX + newWidth / 2f;
        float newBottom = centerY + newHeight / 2f;

        // 检查是否超出View边界，如果超出则按比例缩小
        float maxWidth = getWidth();
        float maxHeight = getHeight();

        if (newLeft < 0 || newRight > maxWidth || newTop < 0 || newBottom > maxHeight) {
            // 计算可用的最大尺寸
            float availableWidth = Math.min(maxWidth, Math.min(centerX * 2, (maxWidth - centerX) * 2));
            float availableHeight = Math.min(maxHeight, Math.min(centerY * 2, (maxHeight - centerY) * 2));

            if (fixedRatio >= 1.0f) {
                // 宽度优先
                newWidth = Math.min(availableWidth, availableHeight * fixedRatio);
                newHeight = newWidth / fixedRatio;
            } else {
                // 高度优先
                newHeight = Math.min(availableHeight, availableWidth / fixedRatio);
                newWidth = newHeight * fixedRatio;
            }

            // 重新计算边界
            newLeft = centerX - newWidth / 2f;
            newTop = centerY - newHeight / 2f;
            newRight = centerX + newWidth / 2f;
            newBottom = centerY + newHeight / 2f;
        }

        // 确保完全在边界内
        newLeft = Math.max(0, newLeft);
        newTop = Math.max(0, newTop);
        newRight = Math.min(maxWidth, newRight);
        newBottom = Math.min(maxHeight, newBottom);

        cropRect.set(newLeft, newTop, newRight, newBottom);
    }

    /**
     * 移动整个裁剪框
     */
    private void moveCropRect(float deltaX, float deltaY) {
        float newLeft = cropRect.left + deltaX;
        float newTop = cropRect.top + deltaY;
        float newRight = cropRect.right + deltaX;
        float newBottom = cropRect.bottom + deltaY;

        // 限制在View范围内
        if (newLeft < 0) {
            deltaX = -cropRect.left;
        } else if (newRight > getWidth()) {
            deltaX = getWidth() - cropRect.right;
        }

        if (newTop < 0) {
            deltaY = -cropRect.top;
        } else if (newBottom > getHeight()) {
            deltaY = getHeight() - cropRect.bottom;
        }

        cropRect.offset(deltaX, deltaY);
    }

    public void setCropRect(RectF cropRect) {
        this.cropRect = new RectF(cropRect);
        invalidate();
    }

    public RectF getCropRect() {
        return cropRect;
    }
}