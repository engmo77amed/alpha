package com.fayroz.alpha.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Navy=Color(0xFF071525); val Navy2=Color(0xFF102B44); val Gold=Color(0xFFCDA77B); val GoldLight=Color(0xFFF3D7B3); val Paper=Color(0xFFF5F5F3); val Ink=Color(0xFF102334); val Muted=Color(0xFF617184); val Success=Color(0xFF2D8672); val Danger=Color(0xFFB94D52)
private val Colors=lightColorScheme(primary=Navy,onPrimary=Color.White,primaryContainer=Color(0xFFE9EFF3),onPrimaryContainer=Navy,secondary=Gold,onSecondary=Navy,secondaryContainer=Color(0xFFF7E9D9),background=Paper,surface=Color.White,onSurface=Ink,onSurfaceVariant=Muted,error=Danger,errorContainer=Color(0xFFFCE9E9),outline=Color(0xFFD8E0E6))
private val Type=Typography(displaySmall=TextStyle(fontWeight=FontWeight.Black,fontSize=30.sp),headlineSmall=TextStyle(fontWeight=FontWeight.ExtraBold,fontSize=21.sp),titleLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=18.sp),titleMedium=TextStyle(fontWeight=FontWeight.Bold,fontSize=16.sp),bodyMedium=TextStyle(fontSize=14.sp),labelLarge=TextStyle(fontWeight=FontWeight.Bold,fontSize=13.sp))
@Composable fun FayrozAlphaTheme(content:@Composable()->Unit)=MaterialTheme(colorScheme=Colors,typography=Type,shapes=androidx.compose.material3.Shapes(small=RoundedCornerShape(14.dp),medium=RoundedCornerShape(20.dp),large=RoundedCornerShape(28.dp)),content=content)
