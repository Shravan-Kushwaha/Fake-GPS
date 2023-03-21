package com.fakegps.utils

import android.app.Activity
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.fakegps.R
import java.lang.ref.WeakReference

class SnacyAlert {

    private var alertView: AlertView? = null

    private val activityDecorView: ViewGroup?
        get() {
            var decorView: ViewGroup? = null

            activityWeakReference?.get()?.let {
                decorView = it.window.decorView as ViewGroup
            }

            return decorView
        }


    fun show(): AlertView? {
        //This will get the Activity Window's DecorView
        activityWeakReference?.get().let {
            it?.runOnUiThread {
                //Add the new Alert to the View Hierarchy
                activityDecorView?.addView(alertView)
            }
        }

        return alertView
    }

    fun setTitle(@StringRes titleId: Int): SnacyAlert {
        alertView?.setTitle(titleId)

        return this
    }

    fun setTitle(title: CharSequence): SnacyAlert {
        alertView?.setTitle(title)

        return this
    }

    fun setTitleTypeface(typeface: Typeface): SnacyAlert {
        alertView?.setTitleTypeface(typeface)

        return this
    }

    fun setText(@StringRes textId: Int): SnacyAlert {
        alertView?.setText(textId)

        return this
    }

    fun setText(text: CharSequence): SnacyAlert {
        alertView?.setText(text)

        return this
    }

    fun setTextTypeface(typeface: Typeface): SnacyAlert {
        alertView?.setTextTypeface(typeface)

        return this
    }

    fun setBackgroundColorInt(@ColorInt colorInt: Int): SnacyAlert {
        alertView?.setAlertBackgroundColor(colorInt)

        return this
    }

    fun setBackgroundColorRes(@ColorRes colorResId: Int): SnacyAlert {
        activityWeakReference?.get().let {
            alertView?.setAlertBackgroundColor(ContextCompat.getColor(it!!, colorResId))
        }

        return this
    }

    fun setIcon(@DrawableRes iconId: Int): SnacyAlert {
        alertView?.setIcon(iconId)

        return this
    }

    fun setIcon(drawable: Drawable): SnacyAlert {
        alertView?.setIcon(drawable)

        return this
    }

    fun hideIcon(): SnacyAlert {
        alertView?.showIcon(false)

        return this
    }

    fun showIcon(showIcon: Boolean): SnacyAlert {
        alertView?.showIcon(showIcon)

        return this
    }

    fun setDuration(milliseconds: Long): SnacyAlert {
        alertView?.duration = milliseconds

        return this
    }

    private fun setActivity(activity: Activity) {
        activityWeakReference = WeakReference(activity)
    }

    private fun setFragment(fragment: Fragment) {
        fragmentWeakReference = WeakReference(fragment)
    }

    companion object {
        private var activityWeakReference: WeakReference<Activity>? = null
        private var fragmentWeakReference: WeakReference<Fragment>? = null

        @JvmStatic
        fun create(activity: Activity?): SnacyAlert {
            if (activity == null) {
                throw IllegalArgumentException("Activity cannot be null!")
            }
            val alerter = SnacyAlert()

            //Hide current Alert, if one is active
            SnacyAlert.clearCurrent(activity)
            alerter.setActivity(activity)
            alerter.alertView = AlertView(activity)


            return alerter
        }

        @JvmStatic
        fun clearCurrent(activity: Activity?) {
            (activity?.window?.decorView as? ViewGroup)?.let {
                //Find all Alert Views in Parent layout
                for (i in 0..it.childCount) {
                    val childView =
                        if (it.getChildAt(i) is AlertView) it.getChildAt(i) as AlertView else null
                    if (childView != null && childView.windowToken != null) {
                        ViewCompat.animate(childView).alpha(0f)
                            .withEndAction(getRemoveViewRunnable(childView))
                    }
                }
            }
        }

        @JvmStatic
        fun hide() {
            activityWeakReference?.get()?.let {
                clearCurrent(it)
            }
        }

        @JvmStatic
        val isShowing: Boolean
            get() {
                var isShowing = false

                activityWeakReference?.get()?.let {
                    isShowing = it.findViewById<View>(R.id.alertCard) != null
                }

                return isShowing
            }

        private fun getRemoveViewRunnable(childView: AlertView?): Runnable {
            return Runnable {
                childView?.let {
                    (childView.parent as? ViewGroup)?.removeView(childView)
                }
            }
        }


    }
}