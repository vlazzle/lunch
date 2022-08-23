package com.alltrails.lunch.list

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.alltrails.lunch.R
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.Lce
import com.alltrails.lunch.ui.theme.LunchTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val placesViewModel: PlacesViewModel by lazy {
        ViewModelProvider(this).get(PlacesViewModel::class.java)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGrantedByPermission: Map<String, Boolean> ->
            if (isGrantedByPermission[permission.ACCESS_FINE_LOCATION] == true ||
                isGrantedByPermission[permission.ACCESS_COARSE_LOCATION] == true) {
                //noinspection MissingPermission
                placesViewModel.onLocationPermissionGranted()
            } else {
                placesViewModel.onLocationPermissionDenied()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            placesViewModel.onLocationPermissionGranted()
        } else {
            requestLocationPermission()
        }

        setContent {
            LunchTheme {
                // A surface container using the 'background' color from the theme
                // TODO: padding
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val placesLce = placesViewModel.nearbySearch().subscribeAsState(Lce.initial())
                    when (val places = placesLce.value) {
                        is Lce.Initial -> {
                            val locationPermissionDenied =
                                placesViewModel.locationPermissionDenied().map { true }.subscribeAsState(false)
                            PlacesInitial(locationPermissionDenied = locationPermissionDenied.value)
                        }
                        is Lce.Loading -> Text(text = stringResource(R.string.loading))
                        is Lce.Content -> PlacesContent(places = places.content)
                        is Lce.Error -> Text(text = "error: ${places.throwable.message}")
                    }
                }
            }
        }
    }

    private fun requestLocationPermission() {
        // TODO: shouldShowRequestPermissionRationale https://github.com/android/permissions-samples/blob/main/PermissionsActivityResultKotlin/Application/src/main/java/com/example/android/basicpermissions/MainActivity.kt#L111
        requestPermissionLauncher.launch(arrayOf(permission.ACCESS_COARSE_LOCATION, permission.ACCESS_FINE_LOCATION))
    }
}

@Composable
fun PlacesInitial(locationPermissionDenied: Boolean) {
    if (!locationPermissionDenied) {
        Text(text = stringResource(R.string.awaiting_location))
    } else {
        // TODO: snackbar instead, with action button to open app settings
        //  https://foso.github.io/Jetpack-Compose-Playground/material/snackbar/
        Text(text = stringResource(R.string.location_permission_denied))
    }
}

@Composable
fun PlacesContent(places: List<NearbySearchResponse.Place>) {
    // TODO: LazyColumn
    Text(text = places.take(20).map { it.name }.joinToString("\n"))
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LunchTheme {
        PlacesContent(listOf(
            NearbySearchResponse.Place("taco bell"),
            NearbySearchResponse.Place("in n out"),
            NearbySearchResponse.Place("chipotle"),
            NearbySearchResponse.Place("popeyes"),
        ))
    }
}