package imageloader.zzf.com.viewlib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by Heyha on 2017/3/14.
 */

public class ImageLoopFrameLayout extends FrameLayout implements LoopViewGroup.LoopViewGroupListener,LoopViewGroup.LoopViewClickListener {

    private LoopViewGroup loopViewGroup;
    private LinearLayout linearLayout;

    public ImageLoopFrameLayout(Context context) {
        this(context, null);
    }

    public ImageLoopFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageLoopFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLoopViewGroup();
        initLinearLayout();
    }

    private void initLoopViewGroup() {
        loopViewGroup = new LoopViewGroup(getContext());
        LayoutParams lp = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        loopViewGroup.setLayoutParams(lp);
        loopViewGroup.setLoopViewGroupListener(this);
        loopViewGroup.setLoopViewClickListener(this);
        addView(loopViewGroup);
    }

    private void initLinearLayout() {
        linearLayout = new LinearLayout(getContext());
        LayoutParams lp = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 40);
        linearLayout.setLayoutParams(lp);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);

        linearLayout.setBackgroundColor(Color.WHITE);

        addView(linearLayout);
        LayoutParams layoutParams = (LayoutParams) linearLayout.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM;
        linearLayout.setLayoutParams(layoutParams);
        //设置透明度
        linearLayout.setBackgroundColor(Color.TRANSPARENT);  //可以替换成下面的代码
        //Android 3.0以上版本，使用setAlpha()，在3.0以下版本，使用setAlpha()，但是调用者不同
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            linearLayout.setAlpha(0.1f);
//        } else {
//            linearLayout.getBackground().setAlpha(100);
//        }
    }

    public void addBitmaps(List<Bitmap> bitmaps) {
        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap bitmap = bitmaps.get(i);
            addBitmapToLoopViewGroup(bitmap);
            addDotToLinearLayout();
        }
    }

    private void addDotToLinearLayout() {
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(5,5,5,5);
        imageView.setLayoutParams(lp);
        imageView.setImageResource(R.drawable.dot_normal);
        linearLayout.addView(imageView);
    }

    private void addBitmapToLoopViewGroup(Bitmap bitmap) {
        ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.setImageBitmap(bitmap);
        loopViewGroup.addView(imageView);

    }

    @Override
    public void selectImage(int index) {
        int count = linearLayout.getChildCount();
        for (int i = 0; i < count; i++){
            ImageView iv = (ImageView) linearLayout.getChildAt(i);
            if (i == index){
                iv.setImageResource(R.drawable.dot_select);
            }else {
                iv.setImageResource(R.drawable.dot_normal);
            }
        }
    }

    @Override
    public void clickImageIndex(int pos) {
        listener.clickImageIndex(pos);
    }


    private ImageLoopFrameLayoutListener listener;

    public ImageLoopFrameLayoutListener getImageLoopFrameLayoutClickListener() {
        return listener;
    }

    public void setImageLoopFrameLayoutClickListener(ImageLoopFrameLayoutListener listener) {
        this.listener = listener;
    }

    public interface ImageLoopFrameLayoutListener{
        void clickImageIndex(int index);
    }
}
