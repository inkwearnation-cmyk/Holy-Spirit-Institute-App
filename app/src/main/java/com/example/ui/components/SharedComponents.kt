package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowBack
import java.util.Calendar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// --- Goa Ambient Sky & Academic Gradient Background ---
@Composable
fun GoaAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientColors = listOf(
        Color(0xFFEFF6FF), // Soft high-trust slate blue tint
        Color(0xFFF1F5F9), // Slate soft white
        LightBackground    // Slate soft white background base
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradientColors))
    ) {
        content()
    }
}

// --- Glassmorphic Card Overlay ---
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val containerColor = Color(0xFFFFFFFF) // Pure White Card Background
    val borderColor = BorderLight // Modern subtle light border

    Column(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(cornerRadius), clip = false)
            .background(containerColor, RoundedCornerShape(cornerRadius))
            .border(borderWidth, borderColor, RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .padding(20.dp)
    ) {
        content()
    }
}

// --- Dynamic Premium App Header ---
@Composable
fun GlassHeader(
    title: String,
    subtitle: String,
    onNotificationClick: () -> Unit,
    notificationCount: Int = 0,
    navigationIcon: (@Composable () -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    userAvatarText: String? = null
) {
    val isDark = isSystemInDarkTheme()
    val realHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        realHour < 12 -> "Good Morning 👋"
        realHour < 17 -> "Good Afternoon ☀️"
        else -> "Good Evening 🌙"
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, BorderLight, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = SchoolPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else if (navigationIcon != null) {
                navigationIcon()
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            Column {
                Text(
                    text = greeting,
                    color = LightTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    color = LightTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = LightTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification Badged Icon Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SchoolPrimary.copy(alpha = 0.08f))
                    .clickable { onNotificationClick() }
                    .testTag("notification_bell"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = SchoolPrimary,
                    modifier = Modifier.size(22.dp)
                )
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-2).dp, y = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SchoolDanger),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Profile Avatar representing premium educator/student/parent
            val initials = userAvatarText ?: if (title.length >= 2) {
                title.split(" ").map { it.take(1) }.take(2).joinToString("").uppercase()
            } else "U"
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SchoolPrimary, SchoolSecondary)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

// --- Stat Counter Item ---
@Composable
fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 14.dp,
        elevation = 3.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) DarkTextPrimary else LightTextPrimary
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = if (isSystemInDarkTheme()) DarkTextSecondary else LightTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- Native Canvas Analytics Bar Chart ---
@Composable
fun SchoolPerformanceBarChart(
    data: List<Pair<String, Float>>, // Labels and fractional percentage values (0.0 to 1.0)
    primaryColor: Color = SchoolPrimary,
    accentColor: Color = SchoolSecondary,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gridColor = if (isDark) Color(0x1FFFFFFF) else Color(0x1F000000)
    val textStyle = MaterialTheme.typography.bodySmall.copy(
        color = if (isDark) DarkTextSecondary else LightTextSecondary,
        fontSize = 10.sp
    )

    Column(modifier = modifier) {
        Text(
            text = "Academic Analytics & Marks Trend",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (isDark) DarkTextPrimary else LightTextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val bottomPadding = 30f
            val leftPadding = 40f
            val chartWidth = canvasWidth - leftPadding
            val chartHeight = canvasHeight - bottomPadding

            // Draw Y Gridlines (0%, 25%, 50%, 75%, 100%)
            val steps = 4
            for (i in 0..steps) {
                val y = chartHeight * (i.toFloat() / steps.toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1f
                )
            }

            // Draw Bars
            if (data.isNotEmpty()) {
                val barSpacing = chartWidth / data.size
                val barWidth = barSpacing * 0.4f

                data.forEachIndexed { index, pair ->
                    val x = leftPadding + (index * barSpacing) + (barSpacing - barWidth) / 2f
                    val barHeightFraction = pair.second.coerceIn(0f, 1f)
                    val topY = chartHeight - (chartHeight * barHeightFraction)

                    // Draw filled bar
                    drawRect(
                        brush = Brush.verticalGradient(listOf(primaryColor, accentColor)),
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, chartHeight - topY)
                    )

                    // Draw simple top dot
                    drawCircle(
                        color = accentColor,
                        radius = 4f,
                        center = Offset(x + barWidth / 2f, topY)
                    )
                }
            }
        }

        // Draw Labels Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEach {
                Text(
                    text = it.first,
                    style = textStyle,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// --- Native Canvas Line Graph for Attendance Logs ---
@Composable
fun AttendanceLineGraph(
    data: List<Float>, // Percentage array, e.g. [0.9f, 0.95f, 0.85f, 1.0f, 0.92f]
    lineColor: Color = SchoolSuccess,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val gridColor = if (isDark) Color(0x15FFFFFF) else Color(0x15000000)

    Column(modifier = modifier) {
        Text(
            text = "Weekly School Attendance Index",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (isDark) DarkTextPrimary else LightTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val chartWidth = canvasWidth
            val chartHeight = canvasHeight

            // Draw vertical guides
            val linesCount = 5
            for (i in 0 until linesCount) {
                val x = chartWidth * (i.toFloat() / (linesCount - 1).toFloat())
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 2f
                )
            }

            if (data.size > 1) {
                val path = Path()
                val stepX = chartWidth / (data.size - 1)

                data.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = chartHeight - (chartHeight * value.coerceIn(0f, 1f))
                    if (index == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                    drawCircle(
                        color = lineColor,
                        radius = 5f,
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = path,
                    color = lineColor,
                    style = Stroke(width = 4f)
                )
            }
        }
    }
}
