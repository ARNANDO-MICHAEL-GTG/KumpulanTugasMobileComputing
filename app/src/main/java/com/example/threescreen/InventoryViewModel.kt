package com.example.threescreen // Ganti dengan nama package Anda

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {
    // a. Simpan Nama User
    // Digunakan saat Login dan ditampilkan di Dashboard
    var userName = mutableStateOf("")
        private set // Hanya bisa diubah di dalam ViewModel

    // b. Simpan Daftar Barang
    // mutableStateListOf akan memicu recomposition saat isinya berubah.
    val itemList = mutableStateListOf<Item>()

    /**
     * Menyimpan nama user saat berhasil login.
     * @param name Nama user dari Login Screen.
     */
    fun setUserName(name: String) {
        userName.value = name.trim()
    }

    /**
     * Menambahkan item baru ke daftar.
     * @param item Objek Item yang akan ditambahkan.
     */
    fun addItem(item: Item) {
        // Cek apakah item dengan nama yang sama sudah ada
        val existingItem = itemList.find { it.name.equals(item.name, ignoreCase = true) }

        if (existingItem != null) {
            // Jika ada, update kuantitasnya
            val index = itemList.indexOf(existingItem)
            val updatedItem = existingItem.copy(quantity = existingItem.quantity + item.quantity)
            itemList[index] = updatedItem
        } else {
            // Jika belum ada, tambahkan item baru
            itemList.add(item)
        }
    }
}