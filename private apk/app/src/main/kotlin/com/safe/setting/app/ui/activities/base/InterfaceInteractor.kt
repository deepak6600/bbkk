package com.safe.setting.app.ui.activities.base

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.safe.setting.app.data.model.DataDelete
import com.safe.setting.app.data.rxFirebase.InterfaceFirebase
import com.google.firebase.auth.FirebaseUser


interface InterfaceInteractor<V : InterfaceView> {

    fun onAttach(view: V)

    fun onDetach()

    fun getView(): V?

    fun isNullView() : Boolean

    fun getContext(): Context

    fun getSupportFragmentManager(): FragmentManager

    fun firebase(): InterfaceFirebase

    fun user(): FirebaseUser?

    fun setMultiSelected(selected : Boolean)

    fun getMultiSelected() : Boolean

    fun onDeleteData(data:MutableList<DataDelete>)

    fun setRecyclerAdapter()
    fun startRecyclerAdapter()
    fun stopRecyclerAdapter()
    fun notifyDataSetChanged()
    fun notifyItemChanged(position:Int)
    fun setSearchQuery(query:String)
    fun stopRecyclerAdapterQuery()

}