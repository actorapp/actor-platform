package im.actor.tour;

import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.auth.AuthActivity;
import im.actor.sdk.util.Screen;
import im.actor.sdk.util.Fonts;
import im.actor.sdk.view.SelectorFactory;


public class TourActivity extends ActionBarActivity {

    private static final int SIGNIN = 1;
    private static final int SIGNUP = 3;
    private static final int SIGNIN_OAUTH = 4;
    private int lastPageIndex = 3;
    private int contentTopPadding;

    @Override
    protected void onStart() {
        super.onStart();

        ActorSDK.sharedActor().waitForReady();

        String authState = getSharedPreferences("properties.ini", Context.MODE_PRIVATE).getString("auth_state", null);
        if (authState != null && !authState.equals("SIGN_UP") && !authState.equals("AUTH_START")) {
            ActorSDK.sharedActor().startMessagingApp(this);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tour_activity);
        final VerticalViewPager viewPager = (VerticalViewPager) findViewById(R.id.viewpager);

        final View backToTopText = findViewById(R.id.back_to_top);
        final View backToTopArrow = findViewById(R.id.back_to_top_arrow);

        final View paralax = findViewById(R.id.paralax);
        final View paralaxImage = findViewById(R.id.paralax_image);
        final View background = findViewById(R.id.background);

        final View loginHolder = findViewById(R.id.login_holder);
        final View welcomeImage = findViewById(R.id.welcome_logo);
        final View welcomeText = findViewById(R.id.welcome_text);

        background.findViewById(R.id.background_colored).setBackgroundColor(ActorSDK.sharedActor().style.getMainColor());
        TextView signupButtonText = (TextView) findViewById(R.id.signUpButtonText);
        StateListDrawable states = SelectorFactory.get(ActorSDK.sharedActor().style.getPrimaryAltColor(), this);
        signupButtonText.setBackgroundDrawable(states);
        signupButtonText.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        signupButtonText.setTypeface(Fonts.medium());
        TextView buttonSignUp = (TextView) findViewById(R.id.signIn);
        buttonSignUp.setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        buttonSignUp.setTypeface(Fonts.medium());
        ((TextView) findViewById(R.id.app_name)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());
        ((TextView) findViewById(R.id.tour_welcome_subtitle)).setTextColor(ActorSDK.sharedActor().style.getTextPrimaryInvColor());

        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle authExtras = new Bundle();
                authExtras.putInt(AuthActivity.SIGN_TYPE_KEY, AuthActivity.SIGN_TYPE_UP);
                ActorSDK.sharedActor().startAuthActivity(TourActivity.this, authExtras);
                finish();

            }
        });

        findViewById(R.id.signIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle authExtras = new Bundle();
                authExtras.putInt(AuthActivity.SIGN_TYPE_KEY, AuthActivity.SIGN_TYPE_IN);
                ActorSDK.sharedActor().startAuthActivity(TourActivity.this, authExtras);
                finish();

            }
        });

        View.OnClickListener jumpToTopListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToTopArrow.animate().alpha(0).setDuration(0).start();
                backToTopText.animate().alpha(0).setDuration(0).start();
                viewPager.setCurrentItem(0, true);
            }
        };
        backToTopText.setOnClickListener(jumpToTopListener);
        backToTopArrow.setOnClickListener(jumpToTopListener);

        viewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                if (position == 0 || position == lastPageIndex + 1)
                    return new Fragment();

                return TourFragment.getInstance(position);
            }

            @Override
            public int getCount() {
                return 4;//6;
            }
        });


        //Necessary or the pager will only have one extra page to show
        // make this at least however many pages you can see
        viewPager.setOffscreenPageLimit(3);
        //A little space between pages
        viewPager.setPageMargin(-Screen.dp(165));
        //If hardware acceleration is enabled, you should also remove
        // clipping on the pager for its children.
        viewPager.setClipChildren(false);

        viewPager.setOnScrollListener(new ScrollListener() {
            public static final int LOGINHOLDER_STATUS_OUT = 1;
            public static final int LOGINHOLDER_STATUS_VISIBLE = 2;
            public int loginHolderStatus;
            public boolean welcomeShowed = false;
            public boolean mainContentHidden = false;
            public boolean loginHolderBackgroundVisible = false;

            @Override
            public void onScroll(int y, float page) {

                if (page < 1) {
                    float alpha = 1 - page * 2;
                    backToTopArrow.animate().alpha(alpha).setDuration(0).start();
                    //status1.setText("alpha:" + alpha);
                    if (loginHolder.getTop() != 0) {
                        float loginY = (float) loginHolder.getTop() * ((float) 1 - (page));
                        loginHolder.animate().y(loginY).setDuration(0).setStartDelay(0).start();
                        background.animate().y(loginY - background.getHeight() + loginHolder.getHeight() +
                                Screen.dp(8)).setDuration(0).start();
                        /*if(loginHolderBackgroundVisible){
                            loginHolderBackgroundVisible = false;
                            loginHolderBackground.animate().alpha(0).setDuration(0).start();
                        } else {

                        }*/
                        //loginHolderBackground.animate().alpha(page).setDuration(0).start();
                        background.animate().alpha(page).setDuration(0).start();
                        loginHolderStatus = LOGINHOLDER_STATUS_VISIBLE;
                        float welcomeImageY = welcomeImage.getTop() - y / 2;
                        float welcomeTextY = welcomeText.getTop() - y / 2;
                        if (alpha > 0) {
                            welcomeImage.animate()
                                    //.scaleX(alpha/2+0.5f).scaleY(alpha/2+0.5f)
                                    .alpha(alpha).y(welcomeImageY).setDuration(0).start();
                            welcomeText.animate()
                                    //.scaleX(alpha/2+0.5f).scaleY(alpha/2+0.5f)
                                    .alpha(alpha).y(welcomeTextY).setDuration(0).start();
                        } else {
                            welcomeImage.animate().alpha(0).setDuration(0).start();
                            welcomeText.animate().alpha(0).setDuration(0).start();
                        }
                    }
                    mainContentHidden = false;
                } else {
                    if (page >= 1 && page <= lastPageIndex) {
                        // todo freeze?
                        if (!mainContentHidden) {
                            mainContentHidden = true;
                            loginHolderBackgroundVisible = true;
                            //signinLastView.setEnabled(false);
                            //signinView.setEnabled(false);
                            //signupLastView.setEnabled(false);
                            //signupView.setEnabled(false);
                            loginHolder.animate().y(0).setDuration(0).start();
                            //loginHolderBackground.animate().alpha(1).setDuration(0).start();
                            background.animate().y(-loginHolder.getTop() + Screen.dp(8)).alpha(1).setDuration(0).start();

                            welcomeImage.animate().alpha(0).setDuration(0).start();
                            welcomeText.animate().alpha(0).setDuration(0).start();
                            backToTopArrow.animate().alpha(0).setDuration(0).start();
                            backToTopText.animate().alpha(0).setDuration(0).start();
                        }
                        if (page > lastPageIndex - 1) {
                            float alpha = (page - (lastPageIndex - 1)) * 3 - 2;
                            if (alpha > 0) {
                                backToTopText.animate().scaleX(alpha).scaleY(alpha).alpha(alpha).setDuration(0).start();
                            } else {
                                backToTopText.animate().scaleX(0).scaleY(0).alpha(0).setDuration(0).start();

                            }
                        }
                    } else {
                        if (page > lastPageIndex) {
                            float progress = page - lastPageIndex;

                            if (progress > 0.5) {
                                if (loginHolderStatus == LOGINHOLDER_STATUS_VISIBLE) {
                                    loginHolderStatus = LOGINHOLDER_STATUS_OUT;
                                    loginHolder.animate().y(-loginHolder.getHeight()).setDuration(200).setInterpolator(new AccelerateDecelerateInterpolator()).setStartDelay(0).start();
                                }

                                mainContentHidden = false;

                            } else {
                                if (loginHolderStatus == LOGINHOLDER_STATUS_OUT) {
                                    loginHolderStatus = LOGINHOLDER_STATUS_VISIBLE;
                                    loginHolder.animate().y(0).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(200).setStartDelay(0).start();
                                }
                                if (!mainContentHidden) {
                                    backToTopArrow.animate().alpha(0).setDuration(0).start();
                                    backToTopText.animate().alpha(0).setDuration(0).start();
                                }
                            }
                        }
                    }
                }

                int paralaxHolderHeight = paralax.getHeight();
                int paralaxImageHeight = paralaxImage.getHeight();
                float paralaxY = -(((float) paralaxImageHeight - paralaxHolderHeight) * ((float) page / (lastPageIndex)));
                paralaxImage.animate().y(paralaxY).setDuration(0).setStartDelay(0).start();

            }
        });
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            contentTopPadding = Screen.getStatusBarHeight();
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) findViewById(R.id.pager_container).getLayoutParams();
            params.topMargin = contentTopPadding;
            findViewById(R.id.pager_container).setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) backToTopArrow.getLayoutParams();
            params.topMargin = contentTopPadding + params.topMargin;
            backToTopArrow.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) welcomeImage.getLayoutParams();
            params.topMargin = contentTopPadding + params.topMargin;
            welcomeImage.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) welcomeText.getLayoutParams();
            params.topMargin = contentTopPadding + params.topMargin;
            welcomeText.setLayoutParams(params);

            params = (RelativeLayout.LayoutParams) backToTopText.getLayoutParams();
            params.topMargin = contentTopPadding + params.topMargin;
            backToTopText.setLayoutParams(params);
        }

    }
}
