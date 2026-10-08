package com.luciferdennica.qrtools.review

import android.app.Activity
import android.content.Context
import android.util.Log
import ru.rustore.sdk.review.RuStoreReviewManager
import ru.rustore.sdk.review.RuStoreReviewManagerFactory

/**
 * Обёртка над RuStore Review SDK.
 *
 * Если будешь делать сборку для Huawei/Amazon — просто УДАЛИ этот файл
 * и все его вызовы в ScannerScreen и SettingsScreen (2 файла).
 */
object RuStoreReview {

    private const val TAG = "RuStoreReview"

    /**
     * Пытается показать нативный диалог оценки.
     * @param onSuccess — вызывается, если диалог показан (не значит, что юзер поставил оценку)
     * @param onError — вызывается, если не получилось (лимит 24 часа, нет обновления и т.п.)
     */
    fun requestReview(
        activity: Activity,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        try {
            val manager: RuStoreReviewManager =
                RuStoreReviewManagerFactory.create(activity)

            manager.requestReviewFlow().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    manager.launchReviewFlow(activity, reviewInfo)
                        .addOnCompleteListener { launchTask ->
                            if (launchTask.isSuccessful) {
                                onSuccess()
                            } else {
                                Log.w(TAG, "launchReviewFlow failed", launchTask.exception)
                                onError(launchTask.exception ?: Exception("Unknown"))
                            }
                        }
                } else {
                    Log.w(TAG, "requestReviewFlow failed", task.exception)
                    onError(task.exception ?: Exception("Unknown"))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "RuStore Review not available", e)
            onError(e)
        }
    }

    /** Проверяет, что устройство использует RuStore (иначе нет смысла вызывать). */
    fun isRuStoreAvailable(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("ru.vk.store", 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
