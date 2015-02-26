package im.actor.messenger.app.fragment.chat.recycler;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import im.actor.messenger.util.Screen;

/**
 * Created by ex3ndr on 26.02.15.
 */
public class FooterHolder extends BaseHolder {

    private static View buildView(Context context) {
        View view = new View(context);
        view.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(10)));
        return view;
    }

    public FooterHolder(Context context) {
        super(buildView(context));
    }
}
