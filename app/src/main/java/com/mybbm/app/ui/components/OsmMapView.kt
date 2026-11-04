package com.mybbm.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.mybbm.app.R
import com.mybbm.app.domain.model.LatLng
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Konversi vector drawable (R.drawable.lokasi_awal / lokasi_tujuan) menjadi BitmapDrawable
 * agar dapat dipakai sebagai ikon pin marker yang tajam pada OSM MapView.
 */
private fun createCustomPinDrawable(context: Context, resId: Int, widthDp: Int = 30, heightDp: Int = 40): Drawable {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return BitmapDrawable(context.resources)
    val density = context.resources.displayMetrics.density
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, widthPx, heightPx)
    drawable.draw(canvas)
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Membuat drawable marker kustom untuk penanda "Lokasi Anda Saat Ini" (GPS Blue Dot + Halo)
 */
private fun createUserLocationDrawable(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (32 * density).toInt().coerceAtLeast(32)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 1. Lingkaran luar transparan (efek radar halo ungu)
    val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(70, 117, 66, 239) // Ungu #7542EF halo
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, outerPaint)

    // 2. Lingkaran cincin putih
    val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.38f, ringPaint)

    // 3. Titik ungu cerah di tengah
    val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(117, 66, 239)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx * 0.28f, dotPaint)

    return BitmapDrawable(context.resources, bitmap)
}

