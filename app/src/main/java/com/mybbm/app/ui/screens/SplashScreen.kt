package com.mybbm.app.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════════════════════════════════════
// EASING & TIMING CONSTANTS (Matched to Figma Smart Animate Transitions)
// ═══════════════════════════════════════════════════════════════════════════════
private val SmoothDecelerateEasing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
private val EntryBouncySpring      = spring<Float>(
    dampingRatio = 0.38f, // Exact Figma "Bouncy" Smart Animate preset (elastic overshoot)
    stiffness = 200f
)
private val SpringSwingEasing      = FastOutSlowInEasing
private val UltraSmoothEasing      = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
private val FluidMorphEasing       = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

private const val T_FRAME2_TO_3 = 450   // Anticipation Swing Right (+129dp)
private const val T_FRAME3_TO_4 = 650   // Slide Left & Brand Text Reveal
private const val T_HOLD_FRAME4 = 850   // Hold full brand lockup
private const val T_FRAME4_TO_5 = 1300  // Morph: Circle glides down, logo rises & scales, welcome slides up

// ═══════════════════════════════════════════════════════════════════════════════
// EXACT FIGMA DESIGN TOKENS
// ═══════════════════════════════════════════════════════════════════════════════
private val EllipseBgColor     = Color(0xFFE8EEFF) // Ellipse 6 & button background
private val CanvasBgColor      = Color(0xFFFFFFFF) // Frame 5 final canvas background
private val TextBrandNavy      = Color(0xFF090F59) // Welcome title & subtitle
private val ButtonTextPurple   = Color(0xFF391DBD) // "HITUNG SEKARANG!" button text

private val RobotoFontFamily = FontFamily(
    Font(R.font.roboto_regular, FontWeight.Normal),
    Font(R.font.roboto_medium, FontWeight.Medium),
    Font(R.font.roboto_bold, FontWeight.Bold),
    Font(R.font.roboto_bold, FontWeight.SemiBold),
    Font(R.font.roboto_bold, FontWeight.ExtraBold)
)

