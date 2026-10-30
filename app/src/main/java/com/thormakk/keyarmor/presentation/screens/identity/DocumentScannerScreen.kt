package com.thormakk.keyarmor.presentation.screens.identity

import com.thormakk.keyarmor.ui.theme.appButtonColors
import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.thormakk.keyarmor.R
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.scale
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.thormakk.keyarmor.ocr.OcrDocumentParser
import com.thormakk.keyarmor.ocr.OcrResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs

private enum class ScanStep { FRONT, BACK }

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DocumentScannerScreen(
    onResult:  (OcrResult, frontPhoto: ByteArray?, backPhoto: ByteArray?) -> Unit,
    onDismiss: () -> Unit
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope          = rememberCoroutineScope()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var scanStep        by remember { mutableStateOf(ScanStep.FRONT) }
    var frontPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }

    val capturedFlow  = remember { MutableStateFlow<OcrResult?>(null) }
    val lastFrameFlow = remember { MutableStateFlow<OcrResult?>(null) }
    val lastFrame     by lastFrameFlow.collectAsState()

    val analyzerActive = remember { AtomicBoolean(false) }
    LaunchedEffect(scanStep) {
        analyzerActive.set(scanStep == ScanStep.BACK)
        capturedFlow.value = null
    }

    var showManualButton by remember { mutableStateOf(false) }
    LaunchedEffect(scanStep) {
        showManualButton = false
        if (scanStep == ScanStep.BACK) {
            kotlinx.coroutines.delay(4_000)
            showManualButton = true
        }
    }

    val analysisExecutor  = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderRef = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val previewView       = remember { PreviewView(context) }

    DisposableEffect(Unit) {
        onDispose {
            cameraProviderRef.value?.unbindAll()
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(capturedFlow) {
        capturedFlow.filterNotNull().collect { result ->
            val src       = previewView.bitmap
            val backBytes = withContext(Dispatchers.Default) {
                src?.let { cropAndDeskewBitmap(it) }
            }
            onResult(result, frontPhotoBytes, backBytes)
        }
    }

    BackHandler { onDismiss() }

    LaunchedEffect(cameraPermission.status) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (cameraPermission.status.isGranted) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

            LaunchedEffect(previewView) {
                val provider = getCameraProvider(context)
                cameraProviderRef.value = provider

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = DocumentAnalyzer(capturedFlow, lastFrameFlow, analyzerActive)

                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply { setAnalyzer(analysisExecutor, analyzer) }

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis
                )
            }

            ScanGuideOverlay(step = scanStep)

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 148.dp)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when (scanStep) {
                        ScanStep.FRONT -> stringResource(R.string.doc_scanner_step_front)
                        ScanStep.BACK  -> stringResource(R.string.doc_scanner_step_back)
                    },
                    color     = Color.White.copy(alpha = 0.55f),
                    style     = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (scanStep) {
                        ScanStep.FRONT -> stringResource(R.string.doc_scanner_align_front)
                        ScanStep.BACK  -> stringResource(R.string.doc_scanner_searching_mrz)
                    },
                    color     = Color.White,
                    style     = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                )
            }

            if (scanStep == ScanStep.FRONT) {
                Button(
                    onClick = {
                        scope.launch {
                            val src = previewView.bitmap
                            frontPhotoBytes = withContext(Dispatchers.Default) {
                                src?.let { cropAndDeskewBitmap(it) }
                            }
                            scanStep = ScanStep.BACK
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 56.dp)
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp), colors = appButtonColors()
                ) { Text(stringResource(R.string.doc_scanner_capture_front_button)) }
            }

            AnimatedVisibility(
                visible  = showManualButton && scanStep == ScanStep.BACK,
                enter    = fadeIn(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
            ) {
                OutlinedButton(
                    onClick = { capturedFlow.value = lastFrame ?: OcrResult() },
                    shape   = RoundedCornerShape(8.dp),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border  = BorderStroke(1.dp, Color.White.copy(alpha = 0.65f)),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) { Text(stringResource(R.string.doc_scanner_capture_now_button)) }
            }

        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.doc_scanner_camera_permission_needed), color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { cameraPermission.launchPermissionRequest() }, colors = appButtonColors()) {
                    Text(stringResource(R.string.doc_scanner_grant_permission_button))
                }
            }
        }

        IconButton(
            onClick  = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(8.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close), tint = Color.White)
        }
    }
}

