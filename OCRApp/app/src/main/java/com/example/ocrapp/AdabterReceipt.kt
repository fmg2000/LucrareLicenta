package com.example.ocrapp

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// AdapterCostum pentru Ricycle View + HolderView
class AdabterReceipt(private var receiptList: List<Receipt>, private val onItemClickListener: OnItemClickListener) : RecyclerView.Adapter<AdabterReceipt.ViewHolder>(){


    interface OnItemClickListener {
        fun onItemClick(receipt: Receipt)
        fun onDeleteClick(receipt: Receipt)
    }

    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        //ne tine vizualizările pentru adugarea in textView....
        val img: ImageView = itemView.findViewById(R.id.imgImageView)
        val sum: TextView = itemView.findViewById(R.id.sumTextView)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)



        fun bind(receipt: Receipt, clickListener: OnItemClickListener) {
            itemView.setOnClickListener {
                clickListener.onItemClick(receipt)
            }
            Glide.with(itemView)
                    .load(receipt.img?.toUri())
                    .placeholder(R.drawable.background) // Imagine temporară
                    .error(R.drawable.ic_launcher_background) // Imagine în caz de eroare
                    .into(img)

            sum.text = receipt.sum.toString()

            deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(receipt, clickListener)
            }
        }
        private fun showDeleteConfirmationDialog(receipt: Receipt,  clickListener: OnItemClickListener) {
            val builder = AlertDialog.Builder(itemView.context)
            builder.setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete this receipt?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        clickListener.onDeleteClick(receipt)
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }.show()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //setează vizualizările pentru a afișa elementele.
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_recycleview_design, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        // returneaza numarul de elemente in lista
        return receiptList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // folosită pentru a lega elementele din listă la widget-urile noastre, cum ar fi TextView, ImageView
        val currentItem = receiptList[position]
        holder.bind(currentItem,onItemClickListener)
//        holder.title.text = currentItem.title
//        holder.author.text = currentItem.author
    }

    fun updateList(newList: List<Receipt>) {
        receiptList = newList
        notifyDataSetChanged()
    }
}

