package com.mybbm.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class WalkthroughAnchor {
    BOTTOM_SHEET,
    PIN_ICONS,
    TOP_RIGHT_FABS,
    CALCULATE_BUTTON
}

data class WalkthroughStep(
    val title: String,
    val description: String,
    val badgeText: String,
    val anchor: WalkthroughAnchor
)

object AppWalkthroughSteps {
    val steps: List<WalkthroughStep> = listOf(
        WalkthroughStep(
            title = "Geser Panel Sesuai Kebutuhan",
            description = "Tarik garis di atas panel ke bawah untuk melihat peta layar penuh, atau tarik ke atas untuk mengisi rute dan melihat saran lokasi.",
            badgeText = "Panel 3-Tahap",
            anchor = WalkthroughAnchor.BOTTOM_SHEET
        ),
        WalkthroughStep(
            title = "Pilih Lokasi Langsung di Peta",
            description = "Ketuk ikon pin merah (Lokasi Awal) atau biru (Lokasi Tujuan), geser peta ke titik tujuan, lalu ketuk pin di tengah peta untuk konfirmasi.",
            badgeText = "Pin Peta Interaktif",
            anchor = WalkthroughAnchor.PIN_ICONS
        ),
        WalkthroughStep(
            title = "Pengaturan, Riwayat & GPS",
            description = "Gunakan 3 tombol di kanan atas untuk mengelola kendaraan & harga BBM kustom, melihat riwayat perjalanan, atau memusatkan peta ke lokasi GPS Anda.",
            badgeText = "Akses Cepat Peta",
            anchor = WalkthroughAnchor.TOP_RIGHT_FABS
        ),
        WalkthroughStep(
            title = "Hitung Biaya & SPBU Terdekat",
            description = "Setelah rute, kendaraan, dan jenis BBM terisi, tekan Hitung Biaya Perjalanan untuk melihat estimasi biaya, liter BBM, rute, dan SPBU terdekat.",
            badgeText = "Siap Menghitung",
            anchor = WalkthroughAnchor.CALCULATE_BUTTON
        )
    )

    fun nextStepIndex(currentIndex: Int, totalSteps: Int = steps.size): Int {
        if (totalSteps <= 0) return 0
        return (currentIndex + 1).coerceAtMost(totalSteps - 1)
    }

    fun previousStepIndex(currentIndex: Int): Int {
        return (currentIndex - 1).coerceAtLeast(0)
    }

    fun isLastStep(currentIndex: Int, totalSteps: Int = steps.size): Boolean {
        return totalSteps > 0 && currentIndex >= totalSteps - 1
    }
}

private fun iconForAnchor(anchor: WalkthroughAnchor): ImageVector {
    return when (anchor) {
        WalkthroughAnchor.BOTTOM_SHEET -> Icons.Outlined.SwapVert
        WalkthroughAnchor.PIN_ICONS -> Icons.Outlined.AddLocationAlt
        WalkthroughAnchor.TOP_RIGHT_FABS -> Icons.Outlined.Tune
        WalkthroughAnchor.CALCULATE_BUTTON -> Icons.Outlined.LocalGasStation
    }
}

/**
 * Interactive Coachmark / Card Tour bertema ungu MyBBM di atas HomeScreen.
 */
@Composable
fun AppWalkthroughOverlay(
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val steps = AppWalkthroughSteps.steps
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentIndex]
    val isLast = AppWalkthroughSteps.isLastStep(currentIndex, steps.size)

    BackHandler(enabled = true) {
        if (currentIndex > 0) {
            currentIndex = AppWalkthroughSteps.previousStepIndex(currentIndex)
        } else {
            onSkip()
        }
    }

    val cardAlignment = when (currentStep.anchor) {
        WalkthroughAnchor.TOP_RIGHT_FABS -> Alignment.TopCenter
        WalkthroughAnchor.BOTTOM_SHEET,
        WalkthroughAnchor.PIN_ICONS,
        WalkthroughAnchor.CALCULATE_BUTTON -> Alignment.Center
    }

    val cardTopPadding = when (currentStep.anchor) {
        WalkthroughAnchor.TOP_RIGHT_FABS -> 112.dp
        else -> 0.dp
    }

    val cardBottomPadding = when (currentStep.anchor) {
        WalkthroughAnchor.BOTTOM_SHEET,
        WalkthroughAnchor.PIN_ICONS,
        WalkthroughAnchor.CALCULATE_BUTTON -> 180.dp
        WalkthroughAnchor.TOP_RIGHT_FABS -> 0.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x88090F59))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        contentAlignment = cardAlignment
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = cardTopPadding, bottom = cardBottomPadding)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0x55090F59),
                    ambientColor = Color(0x55090F59)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF3F1FF), RoundedCornerShape(22.dp))
                .border(1.5.dp, Color(0xFFC0BEF8), RoundedCornerShape(22.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header: Badge langkah + Tombol Lewati
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF7542EF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconForAnchor(currentStep.anchor),
                            contentDescription = currentStep.title,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "PANDUAN ${currentIndex + 1}/${steps.size}",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7542EF),
                                letterSpacing = 0.6.sp
                            )
                        )
                        Text(
                            text = currentStep.badgeText,
                            style = TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A4E69)
                            )
                        )
                    }
                }

                TextButton(
                    onClick = onSkip,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Lewati",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A4E69)
                        )
                    )
                }
            }

            // Konten judul & deskripsi dengan animasi transisi halus
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "walkthroughStepContent"
            ) { step ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = step.title,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF090F59)
                        )
                    )
                    Text(
                        text = step.description,
                        style = TextStyle(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = Color(0xFF303B65)
                        )
                    )
                }
            }

            // Indikator progres titik + Tombol Navigasi (Kembali & Lanjut/Mulai)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    steps.indices.forEach { idx ->
                        val active = idx == currentIndex
                        Box(
                            modifier = Modifier
                                .width(if (active) 20.dp else 7.dp)
                                .height(7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (active) Color(0xFF7542EF) else Color(0xFFC0BEF8)
                                )
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentIndex > 0) {
                        TextButton(
                            onClick = {
                                currentIndex = AppWalkthroughSteps.previousStepIndex(currentIndex)
                            }
                        ) {
                            Text(
                                text = "Kembali",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF303B65)
                                )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isLast) {
                                onFinish()
                            } else {
                                currentIndex = AppWalkthroughSteps.nextStepIndex(currentIndex, steps.size)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isLast) "Mulai" else "Lanjut",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}
