package com.hfad.petlogger

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hfad.petlogger.dao.EventDao
import com.hfad.petlogger.dao.EventPetDao
import com.hfad.petlogger.dao.NoteDao
import com.hfad.petlogger.dao.PetDao
import com.hfad.petlogger.dao.PhotoDao
import com.hfad.petlogger.dao.TagDao
import com.hfad.petlogger.dao.WeightDao
import com.hfad.petlogger.entities.*
import com.hfad.petlogger.util.Converter
import com.hfad.petlogger.util.URIConverter

@Database(
    entities = [
        Pet::class,
        PetPhoto::class,

        Event::class,
        EventPet::class,

        Weight::class,

        Photo::class,
        PetProfilePhoto::class,
        PhotoEvent::class,

        Note::class,
        EventNote::class,
        PetNote::class,
        PhotoNote::class,
        WeightNote::class,
        NoteFts::class,

        Tag::class,
        PetTag::class,
        EventTag::class,
        WeightTag::class,
        NoteTag::class,
        PhotoTag::class
    ],
    version = 1,
    exportSchema = false)
@TypeConverters(
    Converter::class,
    URIConverter::class)
abstract class PetLoggerDatabase: RoomDatabase() {
    abstract val petDao: PetDao
    abstract val eventDao: EventDao
    abstract val eventPetDao: EventPetDao
    abstract val weightDao: WeightDao
    abstract val photoDao: PhotoDao
    abstract val noteDao: NoteDao
    abstract val tagDao: TagDao

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
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                    Log.i("DB", "DATABASE CREATED!!")
                }
                Log.i("DB", "database already created... retrieving")
                return instance
            }
        }
    }
}