package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteVideoDao {
    @Query("SELECT * FROM favourite_videos ORDER BY timestamp DESC")
    fun getAllFavourites(): Flow<List<FavouriteVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavourite(video: FavouriteVideo)

    @Query("DELETE FROM favourite_videos WHERE url = :url")
    suspend fun deleteFavourite(url: String)
}
