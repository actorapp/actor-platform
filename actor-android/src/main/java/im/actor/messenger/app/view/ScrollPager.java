package im.actor.messenger.app.view;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.view.animation.OvershootInterpolator;
import android.widget.ScrollView;
import android.widget.Scroller;

public class ScrollPager implements OnTouchListener {
    // The class encapsulates scrolling.(Overshoot)
    private Scroller scroller;
    // The task make scroll view scrolled.
    private Runnable task;

    private VelocityTracker velocityTracker;

    private ScrollView mScrollView;
    private ViewGroup mContentView;

    public ScrollPager(ScrollView aScrollView, ViewGroup aContentView) {
        mScrollView = aScrollView;
        mContentView = aContentView;
        scroller = new Scroller(mScrollView.getContext(), new OvershootInterpolator(1.0f));
        // scroller = new Scroller(mScrollView.getContext());
        task = new Runnable() {
            @Override
            public void run() {
                scroller.computeScrollOffset();
                mScrollView.scrollTo(0, scroller.getCurrY());

                if (!scroller.isFinished()) {
                    mScrollView.post(this);
                }
            }
        };
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // Stop scrolling calculation.
        scroller.forceFinished(true);
        // Stop scrolling animation.
        mScrollView.removeCallbacks(task);
        if (velocityTracker != null) {
            velocityTracker.addMovement(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (velocityTracker != null) {
                velocityTracker.clear();
            } else {
                velocityTracker = VelocityTracker.obtain();
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // The height of scroll view, in pixels
            int displayHeight = mScrollView.getHeight();
            // The top of content view, in pixels.
            int contentTop = mContentView.getPaddingTop();
            // The top of content view, in pixels.
            int contentBottom = mContentView.getHeight() - mContentView.getPaddingBottom();
            // The top of last page, in pixels.
            int lastPageTop = contentBottom - displayHeight;

            // The scrolled top position of scroll view, in pixels.
            int currScrollY = mScrollView.getScrollY();
            // The scrolled middle position of scroll view, in pixels.
            int currScrollMiddleY = currScrollY + displayHeight / 2 - contentTop;

            // Current page num.
            int currPage = currScrollMiddleY / displayHeight;


            // Next page num.
            int nextPage = currPage;

            if (velocityTracker != null) {
                velocityTracker.computeCurrentVelocity(1000);
                if (velocityTracker.getYVelocity() > (mScrollView.getContext().getResources().getDisplayMetrics().density * 100)) {
                    nextPage--;
                } else if (velocityTracker.getYVelocity() < -(mScrollView.getContext().getResources().getDisplayMetrics().density * 100)) {
                    nextPage++;
                }
            }

            scrollToPage(nextPage);

            // consume(to stop fling)
            return true;
        }

        return false;
    }

    public void scrollToPage(int nextPage) {

        // The height of scroll view, in pixels
        int displayHeight = mScrollView.getHeight();
        // The top of content view, in pixels.
        int contentTop = mContentView.getPaddingTop();
        // The top of content view, in pixels.
        int contentBottom = mContentView.getHeight() - mContentView.getPaddingBottom();
        // The top of last page, in pixels.
        int lastPageTop = contentBottom - displayHeight;

        // The scrolled top position of scroll view, in pixels.
        int currScrollY = mScrollView.getScrollY();

        // The top of next page, in pixels.
        int nextPageTop = contentTop + nextPage * displayHeight;

        // Start scrolling calculation.
        scroller.startScroll(0, currScrollY, 0, Math.max(Math.min(lastPageTop, nextPageTop), contentTop) - currScrollY, 300);

        // Start animation.
        mScrollView.post(task);

        velocityTracker = null;
    }
}