@Composable
private fun ScanGuideOverlay(step: ScanStep) {
    val transition = rememberInfiniteTransition(label = "scan_line")
    val scanProgress by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "progress"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    ) {
        val guideW = size.width  * 0.88f
        val guideH = guideW / 1.586f
        val left   = (size.width  - guideW) / 2f
        val top    = (size.height - guideH) / 2f - size.height * 0.04f

        drawRect(Color.Black.copy(alpha = 0.55f))
        drawRect(
            color     = Color.Transparent,
            topLeft   = Offset(left, top),
            size      = Size(guideW, guideH),
            blendMode = BlendMode.Clear
        )

        val cl = 28.dp.toPx()
        val sw = 3.dp.toPx()
        val c  = if (step == ScanStep.FRONT) Color.White else Color(0xFF00E676)
        drawLine(c, Offset(left,          top),          Offset(left + cl,          top),               sw)
        drawLine(c, Offset(left,          top),          Offset(left,               top + cl),          sw)
        drawLine(c, Offset(left + guideW, top),          Offset(left + guideW - cl, top),               sw)
        drawLine(c, Offset(left + guideW, top),          Offset(left + guideW,      top + cl),          sw)
        drawLine(c, Offset(left,          top + guideH), Offset(left + cl,          top + guideH),      sw)
        drawLine(c, Offset(left,          top + guideH), Offset(left,               top + guideH - cl), sw)
        drawLine(c, Offset(left + guideW, top + guideH), Offset(left + guideW - cl, top + guideH),     sw)
        drawLine(c, Offset(left + guideW, top + guideH), Offset(left + guideW,      top + guideH - cl),sw)

        if (step == ScanStep.BACK) {
            val lineY = top + guideH * scanProgress
            drawLine(
                brush       = Brush.horizontalGradient(
                    colors  = listOf(Color.Transparent, Color(0xFF00E676), Color.Transparent),
                    startX  = left,
                    endX    = left + guideW
                ),
                start       = Offset(left, lineY),
                end         = Offset(left + guideW, lineY),
                strokeWidth = 2.dp.toPx()
            )
        }
    }
}

private fun cropAndDeskewBitmap(src: Bitmap): ByteArray? {
    val w   = src.width.toFloat()
    val h   = src.height.toFloat()

    val guideW = w * 0.88f
    val guideH = guideW / 1.586f
    val guideX = (w - guideW) / 2f
    val guideY = (h - guideH) / 2f - h * 0.04f

    val cx = guideX.coerceAtLeast(0f).toInt()
    val cy = guideY.coerceAtLeast(0f).toInt()
    val cw = guideW.toInt().coerceAtMost(src.width  - cx)
    val ch = guideH.toInt().coerceAtMost(src.height - cy)

    var bmp = Bitmap.createBitmap(src, cx, cy, cw, ch)
    src.recycle()

    val angle = findSkewByVOPP(bmp)
    if (abs(angle) > 0.4f) {
        val m        = Matrix().apply { postRotate(angle, bmp.width / 2f, bmp.height / 2f) }
        val deskewed = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
        bmp.recycle()
        bmp = deskewed
    }

    return ByteArrayOutputStream().also { out ->
        bmp.compress(Bitmap.CompressFormat.JPEG, 88, out)
        bmp.recycle()
    }.toByteArray()
}

private fun findSkewByVOPP(src: Bitmap): Float {
    val scale = (480f / src.width).coerceAtMost(1f)
    val small = if (scale < 0.99f)
        src.scale((src.width * scale).toInt(), (src.height * scale).toInt())
    else src

    var bestAngle = 0f
    var bestVar   = projVariance(small)

    for (tenths in listOf(-50, -40, -30, -20, -10, 10, 20, 30, 40, 50)) {
        val a = tenths / 10f
        val m = Matrix().apply { postRotate(a, small.width / 2f, small.height / 2f) }
        val r = Bitmap.createBitmap(small, 0, 0, small.width, small.height, m, true)
        val v = projVariance(r)
        r.recycle()
        if (v > bestVar) { bestVar = v; bestAngle = a }
    }

    if (small !== src) small.recycle()
    return bestAngle
}

