package im.actor.sdk.controllers.compose;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
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
        if (ActorSDK.sharedActor().style.getFabColor() != 0) {
            fabRoot.setBackgroundColor(ActorSDK.sharedActor().style.getFabColor());
        }
        if (ActorSDK.sharedActor().style.getFabPressedColor() != 0) {
            fabRoot.setRippleColor(ActorSDK.sharedActor().style.getFabPressedColor());
        }
        fabRoot.setOnClickListener(v -> startActivity(new Intent(getActivity(), ComposeActivity.class)));
        return res;
    }
}
