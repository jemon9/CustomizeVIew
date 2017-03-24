package imageloader.zzf.com.viewlib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 轮播图的核心类；实现顺序：
 * 1、手动轮播功能：onMeasure(),onLayout(),onInterceptTouchEvent(),onTouchEvent(),smoothScrollBy(),computeScroll()
 * 2、自动轮播功能：Timer，TimerTask，Handler，
 * 3、图片的点击事件：LoopViewClickListener，监听传递this-->ImageLoopFramelayout-->used
 * 4、底部轮播圆点布局及切换：
 * Created by Heyha on 2017/3/14.
 */

public class LoopViewGroup extends ViewGroup {
    private int mChildCount = 0;
    private int childMeasureWidth = 0;
    private int childMeasureHeight = 0;
    private Scroller mScroller;
    private int x;  //表示每一次移动之前的横坐标
    private int index = 0;  //轮播图片的索引值
    private int filter = ViewConfiguration.get(getContext()).getScaledTouchSlop();

    private boolean isAuto = true;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int periodTime = 4000; //自动轮播的时间，单位是ms，默认是5000ms
    private Handler autoHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (++index >= mChildCount) { //如果当前图片时最后一张，那么下一张图片是第一张
                        index = 0;
                    }
                    scrollTo(index * childMeasureWidth, 0); //1
                    loopViewGroupListener.selectImage(index); //通知ImageLoopFrameLayout进行底部圆点切换
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 可以通过该方法在外界控制是否自动轮播
     */
    public void startAuto() {
        isAuto = true;
    }

    public void stopAuto() {
        isAuto = false;
    }

    //自动轮播

    /**
     * 采用Timer,TimerTask,Handler三者实现自动轮播
     * 此处需要两个方法控制自动轮播，称之为自动轮播的开关，startAuto(),stopAuto()
     * 那么就需要一个标志来表明当前自动轮播的状态时开启还是关闭，isAuto boolean true表示自动轮播，false表示不自动轮播
     * <p>
     * 点击发生时关闭自动轮播，抬起后启动自动轮播
     *
     * @param context
     */

    //轮播图的点击事件
    /**
     * 要想获得点击事件的获取
     * 采用的方法是利用一个变量进行判断，当用户离开屏幕的一瞬间，判断变量开关来判断是点击事件还是移动事件
     */
    private LoopViewClickListener listener;
    private boolean isClick;  //true表示点击事件，false表示移动事件

    public LoopViewClickListener getLoopViewClickListener() {
        return listener;
    }

    public void setLoopViewClickListener(LoopViewClickListener listener) {
        this.listener = listener;
    }

    public interface LoopViewClickListener {
        void clickImageIndex(int pos);
    }

    //底部轮播圆点实现

    /**
     * 实现底部圆点以及圆点的切换功能的实现步骤：
     * 1、需要自定义一个继承FramLayout的布局，利用FrameLayout的特性（在同一个位置放置不同的View，显示最后放入的View），我们就可以实现底部圆点的布局
     * 2、我们需要准备素材，底部圆点的素材，利用Drawable实现两种效果的图片
     * 3、需要自定义一个类继承FrameLayout，在该类的实现过程中，加载自定义的LooPViewGroup类和我们需要实现的底部圆点的布局LinearLayout来实现
     */
    /**
     * 图片轮播后通知底部圆点切换
     */
    private LoopViewGroupListener loopViewGroupListener;

    public LoopViewGroupListener getLoopViewGroupListener() {
        return loopViewGroupListener;
    }

    public void setLoopViewGroupListener(LoopViewGroupListener loopViewGroupListener) {
        this.loopViewGroupListener = loopViewGroupListener;
    }

    public interface LoopViewGroupListener{
        void selectImage(int index);
    }

    public LoopViewGroup(Context context) {
        this(context, null);
    }

