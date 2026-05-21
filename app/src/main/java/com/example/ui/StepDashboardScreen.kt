package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

enum class AppTab {
    DASHBOARD, WORKOUT, HEALTH, WATCHES, COACH, SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepDashboardScreen(
    viewModel: StepViewModel,
    onRequestPermission: () -> Unit
) {
    var currentTab by remember { mutableStateOf(AppTab.DASHBOARD) }

    val todaySteps by viewModel.todaySteps.collectAsState()
    val historySteps by viewModel.allHistory.collectAsState()
    val devices by viewModel.allDevices.collectAsState()
    val vitals by viewModel.todayVitals.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val darkBackground = MaterialTheme.colorScheme.background

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(primaryColor, tertiaryColor)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DirectionsRun,
                                contentDescription = "StepSync Logo icon",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "StepSync Pro",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    val isConnected = devices.any { it.isConnected }
                    Box(modifier = Modifier.padding(end = 16.dp), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { viewModel.requestAutoInsight() }) {
                            Icon(
                                imageVector = Icons.Filled.CloudSync,
                                contentDescription = "Manual Cloud Sync Trigger",
                                tint = if (isConnected) primaryColor else Color.Gray,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        if (isConnected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(top = 8.dp, end = 8.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = darkBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xBB0A0E17),
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TabItem(
                        iconSelected = Icons.Filled.Home,
                        iconUnselected = Icons.Outlined.Home,
                        label = "Activity",
                        isSelected = currentTab == AppTab.DASHBOARD,
                        color = primaryColor,
                        onClick = { currentTab = AppTab.DASHBOARD }
                    )
                    TabItem(
                        iconSelected = Icons.Filled.Map,
                        iconUnselected = Icons.Outlined.Map,
                        label = "GPS",
                        isSelected = currentTab == AppTab.WORKOUT,
                        color = secondaryColor,
                        onClick = { currentTab = AppTab.WORKOUT }
                    )
                    TabItem(
                        iconSelected = Icons.Filled.Favorite,
                        iconUnselected = Icons.Outlined.FavoriteBorder,
                        label = "Vitals",
                        isSelected = currentTab == AppTab.HEALTH,
                        color = tertiaryColor,
                        onClick = { currentTab = AppTab.HEALTH }
                    )
                    TabItem(
                        iconSelected = Icons.Filled.Watch,
                        iconUnselected = Icons.Outlined.Watch,
                        label = "Watch",
                        isSelected = currentTab == AppTab.WATCHES,
                        color = primaryColor,
                        onClick = { currentTab = AppTab.WATCHES }
                    )
                    TabItem(
                        iconSelected = Icons.Filled.Psychology,
                        iconUnselected = Icons.Outlined.Psychology,
                        label = "AI Coach",
                        isSelected = currentTab == AppTab.COACH,
                        color = secondaryColor,
                        onClick = { currentTab = AppTab.COACH }
                    )
                }
            }
        },
        containerColor = darkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
                },
                label = "ScreenSwitch"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.DASHBOARD -> DashboardTabScreen(viewModel, onRequestPermission)
                    AppTab.WORKOUT -> WorkoutTabScreen(viewModel)
                    AppTab.HEALTH -> HealthTabScreen(viewModel)
                    AppTab.WATCHES -> WatchesTabScreen(viewModel, onRequestPermission)
                    AppTab.COACH -> CoachTabScreen(viewModel)
                    else -> DashboardTabScreen(viewModel, onRequestPermission)
                }
            }
        }
    }
}

@Composable
fun RowScope.TabItem(
    iconSelected: androidx.compose.ui.graphics.vector.ImageVector,
    iconUnselected: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSelected) iconSelected else iconUnselected,
            contentDescription = label,
            tint = if (isSelected) color else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

