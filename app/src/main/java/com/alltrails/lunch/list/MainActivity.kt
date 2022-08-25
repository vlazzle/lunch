package com.alltrails.lunch.list

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.alltrails.lunch.R
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.Lce
import com.alltrails.lunch.ui.theme.DarkYellow
import com.alltrails.lunch.ui.theme.LightGray
import com.alltrails.lunch.ui.theme.LunchTheme
import com.alltrails.lunch.ui.theme.Typography
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val placesViewModel: PlacesViewModel by lazy {
        ViewModelProvider(this)[PlacesViewModel::class.java]
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
                    NearbyPlaces(placesViewModel = placesViewModel)
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
private fun NearbyPlaces(placesViewModel: PlacesViewModel) {
    when (val places: Lce<List<NearbySearchResponse.Place>> =
        placesViewModel.nearbySearch().subscribeAsState(Lce.initial()).value) {
        is Lce.Initial -> {
            val locationPermissionDenied =
                placesViewModel.locationPermissionDenied().map { true }.subscribeAsState(false)
            PlacesInitial(locationPermissionDenied = locationPermissionDenied.value)
        }
        is Lce.Loading -> Text(text = stringResource(R.string.loading))
        is Lce.Content -> PlacesNavHost(places = places.content)
        is Lce.Error -> Text(text = "error: ${places.throwable.message}")
    }
}

@Composable
private fun PlacesInitial(locationPermissionDenied: Boolean) {
    if (!locationPermissionDenied) {
        Text(text = stringResource(R.string.awaiting_location))
    } else {
        // TODO: snackbar instead, with action button to open app settings
        //  https://foso.github.io/Jetpack-Compose-Playground/material/snackbar/
        Text(text = stringResource(R.string.location_permission_denied))
    }
}

private enum class Routes {
    PlacesList,
    PlacesMap,
    PlaceDetail,
}

@Composable
private fun PlacesNavHost(
    places: List<NearbySearchResponse.Place>,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.PlacesMap.name
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.PlacesList.name) {
            PlacesList(places)
        }
        composable(Routes.PlacesMap.name) {
            PlacesMap {
                navController.navigate(Routes.PlacesList.name)
            }
        }
        composable(Routes.PlaceDetail.name) {
            Text("place detail")
        }
    }
}

@Composable
private fun PlacesMap(onNavigateToPlacesList: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("places map")
        Button(onClick = onNavigateToPlacesList) {
            Text("places list")
        }
    }
}

@Composable
private fun PlacesList(places: List<NearbySearchResponse.Place> ) {
    val itemHeight = 77.dp
    val photoSizePx = with(LocalDensity.current) {
        itemHeight.roundToPx()
    }
    val key = stringResource(R.string.maps_api_key)

    LazyColumn(
        contentPadding = PaddingValues(vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 17.dp)
    ) {
        items(places) { place ->
            val roundedCornerShape = RoundedCornerShape(7.dp)
            Box(
                modifier = Modifier
                    .background(color = Color.White, shape = roundedCornerShape)
                    .border(width = 1.dp, color = LightGray, shape = roundedCornerShape)
                    .padding(vertical = 18.dp, horizontal = 16.dp)
                    .height(itemHeight)
                    .fillMaxWidth()
            ) {
                Row {
                    val placeName = place.name ?: stringResource(R.string.unnamed_place)
                    val photo = place.photos?.firstOrNull()
                    if (photo != null) {
                        val sizeParam = if (photo.width > photo.height) {
                            "maxwidth"
                        } else {
                            "maxheight"
                        }
                        val url = "https://maps.googleapis.com/maps/api/place/photo?key=$key" +
                                "&$sizeParam=$photoSizePx" +
                                "&photo_reference=${photo.photo_reference}"
                        // TODO: investigate loading photo into a pre-measured placeholder using AsyncImagePainter
                        //  https://coil-kt.github.io/coil/compose/#asyncimagepainter
                        AsyncImage(
                            model = url,
                            contentDescription = stringResource(R.string.place_photo_content_description, placeName),
                            contentScale = ContentScale.Crop,
                            alignment = Alignment.Center,
                            error = ColorPainter(LightGray),
                            placeholder = ColorPainter(LightGray),
                            modifier = Modifier
                                .width(itemHeight)
                                .height(itemHeight)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                    }

                    Column {
                        Text(
                            text = placeName,
                            style = Typography.body1 + TextStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                        StarRating(
                            rating = place.rating,
                            userRatingsTotal = place.user_ratings_total
                        )
                        Price(
                            priceLevel = place.price_level,
                            // TODO: supporting text
                            supportingText = "Supporting Text"
                        )
                    }
                }
                // TODO: heart button
                Text(
                    text = "\u2661",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}

@Composable
private fun StarRating(rating: Float?, userRatingsTotal: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (rating != null && (userRatingsTotal ?: 0) > 0) {
            // TODO: half stars
            Text(
                text = "\u2605".repeat(rating.toInt()),
                style = TextStyle(
                    color = DarkYellow,
                    fontSize = 22.sp
                )
            )
            Text(text = "\u2605".repeat(5 - rating.toInt()),
                style = TextStyle(
                    color = LightGray,
                    fontSize = 22.sp
                ))

            if (userRatingsTotal != null) {
                Text(text = "($userRatingsTotal)",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 5.dp))
            }
        } else {
            Text(text = stringResource(R.string.no_ratings_yet))
        }
    }
}

@Composable
private fun Price(priceLevel: Int?, supportingText: String?) {
    val text = mutableListOf<String>()
    if (priceLevel != null) {
        text.add(Currency.getInstance(Locale.getDefault()).symbol.repeat(priceLevel))
    }
    if (!supportingText.isNullOrBlank()) {
        if (priceLevel != null) {
            text.add(" \u2022 ")
        }
        text.add(supportingText)
    }
    if (text.isNotEmpty()) {
        Text(text = text.joinToString(""))
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    LunchTheme {
        PlacesList(listOf(
            NearbySearchResponse.Place("taco bell"),
            NearbySearchResponse.Place("in n out"),
            NearbySearchResponse.Place("chipotle"),
            NearbySearchResponse.Place("popeyes"),
        ))
    }
}