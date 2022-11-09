package com.hfad.guineapiglog

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    Pet::class,
    PetProfilePhoto::class,
    Event::class,
    EventPet::class,
    Weight::class,
    Photo::class,
    PhotoEvent::class],
    version = 1, exportSchema = false)
@TypeConverters(
    Converter::class,
    URIConverter::class)
abstract class PetLoggerDatabase: RoomDatabase() {
    abstract val petDao: PetDao
    abstract val eventDao: EventDao
    abstract val eventPetDao: EventPetDao
    abstract val weightDao: WeightDao
    abstract val photoDao: PhotoDao

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
                    Log.i("DB", "DATABASE CREATED!!")
                }
                Log.i("DB", "database already created... retrieving")
                return instance
            }
        }
    }
}