package com.genglxy.clipperboard.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import java.util.UUID
import com.genglxy.clipperboard.logic.model.Result
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    @Query("SELECT * FROM result")
    fun getResults(): Flow<List<Result>>

    @Query("SELECT * FROM result WHERE id=(:id)")
    suspend fun getResult(id: UUID): Result

    @Insert
    suspend fun addResult(result: Result)
}