package im.actor.messenger.app.view.anko

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import org.jetbrains.anko.UiHelper
import org.jetbrains.anko.custom.addView
import org.jetbrains.anko.dip

@suppress("NOTHING_TO_INLINE")
public inline fun AnkoViewHolder.dip(value: Int): Int = this.getContext().dip(value)

@suppress("NOTHING_TO_INLINE")
public inline fun AnkoViewHolder.string(value: Int): String = this.getContext().getResources().getString(value)

@suppress("NOTHING_TO_INLINE")
public inline fun AnkoViewHolder.color(value: Int): Int = this.getContext().getResources().getColor(value)