package com.hfad.petlogger

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hfad.petlogger.entities.Pet
import com.hfad.petlogger.entities.PetWithProfilePic

@Composable
fun PetListDisplay(
    viewModel: PetListDisplayViewModel = viewModel(factory = PetListDisplayViewModel.Factory(PetLoggerDatabase.getInstance(LocalContext.current).petDao))
) {
    Column {
        LazyColumn {
        }
    }
}

@Composable
fun PetCard(pet: PetWithProfilePic) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.24f)
            .aspectRatio(0.8f)
            .clickable(onClick = {})
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.84f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(pet.profilePic?.contentUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.placeholder),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .aspectRatio(1f)
                )
                Spacer(Modifier.height(5.dp))
                Text(
                    text = pet.pet.petName,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 100)
@Composable
fun PetCardPreview() {
    val pet = Pet(0, "Pumpkin", "Guinea Pig", "American", "Female")
    val petWithProfilePic = PetWithProfilePic(pet, null)
    PetCard(petWithProfilePic)
}