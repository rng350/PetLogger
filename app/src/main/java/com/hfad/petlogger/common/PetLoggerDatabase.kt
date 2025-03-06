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
import com.hfad.petlogger.common.util.Converter
import com.hfad.petlogger.common.util.URIConverter
import com.hfad.petlogger.dao.EventPetDao
import com.hfad.petlogger.events.data.Event
import com.hfad.petlogger.events.data.EventDao
import com.hfad.petlogger.events.data.EventFts
import com.hfad.petlogger.notes.data.Note
import com.hfad.petlogger.notes.data.NoteDao
import com.hfad.petlogger.notes.data.NoteFts
import com.hfad.petlogger.pets.data.PassedAwayPet
import com.hfad.petlogger.pets.data.Pet
import com.hfad.petlogger.pets.data.PetDao
import com.hfad.petlogger.pets.data.PetFts
import com.hfad.petlogger.photos.data.Photo
import com.hfad.petlogger.photos.data.PhotoDao
import com.hfad.petlogger.tags.data.Tag
import com.hfad.petlogger.tags.data.TagDao
import com.hfad.petlogger.weights.data.Weight
import com.hfad.petlogger.weights.data.WeightDao

@Database(
    entities = [
        Pet::class,
        PetFts::class,
        PetPhoto::class,
        PassedAwayPet::class,

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