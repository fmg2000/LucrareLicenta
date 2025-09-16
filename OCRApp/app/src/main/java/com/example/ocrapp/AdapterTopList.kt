package com.example.ocrapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterTopList(private var reciptList: List<Receipt>) : RecyclerView.Adapter<AdapterTopList.ReceiptViewHolder>() {


    class ReceiptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val textSum: TextView = itemView.findViewById(R.id.textSum)
        val textLocation: TextView = itemView.findViewById(R.id.textLocation)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceiptViewHolder {
        //setează vizualizările pentru a afișa elementele.
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_listview_design, parent, false)
        return ReceiptViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceiptViewHolder, position: Int) {
        if (position < reciptList.size) {
            val receipt = reciptList[position]
            holder.textDate.text = "Date:  ${receipt.date}"
            holder.textSum.text = "TOTAL: ${receipt.sum.toString()}"
            holder.textLocation.text = receipt.location.toString()
        }
        else
        {
            holder.textDate.text = "Date: ---"
            holder.textSum.text = "TOTAL: ---"
            holder.textLocation.text = "Location: ---"
        }
    }

    override fun getItemCount(): Int {
        return 5

    }

    fun updateList(newList: List<Receipt>) {
        reciptList = newList
        notifyDataSetChanged()
    }




}