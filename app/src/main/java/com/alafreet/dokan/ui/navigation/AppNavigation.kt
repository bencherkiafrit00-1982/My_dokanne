package com.alafreet.dokan.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alafreet.dokan.R
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.ui.admin.AdminScreen
import com.alafreet.dokan.ui.inventory.InventoryScreen
import com.alafreet.dokan.ui.pos.POSScreen
import com.alafreet.dokan.ui.reports.ReportsScreen

sealed class DokanMode(val route: String, val labelRes: Int, val icon: ImageVector) {
    data object POS : DokanMode("pos", R.string.mode_pos, Icons.Filled.PointOfSale)
    data object Admin : DokanMode("admin", R.string.mode_admin, Icons.Filled.Settings)
    data object Reports : DokanMode("reports", R.string.mode_reports, Icons.Filled.Assessment)
    data object Inventory : DokanMode("inventory", R.string.mode_inventory, Icons.Filled.Inventory2)
}

private val modes = listOf(DokanMode.POS, DokanMode.Admin, DokanMode.Reports, DokanMode.Inventory)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DokanApp(repository: DokanRepository) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                modes.forEach { mode ->
                    val selected = currentDestination?.hierarchy?.any { it.route == mode.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(mode.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(mode.icon, contentDescription = stringResource(mode.labelRes)) },
                        label = { Text(stringResource(mode.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = DokanMode.POS.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(DokanMode.POS.route) { POSScreen(repository) }
            composable(DokanMode.Admin.route) { AdminScreen(repository) }
            composable(DokanMode.Reports.route) { ReportsScreen(repository) }
            composable(DokanMode.Inventory.route) { InventoryScreen(repository) }
        }
    }
}
