package com.example.chamasegura.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.example.chamasegura.R

class ViewPagerAdapter(var context: Context) : PagerAdapter() {
    private val sliderAllImages = intArrayOf(
        //R.drawable.onboarding1,
        //R.drawable.onboarding2,
        //R.drawable.onboarding3,
        //R.drawable.onboarding4,
        //R.drawable.onboarding5,
    )

    private val sliderAllTitle = intArrayOf(
        R.string.screen1,
        R.string.screen2,
        R.string.screen3,
        R.string.screen4,
        R.string.screen5
    )

    override fun getCount(): Int {
        return sliderAllTitle.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.fragment_onboarding_home, container, false)

        val sliderImage = view.findViewById<ImageView>(R.id.sliderImage)
        val sliderTitle = view.findViewById<TextView>(R.id.sliderTitle)

        sliderImage.setImageResource(sliderAllImages[position])
        sliderTitle.setText(sliderAllTitle[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }
}