package com.fayroz.sitecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import kotlin.math.ceil

private enum class Screen { HOME, ROOM, FLOOR, WALL_TILES, GYPSUM, MASONRY, WATERPROOF }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    App()
                }
            }
        }
    }
}

@Composable
private fun App() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    Scaffold(
        topBar = {
            Surface(shadowElevation = 4.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (screen == Screen.HOME) "FAYROZ SITE CALCULATOR" else titleOf(screen),
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                        Text(
                            if (screen == Screen.HOME) "حاسبة كميات الموقع السريعة" else "الفيروز للمقاولات العامة",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (screen != Screen.HOME) {
                        TextButton(onClick = { screen = Screen.HOME }) { Text("الرئيسية") }
                    }
                }
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (screen) {
                Screen.HOME -> Home { screen = it }
                Screen.ROOM -> RoomCalculator()
                Screen.FLOOR -> FlooringCalculator()
                Screen.WALL_TILES -> WallTilesCalculator()
                Screen.GYPSUM -> GypsumCalculator()
                Screen.MASONRY -> MasonryCalculator()
                Screen.WATERPROOF -> WaterproofCalculator()
            }
        }
    }
}

private fun titleOf(s: Screen) = when (s) {
    Screen.ROOM -> "حصر الفراغ"
    Screen.FLOOR -> "الأرضيات"
    Screen.WALL_TILES -> "سيراميك الحوائط"
    Screen.GYPSUM -> "الجبس بورد"
    Screen.MASONRY -> "المباني"
    Screen.WATERPROOF -> "العزل"
    else -> "FAYROZ"
}