/**
 * 100% Figma-Accurate Splash Screen Storyboard (5 Frames):
 *
 * - Frame 1: Blank soft blue canvas (#E8EEFF). Logo is at center, scale 0, rotated 180°.
 * - Frame 2: Logo expands (0 -> 1) and spins (180° -> 0°) into center position (126.38dp x 98.995dp).
 * - Frame 3: Logo swings right (+129dp) in an anticipation motion.
 * - Frame 4: Logo glides left and brand text ("MYBBM" + tagline) reveals to the right with exact Figma spacing.
 * - Frame 5: Morph into Welcome screen:
 *            - Ellipse circle (1080dp) glides down to y = 680dp (creating the exact bottom curve).
 *            - Logo ascends to top center (178dp) and scales to 167dp x 124dp.
 *            - Welcome content ("Your Journey Starts Here!" + "Hitung BBM, temukan SPBU, mulai perjalananmu." + "HITUNG SEKARANG!" button)
 *              slides UP from the bottom into place.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SplashScreen(
    onGetStartedClick: () -> Unit
) {
    var animationComplete by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        // Proportional scale factor relative to standard Figma artboard (412 x 917 dp)
        val wScale = screenWidth / 412.dp
        val hScale = screenHeight / 917.dp
        val scale = minOf(wScale, hScale)

        // Geometric dimensions based on Figma
        val logoW = 126.38.dp * scale
        val logoH = 98.995.dp * scale

        // Frame 2 & 4 vertical center Y
        val logoMidY = (409.52.dp * hScale)
        // Frame 5 vertical Y
        val logoTopY = (178.dp * hScale)

        // Shifts for horizontal motion:
        // In Figma Frame 4, the entire lockup (Subtract + Frame 5 text) spans from x=57.82 to 333 (width=275.18dp).
        // Difference between text left (x=175) and logo left (x=57.82) is exactly 117.18dp.
        // With total group centered at X=0, logo center is -74dp and text center is +58.5dp,
        // resulting in the exact 3.7dp snug visual gap seen in Figma!
        val shiftRightPx = 129.dp * wScale  // Frame 3 (x=271.82dp)
        val shiftLeftPx  = -74.dp * scale   // Frame 4 (exact Figma logo position)

        // 1080dp Circle (Radius: 540dp)
        val circleRadius = 540.dp * maxOf(wScale, 1f)
        val circleTopStart = -91.dp * hScale
        val circleTopEnd   = 680.dp * hScale

        // ─── ANIMATABLES ────────────────────────────────────────────────────────
        val circleTop       = remember { Animatable(circleTopStart.value) }
        val logoAlpha       = remember { Animatable(0f) }
        val logoScale       = remember { Animatable(0f) }       // Frame 1 starts at 0!
        val logoRotationZ   = remember { Animatable(180f) }    // Frame 1 starts rotated 180°!
        val logoOffsetX     = remember { Animatable(0f) }
        val logoOffsetY     = remember { Animatable(logoMidY.value) }
        val logoScaleMorph  = remember { Animatable(1.0f) }

        val brandTextAlpha  = remember { Animatable(0f) }
        val brandTextOffset = remember { Animatable(25f) }

        val welcomeAlpha    = remember { Animatable(0f) }
        val welcomeOffsetY  = remember { Animatable(160f) } // Slides UP from bottom (Figma y=930 -> 371)

        // ─── STORYBOARD ORCHESTRATOR ────────────────────────────────────────────
        LaunchedEffect(Unit) {
            // Give Compose 100ms for initial layout pass
            delay(100)

            // ── Frame 1 -> Frame 2: Logo Entry with Figma "Bouncy" Spring Animation
            launch {
                logoAlpha.animateTo(1f, tween(300))
            }
            val scaleJob = launch {
                logoScale.animateTo(1f, EntryBouncySpring)
            }
            val rotateJob = launch {
                logoRotationZ.animateTo(0f, EntryBouncySpring)
            }

            // Wait for bouncy spring to naturally settle
            scaleJob.join()
            rotateJob.join()

            // Brief hold after settling before anticipation swing
            delay(250)

            // ── Frame 2 -> Frame 3: Anticipation Swing Right [+129dp]
            logoOffsetX.animateTo(shiftRightPx.value, tween(T_FRAME2_TO_3, easing = SpringSwingEasing))

            // ── Frame 3 -> Frame 4: Slide Left [-74dp] & Brand Text Reveal
            launch {
                logoOffsetX.animateTo(shiftLeftPx.value, tween(T_FRAME3_TO_4, easing = UltraSmoothEasing))
            }
            launch {
                brandTextAlpha.animateTo(1f, tween(T_FRAME3_TO_4, easing = UltraSmoothEasing))
            }
            launch {
                brandTextOffset.animateTo(0f, tween(T_FRAME3_TO_4, easing = UltraSmoothEasing))
            }
            delay(T_FRAME3_TO_4.toLong())

            // ── Hold Frame 4: User absorbs brand identity
            delay(T_HOLD_FRAME4.toLong())

            // ── Frame 4 -> Frame 5: Morph into Welcome Screen
            // 1. Circle glides down to bottom, forming the soft blue convex arc
            launch {
                circleTop.animateTo(circleTopEnd.value, tween(T_FRAME4_TO_5, easing = FluidMorphEasing))
            }
            // 2. Logo centers, ascends to top, scales up to 167dp x 124dp
            launch {
                logoOffsetX.animateTo(0f, tween(T_FRAME4_TO_5, easing = FluidMorphEasing))
            }
            launch {
                logoOffsetY.animateTo(logoTopY.value, tween(T_FRAME4_TO_5, easing = FluidMorphEasing))
            }
            launch {
                logoScaleMorph.animateTo(1.3214f, tween(T_FRAME4_TO_5, easing = FluidMorphEasing))
            }
            // 3. Brand text fades out quickly in ~350ms
            launch {
                brandTextAlpha.animateTo(0f, tween(350, easing = FluidMorphEasing))
            }
            // 4. Welcome content & button slide up from bottom and fade in
            launch {
                welcomeAlpha.animateTo(1f, tween(1100, easing = FluidMorphEasing))
            }
            launch {
                welcomeOffsetY.animateTo(0f, tween(T_FRAME4_TO_5, easing = FluidMorphEasing))
            }

            delay(T_FRAME4_TO_5.toLong())
            animationComplete = true
        }

        // ═════════════════════════════════════════════════════════════════════
        // RENDER LAYERS
        // ═════════════════════════════════════════════════════════════════════

        // ── 1. Background Canvas with Animated Ellipse ────────────────────────
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Clean white background (revealed when circle glides down)
            drawRect(CanvasBgColor)

            // 1080dp circle centered horizontally
            val rPx = circleRadius.toPx()
            val cX = size.width / 2f
            val cY = circleTop.value.dp.toPx() + rPx

            drawCircle(
                color = EllipseBgColor,
                radius = rPx,
                center = Offset(cX, cY)
            )
        }

        // ── 2. Logo Icon "M" ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(
                    x = logoOffsetX.value.dp,
                    y = logoOffsetY.value.dp
                )
                .size(logoW, logoH)
                .graphicsLayer {
                    val currentScale = logoScale.value * logoScaleMorph.value
                    scaleX    = currentScale
                    scaleY    = currentScale
                    rotationZ = logoRotationZ.value
                    alpha     = logoAlpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_mybbm),
                contentDescription = "MyBBM Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── 3. Brand Text ("MYBBM" + Tagline) for Frame 4 ────────────────────
        val textW = 158.dp * scale
        val textH = 64.dp * scale
        // Exact Figma position (+58.5dp) creates the unified brand lockup matching Figma
        val textBaseX = (58.5.dp * scale)
        val textBaseY = (429.dp * hScale)

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(
                    x = textBaseX + brandTextOffset.value.dp,
                    y = textBaseY
                )
                .size(textW, textH)
                .graphicsLayer {
                    alpha = brandTextAlpha.value
                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_brand_text),
                contentDescription = "MyBBM Brand Text",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── 4. Frame 5 Welcome Content (Title + Subtitle + Button) ───────────
        val welcomeTopY = (371.dp * hScale)

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = welcomeTopY + welcomeOffsetY.value.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp * scale)
                .graphicsLayer {
                    alpha = welcomeAlpha.value
                }
        ) {
            // Judul: "Your Journey Starts Here!" (15px SemiBold, #090F59)
            Text(
                text = "Your Journey Starts Here!",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = RobotoFontFamily,
                color = TextBrandNavy,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Deskripsi: "Hitung BBM, temukan SPBU, mulai perjalananmu." (15px Regular, #090F59)
            Text(
                text = "Hitung BBM, temukan SPBU, mulai perjalananmu.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = RobotoFontFamily,
                color = TextBrandNavy,
                textAlign = TextAlign.Center
            )

            // Jarak ke tombol sesuai Figma (496dp - 371dp - ~40dp text = ~85dp gap)
            Spacer(modifier = Modifier.height(75.dp * hScale))

            // Tombol "HITUNG SEKARANG!":
            val buttonW = maxOf(220.dp * scale, 210.dp)
            val buttonH = 64.dp * scale

            Button(
                onClick = {
                    if (animationComplete) {
                        onGetStartedClick()
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EllipseBgColor,
                    contentColor   = ButtonTextPurple
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                modifier = Modifier
                    .width(buttonW)
                    .height(buttonH)
            ) {
                Text(
                    text = "HITUNG SEKARANG!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = RobotoFontFamily,
                    color = ButtonTextPurple,
                    letterSpacing = 0.5.sp,
                    maxLines = 1
                )
            }
        }
    }
}