// ==========================================
// TAB SCREEN 1: THE HIGH-END FITNESS DASHBOARD
// ==========================================
@Composable
fun DashboardTabScreen(
    viewModel: StepViewModel,
    onRequestPermission: () -> Unit
) {
    val todaySteps by viewModel.todaySteps.collectAsState()
    val historySteps by viewModel.allHistory.collectAsState()
    val todayVitals by viewModel.todayVitals.collectAsState()
    val devices by viewModel.allDevices.collectAsState()

    var showGoalSettings by remember { mutableStateOf(false) }

    val stepsCount = todaySteps.steps
    val stepsGoal = todaySteps.goal
    val percentFraction = if (stepsGoal > 0) (stepsCount.toFloat() / stepsGoal) else 0f
    
    val calories = stepsCount * 0.04f
    val distance = stepsCount * 0.00075f
    val minutes = stepsCount / 120

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate = sdf.format(Date())

    val animatedPercent by animateFloatAsState(
        targetValue = percentFraction.coerceAtMost(2.0f),
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "DashboardRing"
    )
    val animatedSteps by animateIntAsState(
        targetValue = stepsCount,
        animationSpec = tween(durationMillis = 800),
        label = "DashboardSteps"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x1F1E293B))
                .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Welcome Back, Athlete",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "All wearable sensor channels in sync",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.OfflineBolt,
                        contentDescription = "Active Streak",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "7 Days Streak",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.2f),
            contentAlignment = Alignment.Center
        ) {
            val trackColor = Color(0x12FFFFFF)
            val primaryBrush = Brush.sweepGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary,
                    MaterialTheme.colorScheme.primary
                )
            )
            val tertiaryColor = MaterialTheme.colorScheme.tertiary

            Canvas(modifier = Modifier.fillMaxSize(0.88f)) {
                val radius = size.minDimension / 2 - 20.dp.toPx()
                val arcSize = Size(radius * 2, radius * 2)
                val topLeft = Offset(
                    x = (size.width - radius * 2) / 2,
                    y = (size.height - radius * 2) / 2
                )

                drawCircle(
                    color = trackColor,
                    radius = radius,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )

                drawArc(
                    brush = primaryBrush,
                    startAngle = -220f,
                    sweepAngle = animatedPercent * 260f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                )

                if (percentFraction > 1.0f) {
                    drawArc(
                        color = tertiaryColor,
                        startAngle = -220f,
                        sweepAngle = ((percentFraction - 1.0f).coerceAtMost(1.0f)) * 260f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Filled.DirectionsWalk,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = DecimalFormat("#,###").format(animatedSteps),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.testTag("today_steps_count_text")
                )
                Text(
                    text = "/ ${DecimalFormat("#,###").format(stepsGoal)} steps goal",
                    fontSize = 13.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = Color(0x33475569),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.clickable { showGoalSettings = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(percentFraction * 100).toInt()}% completed",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit steps goal",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiMetricCard(
                scoreColor = MaterialTheme.colorScheme.secondary,
                label = "Calories",
                score = String.format(Locale.getDefault(), "%.1f", calories),
                unit = "kcal",
                icon = Icons.Filled.LocalFireDepartment,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                scoreColor = MaterialTheme.colorScheme.primary,
                label = "Distance",
                score = String.format(Locale.getDefault(), "%.2f", distance),
                unit = "km",
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KpiMetricCard(
                scoreColor = MaterialTheme.colorScheme.tertiary,
                label = "Active Workout",
                score = "$minutes",
                unit = "min",
                icon = Icons.Filled.AccessTime,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                scoreColor = Color(0xFFFF5400),
                label = "Avg Heart Rate",
                score = "${todayVitals.avgHeartRate}",
                unit = "bpm",
                icon = Icons.Filled.FavoriteBorder,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("simulation_tools_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Quick Step Simulator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Instant Stride Simulator",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = "No smartwatch? Rapidly iterate step counts and telemetry below to stress-test charts and AI health coaching.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addSteps(350) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sim_add_100_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("+350 Steps")
                    }
                    Button(
                        onClick = { viewModel.addSteps(2000) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sim_add_5000_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("+2k Stride")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.resetToday() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("reset_today_button")
                    ) {
                        Text("Reset steps")
                    }
                    OutlinedButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("clear_history_button")
                    ) {
                        Text("Clear stats DB")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Weekly Activity Performance",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                val graphItems = historySteps.filter { it.date != todayDate }.take(7).reversed()
                if (graphItems.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Pending logs synchronicity. Keep stepping!", fontSize = 12.sp, color = Color.Gray)
                    }
                } else {
                    val peakValue = (graphItems.maxOfOrNull { it.steps } ?: 10000).coerceAtLeast(1000)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        graphItems.forEach { log ->
                            val heightFraction = log.steps.toFloat() / peakValue
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom
                            ) {
                                Text(
                                    text = "${log.steps / 1000}k",
                                    fontSize = 9.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(0.55f)
                                        .fillMaxHeight(heightFraction.coerceIn(0.1f, 1.0f))
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                colors = if (log.steps >= log.goal) listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                                else listOf(Color.Gray.copy(0.3f), Color.LightGray.copy(0.4f))
                                            )
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatDayLabel(log.date),
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x177209B7)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MilitaryTech,
                        contentDescription = "Achievements",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlocked Wearable Badges",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BadgeButton(name = "Cadence", icon = Icons.Filled.Speed, active = stepsCount >= 5000)
                    BadgeButton(name = "Century", icon = Icons.Filled.EmojiEvents, active = stepsCount >= 10000)
                    BadgeButton(name = "Supercharged", icon = Icons.Filled.Bolt, active = stepsCount >= 15000)
                    BadgeButton(name = "Explorer", icon = Icons.Filled.Explore, active = historySteps.size >= 5)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showGoalSettings) {
        Dialog(onDismissRequest = { showGoalSettings = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1E293B),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Target Stride Goal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge
                    )

                    var draftGoal by remember { mutableStateOf(stepsGoal.toFloat()) }

                    Text(
                        text = "${draftGoal.toInt()} Steps",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Slider(
                        value = draftGoal,
                        onValueChange = { draftGoal = it },
                        valueRange = 2000f..25000f,
                        steps = 22,
                        modifier = Modifier.testTag("step_goal_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = { showGoalSettings = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                viewModel.setGoal(draftGoal.toInt())
                                showGoalSettings = false
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_step_goal_button")
                        ) {
                            Text("Set Target")
                        }
                    }
                }
            }
        }
    }
}

fun formatDayLabel(dateStr: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())
        val date = parser.parse(dateStr) ?: Date()
        formatter.format(date)
    } catch (e: Exception) {
        dateStr.takeLast(5)
    }
}

