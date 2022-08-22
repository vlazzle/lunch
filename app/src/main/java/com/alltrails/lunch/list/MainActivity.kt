package com.alltrails.lunch.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.alltrails.lunch.R
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.Lce
import com.alltrails.lunch.ui.theme.LunchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LunchTheme {
                val placesViewModel: PlacesViewModel = hiltViewModel()
                // TODO: test both onLocationPermissionGranted first and nearbySearch first
                placesViewModel.onLocationPermissionGranted()
                val placesLce = placesViewModel.nearbySearch().subscribeAsState(Lce.initial())

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    when (val places = placesLce.value) {
                        is Lce.Initial -> {
                        }
                        is Lce.Loading -> {
                            Text(text = stringResource(R.string.loading))
                        }
                        is Lce.Content -> PlacesList(places = places.content)
                        is Lce.Error -> {
                            Text(text = "error: ${places.throwable.message}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlacesList(places: List<NearbySearchResponse.Place>) {
    Text(text = places.take(20).map { it.name }.joinToString("\n"))
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LunchTheme {
        PlacesList(listOf(NearbySearchResponse.Place("taco bell")))
    }
}