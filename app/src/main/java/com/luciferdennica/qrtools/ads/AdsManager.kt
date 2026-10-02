package com.luciferdennica.qrtools.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

object AdsManager {

    private var interstitialAd: InterstitialAd? = null
    private var loading = false

    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || loading) return
        loading = true

        val loader = InterstitialAdLoader(context).apply {
            setAdLoadListener(object : InterstitialAdLoadListener {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: AdRequestError) {
                    interstitialAd = null
                    loading = false
                }
            })
        }

        val request = AdRequest.Builder(AdIds.INTERSTITIAL).build()
        loader.loadAd(request)
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            onDismiss()
            return
        }
        ad.setAdEventListener(object : InterstitialAdEventListener {
            override fun onAdShown() {}

            override fun onAdFailedToShow(adError: AdError) {
                interstitialAd = null
                onDismiss()
            }

            override fun onAdDismissed() {
                interstitialAd = null
                onDismiss()
            }

            override fun onAdClicked() {}

            override fun onAdImpression(impressionData: ImpressionData?) {}
        })
        ad.show(activity)
    }
}