@Composable
fun BadgeButton(name: String, icon: androidx.compose.ui.graphics.vector.ImageVector, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(if (active) Color(0x40B5179E) else Color(0x11FFFFFF))
                .border(
                    2.dp,
                    if (active) MaterialTheme.colorScheme.tertiary else Color.Gray.copy(0.3f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (active) MaterialTheme.colorScheme.tertiary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = name, fontSize = 10.sp, color = if (active) Color.White else Color.Gray)
    }
}

@Composable
fun KpiMetricCard(
    scoreColor: Color,
    label: String,
    score: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = scoreColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = score, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = unit, fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}

// ==========================================
// TAB SCREEN 2: ACTIVE GPS WORKOUT TRACKER
// ==========================================
@Composable
fun WorkoutTabScreen(viewModel: StepViewModel) {
    val isTracking by viewModel.isTrackingWorkout.collectAsState()
    val workoutTimeSeconds by viewModel.workoutTimeSeconds.collectAsState()
    val routePoints by viewModel.workoutRoutePoints.collectAsState()
    val workoutsHistory by viewModel.allWorkouts.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    for (x in 0..w.toInt() step 60) {
                        drawLine(
                            color = Color(0x0A94A3B8),
                            start = Offset(x.toFloat(), 0f),
                            end = Offset(x.toFloat(), h)
                        )
                    }
                    for (y in 0..h.toInt() step 60) {
                        drawLine(
                            color = Color(0x0A94A3B8),
                            start = Offset(0f, y.toFloat()),
                            end = Offset(w, y.toFloat())
                        )
                    }

                    val shapePath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.1f, h * 0.5f)
                        lineTo(w * 0.5f, h * 0.2f)
                        lineTo(w * 0.9f, h * 0.6f)
                        lineTo(w * 0.7f, h * 0.9f)
                        close()
                    }
                    drawPath(path = shapePath, color = Color(0x0B00FF9D))

                    if (routePoints.size > 1) {
                        val strokePx = 6.dp.toPx()
                        var currentX = w / 2
                        var currentY = h / 2

                        routePoints.forEachIndexed { i, pt ->
                            val drawX = currentX + (i * 9f).coerceIn(-currentX, currentX)
                            val drawY = currentY - (i * 4f).coerceIn(-currentY, currentY)

                            if (i > 0) {
                                val prevX = currentX + ((i - 1) * 9f).coerceIn(-currentX, currentX)
                                val prevY = currentY - ((i - 1) * 4f).coerceIn(-currentY, currentY)
                                drawLine(
                                    color = Color(0xFF00FF9D),
                                    start = Offset(prevX, prevY),
                                    end = Offset(drawX, drawY),
                                    strokeWidth = strokePx,
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        val finX = currentX + ((routePoints.size - 1) * 9f).coerceIn(-currentX, currentX)
                        val finY = currentY - ((routePoints.size - 1) * 4f).coerceIn(-currentY, currentY)
                        drawCircle(
                            color = Color(0xFF00F5FF),
                            radius = 10.dp.toPx(),
                            center = Offset(finX, finY)
                        )
                    } else {
                        drawCircle(
                            color = Color(0xFFFF5400),
                            radius = 6.dp.toPx(),
                            center = Offset(w * 0.5f, h * 0.5f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xCC1E293B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isTracking) Color(0xFF00FF9D) else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isTracking) "GPS CONNECTED" else "GPS STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color(0xCC1E293B),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "21°C AQI: 32", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (!isTracking) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x60090D16)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Map, contentDescription = null, tint = Color.White, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Fitness Trail Standby", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xEE1E293B))
                            .border(1.dp, Color(0xFF00FF9D))
                            .padding(12.dp)
                    ) {
                        val minutesValue = workoutTimeSeconds / 60
                        val secondsValue = workoutTimeSeconds % 60
                        val textTimer = String.format(Locale.getDefault(), "%02d:%02d", minutesValue, secondsValue)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Timeline, contentDescription = null, tint = Color(0xFF00FF9D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Elapsed Live Workout: $textTimer",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "High Intensity Route Workout",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Track real-time distance on premium map nodes. Accelerometer coordinates synchronize with smartwatch and sync stats to databases.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                if (!isTracking) {
                    var selectedType by remember { mutableStateOf("Walking") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        WorkoutSelectorPill(name = "Walking", selected = selectedType == "Walking") { selectedType = "Walking" }
                        WorkoutSelectorPill(name = "Running", selected = selectedType == "Running") { selectedType = "Running" }
                        WorkoutSelectorPill(name = "Cycling", selected = selectedType == "Cycling") { selectedType = "Cycling" }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.startWorkout(selectedType) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Workout Track Session")
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.stopAndSaveWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Run")
                        }

                        Button(
                            onClick = { viewModel.discardWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Discard")
                        }
                    }
                }
            }
        }

        Text(
            text = "GPS Performance History",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )

        if (workoutsHistory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No GPS tracks recorded yet.", color = Color.Gray)
            }
        } else {
            workoutsHistory.forEach { wk ->
                val timeMinutes = wk.durationSeconds / 60
                val displayTime = if (timeMinutes > 0) "$timeMinutes mins" else "${wk.durationSeconds} secs"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0x3300FF9D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (wk.type == "Running") Icons.Filled.DirectionsRun else Icons.Filled.DirectionsBike,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = wk.type, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "$displayTime • ${wk.calories} kcal burned", fontSize = 12.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "${wk.distanceKm} KM", fontWeight = FontWeight.Black, color = Color.White)
                            Text(text = "Synced", fontSize = 10.sp, color = primaryColor)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// TAB SCREEN 3: HEALTH VITALS MONITOR
// ==========================================
@Composable
fun HealthTabScreen(viewModel: StepViewModel) {
    val vitals by viewModel.todayVitals.collectAsState()
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    var isScanningHeart by remember { mutableStateOf(false) }
    var mockWaveProgress by remember { mutableStateOf(0f) }

    LaunchedEffect(isScanningHeart) {
        if (isScanningHeart) {
            var i = 0f
            while (isScanningHeart) {
                delay(30)
                mockWaveProgress = i
                i += 0.05f
                if (i > 1.0f) {
                    i = 0f
                    viewModel.triggerHeartRateScan()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Bio-Sensory Heart Rate Scanner", fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(Color(0xFF070B14))
                        .border(1.dp, Color(0x1F94A3B8), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val pulseColor = Color.Red
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val stroke = 3.dp.toPx()

                        if (isScanningHeart) {
                            val p = mockWaveProgress
                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(0f, h * 0.5f)
                            
                            for (x in 0..w.toInt() step 5) {
                                val xFraction = x / w
                                val yVal = if (xFraction > p - 0.2f && xFraction < p + 0.05f) {
                                    val localFraction = (xFraction - (p - 0.05f)) / 0.15f
                                    if (localFraction in 0.0..1.0) {
                                        h * 0.5f - Math.sin(localFraction * Math.PI * 3).toFloat() * 35.dp.toPx()
                                    } else h * 0.5f
                                } else h * 0.5f

                                path.lineTo(x.toFloat(), yVal)
                            }
                            drawPath(path = path, color = pulseColor, style = Stroke(width = stroke))
                        } else {
                            drawLine(
                                color = Color.Red.copy(alpha = 0.4f),
                                start = Offset(0f, h * 0.5f),
                                end = Offset(w, h * 0.5f),
                                strokeWidth = stroke
                            )
                        }
                    }

                    if (!isScanningHeart) {
                        Text("TAP INITIATE TO PULSE SCAN", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Current Heart Pulse", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "${vitals.avgHeartRate} BPM", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }

                    Button(
                        onClick = { isScanningHeart = !isScanningHeart },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isScanningHeart) Color.DarkGray else Color.Red)
                    ) {
                        Text(if (isScanningHeart) "Halt Scan" else "Initiate EKG Pulse")
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Opacity, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SpO2 Levels", fontSize = 11.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "${vitals.spo2}%", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Oxygen Saturation Optimal", fontSize = 9.sp, color = primaryColor)
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NightsStay, contentDescription = null, tint = Color(0xFF7209B7), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nocturnal Sleep", fontSize = 11.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val hours = vitals.sleepMinutes / 60
                    val mins = vitals.sleepMinutes % 60
                    Text(text = "${hours}h ${mins}m", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text("Sleep hygiene $hours hr rating", fontSize = 9.sp, color = Color.LightGray)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1100F5FF)),
            border = BorderStroke(1.dp, Color(0x3300F5FF))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.LocalMall, contentDescription = null, tint = secondaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Structured Water Sync", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text(text = "${vitals.waterIntakeMl}/${vitals.waterGoalMl} ml", fontWeight = FontWeight.Black, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                val completedFraction = if (vitals.waterGoalMl > 0) vitals.waterIntakeMl.toFloat() / vitals.waterGoalMl.toFloat() else 0f
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E293B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(completedFraction.coerceAtMost(1.0f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brush.horizontalGradient(listOf(secondaryColor, primaryColor)))
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.logWaterIntake(250) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor.copy(0.15f), contentColor = secondaryColor)
                    ) {
                        Text("+250ml Glass")
                    }

                    Button(
                        onClick = { viewModel.logWaterIntake(500) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor.copy(0.15f), contentColor = secondaryColor)
                    ) {
                        Text("+500ml Bottle")
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stress Tracker", fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(
                        color = if (vitals.stressScore < 40) Color(0x3300FF9D) else Color(0x33FF5400),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (vitals.stressScore < 40) "RESTIVE" else "HIGH TENSION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Smart galvanic skin and heart-rate sync calculates current tension capacity at: ${vitals.stressScore}/100.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ==========================================
// TAB SCREEN 4: DISCOVERY & DEVICE PAIRING
// ==========================================
@Composable
fun WatchesTabScreen(viewModel: StepViewModel, onRequestPermission: () -> Unit) {
    val devices by viewModel.allDevices.collectAsState()
    val isSensorRegistered by viewModel.isSensorRegistered.collectAsState()
    val sensorAvailable by viewModel.sensorAvailable.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    var pairingScannerActive by remember { mutableStateOf(false) }
    var mockScanningStatus by remember { mutableStateOf("") }
    var mockScanningProgress by remember { mutableStateOf(0f) }

    // Smartwatch Cloud Account Logon states
    var selectedBrand by remember { mutableStateOf("Apple Fitness") }
    var accountId by remember { mutableStateOf("") }
    var accountPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var logonMessage by remember { mutableStateOf("") }
    var logonProgress by remember { mutableStateOf(0f) }
    var loggedInSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(pairingScannerActive) {
        if (pairingScannerActive) {
            mockScanningStatus = "Activating Bluetooth Low Energy (BLE)..."
            delay(1000)
            mockScanningStatus = "Oscillating scan frames on 2.4Ghz channel..."
            mockScanningProgress = 0.3f
            delay(1200)
            mockScanningStatus = "Found local beacons [Apple Watch Ultra 2, Galaxy 6]."
            mockScanningProgress = 0.7f
            delay(1500)
            mockScanningStatus = "QR handshake established! Initializing database pairing..."
            mockScanningProgress = 1.0f
            delay(1000)
            
            val model = listOf("Apple Watch Ultra 2", "Garmin Epix Gen 2", "Galaxy Watch 6 Pro").random()
            viewModel.pairSmartwatch(model, "wearable", "EA:54:BC:C1:F2:09")
            pairingScannerActive = false
        }
    }

    LaunchedEffect(isSigningIn) {
        if (isSigningIn) {
            logonMessage = "Connecting secure SSL endpoint to $selectedBrand servers..."
            logonProgress = 0.2f
            delay(1000)
            logonMessage = "Resolving biometric JWT token exchange..."
            logonProgress = 0.5f
            delay(1200)
            logonMessage = "Synthesizing cross-device sensor permissions..."
            logonProgress = 0.8f
            delay(1000)
            logonMessage = "Handshake success! Registering wearable tracking nodes..."
            logonProgress = 1.0f
            delay(800)
            
            val mac = "F${(0..9).random()}:3F:A9:E${(0..9).random()}:${(10..99).random()}:02"
            val devName = when (selectedBrand) {
                "Apple Fitness" -> "Apple Watch Ultra 2 (" + accountId.take(5) + ")"
                "Samsung Health" -> "Galaxy Watch 6 Classic (" + accountId.take(5) + ")"
                "Garmin Connect" -> "Garmin Epix Gen 2 (" + accountId.take(5) + ")"
                "FitBit Cloud" -> "Fitbit Sense 2 (" + accountId.take(5) + ")"
                else -> "WearOS Wearable Node"
            }
            viewModel.pairSmartwatch(devName, "wearable", mac)
            isSigningIn = false
            loggedInSuccess = true
            accountId = ""
            accountPassword = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // BEAUTIFUL HERO SMARTWATCH LOGO OF THIS APP
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(2.dp, Brush.linearGradient(listOf(primaryColor, secondaryColor)))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.img_smartwatch_logo_1779340183173),
                    contentDescription = "Smartwatch System Live Channel",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xDD0B0E17)))),
                    contentAlignment = Alignment.BottomStart
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(
                            color = primaryColor,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "SECURE LOCAL HANDSHAKE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "StepSync Pro Companion Nodes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // SMARTWATCH CONNECT & REGISTRATION CARD
        Card(
            modifier = Modifier.fillMaxWidth().testTag("smartwatch_logon_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CloudQueue,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smartwatch Cloud Logon Portal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                
                Text(
                    text = "Sign in directly to your wearable account below to retrieve and authorize real-time sensor streams.",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                if (loggedInSuccess) {
                    Surface(
                        color = primaryColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("LOGON SUCCESSFUL", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp)
                                Text("Wearable data mirrors are active.", color = Color.LightGray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                if (!isSigningIn) {
                    Text(text = "Select Wearable Cloud Provider:", fontSize = 11.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val brands = listOf("Apple Fitness", "Samsung Health", "Garmin Connect", "FitBit Cloud")
                        val scrollState = rememberScrollState()
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            brands.forEach { b ->
                                val isSelected = selectedBrand == b
                                Surface(
                                    color = if (isSelected) primaryColor else Color(0x11FFFFFF),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, if (isSelected) primaryColor else Color.Gray.copy(0.3f)),
                                    modifier = Modifier.clickable { 
                                        selectedBrand = b 
                                        loggedInSuccess = false
                                    }
                                ) {
                                    Text(
                                        text = b,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = accountId,
                        onValueChange = { accountId = it },
                        label = { Text("Account Login ID / Email", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, tint = primaryColor) },
                        modifier = Modifier.fillMaxWidth().testTag("logon_email_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = accountPassword,
                        onValueChange = { accountPassword = it },
                        label = { Text("Cloud Access Password / Token", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Filled.Key, contentDescription = null, tint = secondaryColor) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color.LightGray
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("logon_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Button(
                        onClick = { 
                            if (accountId.isNotBlank() && accountPassword.isNotBlank()) {
                                isSigningIn = true 
                            }
                        },
                        enabled = accountId.isNotBlank() && accountPassword.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth().testTag("secure_logon_button")
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Establish Secure Cloud Handshake", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    ) {
                        CircularProgressIndicator(color = primaryColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = logonMessage, fontSize = 11.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { logonProgress },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            color = primaryColor,
                        )
                    }
                }
            }
        }

        // ORIGINAL LOCAL BLUETOOTH / PHYSICAL PAIRING OPTIONS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = secondaryColor, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Smartwatch Bluetooth Connector", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    text = "Sync step, heart beats, SpO2 & Sleep activity instantly with cross-platform companion hardware.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (!pairingScannerActive) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { pairingScannerActive = true },
                            colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("QR Scanner Sync")
                        }

                        Button(
                            onClick = { pairingScannerActive = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.CellTower, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Radio BLE Link")
                        }
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(color = secondaryColor)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = mockScanningStatus, fontSize = 11.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { mockScanningProgress },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            color = primaryColor,
                        )
                    }
                }
            }
        }

        Text(text = "Paired Wearable Nodes", fontWeight = FontWeight.Bold, color = Color.White)

        if (devices.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No paired smartwatches linked yet.", color = Color.Gray)
            }
        } else {
            devices.forEach { dev ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
                    border = BorderStroke(1.dp, if (dev.isConnected) primaryColor.copy(alpha = 0.4f) else Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (dev.isConnected) primaryColor.copy(0.15f) else Color.Gray.copy(0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Watch,
                                contentDescription = null,
                                tint = if (dev.isConnected) primaryColor else Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = dev.name, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = "MAC: ${dev.macAddress}", fontSize = 10.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Battery5Bar, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(text = "${dev.batteryPercent}% battery", fontSize = 10.sp, color = Color.LightGray)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Button(
                                onClick = { viewModel.toggleDeviceConnection(dev) },
                                colors = ButtonDefaults.buttonColors(containerColor = if (dev.isConnected) Color.DarkGray else primaryColor),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(if (dev.isConnected) "Disconnect" else "Connect", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Unpair",
                                tint = Color.LightGray,
                                modifier = Modifier
                                    .size(18.dp)
                                    .clickable { viewModel.removeDevice(dev.macAddress) }
                            )
                        }
                    }
                }
            }
        }

        Text(text = "Internal Mobile Pedometers", fontWeight = FontWeight.Bold, color = Color.White)

        HardwareStatusIndicator(
            sensorAvailable = sensorAvailable,
            isSensorRegistered = isSensorRegistered,
            onRequestPermission = onRequestPermission
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun HardwareStatusIndicator(
    sensorAvailable: Boolean,
    isSensorRegistered: Boolean,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("hardware_status_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Hardware Integration Layer",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isSensorRegistered) Color(0xFF00FF9D) else Color.Gray)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (sensorAvailable) {
                    if (isSensorRegistered) "System is successfully processing real-time physical steps via hardware accelerometer."
                    else "Hardware sensor detected. Tap below to authorize real-time stride sync."
                } else {
                    "No physical accelerometer found on device. App is operating in premium telemetry virtual simulator mode."
                },
                fontSize = 12.sp,
                color = Color.LightGray
            )
            if (sensorAvailable && !isSensorRegistered) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("request_permission_button")
                ) {
                    Text("Authorize Step Sensor", color = Color.Black)
                }
            }
        }
    }
}

// ==========================================
// TAB SCREEN 5: AI HEALTH COACH CHAT
// ==========================================
@Composable
fun CoachTabScreen(viewModel: StepViewModel) {
    val aiInsight by viewModel.aiInsight.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0x1F1E293B)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(primaryColor, secondaryColor))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Animation, contentDescription = null, tint = Color.Black)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("AI Coach Synchronizer", fontWeight = FontWeight.Black, color = Color.White)
                    Text("Powered by Google Gemini 3.5 AI System", fontSize = 11.sp, color = primaryColor)
                }
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Ask Coach: How is my sleep cadence?", color = Color.Gray, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.LightGray,
                focusedBorderColor = secondaryColor,
                unfocusedBorderColor = Color.Gray
            ),
            trailingIcon = {
                IconButton(
                    onClick = {
                        if (searchQuery.isNotBlank()) {
                            viewModel.requestCustomCoachQuery(searchQuery)
                            keyboardController?.hide()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Submit query", tint = secondaryColor)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (searchQuery.isNotBlank()) {
                    viewModel.requestCustomCoachQuery(searchQuery)
                    keyboardController?.hide()
                }
            })
        )

        Text(text = "Instant Diagnostics Prompts", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Button(
                onClick = { viewModel.requestCustomCoachQuery("What cardiovascular insights do you see on my sleep?") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x33B5179E)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Sleep Insights", fontSize = 10.sp, color = Color.White)
            }

            Button(
                onClick = { viewModel.requestCustomCoachQuery("How can I boost my calories burning steps speed?") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300FF9D)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Burn Advice", fontSize = 10.sp, color = Color.White)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI Coach Recommendations", fontWeight = FontWeight.Bold, color = Color.White)
                    if (isAnalyzing) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(primaryColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (aiInsight.isBlank()) "Select a diagnostic template or enter custom questions to stream real-time personal coaches metrics." else aiInsight,
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun WorkoutSelectorPill(name: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selected) MaterialTheme.colorScheme.primary else Color(0x1F94A3B8),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.Black else Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
