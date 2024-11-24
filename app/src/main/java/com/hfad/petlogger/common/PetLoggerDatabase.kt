package com.hfad.petlogger.common

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hfad.petlogger.common.associationentities.EventNote
import com.hfad.petlogger.common.associationentities.EventPet
import com.hfad.petlogger.common.associationentities.EventTag
import com.hfad.petlogger.common.associationentities.NoteTag
import com.hfad.petlogger.common.associationentities.PetNote
import com.hfad.petlogger.common.associationentities.PetPhoto
import com.hfad.petlogger.common.associationentities.PetProfilePhoto
import com.hfad.petlogger.common.associationentities.PetTag
import com.hfad.petlogger.common.associationentities.PhotoEvent
import com.hfad.petlogger.common.associationentities.PhotoNote
import com.hfad.petlogger.common.associationentities.PhotoTag
import com.hfad.petlogger.common.associationentities.WeightNote
import com.hfad.petlogger.common.associationentities.WeightTag
import com.hfad.petlogger.events.EventDao
import com.hfad.petlogger.dao.EventPetDao
import com.hfad.petlogger.notes.NoteDao
import com.hfad.petlogger.pets.PetDao
import com.hfad.petlogger.photos.PhotoDao
import com.hfad.petlogger.tags.TagDao
import com.hfad.petlogger.weights.WeightDao
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.common.util.URIConverter
import com.hfad.petlogger.events.Event
import com.hfad.petlogger.events.EventFts
import com.hfad.petlogger.notes.Note
import com.hfad.petlogger.notes.NoteFts
import com.hfad.petlogger.pets.Pet
import com.hfad.petlogger.photos.Photo
import com.hfad.petlogger.tags.Tag
import com.hfad.petlogger.weights.Weight

@Database(
    entities = [
        Pet::class,
        PetPhoto::class,

        Event::class,
        EventPet::class,
        EventFts::class,

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
    version = 2,
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