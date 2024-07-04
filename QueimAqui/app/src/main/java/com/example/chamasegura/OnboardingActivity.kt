package com.example.chamasegura

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.example.chamasegura.fragments.ViewPagerAdapter


class OnboardingActivity : AppCompatActivity() {
    var slideViewPager: ViewPager? = null
    var dotIndicator: LinearLayout? = null
    var backButton: Button? = null
    var nextButton: Button? = null
    var skipButton: Button? = null
    lateinit var dots: Array<TextView?>
    var viewPagerAdapter: ViewPagerAdapter? = null
    var viewPagerListener: OnPageChangeListener = object : OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            setDotIndicator(position)
            if (position > 0) {
                backButton!!.visibility = View.VISIBLE
            } else {
                backButton!!.visibility = View.INVISIBLE
            }
            if (position == 4) {
                nextButton!!.text = getString(R.string.finish_button)
            } else {
                nextButton!!.text = getString(R.string.next_button)
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)
        backButton?.setOnClickListener(View.OnClickListener {
            if (getItem(0) > 0) {
                slideViewPager!!.setCurrentItem(getItem(-1), true)
            }
        })
        nextButton?.setOnClickListener(View.OnClickListener {
            if (getItem(0) < 4) slideViewPager!!.setCurrentItem(getItem(1), true)
            else {
                val i = Intent(
                    this@OnboardingActivity,
                    MainActivity::class.java
                )
                startActivity(i)
                finish()
            }
        })
        skipButton?.setOnClickListener(View.OnClickListener {
            val i = Intent(
                this@OnboardingActivity,
                MainActivity::class.java
            )
            startActivity(i)
            finish()
        })
        slideViewPager = findViewById<View>(R.id.slideViewPager) as ViewPager
        dotIndicator = findViewById<View>(R.id.dotIndicator) as LinearLayout
        viewPagerAdapter = ViewPagerAdapter(this)
        slideViewPager!!.adapter = viewPagerAdapter
        setDotIndicator(0)
        slideViewPager!!.addOnPageChangeListener(viewPagerListener)
    }

    fun setDotIndicator(position: Int) {
        dots = arrayOfNulls(5)
        dotIndicator!!.removeAllViews()
        for (i in dots.indices) {
            dots[i] = TextView(this)
            dots[i]!!.text = Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY)
            dots[i]!!.textSize = 35f
            dots[i]!!.setTextColor(resources.getColor(R.color.gray, applicationContext.theme))
            dotIndicator!!.addView(dots[i])
        }
        dots[position]!!
            .setTextColor(resources.getColor(R.color.gray, applicationContext.theme))
    }

    private fun getItem(i: Int): Int {
        return slideViewPager!!.currentItem + i
    }
}