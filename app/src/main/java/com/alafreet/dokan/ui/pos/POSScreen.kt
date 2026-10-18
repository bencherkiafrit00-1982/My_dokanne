package com.alafreet.dokan.ui.pos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alafreet.dokan.R
import com.alafreet.dokan.data.CartLine
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.PaymentMethod
import com.alafreet.dokan.data.entities.Product
import java.util.Locale

fun formatCurrency(amount: Double): String = String.format(Locale("ar"), "%.2f ج.م", amount)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POSScreen(repository: DokanRepository) {
    val viewModel: POSViewModel = viewModel(factory = POSViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsState()
    var showCheckoutSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val saleCompletedMessage = stringResource(R.string.sale_completed)

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeError()
        }
    }

    LaunchedEffect(uiState.lastSaleSuccess) {
        if (uiState.lastSaleSuccess) {
            snackbarHostState.showSnackbar(saleCompletedMessage)
            viewModel.consumeSaleSuccessFlag()
            showCheckoutSheet = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.cart.isNotEmpty()) {
                CartSummaryBar(
                    total = uiState.total,
                    itemCount = uiState.cart.sumOf { it.quantity },
                    onCheckoutClick = { showCheckoutSheet = true }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text(stringResource(R.string.search)) },
                singleLine = true
            )

            if (uiState.products.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_products), color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(uiState.products, key = { it.id }) { product ->
                        val qtyInCart = uiState.cart.find { it.product.id == product.id }?.quantity ?: 0
                        ProductTile(
                            product = product,
                            quantityInCart = qtyInCart,
                            onAdd = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }
    }

    if (showCheckoutSheet) {
        CheckoutSheet(
            uiState = uiState,
            onDismiss = { showCheckoutSheet = false },
            onIncrease = viewModel::addToCart,
            onDecrease = viewModel::decreaseFromCart,
            onConfirm = viewModel::checkout
        )
    }
}

@Composable
private fun ProductTile(product: Product, quantityInCart: Int, onAdd: () -> Unit) {
    val outOfStock = product.quantity <= 0
    ElevatedCard(
        onClick = { if (!outOfStock) onAdd() },
        modifier = Modifier.fillMaxWidth(),
        enabled = !outOfStock
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            if (product.discountPercent > 0) {
                Text(
                    formatCurrency(product.price),
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                formatCurrency(product.finalPrice),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (outOfStock) stringResource(R.string.out_of_stock) else "المتاح: ${product.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (outOfStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                )
                if (quantityInCart > 0) {
                    Badge { Text("$quantityInCart") }
                }
            }
        }
    }
}

@Composable
private fun CartSummaryBar(total: Double, itemCount: Int, onCheckoutClick: () -> Unit) {
    Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("$itemCount صنف", style = MaterialTheme.typography.bodyLarge)
            }
            Button(onClick = onCheckoutClick) {
                Text("${stringResource(R.string.checkout)} • ${formatCurrency(total)}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutSheet(
    uiState: POSUiState,
    onDismiss: () -> Unit,
    onIncrease: (Product) -> Unit,
    onDecrease: (Product) -> Unit,
    onConfirm: (PaymentMethod) -> Unit
) {
    var selectedPayment by remember { mutableStateOf(PaymentMethod.CASH) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp)) {
            Text(stringResource(R.string.cart), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            if (uiState.cart.isEmpty()) {
                Text(stringResource(R.string.empty_cart))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(uiState.cart, key = { it.product.id }) { line ->
                        CartLineRow(line, onIncrease, onDecrease)
                    }
                }

                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))

                SummaryRow(stringResource(R.string.subtotal), formatCurrency(uiState.subtotal))
                if (uiState.discountAmount > 0) SummaryRow(stringResource(R.string.discount), "-${formatCurrency(uiState.discountAmount)}")
                SummaryRow(stringResource(R.string.tax), formatCurrency(uiState.taxAmount))
                SummaryRow(stringResource(R.string.total), formatCurrency(uiState.total), emphasize = true)

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.payment_method), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentChip(stringResource(R.string.cash), selectedPayment == PaymentMethod.CASH) { selectedPayment = PaymentMethod.CASH }
                    PaymentChip(stringResource(R.string.card), selectedPayment == PaymentMethod.CARD) { selectedPayment = PaymentMethod.CARD }
                    PaymentChip(stringResource(R.string.wallet), selectedPayment == PaymentMethod.WALLET) { selectedPayment = PaymentMethod.WALLET }
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onConfirm(selectedPayment) },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text(stringResource(R.string.confirm))
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CartLineRow(line: CartLine, onIncrease: (Product) -> Unit, onDecrease: (Product) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(line.product.name, style = MaterialTheme.typography.bodyLarge)
            Text(formatCurrency(line.lineTotal), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onDecrease(line.product) }) {
                Icon(Icons.Filled.Remove, contentDescription = "إنقاص")
            }
            Text("${line.quantity}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { onIncrease(line.product) }) {
                Icon(Icons.Filled.Add, contentDescription = "زيادة")
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, emphasize: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium)
        Text(
            value,
            style = if (emphasize) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            fontWeight = if (emphasize) FontWeight.Bold else FontWeight.Normal,
            color = if (emphasize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PaymentChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) })
}
