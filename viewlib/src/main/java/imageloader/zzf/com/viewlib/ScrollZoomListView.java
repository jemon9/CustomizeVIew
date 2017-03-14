package imageloader.zzf.com.viewlib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * 头部带图片的ListView，图片可以放大缩小
 * Created by Heyha on 2017/2/24.
 */
public class ScrollZoomListView extends ListView {

    private ImageView mHeader;
    private int headerHeight;

    public ScrollZoomListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollZoomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        headerHeight = context.getResources().getDimensionPixelSize(R.dimen.header_height_default);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        if (deltaY < 0) {
            mHeader.getLayoutParams().height = mHeader.getHeight() - deltaY;
            mHeader.requestLayout();
        } else {
            if (mHeader.getHeight() > deltaY) {
                mHeader.getLayoutParams().height = mHeader.getHeight() - deltaY;
                mHeader.requestLayout();
            }
        }
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        View parent = (View) mHeader.getParent();
        int deltaY = parent.getTop();
        if (mHeader.getHeight() > headerHeight) {
            mHeader.getLayoutParams().height = mHeader.getHeight() + deltaY;
            parent.layout(parent.getLeft(), 0, parent.getRight(), parent.getHeight());
            mHeader.requestLayout();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (mHeader.getHeight() > headerHeight) {
                ResetAnimation animation = new ResetAnimation(headerHeight);
                animation.setDuration(300);
                mHeader.startAnimation(animation);
            }
        }
        return super.onTouchEvent(ev);
    }

    private class ResetAnimation extends Animation {
        private int extraHeight;
        private int currentHeight;

        public ResetAnimation(int targetHeight) {
            extraHeight = mHeader.getHeight() - targetHeight;
            currentHeight = mHeader.getHeight();
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mHeader.getLayoutParams().height = (int) (currentHeight - extraHeight * interpolatedTime);
            mHeader.requestLayout();
        }
    }

    public void setZoomImage(ImageView headerImage) {
        mHeader = headerImage;
    }
}
