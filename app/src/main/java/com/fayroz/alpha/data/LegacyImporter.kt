package com.fayroz.alpha.data

import java.io.ByteArrayInputStream
import java.util.zip.GZIPInputStream
import org.json.JSONArray
import org.json.JSONObject

data class ImportedBackup(val normalizedJson: String, val summary: ImportedSummary)

/** Imports Fayroz Accounts backups regardless of file extension (.json or .bin). */
object LegacyImporter {
    fun import(bytes: ByteArray): ImportedBackup {
        require(bytes.isNotEmpty()) { "ملف النسخة الاحتياطية فارغ." }
        val raw = decode(bytes).trim().removePrefix("\uFEFF")
        require(raw.startsWith("{")) {
            "ملف BIN المختار ليس نسخة Fayroz Accounts مقروءة. من التطبيق القديم اختر: الإعدادات ← نسخة احتياطية ← تصدير، ثم اختر الملف الناتج هنا."
        }
        val root = JSONObject(raw)
        val db = root.optJSONObject("db") ?: root
        val required = listOf("clients", "projects", "payments", "dues")
        require(required.all { db.opt(it) is JSONArray }) { "الملف ليس نسخة Fayroz Accounts صحيحة أو أن بياناته ناقصة." }
        val normalized = JSONObject(db.toString()).apply {
            if (!has("costItems")) put("costItems", JSONArray())
            if (!has("expenses")) put("expenses", JSONArray())
        }
        fun count(key: String) = normalized.optJSONArray(key)?.length() ?: 0
        val warnings = buildList {
            if (!db.has("costItems")) add("نسخة قديمة: تم تجهيز بنود التكلفة الناقصة.")
            if (!db.has("expenses")) add("النسخة لا تحتوي على مصروفات تفصيلية.")
        }
        return ImportedBackup(normalized.toString(), ImportedSummary(count("clients"), count("projects"), count("payments"), count("dues"), count("expenses"), warnings))
    }

    private fun decode(bytes: ByteArray): String {
        val plain = bytes.toString(Charsets.UTF_8)
        if (plain.trimStart().startsWith("{")) return plain
        return try { GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() } } catch (_: Exception) { plain }
    }
}
