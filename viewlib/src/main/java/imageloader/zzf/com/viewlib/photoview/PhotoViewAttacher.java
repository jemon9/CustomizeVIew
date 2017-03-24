package imageloader.zzf.com.viewlib.photoview;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.OverScroller;

/**
 * Created by Heyha on 2017/3/23.
 */

public class PhotoViewAttacher implements View.OnTouchListener, View.OnLayoutChangeListener {

    private static final String TAG = "PhotoViewAttacher";

    private ImageView mImageView;
    private static float DEFAULT_MIN_SCALE = 1.0f;
    private static float DEFAULT_MAX_SCALE = 3.0f;
    private static float DEFAULT_MID_SCALE = 1.75f;
    private static int DEFAULT_INTERPOLATOR_TIME = 200;
    private static final int SIXTY_FPS_INTERVAL = 1000 / 60;
    //create gesture detector
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    //listener
    private View.OnLongClickListener mOnLongClickListener;
    private View.OnClickListener mOnClickListener;

    //matrix
    private Matrix mBaseMatrix = new Matrix();   //base
    private Matrix mDrawMatrix = new Matrix();   //final matrix;   mDrawMatrix == mBaseMatrix * mSuppMatrix
    private Matrix mSuppMatrix = new Matrix();   //translate process matrix
    private RectF mDisplayRect = new RectF();    //display rect region
    private float mMatrixValues[] = new float[9];

    //simple
    private boolean mZoomEnabled = true; //判断是否允许缩放
    private float mMaxScale = DEFAULT_MAX_SCALE;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mScaleInterpolatorTime = DEFAULT_INTERPOLATOR_TIME;
    private FlingRunnable flingRunnable;


    public PhotoViewAttacher(final ImageView imageView) {
        this.mImageView = imageView;
        mImageView.setOnTouchListener(this);
        mImageView.addOnLayoutChangeListener(this);
        if (imageView.isInEditMode()) {
            return;
        }

        //只有当双击时才返回true，在ontouch()中会进行判断返回结果，来区别是双击还是缩放
        mGestureDetector = new GestureDetector(imageView.getContext(), simpleOnGestureListener);

        mScaleGestureDetector = new ScaleGestureDetector(imageView.getContext(), scaleGestureListener);
    }

    //do scroll when scaleGestureDetector is not in progress
    private void doScroll(float distanceX, float distanceY) {
        if (mScaleGestureDetector.isInProgress()) {
            return;
        }
        mSuppMatrix.postTranslate(-distanceX, -distanceY);
        checkAndDisplayMatrix();
    }