private fun createSpbuMarkerDrawable(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (36 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Lingkaran putih dengan shadow (meniru desain Figma)
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(6f, 0f, 3f, Color.argb(80, 0, 0, 0))
    }
    val padding = 4 * density
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, (sizePx / 2f) - padding, bgPaint)

    // Ikon bensin di tengah
    val drawable = ContextCompat.getDrawable(context, R.drawable.pertalite)
    if (drawable != null) {
        val iconSize = (20 * density).toInt()
        val left = (sizePx - iconSize) / 2
        val top = (sizePx - iconSize) / 2
        drawable.setBounds(left, top, left + iconSize, top + iconSize)
        drawable.draw(canvas)
    }

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Komponen Compose yang membungkus osmdroid [MapView] interaktif.
 * Menggunakan OpenStreetMap resmi (100% gratis tanpa API key),
 * dipoles dengan ColorFilter skema warna terang estetik (#E8ECEF / #ECEBFF),
 * marker Asal Merah & Tujuan Biru, garis polyline rute ungu (#7542EF),
 * serta auto-fit BoundingBox cerdas agar seluruh rute dan pin kelihatan di layar.
 */
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    centerCoordinate: LatLng = LatLng(-0.5021, 117.1536), // Samarinda (Polnes)
    zoomLevel: Double = 13.0,
    savedCameraCenter: LatLng? = null,
    savedCameraZoom: Double? = null,
    onCameraMoved: ((center: LatLng, zoom: Double) -> Unit)? = null,
    originCoordinate: LatLng? = null,
    originLabel: String? = null,
    destinationCoordinate: LatLng? = null,
    destinationLabel: String? = null,
    userLocationCoordinate: LatLng? = null,
    cameraTarget: LatLng? = null,
    cameraZoom: Double = 16.5,
    cameraTrigger: Long = 0L,
    routePoints: List<LatLng> = emptyList(),
    spbuCoordinates: List<LatLng> = emptyList()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Inisialisasi User-Agent dan cache OSM
    DisposableEffect(Unit) {
        val config = Configuration.getInstance()
        config.load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        config.userAgentValue = "MyBBM/1.0 Android"
        config.osmdroidBasePath = context.filesDir
        config.osmdroidTileCache = java.io.File(context.filesDir, "osmdroid/tiles")
        onDispose { }
    }

    val initialCenter = savedCameraCenter ?: centerCoordinate
    val initialZoom = savedCameraZoom ?: zoomLevel

    // Instansiasi MapView yang di-remembered
    val mapView = remember {
        MapView(context).apply {
            // Latar belakang netral terang (#E8ECEF)
            setBackgroundColor(android.graphics.Color.parseColor("#E8ECEF"))

            // Gunakan TileSourceFactory.MAPNIK (Server Resmi OpenStreetMap - 100% Gratis & Tanpa API Key)
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
            controller.setZoom(initialZoom)
            controller.setCenter(GeoPoint(initialCenter.lat, initialCenter.lng))

            addMapListener(object : MapListener {
                override fun onScroll(event: ScrollEvent?): Boolean {
                    val c = mapCenter
                    onCameraMoved?.invoke(LatLng(c.latitude, c.longitude), zoomLevelDouble)
                    return false
                }

                override fun onZoom(event: ZoomEvent?): Boolean {
                    val c = mapCenter
                    onCameraMoved?.invoke(LatLng(c.latitude, c.longitude), zoomLevelDouble)
                    return false
                }
            })

            // Poles warna tile OSM agar bernuansa terang, bersih, dan selaras dengan tema #E8ECEF / #ECEBFF
            val colorMatrix = ColorMatrix().apply {
                setSaturation(0.45f)
                val brightMatrix = floatArrayOf(
                    0.88f, 0f,    0f,    0f, 40f,
                    0f,    0.88f, 0f,    0f, 40f,
                    0f,    0f,    0.95f, 0f, 50f,
                    0f,    0f,    0f,    1f, 0f
                )
                postConcat(ColorMatrix(brightMatrix))
            }
            overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(colorMatrix))
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

    var autoFittedRouteKey by rememberSaveable { mutableStateOf("") }
    val currentRouteKey = "${originCoordinate?.lat},${originCoordinate?.lng}_${destinationCoordinate?.lat},${destinationCoordinate?.lng}_${routePoints.size}"

    var lastOrigin by remember { mutableStateOf<LatLng?>(null) }
    var lastDestination by remember { mutableStateOf<LatLng?>(null) }
    var lastRoutePointsSize by remember { mutableIntStateOf(0) }
    var lastCameraTrigger by remember { mutableLongStateOf(0L) }
    var isFirstComposition by remember { mutableStateOf(true) }

    // Ikon Marker: Asal (Merah), Tujuan (Biru), dan Lokasi GPS Saat Ini (Ungu Radar)
    val originIcon = remember(context) { createCustomPinDrawable(context, R.drawable.lokasi_awal, 30, 40) }
    val destIcon = remember(context) { createCustomPinDrawable(context, R.drawable.lokasi_tujuan, 30, 40) }
    val userLocationIcon = remember(context) { createUserLocationDrawable(context) }
    val spbuMarkerIcon = remember(context) { createSpbuMarkerDrawable(context) }

    // Perbarui overlay (marker & polyline ungu #7542EF) ketika data berubah
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { mapView },
        update = { map ->
            val cameraTriggerChanged = cameraTrigger != 0L && cameraTrigger != lastCameraTrigger
            val shouldAutoFit = (savedCameraCenter == null && currentRouteKey != autoFittedRouteKey) && (originCoordinate != null || destinationCoordinate != null)

            map.overlays.clear()

            // 1. Gambar Garis Rute (Polyline Halus Warna Ungu #7542EF)
            // HANYA MUNCUL KETIKA SUDAH HITUNG BIAYA / RUTE ADA (routePoints tidak kosong)
            if (routePoints.isNotEmpty()) {
                val geoPoints = routePoints.map { GeoPoint(it.lat, it.lng) }

                // Layer A: Outer Glow Ungu Utama (#7542EF Semi-Transparan)
                val outerGlowPolyline = Polyline(map).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#407542EF")
                    outlinePaint.strokeWidth = 20f
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                }

                // Layer B: Garis Utama Tajam Halus (#7542EF)
                val innerPolyline = Polyline(map).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.parseColor("#7542EF")
                    outlinePaint.strokeWidth = 8f
                    outlinePaint.strokeCap = Paint.Cap.ROUND
                }

                map.overlays.add(outerGlowPolyline)
                map.overlays.add(innerPolyline)
            }

            // 2. Tambahkan Marker Asal (Pin Merah Sesuai Input Field Asal)
            if (originCoordinate != null) {
                val originMarker = Marker(map).apply {
                    position = GeoPoint(originCoordinate.lat, originCoordinate.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = originLabel ?: "Lokasi Asal"
                    icon = originIcon
                }
                map.overlays.add(originMarker)
            }

            // 3. Tambahkan Marker Tujuan (Pin Biru Sesuai Input Field Tujuan)
            if (destinationCoordinate != null) {
                val destMarker = Marker(map).apply {
                    position = GeoPoint(destinationCoordinate.lat, destinationCoordinate.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    title = destinationLabel ?: "Lokasi Tujuan"
                    icon = destIcon
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

            // 4.1. Tambahkan Marker SPBU Terdekat (Tanpa rute garis, bentuk lingkaran putih ber-icon)
            for (coord in spbuCoordinates) {
                val spbuMarker = Marker(map).apply {
                    position = GeoPoint(coord.lat, coord.lng)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = "SPBU Terdekat"
                    icon = spbuMarkerIcon
                }
                map.overlays.add(spbuMarker)
            }

            // 5. Atur Kamera, Zoom, dan Auto-Fit Rute agar Titik Awal & Tujuan Kelihatan Semua
            if (cameraTriggerChanged) {
                lastCameraTrigger = cameraTrigger
                val target = cameraTarget ?: userLocationCoordinate
                if (target != null) {
                    val targetGeo = GeoPoint(target.lat, target.lng)
                    map.controller.setZoom(cameraZoom)
                    map.controller.animateTo(targetGeo)
                }
            } else if (shouldAutoFit) {
                autoFittedRouteKey = currentRouteKey
                if (originCoordinate != null && destinationCoordinate != null) {
                    // Kumpulkan semua titik (rute polyline + asal + tujuan) untuk menghitung BoundingBox
                    val allPoints = mutableListOf<GeoPoint>()
                    if (routePoints.isNotEmpty()) {
                        allPoints.addAll(routePoints.map { GeoPoint(it.lat, it.lng) })
                    }
                    allPoints.add(GeoPoint(originCoordinate.lat, originCoordinate.lng))
                    allPoints.add(GeoPoint(destinationCoordinate.lat, destinationCoordinate.lng))

                    var minLat = Double.MAX_VALUE
                    var maxLat = -Double.MAX_VALUE
                    var minLng = Double.MAX_VALUE
                    var maxLng = -Double.MAX_VALUE

                    for (p in allPoints) {
                        if (p.latitude < minLat) minLat = p.latitude
                        if (p.latitude > maxLat) maxLat = p.latitude
                        if (p.longitude < minLng) minLng = p.longitude
                        if (p.longitude > maxLng) maxLng = p.longitude
                    }

                    val latSpan = (maxLat - minLat).coerceAtLeast(0.02)
                    val lngSpan = (maxLng - minLng).coerceAtLeast(0.02)

                    // Berikan padding margin agar pin dan rute tidak menempel ke tepi layar
                    val boundingBox = BoundingBox(
                        maxLat + (latSpan * 0.22),
                        maxLng + (lngSpan * 0.22),
                        minLat - (latSpan * 0.22),
                        minLng - (lngSpan * 0.22)
                    )

                    val density = context.resources.displayMetrics.density
                    val paddingPx = (50 * density).toInt()

                    map.post {
                        try {
                            map.zoomToBoundingBox(boundingBox, true, paddingPx)
                        } catch (e: Exception) {
                            // Fallback jika view belum siap
                            val midLat = (originCoordinate.lat + destinationCoordinate.lat) / 2
                            val midLng = (originCoordinate.lng + destinationCoordinate.lng) / 2
                            map.controller.setCenter(GeoPoint(midLat, midLng))
                            map.controller.setZoom(9.5)
                        }
                    }
                } else if (originCoordinate != null) {
                    val originGeo = GeoPoint(originCoordinate.lat, originCoordinate.lng)
                    map.controller.setZoom(17.5)
                    map.controller.animateTo(originGeo)
                } else if (destinationCoordinate != null) {
                    val destGeo = GeoPoint(destinationCoordinate.lat, destinationCoordinate.lng)
                    map.controller.setZoom(17.5)
                    map.controller.animateTo(destGeo)
                } else if (isFirstComposition) {
                    map.controller.setCenter(GeoPoint(centerCoordinate.lat, centerCoordinate.lng))
                    map.controller.setZoom(zoomLevel)
                }
            }

            lastOrigin = originCoordinate
            lastDestination = destinationCoordinate
            lastRoutePointsSize = routePoints.size
            isFirstComposition = false

            map.invalidate()
        }
    )
}
