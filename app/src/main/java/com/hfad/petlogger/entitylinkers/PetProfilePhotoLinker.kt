package com.hfad.petlogger.entitylinkers

import com.hfad.petlogger.entities.PetProfilePhoto
import com.hfad.petlogger.dao.PhotoDao

class PetProfilePhotoLinker(private val photoDao: PhotoDao): EntityLinker {
    override suspend fun associateWith(petID: Long, photoID: Long) {
        //Log.d("pet_profile_photo_linker", "linking!")
        photoDao.insert(PetProfilePhoto(petID, photoID))
        //Log.d("pet_profile_photo_linker", "linked!")
    }
}