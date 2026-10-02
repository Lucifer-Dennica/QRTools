package com.luciferdennica.qrtools.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequestConfiguration
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.AdImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoadListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

object AdsManager {

    private var interstitialAd: InterstitialAd? = null
    private var loading = false

    /** Предзагрузить межстраничную (вызывать заранее, чтобы была готова к показу) */
    fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || loading) return
        loading = true
        val loader = InterstitialAdLoader(context)
        val config = AdRequestConfiguration.Builder(AdIds.INTERSTITIAL).build()
        loader.loadAd(config, object : InterstitialAdLoadListener {
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

    /**
     * Показать межстраничную. Если не загружена — сразу вызывает onDismiss.
     */
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
            override fun onAdImpression(impressionData: AdImpressionData?) {}
        })
        ad.show(activity)
    }
}
