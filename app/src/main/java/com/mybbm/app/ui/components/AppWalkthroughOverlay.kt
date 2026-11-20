package com.mybbm.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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

data class BubblePlacement(
    val placeAbove: Boolean,
    val bubbleLeftPx: Float,
    val bubbleTopPx: Float,
    val arrowOffsetInBubblePx: Float
)

object AppWalkthroughSteps {
    val steps: List<WalkthroughStep> = listOf(
        WalkthroughStep(
            title = "Geser Panel Peta",
            description = "Tarik garis ini ke bawah untuk melihat peta penuh, atau tarik ke atas untuk mengisi rute.",
            badgeText = "Panel 3-Tahap",
            anchor = WalkthroughAnchor.BOTTOM_SHEET
        ),
        WalkthroughStep(
            title = "Pilih Lokasi dari Peta",
            description = "Ketuk ikon pin merah atau biru untuk memilih titik lokasi awal & tujuan langsung di peta.",
            badgeText = "Pin Peta Interaktif",
            anchor = WalkthroughAnchor.PIN_ICONS
        ),
        WalkthroughStep(
            title = "Pengaturan, Riwayat & GPS",
            description = "Kelola kendaraan & harga BBM kustom, lihat riwayat perjalanan, atau pusatkan lokasi GPS Anda.",
            badgeText = "Akses Cepat",
            anchor = WalkthroughAnchor.TOP_RIGHT_FABS
        ),
        WalkthroughStep(
            title = "Hitung Biaya & SPBU",
            description = "Tekan tombol ini untuk melihat estimasi biaya BBM, jarak rute, dan SPBU terdekat.",
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

    fun calculateBubblePlacement(
        targetRect: Rect,
        containerWidthPx: Float,
        containerHeightPx: Float,
        bubbleWidthPx: Float,
        bubbleHeightPx: Float,
        arrowHeightPx: Float,
        gapPx: Float,
        horizontalMarginPx: Float,
        verticalMarginPx: Float,
        arrowEdgePaddingPx: Float
    ): BubblePlacement {
        val totalVerticalOffset = arrowHeightPx + gapPx
        var placeAbove = targetRect.center.y > containerHeightPx * 0.45f

        if (placeAbove && targetRect.top - totalVerticalOffset - bubbleHeightPx < verticalMarginPx) {
            placeAbove = false
        } else if (!placeAbove && targetRect.bottom + totalVerticalOffset + bubbleHeightPx > containerHeightPx - verticalMarginPx) {
            placeAbove = true
        }

        val maxLeft = (containerWidthPx - bubbleWidthPx - horizontalMarginPx).coerceAtLeast(horizontalMarginPx)
        val bubbleLeftPx = (targetRect.center.x - bubbleWidthPx / 2f).coerceIn(horizontalMarginPx, maxLeft)

        val rawTop = if (placeAbove) {
            targetRect.top - totalVerticalOffset - bubbleHeightPx
        } else {
            targetRect.bottom + gapPx
        }
        val maxTop = (containerHeightPx - bubbleHeightPx - arrowHeightPx - verticalMarginPx).coerceAtLeast(verticalMarginPx)
        val bubbleTopPx = rawTop.coerceIn(verticalMarginPx, maxTop)

        val maxArrowX = (bubbleWidthPx - arrowEdgePaddingPx).coerceAtLeast(arrowEdgePaddingPx)
        val arrowOffsetInBubblePx = (targetRect.center.x - bubbleLeftPx).coerceIn(arrowEdgePaddingPx, maxArrowX)

        return BubblePlacement(
            placeAbove = placeAbove,
            bubbleLeftPx = bubbleLeftPx,
            bubbleTopPx = bubbleTopPx,
            arrowOffsetInBubblePx = arrowOffsetInBubblePx
        )
    }
}

/**
 * Spotlight App Walkthrough bergaya Referensi Gambar 1:
 * - Layar digelapkan kecuali lubang spotlight terang tepat di atas elemen asli (`PathOperation.Difference`)
 * - Bingkai highlight putus-putus putih mengelilingi elemen target
 * - Bubble tooltip kompak putih dengan panah segitiga menunjuk tepat ke elemen target (`1 / 4` + tombol `Lanjut`/`Oke`)
 * - Tombol bulat `X` untuk melewati panduan kapan saja
 */
@Composable
fun AppWalkthroughOverlay(
    targetRects: Map<WalkthroughAnchor, Rect>,
    onFinish: () -> Unit,
    onSkip: () -> Unit
) {
    val steps = AppWalkthroughSteps.steps
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentIndex]
    val isLast = AppWalkthroughSteps.isLastStep(currentIndex, steps.size)
    val density = LocalDensity.current

    var overlayOriginInRoot by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var bubbleMeasuredHeightPx by remember { mutableFloatStateOf(with(density) { 124.dp.toPx() }) }

    BackHandler(enabled = true) {
        if (currentIndex > 0) {
            currentIndex = AppWalkthroughSteps.previousStepIndex(currentIndex)
        } else {
            onSkip()
        }
    }

    val rawTarget = targetRects[currentStep.anchor]
    val padH = with(density) {
        when (currentStep.anchor) {
            WalkthroughAnchor.BOTTOM_SHEET -> 26.dp.toPx()
            WalkthroughAnchor.PIN_ICONS -> 8.dp.toPx()
            WalkthroughAnchor.TOP_RIGHT_FABS -> 8.dp.toPx()
            WalkthroughAnchor.CALCULATE_BUTTON -> 6.dp.toPx()
        }
    }
    val padV = with(density) {
        when (currentStep.anchor) {
            WalkthroughAnchor.BOTTOM_SHEET -> 14.dp.toPx()
            WalkthroughAnchor.PIN_ICONS -> 8.dp.toPx()
            WalkthroughAnchor.TOP_RIGHT_FABS -> 8.dp.toPx()
            WalkthroughAnchor.CALCULATE_BUTTON -> 6.dp.toPx()
        }
    }
    val cutoutCornerPx = with(density) {
        when (currentStep.anchor) {
            WalkthroughAnchor.BOTTOM_SHEET -> 18.dp.toPx()
            WalkthroughAnchor.PIN_ICONS -> 14.dp.toPx()
            WalkthroughAnchor.TOP_RIGHT_FABS -> 28.dp.toPx()
            WalkthroughAnchor.CALCULATE_BUTTON -> 26.dp.toPx()
        }
    }

    val fallbackRect = remember(containerSize) {
        val w = containerSize.width.toFloat().coerceAtLeast(1f)
        val h = containerSize.height.toFloat().coerceAtLeast(1f)
        Rect(w * 0.25f, h * 0.45f, w * 0.75f, h * 0.55f)
    }

    val targetLocalRect = if (rawTarget != null && !rawTarget.isEmpty) {
        Rect(
            left = rawTarget.left - overlayOriginInRoot.x - padH,
            top = rawTarget.top - overlayOriginInRoot.y - padV,
            right = rawTarget.right - overlayOriginInRoot.x + padH,
            bottom = rawTarget.bottom - overlayOriginInRoot.y + padV
        )
    } else {
        fallbackRect
    }

    val springSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    val animLeft by animateFloatAsState(targetLocalRect.left, springSpec, label = "spotLeft")
    val animTop by animateFloatAsState(targetLocalRect.top, springSpec, label = "spotTop")
    val animRight by animateFloatAsState(targetLocalRect.right, springSpec, label = "spotRight")
    val animBottom by animateFloatAsState(targetLocalRect.bottom, springSpec, label = "spotBottom")
    val animCorner by animateFloatAsState(cutoutCornerPx, springSpec, label = "spotCorner")

    val animatedCutoutRect = Rect(animLeft, animTop, animRight, animBottom)

    val bubbleWidthDp = 248.dp
    val bubbleWidthPx = with(density) { bubbleWidthDp.toPx() }
    val arrowHeightDp = 9.dp
    val arrowWidthDp = 16.dp
    val arrowHeightPx = with(density) { arrowHeightDp.toPx() }
    val gapPx = with(density) { 8.dp.toPx() }
    val horizontalMarginPx = with(density) { 16.dp.toPx() }
    val verticalMarginPx = with(density) { 24.dp.toPx() }
    val arrowEdgePaddingPx = with(density) { 22.dp.toPx() }

    val placement = AppWalkthroughSteps.calculateBubblePlacement(
        targetRect = targetLocalRect,
        containerWidthPx = containerSize.width.toFloat().coerceAtLeast(bubbleWidthPx + horizontalMarginPx * 2),
        containerHeightPx = containerSize.height.toFloat().coerceAtLeast(400f),
        bubbleWidthPx = bubbleWidthPx,
        bubbleHeightPx = bubbleMeasuredHeightPx,
        arrowHeightPx = arrowHeightPx,
        gapPx = gapPx,
        horizontalMarginPx = horizontalMarginPx,
        verticalMarginPx = verticalMarginPx,
        arrowEdgePaddingPx = arrowEdgePaddingPx
    )

    val animBubbleLeft by animateFloatAsState(placement.bubbleLeftPx, springSpec, label = "bubbleLeft")
    val animBubbleTop by animateFloatAsState(placement.bubbleTopPx, springSpec, label = "bubbleTop")
    val animArrowOffset by animateFloatAsState(placement.arrowOffsetInBubblePx, springSpec, label = "arrowOffset")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                overlayOriginInRoot = coords.positionInRoot()
                containerSize = coords.size
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        // 1. Canvas Scrim Gelap dengan Lubang Spotlight Presisi + Bingkai Putus-Putus
        Canvas(modifier = Modifier.fillMaxSize()) {
            val fullScreenPath = Path().apply {
                addRect(Rect(0f, 0f, size.width, size.height))
            }
            val cutoutPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = animatedCutoutRect,
                        cornerRadius = CornerRadius(animCorner, animCorner)
                    )
                )
            }
            val scrimPath = Path.combine(PathOperation.Difference, fullScreenPath, cutoutPath)
            drawPath(
                path = scrimPath,
                color = Color(0xB8060A2E)
            )

            // Bingkai highlight putus-putus putih terang seperti Gambar 1
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(animatedCutoutRect.left, animatedCutoutRect.top),
                size = animatedCutoutRect.size,
                cornerRadius = CornerRadius(animCorner, animCorner),
                style = Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
                )
            )
        }

        // 2. Bubble Tooltip Kompak dengan Panah Segitiga menunjuk tepat ke elemen target
        Column(
            modifier = Modifier
                .offset { IntOffset(animBubbleLeft.roundToInt(), animBubbleTop.roundToInt()) }
                .width(bubbleWidthDp)
        ) {
            if (!placement.placeAbove) {
                // Panah menunjuk ke ATAS (ke elemen di atas bubble)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(arrowHeightDp)
                ) {
                    val halfW = arrowWidthDp.toPx() / 2f
                    val tipX = animArrowOffset
                    val triangle = Path().apply {
                        moveTo(tipX, 0f)
                        lineTo(tipX - halfW, size.height + 1f)
                        lineTo(tipX + halfW, size.height + 1f)
                        close()
                    }
                    drawPath(path = triangle, color = Color.White)
                }
            }

            // Badan Kartu Tooltip Putih Kompak (Gaya Referensi Gambar 1)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        if (size.height > 0) {
                            bubbleMeasuredHeightPx = size.height.toFloat()
                        }
                    }
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(14.dp),
                        spotColor = Color(0x45000000),
                        ambientColor = Color(0x45000000)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tooltipText"
                ) { step ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = step.title,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF090F59)
                            )
                        )
                        Text(
                            text = step.description,
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                lineHeight = 17.5.sp,
                                color = Color(0xFF303B65)
                            )
                        )
                    }
                }

                // Footer Ringkas: "1 / 4" di kiri, Tombol "Lanjut" / "Oke" di kanan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currentIndex + 1} / ${steps.size}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6B7280)
                        )
                    )

                    Button(
                        onClick = {
                            if (isLast) {
                                onFinish()
                            } else {
                                currentIndex = AppWalkthroughSteps.nextStepIndex(currentIndex, steps.size)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1B4B)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(
                            text = if (isLast) "Oke" else "Lanjut",
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (placement.placeAbove) {
                // Panah menunjuk ke BAWAH (ke elemen di bawah bubble)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(arrowHeightDp)
                ) {
                    val halfW = arrowWidthDp.toPx() / 2f
                    val tipX = animArrowOffset
                    val triangle = Path().apply {
                        moveTo(tipX, size.height)
                        lineTo(tipX - halfW, -1f)
                        lineTo(tipX + halfW, -1f)
                        close()
                    }
                    drawPath(path = triangle, color = Color.White)
                }
            }
        }

        // 3. Tombol Bulat (X) untuk menutup / melewati walkthrough kapan saja (pojok kiri atas agar bebas bentrok)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 16.dp, start = 18.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xA611133E), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                .clickable { onSkip() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Tutup Panduan",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
