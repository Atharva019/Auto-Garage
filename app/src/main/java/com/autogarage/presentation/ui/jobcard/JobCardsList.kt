package com.autogarage.presentation.ui.jobcard

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
//import androidx.paging.compose.collectAsLazyPagingItems
//import androidx.paging.compose.items
import com.autogarage.viewmodel.JobCardsViewModel

@Composable
fun JobCardsList(
    viewModel: JobCardsViewModel
) {
    // ✅ OPTIMIZATION: Use Paging 3 for large lists
//    val pagingItems = viewModel.jobCardsPager.collectAsLazyPagingItems()
//
//    LazyColumn {
//        items(
//            items = pagingItems,
//            key = { it.id }
//        ) { jobCard ->
//            jobCard?.let {
//                JobCardItem(
//                    jobCard = it,
//                    onClick = { viewModel.onJobCardClick(it.id) }
//                )
//            }
//        }
//    }
}
