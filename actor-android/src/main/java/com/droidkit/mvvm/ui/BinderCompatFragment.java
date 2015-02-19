package com.droidkit.mvvm.ui;

import android.app.Fragment;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;

import com.droidkit.mvvm.ValueModel;

import im.actor.messenger.app.binding.ActorBinder;
import im.actor.messenger.app.view.AvatarView;
import im.actor.messenger.app.view.CoverAvatarView;
import im.actor.model.entity.Avatar;

/**
 * Created by ex3ndr on 17.09.14.
 */
public class BinderCompatFragment extends android.support.v4.app.Fragment {

    private final ActorBinder BINDER = new ActorBinder();

    public void bind(final TextView textView, im.actor.model.mvvm.ValueModel<String> value) {
        BINDER.bind(textView, value);
    }

    public void bind(final AvatarView avatarView, final im.actor.model.mvvm.ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    public void bind(final CoverAvatarView avatarView, final im.actor.model.mvvm.ValueModel<Avatar> avatar) {
        BINDER.bind(avatarView, avatar);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BINDER.unbindAll();
    }
}
