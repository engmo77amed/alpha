@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.fayroz.alpha.ui

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fayroz.alpha.data.ImportedSummary
import com.fayroz.alpha.data.LegacyImporter

@Composable fun AlphaApp() {
    val context=LocalContext.current
    var summary by remember { mutableStateOf<ImportedSummary?>(loadSummary(context)) }
    var importMessage by remember { mutableStateOf<String?>(null) }
    var selected by remember { mutableIntStateOf(0) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if(uri!=null) runCatching { context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { LegacyImporter.inspect(it.readText()) } ?: error("تعذر قراءة الملف") }
            .onSuccess { s -> saveSummary(context,s); summary=s; importMessage="تم فحص النسخة بنجاح. لم يتم حذف أو تعديل بيانات النسخة القديمة." }
            .onFailure { importMessage=it.message ?: "ملف النسخة غير صالح" }
    }
    Scaffold(containerColor=Paper, bottomBar={ NavigationBar(containerColor=Navy) { listOf("الرئيسية" to Icons.Rounded.Home,"المشروعات" to Icons.Rounded.Business,"الحسابات" to Icons.Rounded.AccountBalance,"الإعدادات" to Icons.Rounded.Settings).forEachIndexed { i,(name,icon)-> NavigationBarItem(selected=selected==i,onClick={selected=i},icon={Icon(icon,name)},label={Text(name)},colors=NavigationBarItemDefaults.colors(selectedIconColor=GoldLight,selectedTextColor=GoldLight,indicatorColor=Color.White.copy(.10f),unselectedIconColor=Color.White.copy(.60f),unselectedTextColor=Color.White.copy(.60f))) } } }) { padding ->
        when(selected) { 0 -> Home(Modifier.padding(padding),summary,{ picker.launch(arrayOf("application/json","text/plain")) },importMessage); 1 -> Projects(Modifier.padding(padding),summary); else -> Placeholder(Modifier.padding(padding),if(selected==2) "الحسابات" else "الإعدادات",if(selected==2) "دفعات العميل، الموردين، العُهد والمصروفات ستظهر هنا في مرحلة الحسابات." else "النسخ الاحتياطي والاستيراد وقفل التطبيق ستظهر هنا.") }
    }
}

