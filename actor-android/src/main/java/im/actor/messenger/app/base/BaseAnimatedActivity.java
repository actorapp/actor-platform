package im.actor.messenger.app.base;

import android.os.Bundle;
import android.view.ViewTreeObserver;

/**
 * Created by ex3ndr on 25.09.14.
 */
public class BaseAnimatedActivity extends BaseActivity {

    protected static final String ENABLE_ANIMATION = "activity.animation";

    private boolean customAnimation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        customAnimation = getIntent().getBooleanExtra(ENABLE_ANIMATION, false);

        if (savedInstanceState != null) {
            if (customAnimation) {
                final ViewTreeObserver viewTreeObserver = getWindow().getDecorView().getViewTreeObserver();
                viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        viewTreeObserver.removeOnPreDrawListener(this);
                        onOpenAnimationStart();
                        return true;
                    }
                });
            }
        }
    }

    protected void onOpenAnimationStart() {
        
    }

    protected void onCloseAnimationStart() {

    }

    @Override
    public void finish() {
        if (customAnimation) {
            onCloseAnimationStart();
        }
        super.finish();
        if (customAnimation) {
            overridePendingTransition(0, 0);
        }
    }
}
