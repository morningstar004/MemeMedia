package com.nothingsound.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class SoundWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_ITEM_CLICK = "com.nothingsound.widget.ACTION_ITEM_CLICK"
        const val EXTRA_ITEM_ID = "extra_item_id"
        const val EXTRA_IS_ADD_TILE = "extra_is_add_tile"

        fun refreshAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                android.content.ComponentName(context, SoundWidgetProvider::class.java),
            )
            mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list)
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId -> updateWidget(context, appWidgetManager, widgetId) }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val rv = RemoteViews(context.packageName, R.layout.widget_sound)

        // The adapter that supplies each swipeable page.
        val adapterIntent = Intent(context, StackRemoteViewsService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            data = android.net.Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        rv.setRemoteAdapter(R.id.widget_list, adapterIntent)
        rv.setEmptyView(R.id.widget_list, R.id.empty_view)

        // For ListView/GridView/StackView, we use a PendingIntent template.
        val clickIntent = Intent(context, SoundWidgetProvider::class.java).apply {
            action = ACTION_ITEM_CLICK
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val flags = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val clickPendingIntent = PendingIntent.getBroadcast(context, widgetId, clickIntent, flags)
        rv.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent)

        appWidgetManager.updateAppWidget(widgetId, rv)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_ITEM_CLICK) {
            val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: return
            val isAddTile = intent.getBooleanExtra(EXTRA_IS_ADD_TILE, false)

            if (isAddTile) {
                val openApp = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(openApp)
            } else {
                val playIntent = Intent(context, SoundPlayService::class.java).apply {
                    putExtra(SoundPlayService.EXTRA_SOUND_ID, itemId)
                }
                context.startService(playIntent)
            }
        }
    }
}
