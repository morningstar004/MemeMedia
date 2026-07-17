package com.nothingsound.widget

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject

/**
 * One entry in the widget's swipeable sound deck.
 *
 * [resName] is a raw resource name (e.g. "faah") for sounds bundled with the app.
 * [filePath] is an absolute path on disk for sounds the user added at runtime.
 * Exactly one of the two should be non-null.
 */
data class SoundItem(
    val id: String,
    val label: String,
    val resName: String? = null,
    val filePath: String? = null,
    val isAddTile: Boolean = false,
)

object SoundRepository {

    private const val PREFS = "nothing_sound_widget_prefs"
    private const val KEY_SOUNDS = "sounds_json"
    private const val ADD_TILE_ID = "__add__"

    /** Index 0 is always the primary tap sound ("faah"). */
    private fun defaults(): List<SoundItem> = listOf(
        SoundItem(id = "faah", label = "faah", resName = "faah"),
        SoundItem(id = "bruh", label = "bruh", resName = "bruh"),
        SoundItem(id = "vine_boom", label = "vine boom", resName = "vine_boom"),
        SoundItem(id = "airhorn", label = "airhorn", resName = "airhorn")
    )

    fun getAll(context: Context): List<SoundItem> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_SOUNDS, null)
        val list = raw?.let { fromJson(it) } ?: defaults()
        // The "+ add sound" tile always sits at the end of the deck.
        return list + SoundItem(id = ADD_TILE_ID, label = "add sound", isAddTile = true)
    }

    fun addUserSound(context: Context, label: String, filePath: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = getAll(context).filterNot { it.isAddTile }.toMutableList()
        current.add(SoundItem(id = "user_${System.currentTimeMillis()}", label = label, filePath = filePath))
        prefs.edit { putString(KEY_SOUNDS, toJson(current)) }
    }

    fun removeSound(context: Context, id: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = getAll(context).filterNot { (it.isAddTile || it.id == id) }
        prefs.edit { putString(KEY_SOUNDS, toJson(current)) }
    }

    fun findById(context: Context, id: String): SoundItem? =
        getAll(context).firstOrNull { it.id == id }

    private fun toJson(list: List<SoundItem>): String {
        val arr = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("label", item.label)
            item.resName?.let { obj.put("resName", it) }
            item.filePath?.let { obj.put("filePath", it) }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun fromJson(raw: String): List<SoundItem> {
        val arr = JSONArray(raw)
        return (0 until arr.length()).map { i ->
            val obj = arr.getJSONObject(i)
            SoundItem(
                id = obj.getString("id"),
                label = obj.getString("label"),
                resName = obj.optString("resName", null.toString()).takeIf { it != "null" },
                filePath = obj.optString("filePath", null.toString()).takeIf { it != "null" }
            )
        }
    }
}