    //do fling
    private void doFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        flingRunnable = new FlingRunnable(mImageView.getContext());
        flingRunnable.fling(getImageViewWidth(mImageView), getImageViewHeight(mImageView), velocityX, velocityY);
        mImageView.post(flingRunnable);
    }

    //双击进行放大缩小
    private void doDoubleTap(MotionEvent e) {
        float x = e.getX();
        float y = e.getY();
        float scale = getScale();

        if (scale < mMidScale) {
            setScale(mMidScale, x, y, true);
        } else if (scale >= mMidScale && scale < mMaxScale) {
            setScale(mMaxScale, x, y, true);
        } else {
            setScale(mMinScale, x, y, true);
        }
    }

    private void setScale(float scale, float focusX, float focusY, boolean animator) {
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must range from minScale to maxScale");
        }
        if (animator) {
            mImageView.post(new AnimateScaleRunnable(getScale(), scale, focusX, focusY));
        } else {
            mSuppMatrix.setScale(scale, scale, focusX, focusY);
            checkAndDisplayMatrix();
        }
    }

    //根据给定的缩放因子和焦点坐标进行缩放,双指进行缩放时调用
    public void doScale(float scaleFactor, float focusX, float focusY) {
        if ((getScale() < mMaxScale || scaleFactor < 1.0f) && (getScale() > mMinScale || scaleFactor > 1.0f)) {
            mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
            checkAndDisplayMatrix();
        }

    }

    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    private boolean checkMatrixBounds() {
        RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        float width = rect.width(), height = rect.height();
        float deltaX = 0, deltaY = 0;

        float viewWidth = getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
        } else if (rect.left > 0) {
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
        }

        float viewHeight = getImageViewHeight(mImageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    public void update() {
        if (mZoomEnabled) {
            updateBaseMatrix(mImageView.getDrawable());
        } else {
            resetMatrix();
        }
    }

    //reset matrix back to fit_center and display its content
    private void resetMatrix() {
        mSuppMatrix.reset();
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    //calculate matrix for fit_center
    private void updateBaseMatrix(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        float viewHeight = getImageViewHeight(mImageView);
        float viewWidth = getImageViewWidth(mImageView);
        float drawableHeigth = drawable.getIntrinsicHeight();
        float drawableWidth = drawable.getIntrinsicWidth();
        mBaseMatrix.reset();
        float widthScale = viewWidth / drawableWidth;
        float heightScale = viewHeight / drawableHeigth;
        if (mScaleType == ImageView.ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2f, (viewHeight - drawableHeigth) / 2f);
        } else if (mScaleType == ImageView.ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeigth * scale) / 2f);
        } else if (mScaleType == ImageView.ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2f, (viewHeight - drawableHeigth * scale) / 2f);
        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeigth);
            RectF mTempDes = new RectF(0, 0, viewWidth, viewHeight);
            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDes, Matrix.ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDes, Matrix.ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDes, Matrix.ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDes, Matrix.ScaleToFit.FILL);
                    break;
            }
        }
        resetMatrix();
    }


    private float getImageViewWidth(ImageView mImageView) {
        return mImageView.getWidth() - mImageView.getPaddingLeft() - mImageView.getPaddingRight();
    }

    private float getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private void setImageViewMatrix(Matrix drawMatrix) {
        mImageView.setImageMatrix(drawMatrix);
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    private RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    private RectF getDisplayRect(Matrix matrix) {
        Drawable drawable = mImageView.getDrawable();
        if (drawable != null) {
            mDisplayRect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    //sqrt(scaleX * scaleX + scaleY * scaleY)
    private float getScale() {
        return (float) Math.sqrt(Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    //get values from Matrix values; get scaleX and ScaleY
    private double getValue(Matrix mSuppMatrix, int mscale) {
        mSuppMatrix.getValues(mMatrixValues);
        return mMatrixValues[mscale];
    }

    public void setOnLongClickListener(View.OnLongClickListener mOnLongClickListener) {
        this.mOnLongClickListener = mOnLongClickListener;
    }

    public void setOnClickListener(View.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public boolean isZoomEnabled() {
        return mZoomEnabled;
    }

    public void setZoomEnabled(boolean mZoomEnabled) {
        this.mZoomEnabled = mZoomEnabled;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        setImageViewMatrix(getDrawMatrix());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean handled = false;
        //check if support zoom and Imageview has drawable
        if (mZoomEnabled && Utils.hasDrawable(mImageView)) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //first,disable parent to intercept touch event
                    ViewParent parent = (ViewParent) v.getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    //then,if we are fling,when action_down,cancle fling
                    cancleFling();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //if current zoomed less than min scale,zoom back to min scale
                    // TODO: 2017/3/23 action_up
                    if (getScale() < mMinScale) {
                        RectF rectF = getDisplayRect();
                        if (rectF != null) {
                            AnimateScaleRunnable animateScaleRunnable = new AnimateScaleRunnable(getScale(), mMinScale, rectF.centerX(), rectF.centerY());
                            v.post(animateScaleRunnable);
                        }
                    }
                    break;
            }
            //try the scale/drag detector
            if (mScaleGestureDetector != null) {
                handled = mScaleGestureDetector.onTouchEvent(event);
            }

            //check to see if the user double tapped
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(event)) {
                handled = true;
            }
        }
        return handled;
    }

    //如果现在有惯性滑动，强制取消
    private void cancleFling() {
        if (flingRunnable != null) {
            flingRunnable.cancleFling();
        }
    }

    private ScaleGestureDetector.OnScaleGestureListener scaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            Log.i(TAG, "onScale");
            float scaleFactor = detector.getScaleFactor();
            if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                return false;
            }
            doScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

        }
    };
    private GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public void onLongPress(MotionEvent e) {
            if (mOnLongClickListener != null) {
                mOnLongClickListener.onLongClick(mImageView);
            }
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            doScroll(distanceX, distanceY);
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            doFling(e1, e2, velocityX, velocityY);
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            doDoubleTap(e);
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mImageView);
            }
            return false;
        }
    };

    private class AnimateScaleRunnable implements Runnable {

        private float currentScale, targetScale;
        private float focusX, focusY;
        private long mStartTime;

        public AnimateScaleRunnable(float currentScale, float targetScale, float focusX, float focusY) {
            this.currentScale = currentScale;
            this.targetScale = targetScale;
            this.focusX = focusX;
            this.focusY = focusY;
            mStartTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            float t = interpolator();
            float scale = currentScale + t * (targetScale - currentScale);
            float deltaScale = scale / getScale();
            doScale(deltaScale, focusX, focusY);
            if (t < 1.0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mImageView.postOnAnimation(this);
                } else {
                    mImageView.postDelayed(this, SIXTY_FPS_INTERVAL);
                }
            }
        }

        private float interpolator() {
            float t = 1.0f * (System.currentTimeMillis() - mStartTime) / mScaleInterpolatorTime;
            t = Math.min(t, 1.0f);
            return mInterpolator.getInterpolation(t);
        }
    }

    private class FlingRunnable implements Runnable {
        private OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancleFling() {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
        }

        public void fling(float viewWidth, float viewHeight, float velocityX, float velocityY) {
            RectF rectF = getDisplayRect();
            if (rectF == null) {
                return;
            }
            int startX = Math.round(-rectF.left);
            int minX, maxX, minY, maxY;
            if (viewWidth < rectF.width()) {
                minX = 0;
                maxX = Math.round(rectF.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            int startY = Math.round(-rectF.top);
            if (viewHeight < rectF.height()) {
                minY = 0;
                maxY = Math.round(rectF.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, Math.round(velocityX), Math.round(velocityY), minX, maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return;
            }
            if (mScroller.computeScrollOffset()) {
                int newX = mScroller.getCurrX();
                int newY = mScroller.getCurrY();
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                checkAndDisplayMatrix();
                mCurrentX = newX;
                mCurrentY = newY;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mImageView.postOnAnimation(this);
            } else {
                mImageView.postDelayed(this, SIXTY_FPS_INTERVAL);
            }
        }
    }

}
