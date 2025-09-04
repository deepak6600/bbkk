package com.safe.setting.app.ui.widget

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager



class OnScrollListener( private val lManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    private var visibleThreshold = 2
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        visibleItemCount = recyclerView.childCount
        totalItemCount = lManager.itemCount
        firstVisibleItem = lManager.findFirstVisibleItemPosition()


    }
}