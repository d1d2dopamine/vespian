package dev.vespian.work

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.util.Log
import dev.vespian.MainActivity
import dev.vespian.R
import dev.vespian.model.ForecastPresentation
import dev.vespian.model.Engine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The sleep window on the home screen.
 *
 * One glance, no app launch. It shows exactly what the Today tab shows and
 * nothing more: the onset band, and the gate under it. If the model has no
 * forecast yet the widget says so instead of printing a comforting number.
 *
 * Widgets are drawn by the launcher, so everything here is RemoteViews. There
 * is no Compose on a home screen.
 */
class ForecastWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        manager: AppWidgetManager,
        ids: IntArray,
    ) {
        render(context, manager, ids)
    }

    private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
        if (ids.isEmpty()) return
        // Move model work off the main thread, retaining the pending broadcast
        // until this coroutine finishes. The system broadcast deadline still applies.
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val views = try {
                    build(context)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    // Never log exception messages: forecasts may contain private data.
                    Log.w("ForecastWidget", "Widget forecast unavailable")
                    unavailable(context, createViews(context))
                }
                ids.forEach {
                    currentCoroutineContext().ensureActive()
                    manager.updateAppWidget(it, views)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                Log.w("ForecastWidget", "Widget update failed")
            } finally {
                pending.finish()
            }
        }
    }

    private fun createViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_forecast)

        val open = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        views.setOnClickPendingIntent(R.id.widget_root, open)

        return views
    }

    private fun unavailable(context: Context, views: RemoteViews): RemoteViews {
        views.setTextViewText(R.id.widget_value, context.getString(R.string.widget_none))
        views.setTextViewText(R.id.widget_range, "")
        views.setTextViewText(R.id.widget_gate, "")
        return views
    }

    private suspend fun build(context: Context): RemoteViews {
        val views = createViews(context)
        val forecast = Engine.forecast(context)
        if (!ForecastPresentation.canPresent(forecast.nights)) {
            return unavailable(context, views)
        }

        val onset = forecast.onset
        // Same honesty rule as the ring: a single time is printed only when the
        // window has earned it. Earned means measured, so the certainty here is
        // the one calibrated against how often the window contained the night,
        // not the band's agreement with itself.
        val certainty = forecast.calib?.confidence(onset) ?: onset.confidence
        views.setTextViewText(
            R.id.widget_value,
            if (certainty >= 0.5) hhmm(onset.median)
            else context.getString(R.string.widget_range, hhmm(onset.low), hhmm(onset.high)),
        )
        views.setTextViewText(
            R.id.widget_range,
            context.getString(R.string.widget_range, hhmm(onset.low), hhmm(onset.high)),
        )
        views.setTextViewText(
            R.id.widget_gate,
            context.getString(R.string.widget_gate, hhmm(forecast.gate.median)),
        )
        return views
    }

    companion object {

        private fun hhmm(hour: Double): String =
            SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(Engine.millisOf(hour)))

        /**
         * Redraws every placed widget. Called from the hourly worker, so the
         * home screen never shows a number older than the model itself.
         */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(
                ComponentName(context, ForecastWidget::class.java)
            )
            if (ids.isEmpty()) return
            val intent = Intent(context, ForecastWidget::class.java)
                .setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}
