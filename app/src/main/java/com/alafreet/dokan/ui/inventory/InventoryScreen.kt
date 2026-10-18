package com.alafreet.dokan.ui.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alafreet.dokan.R
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Product

@Composable
fun InventoryScreen(repository: DokanRepository) {
    val viewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsState()

    Column(Modifier.fillMaxSize()) {
        if (uiState.lowStockProducts.isNotEmpty()) {
            Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${stringResource(R.string.low_stock_alert)} (${uiState.lowStockProducts.size})",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        if (uiState.allProducts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.no_products), color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp)) {
                items(uiState.allProducts, key = { it.id }) { product ->
                    InventoryRow(
                        product = product,
                        onIncrease = { viewModel.adjustStock(product.id, 1) },
                        onDecrease = { viewModel.adjustStock(product.id, -1) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun InventoryRow(product: Product, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    val isLow = product.quantity <= product.minStockThreshold
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${stringResource(R.string.current_stock)}: ${product.quantity}  •  ${stringResource(R.string.min_stock_threshold)}: ${product.minStockThreshold}",
                style = MaterialTheme.typography.bodySmall,
                color = if (isLow) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrease, enabled = product.quantity > 0) {
                Icon(Icons.Filled.Remove, contentDescription = "إنقاص الكمية")
            }
            Text("${product.quantity}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onIncrease) {
                Icon(Icons.Filled.Add, contentDescription = "زيادة الكمية")
            }
        }
    }
}
