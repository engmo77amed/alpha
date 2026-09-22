package com.fayroz.alpha.data

import org.json.JSONArray
import org.json.JSONObject

/** Normalizes both historic Fayroz Accounts JSON backup shapes without changing IDs or linked references. */
object LegacyImporter {
    fun inspect(raw:String): ImportedSummary {
        val root=JSONObject(raw)
        val db=root.optJSONObject("db") ?: root
        fun count(key:String):Int = when(val value=db.opt(key)) { is JSONArray -> value.length(); else -> 0 }
        val clients=count("clients"); val projects=count("projects"); val payments=count("payments"); val dues=count("dues"); val expenses=count("expenses")
        require(clients>0 || projects>0 || payments>0 || dues>0 || expenses>0) { "ملف النسخة الاحتياطية لا يحتوي على بيانات Fayroz Accounts قابلة للاستيراد." }
        val warnings=mutableListOf<String>()
        if(!db.has("costItems")) warnings += "تمت قراءة نسخة قديمة؛ سيتم إنشاء بنود التكلفة الناقصة تلقائيًا."
        if(!db.has("expenses")) warnings += "لا توجد مصروفات تفصيلية داخل هذه النسخة."
        return ImportedSummary(clients,projects,payments,dues,expenses,warnings)
    }
}
