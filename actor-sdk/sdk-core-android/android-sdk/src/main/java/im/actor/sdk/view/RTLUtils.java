package im.actor.sdk.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import im.actor.sdk.R;

/**
 * Created by mohsen on 5/9/16.
 */
public class RTLUtils {

    private Context context;
    private boolean isRightToLeft;

    public RTLUtils(Context current){
        context = current;
        isRightToLeft = context.getResources().getBoolean(R.bool.is_right_to_left);
    }

    public boolean isRTL(){
        return isRightToLeft;
    }

    public static boolean isRTL(Context context){
        return context.getResources().getBoolean(R.bool.is_right_to_left);
    }

    public void setMarginLeft(ViewGroup.MarginLayoutParams lp, int dp){
        if( isRightToLeft ){
            lp.rightMargin = dp;
        }
        else{
            lp.leftMargin = dp;
        }
    }

    public void setMarginRight(ViewGroup.MarginLayoutParams lp, int dp){
        if( isRightToLeft ){
            lp.leftMargin = dp;
        }
        else{
            lp.rightMargin = dp;
        }
    }

    public void setPaddings(View lp, int left, int top, int right, int bottom){
        if( isRightToLeft ){
            lp.setPadding(right,top,left,bottom);
        }
        else{
            lp.setPadding(left,top,right,bottom);
        }
    }
}
