package im.actor.sdk.controllers.calls.view;

import android.view.View;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

public class CallAvatarLayerAnimator {

    private final SpringSystem springSystem;
    private final Spring popAnimationSpring;
    private final Spring popAnimationSpring1;
    private final Spring popAnimationSpring2;
    private final Spring popAnimationSpring3;
    private final Spring popAnimationSpring4;
    private final View layer;
    private final View layer1;
    private final View layer2;
    private final View layer3;
    private final View layer4;


    public CallAvatarLayerAnimator(View layer, View layer1, View layer2, View layer3, View layer4) {
        this.layer = layer;
        this.layer1 = layer1;
        this.layer2 = layer2;
        this.layer3 = layer3;
        this.layer4 = layer4;

        springSystem = SpringSystem.create();

        popAnimationSpring = springSystem.createSpring()
                .setSpringConfig(SpringConfig.fromBouncinessAndSpeed(0, 1))
                .addListener(new SimpleSpringListener() {
                    @Override
                    public void onSpringUpdate(Spring spring) {
                        setPopAnimationProgress((float) spring.getCurrentValue());
                    }
                });

        popAnimationSpring1 = springSystem.createSpring()
                .setSpringConfig(SpringConfig.fromBouncinessAndSpeed(0, 1))
                .addListener(new SimpleSpringListener() {
                    @Override
                    public void onSpringUpdate(Spring spring) {
                        setPopAnimationProgress1((float) spring.getCurrentValue());
                    }
                });

        popAnimationSpring2 = springSystem.createSpring()
                .setSpringConfig(SpringConfig.fromBouncinessAndSpeed(0, 1))
                .addListener(new SimpleSpringListener() {
                    @Override
                    public void onSpringUpdate(Spring spring) {
                        setPopAnimationProgress2((float) spring.getCurrentValue());
                    }
                });

        popAnimationSpring3 = springSystem.createSpring()
                .setSpringConfig(SpringConfig.fromBouncinessAndSpeed(0, 1))
                .addListener(new SimpleSpringListener() {
                    @Override
                    public void onSpringUpdate(Spring spring) {
                        setPopAnimationProgress((float) spring.getCurrentValue());
                    }
                });

        popAnimationSpring4 = springSystem.createSpring()
                .setSpringConfig(SpringConfig.fromBouncinessAndSpeed(9, 20))
                .addListener(new SimpleSpringListener() {
                    @Override
                    public void onSpringUpdate(Spring spring) {
                        setPopAnimationProgress1((float) spring.getCurrentValue());
                    }
                });
    }

    // popAnimation transition

    public void popAnimation(boolean on) {
        popAnimationSpring.setEndValue(on ? 1 : 0);
        popAnimationSpring1.setEndValue(on ? 1 : 0);
        popAnimationSpring2.setEndValue(on ? 1 : 0);
        popAnimationSpring3.setEndValue(on ? 1 : 0);
        popAnimationSpring4.setEndValue(on ? 1 : 0);
    }

    private void setPopAnimationProgress(float progress) {
        float transition2 = transition(progress, 0.8f, 0.85f);
        layer.setScaleX(transition2);
        layer.setScaleY(transition2);
    }


    private void setPopAnimationProgress1(float progress) {
        float transition2 = transition(progress, 0.85f, 0.9f);
        layer1.setScaleX(transition2);
        layer1.setScaleY(transition2);
    }


    private void setPopAnimationProgress2(float progress) {
        float transition2 = transition(progress, 0.7f, 0.75f);
        layer2.setScaleX(transition2);
        layer2.setScaleY(transition2);
    }



    // Utilities
    private float transition (float progress, float startValue, float endValue) {
        return (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, startValue, endValue);
    }

}