private fun projVariance(bmp: Bitmap): Double {
    val w   = bmp.width
    val h   = bmp.height
    val pix = IntArray(w * h)
    bmp.getPixels(pix, 0, w, 0, 0, w, h)

    val rowSums = DoubleArray(h)
    for (y in 0 until h) {
        var s = 0L
        for (x in 0 until w) {
            val c = pix[y * w + x]
            s += (c shr 16 and 0xFF) * 299L + (c shr 8 and 0xFF) * 587L + (c and 0xFF) * 114L
        }
        rowSums[y] = s.toDouble()
    }
    val mean = rowSums.average()
    return rowSums.sumOf { d -> (d - mean) * (d - mean) } / h
}

private class DocumentAnalyzer(
    private val capturedFlow:  MutableStateFlow<OcrResult?>,
    private val lastFrameFlow: MutableStateFlow<OcrResult?>,
    private val active:        AtomicBoolean
) : ImageAnalysis.Analyzer {

    private val recognizer    = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var frameCount    = 0
    private var lastResult:   OcrResult? = null
    private var stableCount   = 0
    private val scanStartTime = System.currentTimeMillis()
    private val recentNumbers = ArrayDeque<String>(MAX_HISTORY)

    companion object {
        private const val MAX_HISTORY       = 20
        private const val MIN_SCAN_MS       = 4_000L
        private const val STABLE_FRAMES_REQ = 3
    }

    override fun analyze(imageProxy: ImageProxy) {
        if (capturedFlow.value != null || !active.get()) {
            imageProxy.close(); return
        }

        frameCount++
        if (frameCount % 3 != 0) { imageProxy.close(); return }

        val rotation  = imageProxy.imageInfo.rotationDegrees
        val rawBitmap = imageProxy.toBitmap()
        imageProxy.close()

        val portrait = if (rotation != 0) {
            val m = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, m, true)
                .also { rawBitmap.recycle() }
        } else rawBitmap

        val inputImage = InputImage.fromBitmap(portrait, 0)

        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                portrait.recycle()
                val lines  = visionText.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
                val result = OcrDocumentParser.parse(lines)
                if (!result.hasAnyData) { stableCount = 0; return@addOnSuccessListener }

                result.number?.let { num ->
                    if (recentNumbers.size >= MAX_HISTORY) recentNumbers.removeFirst()
                    recentNumbers.addLast(num)
                }
                lastFrameFlow.value = result

                if (isSameResult(result, lastResult)) {
                    stableCount++
                    val elapsed = System.currentTimeMillis() - scanStartTime
                    if (stableCount >= STABLE_FRAMES_REQ && elapsed >= MIN_SCAN_MS) {
                        val voted = consensusNumber()
                        capturedFlow.value = if (voted != null) result.copy(number = voted) else result
                    }
                } else {
                    lastResult  = result
                    stableCount = 1
                }
            }
            .addOnFailureListener { portrait.recycle() }
    }

    private fun consensusNumber(): String? =
        recentNumbers.ifEmpty { return null }
            .groupBy { it }.maxByOrNull { it.value.size }?.key

    private fun isSameResult(a: OcrResult, b: OcrResult?): Boolean {
        if (b == null) return false
        return (a.number != null && a.number == b.number) ||
               (a.holderName != null && a.holderName == b.holderName)
    }
}

private suspend fun getCameraProvider(context: Context): ProcessCameraProvider =
    suspendCancellableCoroutine { cont ->
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                @Suppress("BlockingMethodInNonBlockingContext")
                try   { cont.resume(future.get()) }
                catch (e: Exception) { cont.resumeWithException(e) }
            },
            ContextCompat.getMainExecutor(context)
        )
    }
