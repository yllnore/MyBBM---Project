package com.mybbm.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import kotlinx.coroutines.delay

enum class SplashStep {
    INITIAL_BLANK,  // Layar kosong latar belakang ungu lembut
    LOGO_POP,       // Logo M muncul di tengah
    BRAND_REVEAL,   // Logo M geser sedikit & teks MYBBM muncul di samping
    ONBOARDING      // Masuk layar Onboarding (Logo naik, deskripsi & tombol HITUNG SEKARANG!)
}

@Composable
fun SplashScreen(
    onStartClick: () -> Unit
) {
    var step by remember { mutableStateOf(SplashStep.INITIAL_BLANK) }

    // Animasi logo berpindah dari tengah ke atas saat masuk ke ONBOARDING
    val logoScale = remember { Animatable(0.4f) }
    val logoOffsetY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Blank Intro sebentar (400ms)
        delay(400L)
        step = SplashStep.LOGO_POP
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Step 2: Muncul logo selama 700ms, lalu reveal Brand MYBBM
        delay(700L)
        step = SplashStep.BRAND_REVEAL

        // Step 3: Tampilkan brand MYBBM selama 1.2 detik
        delay(1200L)
        step = SplashStep.ONBOARDING
        logoOffsetY.animateTo(
            targetValue = -30f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
    }

    // Latar belakang: Warna putih bersih saat ONBOARDING, atau ungu lembut saat SPLASH
    val backgroundColor = if (step == SplashStep.ONBOARDING) Color(0xFFFFFFFF) else Color(0xFFE8EBFD)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        when (step) {
            SplashStep.INITIAL_BLANK -> {
                // Layar kosong bersih sebelum transisi pertama
            }

            SplashStep.LOGO_POP -> {
                // Logo M muncul di tengah layar secara membesar lembut
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_logo_m),
                        contentDescription = "Logo MyBBM",
                        modifier = Modifier
                            .size(110.dp)
                            .scale(logoScale.value),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            SplashStep.BRAND_REVEAL -> {
                // Logo M dan tulisan MYBBM berjejer di tengah
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_m),
                            contentDescription = "Logo MyBBM",
                            modifier = Modifier.size(90.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "MYBBM",
                                style = TextStyle(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1B0B5E),
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = "Fuel Calculation & Trip Assistant",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5B5780)
                                )
                            )
                        }
                    }
                }
            }

            SplashStep.ONBOARDING -> {
                // Tampilan Onboarding Final sesuai rancangan video Figma
                Box(modifier = Modifier.fillMaxSize()) {
                    // Lengkungan dekoratif di bawah (Lilac Wave / Curve)
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.35f)
                            .align(Alignment.BottomCenter)
                    ) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.45f)
                            quadraticTo(
                                size.width * 0.5f, -size.height * 0.1f,
                                size.width, size.height * 0.45f
                            )
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(
                            path = path,
                            color = Color(0xFFE8EBFD)
                        )
                    }

                    // Konten Utama Onboarding di Tengah
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Logo M di bagian atas konten
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo_m),
                            contentDescription = "Logo MyBBM",
                            modifier = Modifier
                                .size(125.dp)
                                .offset(y = logoOffsetY.value.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // Headline
                        Text(
                            text = "Your Journey Starts Here!",
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF11133E),
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sub-headline
                        Text(
                            text = "Hitung BBM, temukan SPBU, mulai perjalananmu.",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF4A4B57),
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(44.dp))

                        // Tombol Aksi: HITUNG SEKARANG!
                        Button(
                            onClick = onStartClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFE7DBFF) // Lavender pastel sesuai Figma
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 1.dp
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .fillMaxWidth(0.68f)
                        ) {
                            Text(
                                text = "HITUNG SEKARANG!",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3310AA), // Ungu tua sesuai Figma
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
