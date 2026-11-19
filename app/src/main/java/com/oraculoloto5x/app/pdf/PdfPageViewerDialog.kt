package com.oraculoloto5x.app.pdf

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oraculoloto5x.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPageViewerDialog(
    citation: BookCitation,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp

    var currentPageIndex by remember { mutableStateOf(citation.pdfPageIndex) }
    var pageCount by remember { mutableStateOf(0) }
    var renderedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Pinch-to-zoom state
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    // SAF folder picker
    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            NativePdfManager.saveTreeUri(context, uri)
            isLoading = true
            errorMessage = null
            scope.launch { loadPage(context, citation, currentPageIndex, screenWidth) { bmp, pg, err ->
                renderedBitmap = bmp
                pageCount = pg
                isLoading = false
                errorMessage = err
            }}
        }
    }

    // Load page on first composition and when pageIndex changes
    LaunchedEffect(currentPageIndex) {
        isLoading = true
        renderedBitmap = null
        scale = 1f; offsetX = 0f; offsetY = 0f
        loadPage(context, citation, currentPageIndex, screenWidth) { bmp, pg, err ->
            renderedBitmap = bmp
            pageCount = pg
            isLoading = false
            errorMessage = err
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0C10))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top bar ──────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = citation.bookTitle,
                                color = GoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${citation.chapterLabel}  ·  ${if (pageCount > 0) "Pág. ${currentPageIndex + 1} / $pageCount" else "Cargando..."}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                        }
                    }
                }

                // ── Page content ─────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        errorMessage != null -> {
                            // Not linked or file not found
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text(
                                    text = "📚",
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = "Libro no encontrado",
                                    color = GoldLight,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vincula la carpeta que contiene:\n«${citation.expectedFileName}»",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )

                                // Quoted text as fallback
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF1A2235))
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(14.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(
                                            text = "\"${citation.literalQuote}\"",
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                        Text(
                                            text = "— ${citation.author}, ${citation.chapterLabel}",
                                            color = GoldPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Button(
                                    onClick = { folderPicker.launch(null) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldPrimary,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Vincular carpeta de PDFs", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        isLoading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = GoldPrimary, modifier = Modifier.size(40.dp))
                                Text("Renderizando página ${currentPageIndex + 1}...", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        renderedBitmap != null -> {
                            // Pinch-to-zoom image
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, zoom, _ ->
                                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                                            offsetX += pan.x
                                            offsetY += pan.y
                                        }
                                    }
                            ) {
                                Image(
                                    bitmap = renderedBitmap!!.asImageBitmap(),
                                    contentDescription = "Página ${currentPageIndex + 1}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = scale,
                                            scaleY = scale,
                                            translationX = offsetX,
                                            translationY = offsetY
                                        )
                                )
                            }
                        }
                    }
                }

                // ── Bottom navigation ────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                        enabled = currentPageIndex > 0 && !isLoading
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Página anterior",
                            tint = if (currentPageIndex > 0) GoldPrimary else TextMuted
                        )
                    }

                    Text(
                        text = if (pageCount > 0) "${currentPageIndex + 1} / $pageCount" else "...",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    IconButton(
                        onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                        enabled = currentPageIndex < pageCount - 1 && !isLoading
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "Página siguiente",
                            tint = if (currentPageIndex < pageCount - 1) GoldPrimary else TextMuted
                        )
                    }
                }
            }
        }
    }
}

/** Helper to load a page and invoke callback with result */
private suspend fun loadPage(
    context: Context,
    citation: BookCitation,
    pageIndex: Int,
    screenWidthDp: Int,
    callback: (Bitmap?, Int, String?) -> Unit
) {
    val fileUri = NativePdfManager.findFileUri(context, citation.expectedFileName)
    if (fileUri == null) {
        callback(null, 0, "Archivo no encontrado")
        return
    }
    val widthPx = (screenWidthDp * context.resources.displayMetrics.density).toInt()
    val bitmap = NativePdfManager.renderPage(context, fileUri, pageIndex, widthPx)
    val count = NativePdfManager.getPageCount(context, fileUri)
    if (bitmap == null) {
        callback(null, count, "Error renderizando página")
    } else {
        callback(bitmap, count, null)
    }
}
