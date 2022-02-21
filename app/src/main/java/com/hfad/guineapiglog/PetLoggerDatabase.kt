package com.hfad.guineapiglog

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Pet::class, Event::class, EventPet::class, Weight::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class PetLoggerDatabase: RoomDatabase() {
    abstract val petDao: PetDao
    abstract val eventDao: EventDao
    abstract val weightDao: WeightDao

    companion object {
        @Volatile
        private var INSTANCE: PetLoggerDatabase? = null
        fun getInstance(context: Context): PetLoggerDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        PetLoggerDatabase::class.java,
                        "pet_logger_database"
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}