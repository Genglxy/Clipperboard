package com.genglxy.clipperboard.logic

import android.content.Context
import androidx.room.Room
import com.genglxy.clipperboard.logic.dao.ResultDatabase
import com.genglxy.clipperboard.logic.model.Result
import kotlinx.coroutines.flow.Flow
import java.util.UUID

private const val DATABASE_NAME = "result-database"

class ResultRepository private constructor(context: Context){

    private val database: ResultDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ResultDatabase::class.java,
            DATABASE_NAME
        )
        .build()

    fun getResults(): Flow<List<Result>> = database.resultDao().getResults()

    suspend fun addResult(result: Result) {
        database.resultDao().addResult(result)
    }

    suspend fun getResult(id: UUID): Result = database.resultDao().getResult(id)

    companion object {
        private var INSTANCE: ResultRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ResultRepository(context)
            }
        }

        fun get(): ResultRepository {
            return INSTANCE ?:
            throw IllegalStateException("ResultRepository must be initialized")
        }
    }
}