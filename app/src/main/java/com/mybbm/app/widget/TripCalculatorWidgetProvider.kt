package com.mybbm.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.mybbm.app.MainActivity
import com.mybbm.app.R
import com.mybbm.app.domain.model.CalculationResult
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

class TripCalculatorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val PREFERENCES_NAME = "trip_calculator_widget"
        private const val KEY_ORIGIN = "origin"
        private const val KEY_DESTINATION = "destination"
        private const val KEY_DISTANCE = "distance"
        private const val KEY_COST = "cost"

        fun updateLatestTrip(context: Context, result: CalculationResult) {
            val currency = NumberFormat.getCurrencyInstance(Locale("id", "ID")).apply {
                maximumFractionDigits = 0
                minimumFractionDigits = 0
            }
            val distance = NumberFormat.getNumberInstance(Locale("id", "ID")).format(
                result.distanceKm.setScale(1, RoundingMode.HALF_UP)
            )

            context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ORIGIN, result.originName)
                .putString(KEY_DESTINATION, result.destinationName)
                .putString(KEY_DISTANCE, "$distance km")
                .putString(KEY_COST, currency.format(result.totalCost))
                .apply()

            refreshWidgets(context)
        }

        private fun refreshWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, TripCalculatorWidgetProvider::class.java)
            manager.getAppWidgetIds(component).forEach { appWidgetId ->
                updateWidget(context, manager, appWidgetId)
            }
        }

        private fun updateWidget(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            val hasLatestTrip = preferences.contains(KEY_DESTINATION)
            val views = RemoteViews(context.packageName, R.layout.widget_trip_calculator)

            views.setTextViewText(
                R.id.widget_label,
                if (hasLatestTrip) context.getString(R.string.widget_last_trip_label)
                else context.getString(R.string.widget_empty_label)
            )
            views.setTextViewText(
                R.id.widget_destination,
                preferences.getString(KEY_DESTINATION, null)
                    ?: context.getString(R.string.widget_empty_destination)
            )
            views.setTextViewText(
                R.id.widget_origin,
                preferences.getString(KEY_ORIGIN, null)
                    ?: context.getString(R.string.widget_empty_origin)
            )
            views.setTextViewText(
                R.id.widget_cost,
                preferences.getString(KEY_COST, null)
                    ?: context.getString(R.string.widget_empty_cost)
            )
            views.setTextViewText(
                R.id.widget_distance,
                preferences.getString(KEY_DISTANCE, null)
                    ?: context.getString(R.string.widget_empty_distance)
            )
            views.setTextViewText(
                R.id.widget_action,
                context.getString(
                    if (hasLatestTrip) R.string.widget_new_trip_action
                    else R.string.widget_empty_action
                )
            )

            val launchIntent = Intent(context, MainActivity::class.java).apply {
                action = "com.mybbm.app.action.OPEN_CALCULATOR"
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra(MainActivity.EXTRA_OPEN_CALCULATOR, true)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_action, pendingIntent)

            manager.updateAppWidget(appWidgetId, views)
        }
    }
}
