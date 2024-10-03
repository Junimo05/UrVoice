package com.example.urvoices.data.repository

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.data.service.FirebaseUserService
import com.example.urvoices.utils.SharedPreferencesHelper
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseUserService: FirebaseUserService,
    private val sharedPref: SharedPreferencesHelper,

){
    val TAG = "UserRepository"

    suspend fun getInfoUserByUserID(userId: String) = firebaseUserService.getInfoUserByUserID(userId)

    suspend fun getFollowStatus(userId: String) = firebaseUserService.getFollowStatus(userId)

    suspend fun getFollowerCount(userId: String) = firebaseUserService.getFollowerCounts(userId)

    suspend fun getFollowingCount(userId: String) = firebaseUserService.getFollowingCounts(userId)


    //TODO: Get Paging
    suspend fun getPagingFollowerList(userId: String): PagingSource<Int, User> {
        return object : PagingSource<Int, User>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
                return try {
                    val nextPage = params.key ?: 1
                    val followerList = firebaseUserService.getFollowerDetail(userId)
                    LoadResult.Page(
                        data = followerList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (followerList.isEmpty()) null else nextPage + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, User>): Int? {
                return state.anchorPosition
            }
        }
    }

    suspend fun getPagingFollowingList(userId: String): PagingSource<Int, User> {
        return object : PagingSource<Int, User>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
                return try {
                    val nextPage = params.key ?: 1
                    val followingList = firebaseUserService.getFollowingDetail(userId)
                    LoadResult.Page(
                        data = followingList,
                        prevKey = if (nextPage == 1) null else nextPage - 1,
                        nextKey = if (followingList.isEmpty()) null else nextPage + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, User>): Int? {
                return state.anchorPosition
            }
        }
    }

}