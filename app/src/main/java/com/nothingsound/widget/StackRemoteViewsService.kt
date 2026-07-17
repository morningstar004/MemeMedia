package com.nothingsound.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService

class StackRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        SoundStackFactory(applicationContext)

    class SoundStackFactory(private val context: android.content.Context) :
        RemoteViewsService.RemoteViewsFactory {

        private var sounds: List<SoundItem> = emptyList()

        override fun onCreate() { /* no-op, data loaded in onDataSetChanged */ }

        override fun onDataSetChanged() {
            sounds = SoundRepository.getAll(context)
        }

        override fun onDestroy() { sounds = emptyList() }

        override fun getCount(): Int = sounds.size

        override fun getViewAt(position: Int): RemoteViews {
            if (position >= sounds.size) return RemoteViews(context.packageName, R.layout.widget_stack_item)
            val item = sounds[position]
            val rv = RemoteViews(context.packageName, R.layout.widget_stack_item)

            rv.setTextViewText(R.id.tile_label, item.label)

            rv.setImageViewResource(
                R.id.tile_icon,
                when {
                    item.isAddTile -> R.drawable.ic_dot_add
                    item.id == "faah" -> R.drawable.ic_dot_wave
                    else -> R.drawable.ic_dot_meme
                }
            )

            // fillInIntent supplies the per-item extras onto the click template
            // registered on the StackView in SoundWidgetProvider.
            val fillInIntent = Intent().apply {
                putExtra(SoundWidgetProvider.EXTRA_ITEM_ID, item.id)
                putExtra(SoundWidgetProvider.EXTRA_IS_ADD_TILE, item.isAddTile)
            }
            rv.setOnClickFillInIntent(R.id.stack_item_root, fillInIntent)

            return rv
        }

        override fun getLoadingView(): RemoteViews? = null
        override fun getViewTypeCount(): Int = 1
        override fun getItemId(position: Int): Long = position.toLong()
        override fun hasStableIds(): Boolean = true
    }
}
