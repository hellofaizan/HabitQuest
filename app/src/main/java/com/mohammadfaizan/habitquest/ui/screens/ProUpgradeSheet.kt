package com.mohammadfaizan.habitquest.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProSheetBackground = Color(0xFF121214)
private val ProCardBackground = Color(0xFF1B1B1E)
private val ProCardBorder = Color(0xFF2C2C30)
private val ProAccent = Color(0xFF8B7FF5)
private val ProSecondaryText = Color(0xFF9C9CA6)

private data class ProPlan(
    val title: String,
    val price: String,
    val period: String,
    val description: String,
    val badge: String? = null
)

private val proPlans = listOf(
    ProPlan("Monthly", "₹49", "/month", "Recurring billing. Cancel anytime."),
    ProPlan("Annual", "₹499", "/year", "Recurring billing. Cancel anytime.", badge = "BEST VALUE"),
    ProPlan("Lifetime", "₹1,999", "one-time", "Pay once. Unlimited access forever.")
)

private data class ProFeature(
    val emoji: String,
    val badgeColor: Color,
    val title: String,
    val description: String
)

private val proFeatures = listOf(
    ProFeature("🧩", Color(0xFF1C3A5E), "Home Screen Widget", "Track today's habits right from your home screen"),
    ProFeature("🔔", Color(0xFF4A1F2B), "Multiple Reminders", "Set more than one reminder time for the same habit"),
    ProFeature("📈", Color(0xFF4A3410), "Analytics Dashboard", "See charts, streak trends, and completion insights"),
    ProFeature("🗂️", Color(0xFF12332E), "Dashboard Customization", "Show streak badges, labels, and category filters"),
    ProFeature("☁️", Color(0xFF2A2350), "Backup & Restore", "Export and restore your habit data anytime")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProUpgradeSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPlan by remember { mutableStateOf(proPlans[1]) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ProSheetBackground,
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A3E))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF232326))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "✕", color = Color.White, fontSize = 14.sp)
                }

                Text(
                    text = buildAnnotatedString {
                        append("Unlock ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append("Habit")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = ProAccent)) {
                            append("Quest")
                        }
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(" Pro")
                        }
                    },
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 44.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            proPlans.forEach { plan ->
                ProPlanCard(
                    plan = plan,
                    isSelected = plan.title == selectedPlan.title,
                    onClick = { selectedPlan = plan }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Already subscribed? Restore purchase",
                color = ProAccent,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "No previous purchase found", Toast.LENGTH_SHORT).show()
                    }
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "BY UPGRADING YOU'LL ALSO UNLOCK:",
                color = ProSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            proFeatures.forEach { feature ->
                ProFeatureRow(feature)
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        "Payments aren't set up yet — coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ProAccent),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Continue", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun ProPlanCard(
    plan: ProPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) ProAccent.copy(alpha = 0.12f) else ProCardBackground)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ProAccent else ProCardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (isSelected) ProAccent else Color(0xFF4A4A4E), CircleShape)
                .background(if (isSelected) ProAccent else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = plan.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (plan.badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ProAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = plan.badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(text = plan.description, color = ProSecondaryText, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(text = plan.price, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(text = plan.period, color = ProSecondaryText, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProFeatureRow(feature: ProFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(feature.badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = feature.emoji, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(text = feature.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = feature.description, color = ProSecondaryText, fontSize = 13.sp)
        }
    }
}
