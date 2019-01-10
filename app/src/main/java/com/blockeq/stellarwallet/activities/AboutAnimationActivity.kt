package com.blockeq.stellarwallet.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import com.blockeq.stellarwallet.R
import kotlinx.android.synthetic.main.activity_about_animation.*

class AboutAnimationActivity : AppCompatActivity() {
    private var imageViews = arrayListOf<ImageView>()
    private var gradientAnimationInProgress : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_animation)

        imageViews.add(blockeq1)
        imageViews.add(blockeq2)
        imageViews.add(blockeq3)
        imageViews.add(blockeq4)
        imageViews.add(blockeq5)
        imageViews.add(blockeq6)
        imageViews.add(blockeq7)
        imageViews.add(blockeq8)
        imageViews.add(blockeq9)
        imageViews.add(blockeq10)
        imageViews.add(blockeq11)

        imageViews.reverse()
        var counter : Long = 0
        imageViews.forEachIndexed { index, image ->

            slideView(image, 10f, 0f, 85*counter, 700,  object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) { }
                override fun onAnimationStart(animation: Animation?) { }
                override fun onAnimationEnd(animation: Animation?) {
                    if (index == imageViews.lastIndex) {
                        startGradientAnimation()
                    }
                }
            })

            counter++
        }

        slideView(circle, -20f, 0f, 0,850)

        blocks_wrapper.setOnClickListener {
            if (!gradientAnimationInProgress) {
                startGradientAnimation()
            }
        }

        about_animation_root.setOnClickListener {
            finish()
        }

    }

    private fun startGradientAnimation() {
        val animation = TranslateAnimation(-circle.width.toFloat(), circle.width.toFloat(), 0f, 0f)
        animation.duration = 1500
        animation.fillAfter = false
        animation.interpolator = AccelerateDecelerateInterpolator()
        gradient.startAnimation(animation)
        animation.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                gradient.visibility = View.GONE
                gradientAnimationInProgress = false
            }

            override fun onAnimationStart(animation: Animation?) {
                gradient.visibility = View.VISIBLE
                gradientAnimationInProgress = true
            }

        })
    }

    private fun slideView(view : View, from: Float, to: Float = 0f, offset: Long = 0, duration: Long, listener : Animation.AnimationListener? = null) {

        val slide: Animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
            from, Animation.RELATIVE_TO_SELF, to)
        slide.startOffset = offset
        slide.duration = duration
        slide.fillAfter = true
        slide.isFillEnabled = true
        if (listener != null) {
            slide.setAnimationListener(listener)
        }
        view.startAnimation(slide)
    }
}
