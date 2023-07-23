package com.hfad.guineapiglog.entitylinkers

import com.hfad.guineapiglog.dao.PhotoDao
import com.hfad.guineapiglog.entities.PetProfilePhoto

class PetProfilePhotoUpdater(private val photoDao: PhotoDao): EntityLinker {
    override suspend fun associateWith(petID: Long, photoID: Long) {
        //Log.d("pet_profile_photo_linker", "linking!")
        photoDao.update(PetProfilePhoto(petID, photoID))
        //Log.d("pet_profile_photo_linker", "linked!")
    }
}