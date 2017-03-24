package imageloader.zzf.com.viewlib.photoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 实现图片各类操作功能
 * Created by Heyha on 2017/3/23.
 */

public class PhotoView extends ImageView {
    private PhotoViewAttacher attacher;


    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        attacher = new PhotoViewAttacher(this);
        super.setScaleType(ScaleType.MATRIX);
    }

    public PhotoViewAttacher getAttacher() {
        return attacher;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        attacher.update();
    }

    /**
     * called in layout when layout changed
     */
    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            attacher.update();
        }
        return changed;
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        attacher.setOnClickListener(l);
    }

    public void setZoomEnabled(boolean zoomEnabled) {
        attacher.setZoomEnabled(zoomEnabled);
    }

    public boolean getZoomEnabled() {
        return attacher.isZoomEnabled();
    }
}
