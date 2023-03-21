package com.fakegps.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import com.fakegps.R


class AlertView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), Animation.AnimationListener {

    private var enterAnimation: Animation =
        AnimationUtils.loadAnimation(context, R.anim.slide_in_from_top)
    private var exitAnimation: Animation =
        AnimationUtils.loadAnimation(context, R.anim.slide_out_to_top)

    internal var duration = 1000L

    private var runningAnimation: Runnable? = null

    init {
        View.inflate(context, R.layout.layout_alert_view, this)
        ViewCompat.setTranslationZ(this, Int.MAX_VALUE.toFloat())
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        enterAnimation.setAnimationListener(this)
        animation = enterAnimation
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        enterAnimation.setAnimationListener(null)
    }

    override fun onAnimationRepeat(animation: Animation?) {

    }

    override fun onAnimationEnd(animation: Animation?) {
        startHideAnimation()
    }

    override fun onAnimationStart(animation: Animation?) {

    }

    private fun startHideAnimation() {
        runningAnimation = Runnable { hide() }
        postDelayed(runningAnimation, duration)
    }

    private fun hide() {
        try {
            exitAnimation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    removeFromParent()
                }

                override fun onAnimationStart(animation: Animation?) {

                }
            })
            startAnimation(exitAnimation)
        } catch (e: Exception) {
            Log.e(javaClass.name, Log.getStackTraceString(e))
        }
    }

    private fun removeFromParent() {
        clearAnimation()
        visibility = View.GONE

        postDelayed(object : Runnable {
            override fun run() {
                try {
                    if (parent != null) {
                        try {
                            (parent as ViewGroup).removeView(this@AlertView)
                        } catch (e: Exception) {
                            Log.e(javaClass.simpleName, Log.getStackTraceString(e))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(javaClass.simpleName, Log.getStackTraceString(e))
                }
            }
        }, 100)
    }

    fun setAlertBackgroundColor(@ColorInt color: Int) {
        findViewById<CardView>(R.id.alertCard).setCardBackgroundColor(color)
    }

    fun setTitle(@StringRes titleId: Int) {
        setTitle(context.getString(titleId))
    }

    fun setText(@StringRes textId: Int) {
        setText(context.getString(textId))
    }

    @SuppressLint("CutPasteId")
    fun setTitle(title: CharSequence) {
        if (!TextUtils.isEmpty(title)) {
            findViewById<AppCompatTextView>(R.id.txtTitle)?.visibility = View.VISIBLE
            findViewById<AppCompatTextView>(R.id.txtTitle)?.text = title
        }
    }

    @SuppressLint("CutPasteId")
    fun setText(text: CharSequence) {
        if (!TextUtils.isEmpty(text)) {
            findViewById<AppCompatTextView>(R.id.txtMessage)?.visibility = View.VISIBLE
            findViewById<AppCompatTextView>(R.id.txtMessage)?.text = text
        }
    }

    fun setTitleTypeface(typeface: Typeface) {
        findViewById<AppCompatTextView>(R.id.txtTitle)?.typeface = typeface
    }

    fun setTextTypeface(typeface: Typeface) {
        findViewById<AppCompatTextView>(R.id.txtMessage)?.typeface = typeface
    }

    fun setIcon(@DrawableRes iconId: Int) {
        findViewById<AppCompatImageView>(R.id.img)?.setImageDrawable(
            AppCompatResources.getDrawable(
                context,
                iconId
            )
        )
    }

    fun setIcon(drawable: Drawable) {
        findViewById<AppCompatImageView>(R.id.img)?.setImageDrawable(drawable)
    }

    fun showIcon(showIcon: Boolean) {
        if (showIcon) {
            findViewById<AppCompatImageView>(R.id.img)?.visibility = View.VISIBLE
        }
    }
}