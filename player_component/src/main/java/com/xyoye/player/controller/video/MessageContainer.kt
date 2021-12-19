package com.xyoye.player.controller.video

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.xyoye.common_component.utils.dp2px
import com.xyoye.player.utils.MessageTime
import com.xyoye.player_component.databinding.LayoutVideoMessageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MessageContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    fun showMessage(text: String, time: MessageTime) {
        if (text.isEmpty())
            return

        if (childCount == 2) {
            removeMessage(getChildAt(0))
        }

        postRemoveMessage(addView(text), time)
    }

    fun clearMessage() {
        removeAllViews()
    }

    private fun addView(text: String): View {
        val messageView = generateMessageView()
        messageView.text = text

        val layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams.topMargin = dp2px(10)
        layoutParams.leftMargin = dp2px(10)

        val transition = Slide(Gravity.START)
        TransitionManager.beginDelayedTransition(this, transition)
        addView(messageView, layoutParams)
        return messageView
    }

    private fun generateMessageView(): TextView {
        val inflater = LayoutInflater.from(context)
        val binding = LayoutVideoMessageBinding.inflate(inflater, this, false)
        return binding.tvMessage
    }

    private fun postRemoveMessage(messageView: View, time: MessageTime) {
        (context as LifecycleOwner).lifecycleScope.launch(Dispatchers.Default) {
            delay(time.time)
            withContext(Dispatchers.Main) {
                removeMessage(messageView)
            }
        }
    }

    private fun removeMessage(messageView: View) {
        removeView(messageView)
    }
}