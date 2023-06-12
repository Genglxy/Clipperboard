package com.genglxy.clipperboard.logic.dao

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.genglxy.clipperboard.logic.model.Result

@Database(entities = [Result::class], version = 1)
@TypeConverters(ResultTypeConverters::class)
abstract class ResultDatabase : RoomDatabase() {
    abstract fun resultDao(): ResultDao
}