package com.example.threescreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.threescreen.ui.theme.ThreeScreenTheme
import java.lang.NumberFormatException

// Hapus Student Data Model dan StudentRoster yang lama

// 1. Define Navigation Routes (Tetap sama)
object Destinations {
    const val LOGIN_ROUTE = "login"
    const val DASHBOARD_ROUTE = "dashboard"
    const val ADD_ITEM_ROUTE = "add_item" // Ganti rute
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThreeScreenTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

// 2. Main Navigation Composable
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Inisialisasi ViewModel di level tertinggi agar bisa dibagikan (shared)
    val inventoryViewModel: InventoryViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Destinations.LOGIN_ROUTE
    ) {
        // LOGIN SCREEN Route
        composable(Destinations.LOGIN_ROUTE) {
            LoginScreen(
                inventoryViewModel = inventoryViewModel, // Kirim ViewModel
                onLoginSuccess = {
                    navController.navigate(Destinations.DASHBOARD_ROUTE) {
                        popUpTo(Destinations.LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        // DASHBOARD SCREEN Route
        composable(Destinations.DASHBOARD_ROUTE) {
            DashboardScreen(
                inventoryViewModel = inventoryViewModel, // Kirim ViewModel
                onAddItemClick = {
                    navController.navigate(Destinations.ADD_ITEM_ROUTE)
                }
            )
        }

        // ADD ITEM SCREEN Route
        composable(Destinations.ADD_ITEM_ROUTE) {
            AddItemScreen(
                inventoryViewModel = inventoryViewModel, // Kirim ViewModel
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// -----------------------------------------------------------
// A. Login Screen Implementation (Kondisi 1.a)
// -----------------------------------------------------------

@Composable
fun LoginScreen(
    inventoryViewModel: InventoryViewModel, // Terima ViewModel
    onLoginSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    // Hapus password karena tidak diperlukan validasi

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Inventory Login", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.padding(bottom = 32.dp))
        OutlinedTextField(
            value = username, onValueChange = { username = it }, label = { Text("Username") },
            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = "Username") }, singleLine = true, modifier = Modifier.fillMaxWidth(0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                // Syarat 1.a: Setiap nama user selalu diterima/tidak ada kegagalan login
                if (username.isNotBlank()) {
                    inventoryViewModel.setUserName(username) // Simpan nama user di ViewModel
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) { Text("Login") }
    }
}

// -----------------------------------------------------------
// B. Dashboard Screen Implementation (Kondisi 1.b & 1.c)
// -----------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    inventoryViewModel: InventoryViewModel, // Terima ViewModel
    onAddItemClick: () -> Unit
) {
    // Ambil data dari ViewModel
    val userName by inventoryViewModel.userName // Syarat 1.b: Menampilkan nama user
    val items = inventoryViewModel.itemList // Syarat 1.c: Data barang

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventory Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddItemClick, // Syarat 1.c: Tombol tambah barang
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add Item") },
                text = { Text("Add Item") }
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Tampilkan nama user (Syarat 1.b)
            Text(
                text = "Welcome, $userName!",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Header Tabel
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.7f)
                )
                Text(
                    text = "Qty",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(0.3f),
                    textAlign = TextAlign.End
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Daftar Barang (Syarat 1.c)
            if (items.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { Text(text = "Inventory is Empty. Click '+' to add an item.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(items) { item ->
                        ItemListItem(item = item)
                    }
                }
            }
        }
    }
}

// Komponen untuk setiap baris barang
@Composable
fun ItemListItem(item: Item) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nama Barang (Kolom 1)
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(0.7f)
            )
            // Banyaknya Barang (Kolom 2)
            Text(
                text = item.quantity.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(0.3f),
                textAlign = TextAlign.End
            )
        }
    }
}


// -----------------------------------------------------------
// C. Add Item Screen Implementation (Syarat 1.c)
// -----------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemScreen(
    inventoryViewModel: InventoryViewModel, // Terima ViewModel
    onRegisterSuccess: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var itemQuantityString by remember { mutableStateOf("") }
    var showEmptyError by remember { mutableStateOf(false) }
    var showQuantityError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Item") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(horizontal = 24.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = itemName, onValueChange = { itemName = it }, label = { Text("Item Name") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = itemQuantityString, onValueChange = {
                    itemQuantityString = it.filter { char -> char.isDigit() } // Hanya menerima angka
                    showQuantityError = false // Reset error saat ada perubahan input
                },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (showEmptyError) {
                Text(text = "Please fill in all fields.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (showQuantityError) {
                Text(text = "Quantity must be a valid number and greater than 0.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    showEmptyError = itemName.isBlank() || itemQuantityString.isBlank()

                    if (showEmptyError) return@Button // Berhenti jika ada field kosong

                    val quantity = itemQuantityString.toIntOrNull()
                    if (quantity == null || quantity <= 0) {
                        showQuantityError = true
                        return@Button
                    }

                    // Tambahkan barang ke ViewModel
                    val newItem = Item(
                        name = itemName.trim(),
                        quantity = quantity
                    )
                    inventoryViewModel.addItem(newItem)

                    // Kembali ke dashboard
                    onRegisterSuccess()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Add Item to Inventory") }
        }
    }
}