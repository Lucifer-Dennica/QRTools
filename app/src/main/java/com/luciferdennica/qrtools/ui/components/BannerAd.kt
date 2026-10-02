package com.luciferdennica.qrtools.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.luciferdennica.qrtools.ads.AdIds
import com.yandex.mobile.ads.banner.BannerAdEventListener
import com.yandex.mobile.ads.banner.BannerAdSize
import com.yandex.mobile.ads.banner.BannerAdView
import com.yandex.mobile.ads.common.AdRequest
import com.yandex.mobile.ads.common.AdRequestError
import com.yandex.mobile.ads.common.ImpressionData

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            BannerAdView(ctx).apply {
                setAdUnitId(AdIds.BANNER)
                setAdSize(BannerAdSize.stickySize(ctx, 320))
                setBannerAdEventListener(object : BannerAdEventListener {
                    override fun onAdLoaded() {}
                    override fun onAdFailedToLoad(error: AdRequestError) {}
                    override fun onAdClicked() {}
                    override fun onLeftApplication() {}
                    override fun onReturnedToApplication() {}
                    override fun onImpression(impressionData: ImpressionData?) {}
                })
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
