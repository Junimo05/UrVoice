package com.example.urvoices.data.repository

import android.net.Uri
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.urvoices.data.model.Post
import com.example.urvoices.data.model.User
import com.example.urvoices.data.service.FirebaseNotificationService
import com.example.urvoices.data.service.FirebaseUserService
import com.example.urvoices.utils.SharedPreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val firebaseUserService: FirebaseUserService,
    private val sharedPref: SharedPreferencesHelper,

){
    val TAG = "UserRepository"
    val scope = CoroutineScope(Dispatchers.IO)
    suspend fun getInfoUserByUserID(userId: String) = firebaseUserService.getInfoUserByUserID(userId)

    suspend fun getFollowStatus(userId: String) = firebaseUserService.getFollowStatus(userId)

    suspend fun getFollowerCount(userId: String) = firebaseUserService.getFollowerCounts(userId)

    suspend fun getFollowingCount(userId: String) = firebaseUserService.getFollowingCounts(userId)

    suspend fun getPostCount(userId: String) = firebaseUserService.getPostCounts(userId)

    suspend fun followUser(userId: String, followStatus: Boolean) = firebaseUserService.followUser(userId, followStatus)


    fun getPagingFollowerList(userId: String): PagingSource<Int, User> {
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

    //Update Func
    suspend fun updateUser(
        username: String,
        bio: String,
        country: String,
        avatarUri: Uri,
        oldUser: User
    ): Boolean{
        try {
            val updateTasks = mutableListOf<Deferred<Boolean>>()
            if(username != oldUser.username){
                updateTasks.add(scope.async { firebaseUserService.updateUsername(username) })
            }
            if(bio != oldUser.bio){
                updateTasks.add(scope.async { firebaseUserService.updateBio(bio) })
            }
            if(country != oldUser.country){
                updateTasks.add(scope.async { firebaseUserService.updateCountry(country) })
            }
            if(avatarUri != Uri.EMPTY){
                updateTasks.add(scope.async { firebaseUserService.updateAvatar(avatarUri) })
            }
            val results = updateTasks.awaitAll()
            if(results.contains(false)){
                Log.e(TAG, "updateUser: Error")
                //rollback Update
                //TODO: Rollback Update
                return false
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "updateUser: Error")
        }
        return false
    }


    /*
      Shared Preferences
     */
    suspend fun getUserSettingsByID(userId: String): Map<String, Any>?  {
        try {
            val userSettings = firebaseUserService.getUserSettingsById(userId)
            return userSettings
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "getUserSettingsByID: Error")
        }
        return null
    }

    suspend fun saveUserSettings(): Boolean{
        try {
            return firebaseUserService.saveUserSettings()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "saveUserSettings: Error")
        }
        return false
    }

}