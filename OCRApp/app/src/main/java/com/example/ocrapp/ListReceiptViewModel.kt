package com.example.ocrapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ListReceiptViewModel: ViewModel() {

    private val repository = ListReceipt()
    private val _receiptList = MutableLiveData<List<Receipt>>()
    val receiptList: LiveData<List<Receipt>> get() = _receiptList

    fun addStore(receipt: Receipt) {
        repository.addItem(receipt)
        _receiptList.value = repository.getList()
    }

    fun deleteStore(receipt: Receipt) {
        repository.deleteItem(receipt)
        _receiptList.value = repository.getList()
    }

//    fun clearData()
//    {
//        _storeList.value
//    }

//    fun getList(): LiveData<List<Store>> {
//        return _storeList
//    }
}

