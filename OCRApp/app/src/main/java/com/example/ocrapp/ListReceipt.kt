package com.example.ocrapp

class ListReceipt {
    private val receiptList = mutableListOf<Receipt>()

    fun addItem(receipt: Receipt) {
        receiptList.add(receipt)
    }

    fun deleteItem(receipt: Receipt)
    {
        receiptList.remove(receipt)
    }

    fun getList(): List<Receipt> {
        return receiptList.toList()
    }
}