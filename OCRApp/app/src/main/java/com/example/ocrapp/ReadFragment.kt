package com.example.ocrapp

import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.bumptech.glide.Glide


class ReadFragment : Fragment() {

    companion object {
        private const val STORE_KEY = "store_key"
        fun newInstance(receipt: Receipt): Fragment {
            val fragment = ReadFragment()
            val args = Bundle()
            args.putParcelable(STORE_KEY, receipt)
            fragment.arguments = args
            return fragment
        }
    }

    private var receipt: Receipt? = null
    private lateinit var textTotalSum : TextView
    private lateinit var textDate: TextView
    private lateinit var textLocation:TextView
    private lateinit var image : ImageView

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
             receipt = arguments?.getParcelable(STORE_KEY)
        else
            receipt = arguments?.getParcelable(STORE_KEY, Receipt::class.java)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_read, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textTotalSum = view.findViewById(R.id.textSumInfo)
        textDate = view.findViewById(R.id.textDateInfo)
        textLocation = view.findViewById(R.id.textLocationInfo)
        image = view.findViewById(R.id.imageInfo)

        textTotalSum.setText("TOTAL:  " +receipt?.sum.toString())
        textDate.setText(receipt?.date)
        textLocation.setText(receipt?.location)
        Glide.with(view)
                .load(receipt?.img?.toUri())
                .placeholder(R.drawable.background) // Imagine temporară
                .error(R.drawable.ic_launcher_background) // Imagine în caz de eroare
                .into(image)

    }

}
