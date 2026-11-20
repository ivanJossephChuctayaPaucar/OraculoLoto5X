package com.oraculoloto5x.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.oraculoloto5x.app.ui.screens.*
import com.oraculoloto5x.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OraculoLoto5XTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isProfileOpen by viewModel.isProfileDialogOpen.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val drawHistory by viewModel.drawHistory.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showHistoryModal by remember { mutableStateOf(false) }
    var showBooksModal by remember { mutableStateOf(false) }
    var showHoursModal by remember { mutableStateOf(false) }
    var showClearChatDialog by remember { mutableStateOf(false) }

    val lastDrawDate = remember(drawHistory) {
        drawHistory.firstOrNull()?.drawDate?.toString() ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "ORÁCULO 5X",
                            color = GoldLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                actions = {
                    // Badge Interactivo de Sincronización de La Tinka
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isSyncing) {
                                viewModel.syncTinkaDrawsOnline(showFeedback = true)
                            },
                        color = Color(0xFF1E2838),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSyncing) GoldPrimary else BallBorderGold.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    strokeWidth = 2.dp,
                                    color = GoldLight
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sync...",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Sincronizar",
                                    tint = GoldLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lastDrawDate.isNotEmpty()) lastDrawDate.takeLast(5) else "Sync",
                                    color = GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Botón para Limpiar Chat
                    IconButton(onClick = { showClearChatDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Limpiar Chat",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Menú de opciones (Ajustes, Tratados, Historial, Reloj)
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menú",
                                tint = GoldLight
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = null,
                                        tint = GoldLight
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Ajustes y API Key",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.setProfileDialogOpen(true)
                                }
                            )

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = GoldLight
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Historial y Backtest",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showHistoryModal = true
                                }
                            )

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = GoldLight
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Biblioteca de Tratados",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showBooksModal = true
                                }
                            )

                            DropdownMenuItem(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = GoldLight
                                    )
                                },
                                text = {
                                    Text(
                                        text = "Reloj Planetario 24h",
                                        color = TextPrimary,
                                        fontSize = 13.sp
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showHoursModal = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = GoldLight
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            UnifiedOracleChatScreen(viewModel = viewModel)
        }
    }

    // ── DIÁLOGO CONFIRMACIÓN LIMPIAR CHAT ─────────────────────────────────────
    if (showClearChatDialog) {
        AlertDialog(
            onDismissRequest = { showClearChatDialog = false },
            title = {
                Text(
                    text = "¿Reiniciar conversación?",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Se borrará el historial de mensajes de la pantalla para comenzar una nueva sesión con el Oráculo.",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearChatHistory()
                        showClearChatDialog = false
                    }
                ) {
                    Text(text = "Limpiar", color = SemaphoreRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearChatDialog = false }) {
                    Text(text = "Cancelar", color = TextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // ── MODAL AJUSTES Y API KEY ────────────────────────────────────────────────
    if (isProfileOpen) {
        ProfileSettingsDialog(
            currentProfile = userProfile,
            onDismiss = { viewModel.setProfileDialogOpen(false) },
            onSaveProfile = { newProfile ->
                viewModel.updateProfile(newProfile)
            }
        )
    }

    // ── MODAL HISTORIAL Y BACKTESTING ──────────────────────────────────────────
    if (showHistoryModal) {
        Dialog(
            onDismissRequest = { showHistoryModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Historial y Backtesting",
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { showHistoryModal = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = GoldLight
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBackground,
                            titleContentColor = GoldLight
                        )
                    )
                },
                containerColor = DarkBackground
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    HistoryBacktestScreen(viewModel = viewModel)
                }
            }
        }
    }

    // ── MODAL BIBLIOTECA DE LOS 5 TRATADOS ────────────────────────────────────
    if (showBooksModal) {
        Dialog(
            onDismissRequest = { showBooksModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Tratados Herméticos (5 PDFs)",
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { showBooksModal = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = GoldLight
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBackground,
                            titleContentColor = GoldLight
                        )
                    )
                },
                containerColor = DarkBackground
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    FiveBooksAnalysisScreen(viewModel = viewModel)
                }
            }
        }
    }

    // ── MODAL RELOJ PLANETARIO 24 HORAS ────────────────────────────────────────
    if (showHoursModal) {
        Dialog(
            onDismissRequest = { showHoursModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Reloj Planetario 24 Horas",
                                color = GoldLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { showHoursModal = false }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = GoldLight
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = DarkBackground,
                            titleContentColor = GoldLight
                        )
                    )
                },
                containerColor = DarkBackground
            ) { padding ->
                Box(modifier = Modifier.padding(padding)) {
                    HoursAndDaysScreen(viewModel = viewModel)
                }
            }
        }
    }
}
