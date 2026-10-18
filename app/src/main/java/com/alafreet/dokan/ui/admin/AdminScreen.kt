package com.alafreet.dokan.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alafreet.dokan.R
import com.alafreet.dokan.data.DokanRepository
import com.alafreet.dokan.data.entities.Category
import com.alafreet.dokan.data.entities.Product
import com.alafreet.dokan.ui.pos.formatCurrency

@Composable
fun AdminScreen(repository: DokanRepository) {
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModel.Factory(repository))
    val uiState by viewModel.uiState.collectAsState()

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = { editingProduct = null; showProductDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            } else {
                FloatingActionButton(onClick = { showCategoryDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("المنتجات") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("التصنيفات") })
            }

            if (selectedTab == 0) {
                if (uiState.products.isEmpty()) {
                    EmptyState(stringResource(R.string.no_products))
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(uiState.products, key = { it.id }) { product ->
                            ProductAdminRow(
                                product = product,
                                categoryName = uiState.categories.find { it.id == product.categoryId }?.name,
                                onEdit = { editingProduct = product; showProductDialog = true },
                                onDelete = { viewModel.deleteProduct(product) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            } else {
                if (uiState.categories.isEmpty()) {
                    EmptyState("لا توجد تصنيفات بعد")
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        items(uiState.categories, key = { it.id }) { category ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category.name, style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    if (showProductDialog) {
        ProductEditDialog(
            product = editingProduct,
            categories = uiState.categories,
            onDismiss = { showProductDialog = false },
            onSave = { viewModel.saveProduct(it); showProductDialog = false }
        )
    }

    if (showCategoryDialog) {
        CategoryAddDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = { viewModel.addCategory(it); showCategoryDialog = false }
        )
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun ProductAdminRow(product: Product, categoryName: String?, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "${formatCurrency(product.finalPrice)} • الكمية: ${product.quantity}" + (categoryName?.let { " • $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Row {
            IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit)) }
            IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete)) }
        }
    }
}

@Composable
private fun ProductEditDialog(
    product: Product?,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var price by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var discount by remember { mutableStateOf(product?.discountPercent?.toString() ?: "0") }
    var quantity by remember { mutableStateOf(product?.quantity?.toString() ?: "") }
    var minStock by remember { mutableStateOf(product?.minStockThreshold?.toString() ?: "5") }
    var barcode by remember { mutableStateOf(product?.barcode ?: "") }
    var selectedCategoryId by remember { mutableStateOf(product?.categoryId) }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) stringResource(R.string.add) else stringResource(R.string.edit)) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.product_name)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text(stringResource(R.string.product_price)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = discount, onValueChange = { discount = it }, label = { Text(stringResource(R.string.discount) + " %") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text(stringResource(R.string.product_quantity)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = minStock, onValueChange = { minStock = it }, label = { Text(stringResource(R.string.min_stock_threshold)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text(stringResource(R.string.product_barcode)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
                    OutlinedTextField(
                        value = categories.find { it.id == selectedCategoryId }?.name ?: "بدون تصنيف",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.product_category)) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                        DropdownMenuItem(text = { Text("بدون تصنيف") }, onClick = { selectedCategoryId = null; categoryMenuExpanded = false })
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategoryId = cat.id; categoryMenuExpanded = false })
                        }
                    }
                }

                errorText?.let {
                    Spacer(Modifier.height(6.dp))
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val priceVal = price.toDoubleOrNull()
                val qtyVal = quantity.toIntOrNull()
                val discountVal = discount.toDoubleOrNull() ?: 0.0
                val minStockVal = minStock.toIntOrNull() ?: 5

                if (name.isBlank() || priceVal == null || qtyVal == null) {
                    errorText = "يرجى إدخال اسم وسعر وكمية صحيحة"
                    return@TextButton
                }

                onSave(
                    Product(
                        id = product?.id ?: 0,
                        name = name.trim(),
                        price = priceVal,
                        discountPercent = discountVal.coerceIn(0.0, 100.0),
                        quantity = qtyVal,
                        minStockThreshold = minStockVal,
                        categoryId = selectedCategoryId,
                        barcode = barcode.ifBlank { null },
                        isActive = true
                    )
                )
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun CategoryAddDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add) + " تصنيف") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم التصنيف") }, singleLine = true)
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}
