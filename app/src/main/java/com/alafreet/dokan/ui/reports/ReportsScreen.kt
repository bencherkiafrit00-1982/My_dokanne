package com.alafreet.dokan.ui.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alafreet.dokan.R
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Sale
import com.alafreet.dokan.ui.pos.formatCurrency
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(repository: DokanRepository) {
    val viewModel: ReportsViewModel = viewModel(factory = ReportsViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = stringResource(R.string.today_sales),
                    value = formatCurrency(uiState.todayRevenue),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.number_of_transactions),
                    value = "${uiState.todayTransactionCount}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(stringResource(R.string.best_selling), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (uiState.bestSellers.isEmpty()) {
            item { Text("لا توجد بيانات مبيعات كافية بعد اليوم", color = MaterialTheme.colorScheme.outline) }
        } else {
            items(uiState.bestSellers) { seller ->
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(seller.productNameSnapshot, style = MaterialTheme.typography.bodyLarge)
                        Text("${seller.totalQuantity} قطعة • ${formatCurrency(seller.totalRevenue)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.sales_history), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (uiState.recentSales.isEmpty()) {
            item { Text("لا توجد عمليات بيع مسجلة بعد", color = MaterialTheme.colorScheme.outline) }
        } else {
            items(uiState.recentSales) { sale ->
                SaleHistoryRow(sale)
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

private val saleDateFormatter = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale("ar"))

@Composable
private fun SaleHistoryRow(sale: Sale) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(saleDateFormatter.format(Date(sale.timestamp)), style = MaterialTheme.typography.bodyMedium)
        Text(formatCurrency(sale.total), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
    HorizontalDivider()
}
