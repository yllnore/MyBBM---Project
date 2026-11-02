package com.mybbm.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Komponen Visual Overlay Map estetik untuk HomeScreen dan ResultScreen.
 * Menampilkan peta latar belakang berskema warna terang (#E8ECEF / #ECEBFF),
 * jaringan jalan kota presisi, dan garis rute simulasi perjalanan (Polyline)
 * yang halus dengan warna ungu utama (#7542EF).
 */
@Composable
fun MapVisualOverlay(
    modifier: Modifier = Modifier,
    originLabel: String = "Lokasi Awal",
    destinationLabel: String = "Lokasi Tujuan",
    showRoutePoints: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        // Kanvas Peta Estetik: Grid Jalan, Blok Kota, dan Polyline Rute Ungu #7542EF
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // 1. Gambar latar belakang gradien halus skema warna terang (#E8ECEF -> #ECEBFF)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFECEBFF), // Aksen lilac terang di atas
                        Color(0xFFE8ECEF), // Warna peta netral di tengah
                        Color(0xFFDFE4E8)  // Sedikit kontras di bawah
                    )
                ),
                size = size
            )

            // 2. Gambar Blok-Blok Kota (City Blocks / Kawasan Presisi)
            drawCityBlocks(width, height)

            // 3. Gambar Lekukan Sungai / Unsur Alam (Melambangkan Sungai Mahakam yang Estetik)
            drawWaterway(width, height)

            // 4. Gambar Jaringan Jalanan Presisi (Arterial, Secondary, Local Roads)
            drawRoadNetwork(width, height)

            // 5. Gambar Garis Rute Simulasi Perjalanan (Polyline) Halus Warna Ungu Utama (#7542EF)
            if (showRoutePoints) {
                drawRoutePolyline(width, height)
            }
        }

        // Penanda Koordinat / Peta Presisi di Kiri Bawah (Di atas batas lipatan sheet)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, bottom = 40.dp)
                .background(
                    color = Color(0xCCFFFFFF),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Peta Kaltim • 0.5021° S, 117.1536° E",
                style = TextStyle(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF6B7280)
                )
            )
        }
    }
}

/**
 * Menggambar blok-blok area kawasan kota modern dengan sudut melengkung
 */
