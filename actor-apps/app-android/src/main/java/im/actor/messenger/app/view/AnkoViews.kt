package im.actor.messenger.app.view

import android.view.ViewManager
import org.jetbrains.anko.__dslAddView

/**
 * Created by ex3ndr on 31.07.15.
 */

fun ViewManager.avatarView(init: AvatarView.() -> Unit = {}) =
        __dslAddView({AvatarView(it)}, init, this)

fun ViewManager.tintImageView(init: TintImageView.() -> Unit = {}) =
        __dslAddView({TintImageView(it)}, init, this)