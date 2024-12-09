/*
 *     This file is part of "ShowCase" formerly Movie DB. <https://github.com/WirelessAlien/MovieDB>
 *     forked from <https://notabug.org/nvb/MovieDB>
 *
 *     Copyright (C) 2024  WirelessAlien <https://github.com/WirelessAlien>
 *
 *     ShowCase is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ShowCase is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with "ShowCase".  If not, see <https://www.gnu.org/licenses/>.
 */
package com.wirelessalien.android.moviedb.tmdb.account

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.wirelessalien.android.moviedb.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

class AddEpisodeRating(
    private val tvShowId: Int,
    private val seasonNumber: Int,
    private val episodeNumber: Int,
    private val rating: Double,
    private val context: Context?
) {
    private val accessToken: String?
    private var success: Boolean = false

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context!!)
        accessToken = preferences.getString("access_token", "")
    }

    suspend fun addRating() {
        var success1 = false
        var success2 = false
        try {
            val client = OkHttpClient()
            val mediaType = MediaType.parse("application/json;charset=utf-8")
            val jsonParam = JSONObject().apply {
                put("value", rating)
            }
            val body = RequestBody.create(mediaType, jsonParam.toString())
            val request = Request.Builder()
                .url("https://api.themoviedb.org/3/tv/$tvShowId/season/$seasonNumber/episode/$episodeNumber/rating")
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            val response = withContext(Dispatchers.IO) {
                client.newCall(request).execute()
            }
            val responseBody = response.body()!!.string()
            val jsonResponse = JSONObject(responseBody)
            val statusCode = jsonResponse.getInt("status_code")
            success1 = statusCode == 1
            success2 = statusCode == 12
        } catch (e: Exception) {
            e.printStackTrace()
        }
        success = success1 || success2
        if (context is Activity) {
            context.runOnUiThread {
                if (success) {
                    Toast.makeText(context, R.string.rating_added_successfully, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.failed_to_add_rating, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun isSuccessful(): Boolean {
        return success
    }
}