package com.safe.setting.app.ui.widget.pinlockview

import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView



abstract class PinLockAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_NUMBER = 0
    private val VIEW_TYPE_DELETE = 1
    private val VIEW_TYPE_OK = 2

    private var mCustomizationOptionsBundle: CustomizationOptionsBundle? = null
    private var onPinButtonClickListener: OnPinButtonClickListener? = null
    var pinLength: Int = 0

    private val mKeyValues: IntArray = getAdjustKeyValues(intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0))



    override fun getItemCount(): Int = 12


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            itemCount - 1 -> VIEW_TYPE_OK
            9 -> VIEW_TYPE_DELETE
            else -> VIEW_TYPE_NUMBER
        }
    }

    private fun getAdjustKeyValues(keyValues: IntArray): IntArray {
        val adjustedKeyValues = IntArray(keyValues.size + 1)
        for (i in keyValues.indices) {
            if (i < 9) {
                adjustedKeyValues[i] = keyValues[i]
            } else {
                adjustedKeyValues[i] = -1
                adjustedKeyValues[i + 1] = keyValues[i]
            }
        }
        return adjustedKeyValues
    }


    fun setCustomizationOptions(customizationOptionsBundle: CustomizationOptionsBundle) {
        this.mCustomizationOptionsBundle = customizationOptionsBundle
    }

    fun setOnPinButtonClickListener(onPinButtonClickListener: OnPinButtonClickListener) {
        this.onPinButtonClickListener = onPinButtonClickListener
    }

    interface OnPinButtonClickListener {
        fun onNumberClicked(keyValue: Int)
        fun onConfirmClicked()
        fun onDeleteClicked()
        fun onDeleteLongClicked()
    }

}
