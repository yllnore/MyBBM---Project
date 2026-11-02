package com.mybbm.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mybbm.app.domain.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Membuat drawable marker kustom untuk penanda "Lokasi Anda Saat Ini" (GPS Blue Dot + Halo)
 */
private fun createUserLocationDrawable(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (32 * density).toInt().coerceAtLeast(32)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Lingkaran luar transparan (efek radar halo)
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(70, 31, 107, 232)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, outerPaint)

    // 2. Lingkaran cincin putih
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.38f, ringPaint)

    // 3. Titik biru cerah di tengah
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(31, 107, 232)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.28f, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Komponen Compose yang membungkus osmdroid [MapView] dengan lifecycle-aware,
 * dukungan multi-touch gesture zoom, penanda lokasi (markers), dan polyline rute.
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerCoordinate: LatLng = LatLng(-0.5021, 117.1536), // Samarinda (Polnes)
    zoomLevel: Double = 13.0,
    originCoordinate: LatLng? = null,
    originLabel: String? = null,
    destinationCoordinate: LatLng? = null,
    destinationLabel: String? = null,
    userLocationCoordinate: LatLng? = null,
    cameraTarget: LatLng? = null,
    cameraZoom: Double = 16.5,
    cameraTrigger: Long = 0L,
    routePoints: List<LatLng> = emptyList(),
    routeColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color(0xFF1F6BE8)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Inisialisasi User-Agent dan cache OSM sekali agar tile peta tidak diblokir server OSM
    DisposableEffect(Unit) {
        val config = Configuration.getInstance()
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        
        // Set User-Agent yang valid agar server OSM/CartoDB tidak reject request
        config.userAgentValue = "MyBBM/1.0 Android"
        
        // Set cache path ke internal storage app (tidak butuh storage permission)
        config.osmdroidBasePath = context.filesDir
        config.osmdroidTileCache = java.io.File(context.filesDir, "osmdroid/tiles")
        
        onDispose { }
    }

    // Instansiasi MapView yang di-remembered
    val mapView = remember {
        MapView(context).apply {
            // OSM Default (STABLE & TESTED - Tile pasti jalan)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(zoomLevel)
            controller.setCenter(GeoPoint(centerCoordinate.lat, centerCoordinate.lng))
        }
    }

    // Penanganan lifecycle onResume & onPause yang aman
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_DESTROY -> mapView.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    // State untuk mencatat perubahan koordinat agar animasi kamera tidak terpanggil berulang kali saat recompose
    var lastOrigin by remember { mutableStateOf<LatLng?>(null) }
    var lastDestination by remember { mutableStateOf<LatLng?>(null) }
    var lastRoutePointsSize by remember { mutableIntStateOf(0) }
    var lastCameraTrigger by remember { mutableLongStateOf(0L) }

    // Icon lokasi pengguna
    val userLocationIcon = remember(context) { createUserLocationDrawable(context) }

    // Perbarui overlay (marker & polyline) ketika data rute/marker berubah
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { map ->
            val originChanged = originCoordinate != lastOrigin
            val destChanged = destinationCoordinate != lastDestination
            val routeChanged = routePoints.size != lastRoutePointsSize
            val cameraTriggerChanged = cameraTrigger != 0L && cameraTrigger != lastCameraTrigger

            map.overlays.clear()

            // 1. Gambar Garis Rute (Polyline 2-Layer Glowing Neon) jika tersedia
            if (routePoints.isNotEmpty()) {
                val geoPoints = routePoints.map { GeoPoint(it.lat, it.lng) }
                
                // Layer A: Outer Glow (Tebal & Transparan untuk Efek Neon Glow)
                val outerGlowPolyline = Polyline(map).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#407542EF") // Ungu Neon Accent
                    outlinePaint.strokeWidth = 22f
                    outlinePaint.alpha = 100 // Semi-transparan glow
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                }

                // Layer B: Inner Line (Garis Utama Tajam & Terang)
                val innerPolyline = Polyline(map).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#7542EF") // Pink Magenta Bright
                    outlinePaint.strokeWidth = 8f
                    outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                }

                map.overlays.add(outerGlowPolyline)
                map.overlays.add(innerPolyline)
            }

            // 2. Tambahkan Marker Asal
            if (originCoordinate != null) {
                val originMarker = Marker(map).apply {
                    position = GeoPoint(originCoordinate.lat, originCoordinate.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = originLabel ?: "Lokasi Asal"
                }
                map.overlays.add(originMarker)
            }

            // 3. Tambahkan Marker Tujuan
            if (destinationCoordinate != null) {
                val destMarker = Marker(map).apply {
                    position = GeoPoint(destinationCoordinate.lat, destinationCoordinate.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = destinationLabel ?: "Lokasi Tujuan"
                }
                map.overlays.add(destMarker)
            }

            // 4. Tambahkan Marker Lokasi Saat Ini Pengguna (GPS)
            if (userLocationCoordinate != null) {
                val userMarker = Marker(map).apply {
                    position = GeoPoint(userLocationCoordinate.lat, userLocationCoordinate.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "Lokasi Anda Saat Ini"
                    icon = userLocationIcon
                }
                map.overlays.add(userMarker)
            }

            // Atur zoom dan animasi kamera: prioritas utama jika ada pemicu pemusatan manual
            if (cameraTriggerChanged) {
                lastCameraTrigger = cameraTrigger
                val target = cameraTarget ?: userLocationCoordinate
                if (target != null) {
                    val targetGeo = GeoPoint(target.lat, target.lng)
                    map.controller.setZoom(cameraZoom)
                    map.controller.animateTo(targetGeo)
                }
            } else if (originChanged || destChanged || routeChanged) {
                if (originCoordinate != null && destinationCoordinate != null) {
                    // Posisi tengah di antara asal dan tujuan
                    val midLat = (originCoordinate.lat + destinationCoordinate.lat) / 2
                    val midLng = (originCoordinate.lng + destinationCoordinate.lng) / 2
                    map.controller.setCenter(GeoPoint(midLat, midLng))
                    map.controller.setZoom(12.0)
                } else if (originCoordinate != null) {
                    val originGeo = GeoPoint(originCoordinate.lat, originCoordinate.lng)
                    map.controller.setZoom(17.5) // Zoom dekat tingkat jalan/gang/blok
                    map.controller.animateTo(originGeo)
                } else if (destinationCoordinate != null) {
                    val destGeo = GeoPoint(destinationCoordinate.lat, destinationCoordinate.lng)
                    map.controller.setZoom(17.5)
                    map.controller.animateTo(destGeo)
                } else {
                    map.controller.setCenter(GeoPoint(centerCoordinate.lat, centerCoordinate.lng))
                    map.controller.setZoom(zoomLevel)
                }
            }

            lastOrigin = originCoordinate
            lastDestination = destinationCoordinate
            lastRoutePointsSize = routePoints.size

            map.invalidate()
        }
    )
}