@Composable private fun Home(modifier:Modifier,summary:ImportedSummary?,onImport:()->Unit,message:String?) { Column(modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) { Hero(); SummaryCards(summary); Button(onClick=onImport,modifier=Modifier.fillMaxWidth().height(56.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Navy)) { Icon(Icons.Rounded.UploadFile,null); Spacer(Modifier.width(9.dp)); Text("استيراد بيانات النسخة الحالية") }; message?.let { AssistCard(it) }; Text("إجراءات سريعة",style=MaterialTheme.typography.headlineSmall); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) { Action("مشروع جديد",Icons.Rounded.Add,Modifier.weight(1f)); Action("دفعة عميل",Icons.Rounded.Payments,Modifier.weight(1f)); Action("مصروف أو عهدة",Icons.Rounded.ReceiptLong,Modifier.weight(1f)) }; Text("خطة البناء",style=MaterialTheme.typography.headlineSmall); AssistCard("Alpha يبدأ بقاعدة مستقلة واستيراد غير هدّام. سيتم اختبار كل شاشة وكل ترقية بيانات قبل تسليمها.") } }
@Composable private fun Hero() { Surface(shape=RoundedCornerShape(28.dp),shadowElevation=7.dp,color=Navy) { Box(Modifier.fillMaxWidth().height(188.dp).background(Brush.linearGradient(listOf(Navy,Navy2)))) { Box(Modifier.align(Alignment.TopEnd).padding(22.dp).size(56.dp).clip(RoundedCornerShape(18.dp)).background(Color.White.copy(.10f)),contentAlignment=Alignment.Center) { Icon(Icons.Rounded.AccountBalance,null,tint=GoldLight,modifier=Modifier.size(31.dp)) }; Column(Modifier.align(Alignment.BottomStart).padding(22.dp)) { Text("Fayroz",color=Color.White,fontWeight=FontWeight.Black,fontSize=30.sp); Text("A C C O U N T S",color=GoldLight,fontSize=10.sp,letterSpacing=3.sp); Spacer(Modifier.height(8.dp)); Text("إدارة حسابات المشروعات\nبكل وضوح وتحت السيطرة",color=Color.White.copy(.88f),style=MaterialTheme.typography.bodyMedium) } } } }
@Composable private fun SummaryCards(summary:ImportedSummary?) { Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) { Metric("المشروعات",summary?.projects?:0,Color(0xFFE9EFF8),Color(0xFF356CA7),Modifier.weight(1f)); Metric("العملاء",summary?.clients?:0,Color(0xFFE7F3EE),Success,Modifier.weight(1f)); Metric("الحركات",(summary?.payments?:0)+(summary?.dues?:0)+(summary?.expenses?:0),Color(0xFFF8E9E8),Danger,Modifier.weight(1f)) } }
@Composable private fun Metric(label:String,value:Int,bg:Color,accent:Color,modifier:Modifier) { Card(modifier,colors=CardDefaults.cardColors(containerColor=bg),elevation=CardDefaults.cardElevation(0.dp)) { Column(Modifier.padding(12.dp)) { Icon(Icons.Rounded.PieChart,null,tint=accent,modifier=Modifier.size(20.dp)); Spacer(Modifier.height(8.dp)); Text(value.toString(),fontWeight=FontWeight.Black,fontSize=21.sp); Text(label,style=MaterialTheme.typography.labelLarge,color=Ink) } } }
@Composable private fun Action(text:String,icon:androidx.compose.ui.graphics.vector.ImageVector,modifier:Modifier) { Card(modifier,onClick={},colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(2.dp)) { Column(Modifier.height(112.dp).padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) { Icon(icon,null,tint=Gold,modifier=Modifier.size(28.dp)); Spacer(Modifier.height(8.dp)); Text(text,style=MaterialTheme.typography.labelLarge) } } }
@Composable private fun Projects(modifier:Modifier,summary:ImportedSummary?) { Placeholder(modifier,"المشروعات",if(summary==null) "استورد بيانات النسخة الحالية أولًا؛ ستظهر هنا كل المشروعات المرتبطة بالعملاء." else "تمت قراءة ${summary.projects} مشروع. سيتم عرضها في قائمة قابلة للبحث والتصفية.") }
@Composable private fun Placeholder(modifier:Modifier,title:String,body:String) { Column(modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.Center,horizontalAlignment=Alignment.CenterHorizontally) { Icon(Icons.Rounded.Construction,null,tint=Gold,modifier=Modifier.size(58.dp)); Spacer(Modifier.height(14.dp)); Text(title,style=MaterialTheme.typography.headlineSmall); Spacer(Modifier.height(8.dp)); Text(body,color=Muted,style=MaterialTheme.typography.bodyMedium) } }
@Composable private fun AssistCard(text:String) { Card(colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(1.dp)) { Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically) { Icon(Icons.Rounded.Info,null,tint=Gold); Spacer(Modifier.width(10.dp)); Text(text,color=Muted,style=MaterialTheme.typography.bodyMedium) } } }
private fun saveSummary(context:Context,s:ImportedSummary) { context.getSharedPreferences("alpha_import",Context.MODE_PRIVATE).edit().putString("summary","${s.clients},${s.projects},${s.payments},${s.dues},${s.expenses}").apply() }
private fun loadSummary(context:Context):ImportedSummary? { val raw=context.getSharedPreferences("alpha_import",Context.MODE_PRIVATE).getString("summary",null)?:return null; val p=raw.split(',').mapNotNull { it.toIntOrNull() }; return if(p.size==5) ImportedSummary(p[0],p[1],p[2],p[3],p[4],emptyList()) else null }
