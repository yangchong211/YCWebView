package com.ycbjie.webviewlib;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * <pre>
 *     @author  yangchong
 *     email  : yangchong211@163.com
 *     time   : 2020/04/27
 *     desc   : 有赞公共封装父类activity
 *     revise:
 * </pre>
 */
public class AndroidBug5497Workaround {

    /**
     * 优雅解决
     * 具体看https://code.google.com/p/android/issues/detail?id=5497
     *
     * 遇到问题
     * 软键盘是由WebView中的网页元素所触发弹出的，在web页面android软键盘覆盖问题
     *
     * 最简单解决办法
     * 出现坑的条件是：带有WebView的activity使用了全屏模式或者adjustPan模式。
     * 如果activity中有WebView，就不要使用全屏模式，并且把它的windowSoftInputMode值设为adjustResize就好了
     *
     * 如果WebView是全屏模式
     * 1.找到activity的根View，也就是根view，然后获取到第一个Child，也就是setContentView这个布局
     * 2.设置一个Listener监听View树变化，
     * 3.界面变化之后，获取"可用高度"
     * 4.最后一步，重设高度
     */



    public static void assistActivity (Activity activity) {
        new AndroidBug5497Workaround(activity);
    }

    private View mChildOfContent;
    private int usableHeightPrevious;
    private FrameLayout.LayoutParams frameLayoutParams;

    private AndroidBug5497Workaround(Activity activity) {
        FrameLayout content = activity.findViewById(android.R.id.content);
        mChildOfContent = content.getChildAt(0);
        mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                possiblyResizeChildOfContent();
            }
        });
        frameLayoutParams = (FrameLayout.LayoutParams) mChildOfContent.getLayoutParams();
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != usableHeightPrevious) {
            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > (usableHeightSansKeyboard/4)) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightSansKeyboard;
            }
            mChildOfContent.requestLayout();
            usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        mChildOfContent.getWindowVisibleDisplayFrame(r);
        //rect.top其实是状态栏的高度，如果是全屏主题，直接 return rect.bottom就可以了
        return (r.bottom - r.top);// 全屏模式下： return r.bottom
    }


}