@Composable
private fun Home(open: (Screen) -> Unit) {
    val items = listOf(
        Triple("محارة + دهانات + أسقف + وزرات", "أدخل أبعاد الغرفة والفتحات مرة واحدة", Screen.ROOM),
        Triple("الأرضيات", "مساحة + هالك + عدد البلاطات", Screen.FLOOR),
        Triple("سيراميك الحوائط", "محيط × ارتفاع مع خصم الفتحات", Screen.WALL_TILES),
        Triple("الجبس بورد", "مسطح السقف وعدد الألواح", Screen.GYPSUM),
        Triple("المباني", "مساحة وحجم الحائط بعد الخصم", Screen.MASONRY),
        Triple("العزل", "أرضية + رقبة العزل", Screen.WATERPROOF)
    )
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text("الكمية الصح في ثواني", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Spacer(Modifier.height(6.dp))
                    Text("مصمم للاستخدام السريع في الموقع — بدون إنترنت.")
                }
            }
        }
        items(items.size) { i ->
            val item = items[i]
            Card(onClick = { open(item.third) }) {
                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                    Text(item.first, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(item.second)
                }
            }
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { s -> onValue(s.filter { it.isDigit() || it == '.' }) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun d(s: String) = s.toDoubleOrNull() ?: 0.0
private fun f(v: Double) = if (v.isFinite()) String.format("%.2f", v) else "0.00"

@Composable
private fun Result(label: String, value: String, unit: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text("$value $unit", fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Page(content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card {
                Column(Modifier.fillMaxWidth().padding(16.dp), content = content)
            }
        }
    }
}

@Composable
private fun RoomCalculator() {
    var l by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var h by remember { mutableStateOf("") }
    var doors by remember { mutableStateOf("") }
    var windows by remember { mutableStateOf("") }
    var waste by remember { mutableStateOf("5") }

    val length = d(l); val width = d(w); val height = d(h)
    val floor = length * width
    val perimeter = 2 * (length + width)
    val grossWalls = perimeter * height
    val openings = d(doors) + d(windows)
    val netWalls = (grossWalls - openings).coerceAtLeast(0.0)
    val plasterWallsCeiling = netWalls + floor
    val skirting = (perimeter - if (height > 0) d(doors) / height else 0.0).coerceAtLeast(0.0)
    val factor = 1 + d(waste) / 100.0

    Page {
        Text("أبعاد الفراغ", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("الطول بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("العرض بالمتر", w) { w = it }
        Spacer(Modifier.height(8.dp))
        NumberField("الارتفاع بالمتر", h) { h = it }
        Spacer(Modifier.height(8.dp))
        NumberField("إجمالي مساحة الأبواب بالمتر المربع", doors) { doors = it }
        Spacer(Modifier.height(8.dp))
        NumberField("إجمالي مساحة الشبابيك بالمتر المربع", windows) { windows = it }
        Spacer(Modifier.height(8.dp))
        NumberField("هالك %", waste) { waste = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("الأرضيات", f(floor * factor), "م²")
        Result("السقف", f(floor * factor), "م²")
        Result("صافي الحوائط", f(netWalls), "م²")
        Result("محارة الحوائط + السقف", f(plasterWallsCeiling * factor), "م²")
        Result("دهانات الحوائط", f(netWalls * factor), "م²")
        Result("الوزرات تقريبًا", f(skirting * factor), "م ط")
    }
}

@Composable
private fun FlooringCalculator() {
    var l by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var waste by remember { mutableStateOf("7") }
    var tw by remember { mutableStateOf("60") }
    var th by remember { mutableStateOf("60") }

    val area = d(l) * d(w)
    val finalArea = area * (1 + d(waste) / 100.0)
    val tileArea = d(tw) / 100.0 * d(th) / 100.0
    val pieces = if (tileArea > 0) ceil(finalArea / tileArea).toInt() else 0

    Page {
        Text("حصر الأرضيات", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("الطول بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("العرض بالمتر", w) { w = it }
        Spacer(Modifier.height(8.dp))
        NumberField("الهالك %", waste) { waste = it }
        Spacer(Modifier.height(8.dp))
        NumberField("عرض البلاطة بالسنتيمتر", tw) { tw = it }
        Spacer(Modifier.height(8.dp))
        NumberField("طول البلاطة بالسنتيمتر", th) { th = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("المسطح الصافي", f(area), "م²")
        Result("الكمية بالهالك", f(finalArea), "م²")
        Result("عدد البلاطات", pieces.toString(), "قطعة")
    }
}

@Composable
private fun WallTilesCalculator() {
    var l by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var h by remember { mutableStateOf("2.4") }
    var openings by remember { mutableStateOf("") }
    var waste by remember { mutableStateOf("7") }
    val net = (2 * (d(l) + d(w)) * d(h) - d(openings)).coerceAtLeast(0.0)
    val finalArea = net * (1 + d(waste) / 100.0)

    Page {
        Text("سيراميك الحوائط", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("طول الفراغ بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("عرض الفراغ بالمتر", w) { w = it }
        Spacer(Modifier.height(8.dp))
        NumberField("ارتفاع السيراميك بالمتر", h) { h = it }
        Spacer(Modifier.height(8.dp))
        NumberField("مساحة الفتحات بالمتر المربع", openings) { openings = it }
        Spacer(Modifier.height(8.dp))
        NumberField("الهالك %", waste) { waste = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("الصافي", f(net), "م²")
        Result("المطلوب بالهالك", f(finalArea), "م²")
    }
}

@Composable
private fun GypsumCalculator() {
    var l by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var waste by remember { mutableStateOf("10") }
    var boardW by remember { mutableStateOf("1.2") }
    var boardL by remember { mutableStateOf("2.4") }
    val area = d(l) * d(w)
    val finalArea = area * (1 + d(waste) / 100.0)
    val boardArea = d(boardW) * d(boardL)
    val boards = if (boardArea > 0) ceil(finalArea / boardArea).toInt() else 0

    Page {
        Text("الجبس بورد", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("الطول بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("العرض بالمتر", w) { w = it }
        Spacer(Modifier.height(8.dp))
        NumberField("الهالك %", waste) { waste = it }
        Spacer(Modifier.height(8.dp))
        NumberField("عرض اللوح بالمتر", boardW) { boardW = it }
        Spacer(Modifier.height(8.dp))
        NumberField("طول اللوح بالمتر", boardL) { boardL = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("المسطح", f(area), "م²")
        Result("المسطح بالهالك", f(finalArea), "م²")
        Result("عدد الألواح التقريبي", boards.toString(), "لوح")
    }
}

@Composable
private fun MasonryCalculator() {
    var l by remember { mutableStateOf("") }
    var h by remember { mutableStateOf("") }
    var t by remember { mutableStateOf("0.12") }
    var openings by remember { mutableStateOf("") }
    val area = (d(l) * d(h) - d(openings)).coerceAtLeast(0.0)
    val volume = area * d(t)

    Page {
        Text("المباني", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("طول الحائط بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("ارتفاع الحائط بالمتر", h) { h = it }
        Spacer(Modifier.height(8.dp))
        NumberField("سمك الحائط بالمتر", t) { t = it }
        Spacer(Modifier.height(8.dp))
        NumberField("مساحة الفتحات بالمتر المربع", openings) { openings = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("صافي مساحة المباني", f(area), "م²")
        Result("حجم المباني", f(volume), "م³")
    }
}

@Composable
private fun WaterproofCalculator() {
    var l by remember { mutableStateOf("") }
    var w by remember { mutableStateOf("") }
    var upstand by remember { mutableStateOf("0.20") }
    var waste by remember { mutableStateOf("5") }
    val floor = d(l) * d(w)
    val neck = 2 * (d(l) + d(w)) * d(upstand)
    val total = (floor + neck) * (1 + d(waste) / 100.0)

    Page {
        Text("العزل", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        NumberField("الطول بالمتر", l) { l = it }
        Spacer(Modifier.height(8.dp))
        NumberField("العرض بالمتر", w) { w = it }
        Spacer(Modifier.height(8.dp))
        NumberField("ارتفاع رقبة العزل بالمتر", upstand) { upstand = it }
        Spacer(Modifier.height(8.dp))
        NumberField("الهالك %", waste) { waste = it }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Result("مسطح الأرضية", f(floor), "م²")
        Result("رقبة العزل", f(neck), "م²")
        Result("إجمالي العزل بالهالك", f(total), "م²")
    }
}
