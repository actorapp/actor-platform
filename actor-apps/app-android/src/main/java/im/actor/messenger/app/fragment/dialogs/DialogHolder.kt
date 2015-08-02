package im.actor.messenger.app.fragment.dialogs

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import im.actor.android.view.RootViewType
import im.actor.messenger.R
import im.actor.messenger.app.core.Core.messenger
import im.actor.messenger.app.core.Core.myUid
import im.actor.messenger.app.util.Screen
import im.actor.messenger.app.view.*
import im.actor.messenger.app.view.anko.*
import im.actor.messenger.app.view.emoji.SmileProcessor
import im.actor.messenger.app.view.emoji.SmileProcessor.emoji
import im.actor.messenger.app.view.keyboard.emoji.smiles.SmilesListener
import im.actor.model.entity.Dialog
import im.actor.model.entity.MessageState
import im.actor.model.entity.PeerType
import im.actor.model.mvvm.ValueChangedListener
import im.actor.model.mvvm.ValueModel
import org.jetbrains.anko.*

public class DialogHolder(private val _context: Context, onClickListener: OnItemClickedListener<Dialog>) : AnkoViewHolder(_context) {

    private var avatar: AvatarView? = null
    private var title: TextView? = null
    private var text: TextView? = null
    private var time: TextView? = null

    private var state: TintImageView? = null
    private var counter: TextView? = null

    private var separator: View? = null

    private var bindedText: CharSequence? = null
    private var bindedUid: Int = 0
    private var bindedGid: Int = 0
    private var privateTypingListener: ValueChangedListener<Boolean>? = null
    private var groupTypingListener: ValueChangedListener<IntArray>? = null
    private var bindedItem: Dialog? = null

    private val pendingColor: Int
    private val sentColor: Int
    private val receivedColor: Int
    private val readColor: Int
    private val errorColor: Int

    private var binded: Long = 0

    init {
        val paddingH = Screen.dp(11f)
        val paddingV = Screen.dp(9f)

        pendingColor = context.getResources().getColor(R.color.chats_state_pending)
        sentColor = context.getResources().getColor(R.color.chats_state_sent)
        receivedColor = context.getResources().getColor(R.color.chats_state_delivered)
        readColor = context.getResources().getColor(R.color.chats_state_read)
        errorColor = context.getResources().getColor(R.color.chats_state_error)

        with(itemView as FrameLayout) {

            layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Screen.dp(73f))
            backgroundResource = R.drawable.selector_fill

            avatar = avatarView {
                init(dip(52), 24f)

                val viewLayoutParams = FrameLayout.LayoutParams(Screen.dp(52f), Screen.dp(52f))
                viewLayoutParams.gravity = Gravity.CENTER_VERTICAL or Gravity.LEFT
                viewLayoutParams.leftMargin = paddingH
                layoutParams = viewLayoutParams
            }

            verticalLayout {
                gravity = Gravity.TOP

                linearLayout {
                    title = textView {
                        textColor = color(R.color.chats_title)
                        typeface = Fonts.medium()
                        textSize = 17f

                        paddingTop = dip(1)
                        singleLine = true
                        compoundDrawablePadding = dip(4)
                        ellipsize = TextUtils.TruncateAt.END
                    }.layoutParams {
                        height = wrapContent
                        width = 0
                        weight = 1f
                    }

                    time = textView {
                        textColor = color(R.color.chats_time)
                        typeface = Fonts.regular()
                        textSize = 13f

                        paddingLeft = dip(6)
                        singleLine = true
                    }
                }.layoutParams {
                    width = matchParent
                    height = wrapContent
                }

                text = textView {
                    typeface = Fonts.regular()
                    textColor = color(R.color.chats_text)
                    textSize = 15f
                    paddingTop = dip(5)
                    paddingRight = dip(28)
                    singleLine = true
                    ellipsize = TextUtils.TruncateAt.END
                }.layoutParams {

                }

                val viewLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                viewLayoutParams.rightMargin = paddingH
                viewLayoutParams.leftMargin = Screen.dp(79f)
                viewLayoutParams.topMargin = paddingV
                viewLayoutParams.bottomMargin = paddingV
                layoutParams = viewLayoutParams
            }

            separator = view {
                backgroundColor = color(R.color.chats_divider)

                val viewLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, context.getResources().getDimensionPixelSize(R.dimen.div_size))
                viewLayoutParams.leftMargin = Screen.dp(76f)
                viewLayoutParams.gravity = Gravity.BOTTOM
                layoutParams = viewLayoutParams
            }

            state = tintImageView {
                val viewLayoutParams = FrameLayout.LayoutParams(Screen.dp(28f), Screen.dp(12f), Gravity.BOTTOM or Gravity.RIGHT)
                viewLayoutParams.bottomMargin = dip(16)
                viewLayoutParams.rightMargin = dip(9)
                layoutParams = viewLayoutParams
            }