    public LoopViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoopViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (mScroller == null) {
            mScroller = new Scroller(getContext());
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (isAuto) {
                    autoHandler.sendEmptyMessage(0);
                }
            }
        };
        timer.schedule(timerTask, 100, periodTime);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //1、获取子视图的个数
        mChildCount = getChildCount();
        //2、测量子视图的宽高
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        View view = getChildAt(0);
        if (view != null && view.getVisibility() != GONE) {
            childMeasureWidth = view.getMeasuredWidth();
            childMeasureHeight = view.getMeasuredHeight();
        }
        //3、设置测量宽高
        if (mChildCount == 0) {
            setMeasuredDimension(0, 0);
        } else {
            setMeasuredDimension(childMeasureWidth * mChildCount, childMeasureHeight);
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            int marginLeft = 0;
            for (int i = 0; i < mChildCount; i++) {
                View childView = getChildAt(i);
                if (childView != null && childView.getVisibility() != GONE) {
                    childView.layout(marginLeft, t, marginLeft + childMeasureWidth, b);
                    marginLeft += childMeasureWidth;
                }
            }
        }
    }

    /**
     * 事件分发时拦截事件，返回true表示拦截，即该View处理一系列事件，返回false表示不拦截，由子视图去处理事件，
     * 很显然在此应该返回true
     *
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * 1、利用Scroller对象实现手动轮播
     * <p>
     * 第一：我们在滑动图片的过程中，其实就是我们自定义ViewGroup滑动子试图的移动过程，那么只需要知道滑动之前的横坐标和滑动之后的横坐标
     * ，此时可以求得此次移动过程的距离，我们再利用scrollBy方法实现图片的滑动，所以，有两个值需要知道，滑动前的横坐标和滑动后的横坐标；
     * <p>
     * 第二：我们在第一次按下的一瞬间，此时的移动之前的横坐标和移动之后的横坐标是相等的，也就是我们按下的那一点的横坐标；
     * <p>
     * 第三：我们在滑动过程中，是不断的调用ACTION_MOVE方法的，因此我们就应该将移动之前和移动之后的值进行保存，以便我们计算滑动的距离；
     * 此时如果是第一张图片，就不允许再向右滑，如果是最后一张图片，就不允许再向左滑
     * <p>
     * 第四：我们在抬起手的一瞬间，需要计算将要滑动到哪个图片上
     * 此时可以求的将要滑动到那张图片的索引值：（当前ViewGroup的滑动位置 + 我们每张图片的宽度 / 2） / 每张图片的宽度
     * 此时就可以利用Scroller滑动到图片上
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:  //按下的一瞬间
                isClick = true;
                if (!mScroller.isFinished()) {  //如果滑动还未停止，又发生了点击事件，则停止之前的滑动，重新开始
                    mScroller.abortAnimation();
                }
                stopAuto();
                x = (int) event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX = (int) event.getX();
                int distance = moveX - x;
                if (Math.abs(distance) > filter){
                    isClick = false;
                }
                if (distance > 0 && index <= 0) {
                } else if (distance < 0 && index >= mChildCount - 1) {
                } else {
                    scrollBy(-distance, 0);
                    x = moveX;
                }
                break;
            case MotionEvent.ACTION_UP:
                startAuto();
                int scroll = getScrollX();
                index = (scroll + childMeasureWidth / 2) / childMeasureWidth;
                if (index < 0) {
                    index = 0;
                } else if (index > mChildCount - 1) {
                    index = mChildCount - 1;
                } else {
                    if (isClick){
                        listener.clickImageIndex(index);
                    }else {
                        int dx = index * childMeasureWidth - scroll; //需要滑动的距离
                        smoothScrollBy(dx, 0);
                        loopViewGroupListener.selectImage(index);  //通知ImageLoopFrameLayout进行底部圆点切换
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void smoothScrollBy(int dx, int dy) {
        mScroller.startScroll(getScrollX(), 0, dx, dy, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }
}
