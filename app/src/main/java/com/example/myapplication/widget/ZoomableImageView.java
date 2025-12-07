package com.example.myapplication.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * 支持手势缩放和平移的ImageView
 * 图片完整显示，保持原始比例，缩放范围：0.5倍-2倍
 */
public class ZoomableImageView extends AppCompatImageView {

    private static final float MIN_SCALE = 0.5f;  // 最小缩放比例
    private static final float MAX_SCALE = 2.0f;  // 最大缩放比例

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    // 缩放相关
    private ScaleGestureDetector scaleDetector;
    private float currentScale = 1f;
    private float initScale = 1f; // 初始缩放比例（使图片完整显示）

    // 平移相关
    private PointF lastPoint = new PointF();
    private boolean isDragging = false;

    // 图片和视图尺寸
    private RectF imageRect = new RectF();
    private boolean isInitialized = false;

    public ZoomableImageView(Context context) {
        super(context);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setScaleType(ScaleType.MATRIX);
        scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        isInitialized = false;
        post(() -> initImageMatrix());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (!isInitialized && getDrawable() != null) {
            initImageMatrix();
        }
    }

    /**
     * 初始化图片矩阵，使图片完整显示在屏幕内（保持原始比例）
     */
    private void initImageMatrix() {
        Drawable drawable = getDrawable();
        if (drawable == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        int imageWidth = drawable.getIntrinsicWidth();
        int imageHeight = drawable.getIntrinsicHeight();
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (imageWidth <= 0 || imageHeight <= 0) {
            return;
        }

        matrix.reset();

        // 计算缩放比例，使图片完整显示（适配屏幕）
        float scaleX = (float) viewWidth / imageWidth;
        float scaleY = (float) viewHeight / imageHeight;

        // 取较小的缩放比例，确保图片完整显示
        initScale = Math.min(scaleX, scaleY);

        // 计算图片居中位置
        float scaledWidth = imageWidth * initScale;
        float scaledHeight = imageHeight * initScale;
        float dx = (viewWidth - scaledWidth) / 2f;
        float dy = (viewHeight - scaledHeight) / 2f;

        // 设置缩放和平移
        matrix.postScale(initScale, initScale);
        matrix.postTranslate(dx, dy);

        // 保存初始图片区域
        imageRect.set(0, 0, imageWidth, imageHeight);

        currentScale = 1f;
        setImageMatrix(matrix);
        isInitialized = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getDrawable() == null || !isInitialized) {
            return super.onTouchEvent(event);
        }

        // 处理缩放手势
        scaleDetector.onTouchEvent(event);

        // 处理平移手势
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                lastPoint.set(event.getX(), event.getY());
                isDragging = true;
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && event.getPointerCount() == 1 && !scaleDetector.isInProgress()) {
                    float dx = event.getX() - lastPoint.x;
                    float dy = event.getY() - lastPoint.y;

                    matrix.set(savedMatrix);
                    matrix.postTranslate(dx, dy);
                    checkBoundsAndCenter();
                    setImageMatrix(matrix);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }

        return true;
    }

    /**
     * 缩放手势监听器
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            savedMatrix.set(matrix);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            float newScale = currentScale * scaleFactor;

            // 限制缩放范围
            if (newScale < MIN_SCALE) {
                scaleFactor = MIN_SCALE / currentScale;
                newScale = MIN_SCALE;
            } else if (newScale > MAX_SCALE) {
                scaleFactor = MAX_SCALE / currentScale;
                newScale = MAX_SCALE;
            }

            matrix.set(savedMatrix);
            matrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            currentScale = newScale;

            checkBoundsAndCenter();
            setImageMatrix(matrix);
            return true;
        }
    }

    /**
     * 检查边界并居中显示
     */
    private void checkBoundsAndCenter() {
        RectF rect = getDisplayRect();
        if (rect == null) return;

        float deltaX = 0;
        float deltaY = 0;

        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // 宽度方向的边界处理
        if (rect.width() <= viewWidth) {
            // 图片宽度小于等于屏幕宽度，居中显示
            deltaX = viewWidth / 2f - rect.left - rect.width() / 2f;
        } else {
            // 图片宽度大于屏幕宽度，限制边界
            if (rect.left > 0) {
                deltaX = -rect.left;
            } else if (rect.right < viewWidth) {
                deltaX = viewWidth - rect.right;
            }
        }

        // 高度方向的边界处理
        if (rect.height() <= viewHeight) {
            // 图片高度小于等于屏幕高度，居中显示
            deltaY = viewHeight / 2f - rect.top - rect.height() / 2f;
        } else {
            // 图片高度大于屏幕高度，限制边界
            if (rect.top > 0) {
                deltaY = -rect.top;
            } else if (rect.bottom < viewHeight) {
                deltaY = viewHeight - rect.bottom;
            }
        }

        matrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 获取当前图片的显示区域
     */
    private RectF getDisplayRect() {
        Drawable drawable = getDrawable();
        if (drawable == null) return null;

        RectF rect = new RectF();
        rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        matrix.mapRect(rect);
        return rect;
    }

    /**
     * 重置缩放和位置
     */
    public void resetZoom() {
        isInitialized = false;
        initImageMatrix();
    }

    /**
     * 获取当前缩放倍数（相对于完整显示时的大小）
     */
    public float getCurrentScale() {
        return currentScale;
    }

    /**
     * 获取当前实际缩放倍数（相对于原始图片）
     */
    public float getActualScale() {
        return initScale * currentScale;
    }
}