private fun DrawScope.drawCityBlocks(w: Float, h: Float) {
    val blockColor = Color(0xFFF7F8FA)
    val blockAccent = Color(0xFFECEBFF)

    val blocks = listOf(
        RectDef(w * 0.05f, h * 0.08f, w * 0.28f, h * 0.12f, blockColor),
        RectDef(w * 0.38f, h * 0.06f, w * 0.32f, h * 0.10f, blockAccent),
        RectDef(w * 0.74f, h * 0.10f, w * 0.22f, h * 0.15f, blockColor),

        RectDef(w * 0.04f, h * 0.24f, w * 0.25f, h * 0.14f, blockAccent),
        RectDef(w * 0.34f, h * 0.22f, w * 0.36f, h * 0.16f, blockColor),
        RectDef(w * 0.75f, h * 0.28f, w * 0.21f, h * 0.18f, blockAccent),

        RectDef(w * 0.06f, h * 0.44f, w * 0.30f, h * 0.14f, blockColor),
        RectDef(w * 0.42f, h * 0.43f, w * 0.28f, h * 0.15f, blockAccent),
        RectDef(w * 0.76f, h * 0.48f, w * 0.20f, h * 0.14f, blockColor),

        RectDef(w * 0.08f, h * 0.62f, w * 0.32f, h * 0.14f, blockAccent),
        RectDef(w * 0.46f, h * 0.61f, w * 0.30f, h * 0.16f, blockColor)
    )

    blocks.forEach { b ->
        drawRoundRect(
            color = b.color,
            topLeft = Offset(b.x, b.y),
            size = Size(b.w, b.h),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }
}

/**
 * Menggambar alur air/sungai estetik khas Kalimantan Timur
 */
private fun DrawScope.drawWaterway(w: Float, h: Float) {
    val riverPath = Path().apply {
        moveTo(-20f, h * 0.38f)
        cubicTo(
            w * 0.30f, h * 0.34f,
            w * 0.45f, h * 0.46f,
            w * 0.70f, h * 0.40f
        )
        cubicTo(
            w * 0.85f, h * 0.36f,
            w * 0.95f, h * 0.42f,
            w + 40f, h * 0.39f
        )
    }

    drawPath(
        path = riverPath,
        color = Color(0xFFD8E5F8), // Biru pastel sangat lembut
        style = Stroke(
            width = 32f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Menggambar jaringan jalanan presisi (Jalan arteri putih bersih dan jalan lokal)
 */
private fun DrawScope.drawRoadNetwork(w: Float, h: Float) {
    val secondaryRoadColor = Color(0xFFE2E7ED)
    val mainRoadBorderColor = Color(0xFFD3DAE2)
    val mainRoadColor = Color(0xFFFFFFFF)

    // A. Jalan Lokal / Sekunder Halus (Grid halus melintang)
    val secondaryPaths = listOf(
        // Garis-garis horizontal/diagonal
        Path().apply { moveTo(0f, h * 0.18f); lineTo(w, h * 0.20f) },
        Path().apply { moveTo(0f, h * 0.36f); lineTo(w, h * 0.35f) },
        Path().apply { moveTo(0f, h * 0.56f); lineTo(w, h * 0.58f) },
        Path().apply { moveTo(0f, h * 0.74f); lineTo(w, h * 0.72f) },

        // Garis-garis vertikal
        Path().apply { moveTo(w * 0.22f, 0f); lineTo(w * 0.20f, h) },
        Path().apply { moveTo(w * 0.55f, 0f); lineTo(w * 0.57f, h) },
        Path().apply { moveTo(w * 0.82f, 0f); lineTo(w * 0.80f, h) }
    )

    secondaryPaths.forEach { path ->
        drawPath(
            path = path,
            color = secondaryRoadColor,
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )
    }

    // B. Jalan Arteri Utama (Melengkung estetik menghubungkan kota)
    val arterialRoads = listOf(
        Path().apply {
            moveTo(w * 0.12f, h)
            cubicTo(w * 0.16f, h * 0.65f, w * 0.35f, h * 0.50f, w * 0.48f, h * 0.32f)
            cubicTo(w * 0.58f, h * 0.18f, w * 0.75f, h * 0.12f, w * 0.88f, 0f)
        },
        Path().apply {
            moveTo(0f, h * 0.48f)
            cubicTo(w * 0.25f, h * 0.45f, w * 0.50f, h * 0.52f, w, h * 0.46f)
        }
    )

    // Border jalan arteri
    arterialRoads.forEach { path ->
        drawPath(
            path = path,
            color = mainRoadBorderColor,
            style = Stroke(width = 18f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Inti jalan arteri (Putih bersih)
    arterialRoads.forEach { path ->
        drawPath(
            path = path,
            color = mainRoadColor,
            style = Stroke(width = 12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Menggambar garis rute abstrak/simulasi perjalanan (Polyline) yang halus
 * dengan warna ungu utama (#7542EF) beserta titik mula dan tujuan
 */
private fun DrawScope.drawRoutePolyline(w: Float, h: Float) {
    val purplePrimary = Color(0xFF7542EF)
    val purpleGlow = Color(0x337542EF)
    val purpleHalo = Color(0x187542EF)

    // Koordinat Titik Asal & Tujuan Simulasi (Samarinda Seberang -> Balikpapan)
    val startX = w * 0.24f
    val startY = h * 0.58f
    val endX = w * 0.76f
    val endY = h * 0.18f

    // Jalur Polyline Halus (Smooth Cubic Bezier Kurva Navigasi)
    val routePath = Path().apply {
        moveTo(startX, startY)
        cubicTo(
            w * 0.32f, h * 0.52f,
            w * 0.38f, h * 0.42f,
            w * 0.50f, h * 0.35f
        )
        cubicTo(
            w * 0.60f, h * 0.28f,
            w * 0.68f, h * 0.25f,
            endX, endY
        )
    }

    // 1. Layer Paling Luar: Soft Halo Glow Ungu (#7542EF transparan)
    drawPath(
        path = routePath,
        color = purpleHalo,
        style = Stroke(
            width = 24f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 2. Layer Tengah: Outer Glow Ungu (#7542EF semi-transparan)
    drawPath(
        path = routePath,
        color = purpleGlow,
        style = Stroke(
            width = 14f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 3. Layer Utama: Garis Rute Presisi dan Halus (#7542EF)
    drawPath(
        path = routePath,
        color = purplePrimary,
        style = Stroke(
            width = 6f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 4. Titik Asal (Start Pin Marker - Dot Putih dengan Ring Ungu #7542EF)
    // Radar pulse luar
    drawCircle(
        color = purpleHalo,
        radius = 22f,
        center = Offset(startX, startY)
    )
    // Ring luar putih
    drawCircle(
        color = Color.White,
        radius = 12f,
        center = Offset(startX, startY)
    )
    // Ring tengah ungu utama
    drawCircle(
        color = purplePrimary,
        radius = 8f,
        center = Offset(startX, startY)
    )
    // Dot putih di pusat
    drawCircle(
        color = Color.White,
        radius = 3.5f,
        center = Offset(startX, startY)
    )

    // 5. Titik Tujuan (Destination Pin Marker - Ikon Penanda dengan Aksen Ungu #7542EF)
    // Halo glow tujuan
    drawCircle(
        color = purpleHalo,
        radius = 26f,
        center = Offset(endX, endY)
    )
    // Outer lingkaran putih tebal
    drawCircle(
        color = Color.White,
        radius = 14f,
        center = Offset(endX, endY)
    )
    // Titik ungu utama
    drawCircle(
        color = purplePrimary,
        radius = 10f,
        center = Offset(endX, endY)
    )
    // Dot putih kecil
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(endX, endY)
    )
}

private data class RectDef(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val color: Color
)
