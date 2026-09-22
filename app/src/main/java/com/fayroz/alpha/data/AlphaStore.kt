package com.fayroz.alpha.data

import android.content.Context
import java.io.File
import org.json.JSONObject

/** Atomic local copy of imported Fayroz Accounts data; the original backup is never changed. */
class AlphaStore(private val context: Context) {
    private val dataFile = File(context.filesDir, "fayroz-import.json")
    private val tempFile = File(context.filesDir, "fayroz-import.tmp")
    private val prefs = context.getSharedPreferences("alpha_import", Context.MODE_PRIVATE)

    fun replace(backup: ImportedBackup) {
        tempFile.writeText(backup.normalizedJson, Charsets.UTF_8)
        if (dataFile.exists()) check(dataFile.delete()) { "تعذر استبدال نسخة البيانات القديمة داخل Alpha." }
        check(tempFile.renameTo(dataFile)) { "تعذر حفظ النسخة داخل Alpha." }
        prefs.edit().putString("summary", listOf(backup.summary.clients, backup.summary.projects, backup.summary.payments, backup.summary.dues, backup.summary.expenses).joinToString(",")).apply()
    }

    fun summary(): ImportedSummary? {
        if (!dataFile.exists()) return null
        val values = prefs.getString("summary", null)?.split(',')?.mapNotNull { it.toIntOrNull() } ?: return null
        return values.takeIf { it.size == 5 }?.let { ImportedSummary(it[0], it[1], it[2], it[3], it[4], emptyList()) }
    }

    fun projectNames(): List<String> = runCatching {
        val items = JSONObject(dataFile.readText()).optJSONArray("projects") ?: return emptyList()
        List(items.length()) { index ->
            val item = items.optJSONObject(index)
            item?.optString("name")?.takeIf { it.isNotBlank() }
                ?: item?.optString("title")?.takeIf { it.isNotBlank() }
                ?: "مشروع بدون اسم"
        }
    }.getOrDefault(emptyList())
}
