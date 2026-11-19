package com.oraculoloto5x.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oraculoloto5x.app.data.models.LotteryType
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

enum class NavigationTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    TODAY("Hoy", Icons.Default.AutoAwesome),
    FIVE_BOOKS("Tratados", Icons.Default.MenuBook),
    HOURS_DAYS("Horas y Días", Icons.Default.Schedule),
    HISTORY("Historial", Icons.Default.History)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.TODAY) }
    val selectedLottery by viewModel.selectedLottery.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val isProfileOpen by viewModel.isProfileDialogOpen.collectAsState()

    var lotteryMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ORÁCULO LOTO 5X",
                            color = GoldLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                },
                actions = {
                    // Selector de Lotería
                    Box {
                        TextButton(
                            onClick = { lotteryMenuExpanded = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = GoldPrimary)
                        ) {
                            Text(
                                text = selectedLottery.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Seleccionar Lotería",
                                tint = GoldPrimary
                            )
                        }

                        DropdownMenu(
                            expanded = lotteryMenuExpanded,
                            onDismissRequest = { lotteryMenuExpanded = false },
                            modifier = Modifier.background(DarkSurfaceVariant)
                        ) {
                            for (lottery in LotteryType.entries) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = lottery.displayName,
                                            color = if (lottery == selectedLottery) GoldLight else TextPrimary,
                                            fontWeight = if (lottery == selectedLottery) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectLottery(lottery)
                                        lotteryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Botón de Perfil
                    IconButton(onClick = { viewModel.setProfileDialogOpen(true) }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Perfil",
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
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                for (tab in NavigationTab.entries) {
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.label,
                                tint = if (isSelected) GoldPrimary else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                color = if (isSelected) GoldLight else TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFF263345)
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                NavigationTab.TODAY -> TodayOracleScreen(
                    viewModel = viewModel
                )
                NavigationTab.FIVE_BOOKS -> FiveBooksAnalysisScreen(
                    viewModel = viewModel
                )
                NavigationTab.HOURS_DAYS -> HoursAndDaysScreen(
                    viewModel = viewModel
                )
                NavigationTab.HISTORY -> HistoryBacktestScreen(
                    viewModel = viewModel
                )
            }
        }
    }

    if (isProfileOpen) {
        ProfileSettingsDialog(
            currentProfile = userProfile,
            onDismiss = { viewModel.setProfileDialogOpen(false) },
            onSaveProfile = { newProfile ->
                viewModel.updateProfile(newProfile)
            }
        )
    }
}
