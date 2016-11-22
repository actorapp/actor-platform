package im.actor.sdk.controllers.compose;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import im.actor.sdk.ActorSDK;
import im.actor.sdk.R;
import im.actor.sdk.controllers.BaseFragment;

public class ComposeFabFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View res = inflater.inflate(R.layout.fragment_fab, container, false);
        FloatingActionButton fabRoot = (FloatingActionButton) res.findViewById(R.id.fab);
        fabRoot.setImageResource(R.drawable.ic_edit_white_24dp);
        fabRoot.setBackgroundTintList(new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_pressed},
                StateSet.WILD_CARD,

        }, new int[]{
                ActorSDK.sharedActor().style.getFabPressedColor(),
                ActorSDK.sharedActor().style.getFabColor(),
        }));
        fabRoot.setRippleColor(ActorSDK.sharedActor().style.getFabPressedColor());
        fabRoot.setOnClickListener(v -> startActivity(new Intent(getActivity(), ComposeActivity.class)));
        return res;
    }
}
