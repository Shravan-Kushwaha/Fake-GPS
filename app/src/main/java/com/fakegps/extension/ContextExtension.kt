package com.fakegps.extension

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.widget.ContentLoadingProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.fakegps.R
import com.fakegps.databinding.ViewSnackbarBinding
import com.fakegps.utils.Logger
import com.fakegps.utils.SnacyAlert
import com.google.android.material.textview.MaterialTextView


fun Context.setGlideImage(
    imageUrl: String, imageView: ImageView,
    loaderProgress: ContentLoadingProgressBar?
) {
    Logger.d(imageUrl)
    if (!imageUrl.isNullOrEmpty()) {
        Glide.with(applicationContext).load(imageUrl)
            .skipMemoryCache(true)
            .apply(RequestOptions().error(R.drawable.default_user))
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (loaderProgress != null)
                        loaderProgress.visibility = View.GONE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?, model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (loaderProgress != null)
                        loaderProgress.visibility = View.GONE
                    return false
                }
            })
            .apply(RequestOptions.circleCropTransform()).into(imageView)
    }
}


fun Context.customDialog(string: String) {

    val dialog = Dialog(this, R.style.PauseDialog)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    dialog.show()
    dialog.window?.setGravity(Gravity.TOP)
    dialog.window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    )
    dialog.setCancelable(true)
    dialog.setContentView(R.layout.snackbar_view)
    val materialTextView: MaterialTextView = dialog.findViewById(R.id.textView_snackbarView)
    materialTextView.text = string
    val timer = object : CountDownTimer(1200, 1200) {
        override fun onTick(millisUntilFinished: Long) {
        }

        override fun onFinish() {
            dialog.dismiss()
        }
    }
    timer.start()
}

fun Context.dpToPx(dp: Float): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    ).toInt()
}
fun Context.showToast(id: ViewGroup, message: String = "", isError: Boolean = true) {

    val mLength: Long = 1500
    val h = Handler(Looper.getMainLooper())
    val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein)
    val animFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

    val mParent = (this as Activity).window.decorView as ViewGroup
    val mToastHolder = FrameLayout(this)
    val inflater = LayoutInflater.from(this)
    val binding: ViewSnackbarBinding = ViewSnackbarBinding.inflate(inflater)
    binding.tvMessage.text = message
    binding.isError = isError
    val mLayoutParams: FrameLayout.LayoutParams = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        Gravity.TOP or Gravity.CENTER
    )
    mLayoutParams.setMargins(
        this.dpToPx(10f),
        this.dpToPx(30f),
        this.dpToPx(10f),
        this.dpToPx(30f),
    )
    mToastHolder.layoutParams = mLayoutParams

    val isShown = mToastHolder.isShown
    if (isShown) {
        mToastHolder.startAnimation(animFadeOut)
        mToastHolder.removeAllViews()
        mParent.removeView(mToastHolder)
    } else {
        mToastHolder.bringToFront()

        mParent.addView(mToastHolder)
        mToastHolder.startAnimation(animFadeIn)
    }

    mToastHolder.addView(binding.root)
    h.postDelayed({
        if (mToastHolder.isShown) {
            mToastHolder.startAnimation(animFadeOut)
        }
        mParent.removeView(mToastHolder)
    }, mLength)
    mParent.visibility = View.VISIBLE
    mToastHolder.visibility = View.VISIBLE
    mToastHolder.bringToFront()
    mParent.bringToFront()
    id.bringChildToFront(mParent)

}

fun Activity.showErrorAlert(
    message: String = " ",
    title: String? = getString(R.string.required),
    bgColor: Int = R.color.red,
    icon: Int = R.drawable.ic_baseline_required_24,
) {
    SnacyAlert.create(this)
        .setText(message)
        .setTitle(title.toString())
        .setBackgroundColorRes(bgColor)
        .setDuration(1500)
        .showIcon(true)
        .setIcon(icon)
        .show()
}
fun Activity.showSuccessAlert(message: String = "") {
    SnacyAlert.create(this)
        .setText(message)
        .setTitle(getString(R.string.success))
        .setBackgroundColorRes(R.color.green)
        .setDuration(1500)
        .showIcon(true)
        .setIcon(R.drawable.ic_radio_checked)
        .show()
}
fun Context.showMsgDialog(
    msg: String,
    positiveText: String? = "OK",
    listener: DialogInterface.OnClickListener? = null,
    negativeText: String? = "Cancel",
    negativeListener: DialogInterface.OnClickListener? = null,
    title: String? = "Need Permissions",
    icon: Int? = null,
    isCancelable: Boolean = true
) {

    val builder = AlertDialog.Builder(this,R.style.AlertDialogCustom)
    builder.setTitle(title)
    builder.setMessage(msg)
    builder.setCancelable(isCancelable)
    builder.setPositiveButton(positiveText) { dialog, which ->

        listener?.onClick(dialog, which)
    }
    if (negativeListener != null) {
        builder.setNegativeButton(negativeText) { dialog, which ->
            negativeListener.onClick(dialog, which)
        }
    }
    if (icon != null) {
        builder.setIcon(icon)
    }
    if (isCancelable) {
        builder.setOnDismissListener {
            // dialog?.dismiss()
        }
    }
    builder.create().show()


}
