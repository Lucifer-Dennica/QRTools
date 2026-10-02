package com.luciferdennica.qrtools.ads

import android.app.Activity
import android.content.Context
import com.yandex.mobile.ads.common.AdError
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData
import com.yandex.mobile.ads.interstitial.InterstitialAd
import com.yandex.mobile.ads.interstitial.InterstitialAdEventListener
import com.yandex.mobile.ads.interstitial.InterstitialAdLoader

object AdsManager {

    private var interstitialAd: InterstitialAd? = null
    private var loading = false

    /**
     * Предзагрузить межстраничную.
     * Это suspend-функция, поэтому ее нужно вызывать из корутины.
     */
    suspend fun preloadInterstitial(context: Context) {
        if (interstitialAd != null || loading) return
        loading = true

        val loader = InterstitialAdLoader(context)
        // В SDK 8.x AdRequest создается с adUnitId
        val adRequest = AdRequest.Builder(AdIds.INTERSTITIAL).build()

        // loadAd теперь suspend-функция и возвращает результат
        val result = loader.loadAd(adRequest)

        result.onSuccess { ad ->
            interstitialAd = ad
        }.onFailure { error ->
            // Обработка ошибки
            interstitialAd = null
        }
        loading = false
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
            override fun onAdImpression(impressionData: ImpressionData?) {}
        })
        ad.show(activity)
    }
}
