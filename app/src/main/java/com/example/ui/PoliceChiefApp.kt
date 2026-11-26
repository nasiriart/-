package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MissionStatus
import com.example.ui.components.CommandTopBar
import com.example.ui.components.DispatchMissionDialog
import com.example.ui.components.MissionResultDialog
import com.example.ui.screens.GarageScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MissionsScreen
import com.example.ui.screens.OfficersScreen
import com.example.ui.screens.PrisonScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StationScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.AlertRed
import com.example.ui.theme.IceBlueSurface
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.PoliceBlue
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SuccessGreenBright
import com.example.ui.viewmodel.GameViewModel
import com.example.util.PersianFormat

enum class GameDestination(
    val titleFa: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("فرماندهی", Icons.Filled.LocalPolice, Icons.Outlined.LocalPolice, "nav_home"),
    MISSIONS("مأموریت‌ها", Icons.Filled.Assignment, Icons.Outlined.Assignment, "nav_missions"),
    OFFICERS("نیروها", Icons.Filled.Groups, Icons.Outlined.Groups, "nav_officers"),
    GARAGE("گاراژ", Icons.Filled.DirectionsCar, Icons.Outlined.DirectionsCar, "nav_garage"),
    STATION("کلانتری", Icons.Filled.Apartment, Icons.Outlined.Apartment, "nav_station"),
    PRISON("زندان", Icons.Filled.Lock, Icons.Outlined.Lock, "nav_prison"),
    PROFILE("پروفایل", Icons.Filled.Person, Icons.Outlined.Person, "nav_profile"),
    STATS("آمار", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_stats")
}

@Composable
fun PoliceChiefApp(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dispatchMissionId by viewModel.dispatchMissionId.collectAsStateWithLifecycle()
    val missionResult by viewModel.missionResultDialog.collectAsStateWithLifecycle()

    var showSplash by rememberSaveable { mutableStateOf(true) }
    var currentDestination by rememberSaveable { mutableStateOf(GameDestination.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.feedbackMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showSplash) {
        SplashScreen(
            playerLevel = uiState.profile.level,
            onEnterGame = { showSplash = false }
        )
        return
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PoliceBlue)
        }
        return
    }

    // First-time Character & Profile Builder before entering HQ
    if (!uiState.profile.profileConfigured) {
        ProfileScreen(
            profile = uiState.profile,
            isOnboarding = true,
            onSaveProfile = { name, callsign, avatarKey, trait, bio ->
                viewModel.saveChiefProfile(name, callsign, avatarKey, trait, bio)
            }
        )
        return
    }

    // Back button from any secondary screen returns cleanly to Home Command Center
    BackHandler(enabled = currentDestination != GameDestination.HOME) {
        currentDestination = GameDestination.HOME
    }

    val bottomTabs = remember {
        listOf(
            GameDestination.HOME,
            GameDestination.MISSIONS,
            GameDestination.OFFICERS,
            GameDestination.GARAGE,
            GameDestination.STATION
        )
    }

    val missionBadgeCount = uiState.readyToClaimCount.takeIf { it > 0 } ?: uiState.activeMissions.size

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = IceBlueSurface,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                CommandTopBar(
                    profile = uiState.profile,
                    isStatsSelected = currentDestination == GameDestination.STATS,
                    isPrisonSelected = currentDestination == GameDestination.PRISON,
                    isProfileSelected = currentDestination == GameDestination.PROFILE,
                    detainedCount = uiState.activeInmates.size,
                    prisonBreakoutActive = uiState.breakoutAlertsCount > 0 || uiState.prisonBreakoutPursuits.isNotEmpty(),
                    onProfileClick = {
                        currentDestination = if (currentDestination == GameDestination.PROFILE) {
                            GameDestination.HOME
                        } else {
                            GameDestination.PROFILE
                        }
                    },
                    onPrisonClick = {
                        currentDestination = if (currentDestination == GameDestination.PRISON) {
                            GameDestination.HOME
                        } else {
                            GameDestination.PRISON
                        }
                    },
                    onStatsClick = {
                        currentDestination = if (currentDestination == GameDestination.STATS) {
                            GameDestination.HOME
                        } else {
                            GameDestination.STATS
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = PureWhite,
                        tonalElevation = 6.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomTabs.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    if (dest == GameDestination.MISSIONS && missionBadgeCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = when {
                                                        uiState.urgentAlertsCount > 0 -> AlertRed
                                                        uiState.readyToClaimCount > 0 -> SuccessGreenBright
                                                        else -> PoliceBlue
                                                    },
                                                    contentColor = PureWhite
                                                ) {
                                                    Text(PersianFormat.number(missionBadgeCount))
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = dest.titleFa,
                                                modifier = Modifier.size(23.dp)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.titleFa,
                                            modifier = Modifier.size(23.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = dest.titleFa,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PoliceBlue,
                                    selectedTextColor = NavyDeep,
                                    indicatorColor = PoliceBlue.copy(alpha = 0.14f),
                                    unselectedIconColor = SlateMuted,
                                    unselectedTextColor = SlateMuted
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = PureWhite
                    ) {
                        GameDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentDestination = dest },
                                modifier = Modifier.testTag(dest.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.titleFa
                                    )
                                },
                                label = { Text(dest.titleFa, style = MaterialTheme.typography.labelSmall) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = PoliceBlue,
                                    selectedTextColor = NavyDeep,
                                    indicatorColor = PoliceBlue.copy(alpha = 0.14f),
                                    unselectedIconColor = SlateMuted,
                                    unselectedTextColor = SlateMuted
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                    ) {
                        when (currentDestination) {
                            GameDestination.HOME -> HomeScreen(
                                state = uiState,
                                onOpenMissions = { currentDestination = GameDestination.MISSIONS },
                                onOpenStation = { currentDestination = GameDestination.STATION },
                                onOpenPrison = { currentDestination = GameDestination.PRISON },
                                onOpenProfile = { currentDestination = GameDestination.PROFILE },
                                onOpenStats = { currentDestination = GameDestination.STATS },
                                onScanNewCrime = viewModel::scanForNewCrime,
                                onMissionPinClick = { mission ->
                                    if (mission.status == MissionStatus.AVAILABLE.name) {
                                        viewModel.openDispatchSheet(mission.id)
                                    } else {
                                        currentDestination = GameDestination.MISSIONS
                                    }
                                },
                                onClaimMission = viewModel::claimMissionResult,
                                onTacticalDecision = viewModel::respondToTacticalAlert
                            )

                            GameDestination.MISSIONS -> MissionsScreen(
                                state = uiState,
                                onOpenDispatch = viewModel::openDispatchSheet,
                                onClaimMission = viewModel::claimMissionResult,
                                onScanNewCrime = viewModel::scanForNewCrime,
                                onTacticalDecision = viewModel::respondToTacticalAlert
                            )

                            GameDestination.OFFICERS -> OfficersScreen(
                                state = uiState,
                                onHireOfficer = viewModel::hireOfficer,
                                onUpgradeOfficer = viewModel::upgradeOfficer
                            )

                            GameDestination.GARAGE -> GarageScreen(
                                state = uiState,
                                onBuyVehicle = viewModel::buyVehicle,
                                onUpgradeVehicle = viewModel::upgradeVehicle
                            )

                            GameDestination.STATION -> StationScreen(
                                state = uiState,
                                onUpgradeBuilding = viewModel::upgradeBuilding,
                                onUpgradeCapacity = viewModel::upgradeCapacity,
                                onUpgradeEquipment = viewModel::upgradeEquipment
                            )

                            GameDestination.PRISON -> PrisonScreen(
                                state = uiState,
                                onInterrogateInmate = viewModel::interrogateInmate,
                                onTransferInmate = viewModel::transferInmate,
                                onQuellBreakout = viewModel::quellPrisonBreakout,
                                onInspectCellblock = viewModel::inspectPrisonCellblock,
                                onPursueEscapedMission = { pursuit ->
                                    if (pursuit.status == MissionStatus.AVAILABLE.name) {
                                        viewModel.openDispatchSheet(pursuit.id)
                                    } else {
                                        currentDestination = GameDestination.MISSIONS
                                    }
                                },
                                onUpgradePrison = viewModel::upgradePrison
                            )

                            GameDestination.PROFILE -> ProfileScreen(
                                profile = uiState.profile,
                                isOnboarding = false,
                                onSaveProfile = { name, callsign, avatarKey, trait, bio ->
                                    viewModel.saveChiefProfile(name, callsign, avatarKey, trait, bio)
                                }
                            )

                            GameDestination.STATS -> StatsScreen(
                                state = uiState,
                                onResetCareer = viewModel::resetCareer
                            )
                        }
                    }
                }
            }
        }
    }

    // Dispatch Dialog when player selects a mission to send officers to
    val selectedMission = dispatchMissionId?.let { id ->
        uiState.availableMissions.firstOrNull { it.id == id }
    }
    if (selectedMission != null) {
        DispatchMissionDialog(
            mission = selectedMission,
            profile = uiState.profile,
            officers = uiState.officers,
            vehicles = uiState.vehicles,
            onDismiss = viewModel::closeDispatchSheet,
            onConfirmDispatch = { officerIds, vehicleId ->
                viewModel.dispatchMission(selectedMission.id, officerIds, vehicleId)
            }
        )
    }

    // Mission Result Report Dialog
    val result = missionResult
    if (result != null) {
        MissionResultDialog(
            result = result,
            onDismiss = viewModel::dismissMissionResult
        )
    }
}