            counter = textView {
                textColor = color(R.color.chats_counter)
                backgroundColor = color(R.color.chats_counter_bg)
                typeface = Fonts.regular()
                gravity = Gravity.CENTER

                paddingLeft = dip(4)
                paddingRight = dip(4)
                textSize = 10f
                setIncludeFontPadding(false)
                minimumWidth = dip(14)

                val viewLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, Screen.dp(14f), Gravity.BOTTOM or Gravity.RIGHT)
                viewLayoutParams.bottomMargin = dip(12)
                viewLayoutParams.rightMargin = dip(10)
                layoutParams = viewLayoutParams
            }
        }

        itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (bindedItem != null) {
                    onClickListener.onClicked(bindedItem)
                }
            }
        })
        itemView.setOnLongClickListener(object : View.OnLongClickListener {
            override fun onLongClick(v: View): Boolean {
                if (bindedItem != null) {
                    return onClickListener.onLongClicked(bindedItem)
                }
                return false
            }
        })
    }

    public fun bind(data: Dialog, isLast: Boolean) {
        this.binded = data.getPeer().getUnuqueId()
        this.bindedItem = data

        avatar!!.bind(data)

        if (data.getUnreadCount() > 0) {
            counter!!.setText(Integer.toString(data.getUnreadCount()))
            counter!!.setVisibility(View.VISIBLE)
        } else {
            counter!!.setVisibility(View.GONE)
        }

        title!!.setText(data.getDialogTitle())

        var left: Drawable? = null
        if (data.getPeer().getPeerType() === PeerType.GROUP) {
            left = TintDrawable(R.drawable.dialogs_group, R.color.chats_title, context)
        }
        title!!.setCompoundDrawablesWithIntrinsicBounds(left, null, null, null)

        if (data.getDate() > 0) {
            time!!.setVisibility(View.VISIBLE)
            time!!.setText(messenger().getFormatter().formatShortDate(data.getDate()))
        } else {
            time!!.setVisibility(View.GONE)
        }

        //        Bypass bypass = new Bypass(context);

        //        bindedText = bypass.markdownToSpannable(messenger().getFormatter().formatDialogText(data), true);
        bindedText = messenger().getFormatter().formatDialogText(data)

        if (SmileProcessor.containsEmoji(bindedText)) {
            if (emoji().isLoaded()) {
                bindedText = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_BUBBLES)
            } else {
                emoji().registerListener(object : SmilesListener {
                    override fun onSmilesUpdated(completed: Boolean) {
                        val emojiProcessed = emoji().processEmojiCompatMutable(bindedText, SmileProcessor.CONFIGURATION_DIALOGS)
                        if (text!!.getText() == bindedText) {
                            text!!.setText(emojiProcessed)
                        }
                        bindedText = emojiProcessed
                    }
                })
            }
        }

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid)!!.unsubscribe(privateTypingListener)
            privateTypingListener = null
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid)!!.unsubscribe(groupTypingListener)
            groupTypingListener = null
        }

        if (data.getPeer().getPeerType() === PeerType.PRIVATE) {
            bindedUid = data.getPeer().getPeerId()
            privateTypingListener = object : ValueChangedListener<Boolean> {
                override fun onChanged(`val`: Boolean?, valueModel: ValueModel<Boolean>) {
                    if (`val`!!) {
                        text!!.setText(messenger().getFormatter().formatTyping())
                        text!!.setTextColor(context.getResources().getColor(R.color.chats_typing))
                    } else {
                        text!!.setText(bindedText)
                        text!!.setTextColor(context.getResources().getColor(R.color.chats_text))
                    }
                }
            }
            messenger().getTyping(bindedUid)!!.subscribe(privateTypingListener)
        } else if (data.getPeer().getPeerType() === PeerType.GROUP) {
            bindedGid = data.getPeer().getPeerId()
            groupTypingListener = object : ValueChangedListener<IntArray> {
                override fun onChanged(`val`: IntArray, valueModel: ValueModel<IntArray>) {
                    if (`val`.size() != 0) {
                        if (`val`.size() == 1) {
                            text!!.setText(messenger().getFormatter().formatTyping(messenger().getUsers()!!.get(`val`[0].toLong()).getName().get()))
                        } else {
                            text!!.setText(messenger().getFormatter().formatTyping(`val`.size()))
                        }
                        text!!.setTextColor(context.getResources().getColor(R.color.chats_typing))
                    } else {
                        text!!.setText(bindedText)
                        text!!.setTextColor(context.getResources().getColor(R.color.chats_text))
                    }
                }
            }
            messenger().getGroupTyping(bindedGid)!!.subscribe(groupTypingListener)
        } else {
            text!!.setText(bindedText)
            text!!.setTextColor(context.getResources().getColor(R.color.chats_text))
        }

        if (data.getSenderId() != myUid()) {
            state!!.setVisibility(View.GONE)
        } else {
            when (data.getStatus()) {
                MessageState.SENT -> {
                    state!!.setResource(R.drawable.msg_check_1)
                    state!!.setTint(sentColor)
                }
                MessageState.RECEIVED -> {
                    state!!.setResource(R.drawable.msg_check_2)
                    state!!.setTint(receivedColor)
                }
                MessageState.READ -> {
                    state!!.setResource(R.drawable.msg_check_2)
                    state!!.setTint(readColor)
                }
                MessageState.ERROR -> {
                    state!!.setResource(R.drawable.msg_error)
                    state!!.setTint(errorColor)
                }
                MessageState.PENDING -> {
                    state!!.setResource(R.drawable.msg_clock)
                    state!!.setTint(pendingColor)
                }
                else -> {
                    state!!.setResource(R.drawable.msg_clock)
                    state!!.setTint(pendingColor)
                }
            }
            state!!.setVisibility(View.VISIBLE)
        }

        if (isLast) {
            separator!!.setVisibility(View.GONE)
        } else {
            separator!!.setVisibility(View.VISIBLE)
        }
    }

    public fun unbind() {
        this.bindedItem = null

        this.avatar!!.unbind()

        if (privateTypingListener != null) {
            messenger().getTyping(bindedUid)!!.unsubscribe(privateTypingListener)
            privateTypingListener = null
        }

        if (groupTypingListener != null) {
            messenger().getGroupTyping(bindedGid)!!.unsubscribe(groupTypingListener)
            groupTypingListener = null
        }
    }
}
