package com.alltrails.lunch.list

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rxjava3.subscribeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.alltrails.lunch.BuildConfig
import com.alltrails.lunch.R
import com.alltrails.lunch.backend.NearbySearchResponse
import com.alltrails.lunch.core.LatLng
import com.alltrails.lunch.core.Lce
import com.alltrails.lunch.core.NearbyPlaces
import com.alltrails.lunch.ui.theme.DarkYellow
import com.alltrails.lunch.ui.theme.LightGray
import com.alltrails.lunch.ui.theme.LunchTheme
import com.alltrails.lunch.ui.theme.Typography
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import com.google.android.gms.maps.model.LatLng as GLatLng

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
            //noinspection MissingPermission
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
    Column {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 30.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_alltrails),
                contentDescription = stringResource(R.string.alltrails_logo)
            )
            Text(
                text = stringResource(R.string.at_lunch),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 5.dp)
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val lce: Lce<NearbyPlaces> = placesViewModel.nearbySearch().subscribeAsState(Lce.initial()).value
            val places: NearbyPlaces?
            when (lce) {
                is Lce.Initial -> {
                    val locationPermissionDenied =
                        placesViewModel.locationPermissionDenied().map { true }.subscribeAsState(false)
                    PlacesInitial(locationPermissionDenied = locationPermissionDenied.value)
                    places = null
                }
                is Lce.Loading -> {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(8.dp)
                            .align(Alignment.BottomCenter)
                    )
                    places = lce.oldContent
                }
                is Lce.Content -> {
                    places = lce.content
                }
                is Lce.Error -> {
                    Text(text = "error: ${lce.throwable.message}")
                    places = null
                }
            }

            if (places != null) {
                PlacesNavHost(places = places, navController = navController)
            }
        }
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

private sealed class Routes(val route: String) {
    object PlacesList : Routes("placesList")
    object PlacesMap : Routes("placesMap")
    object PlaceDetail : Routes("placesDetail/{placeId}") {
        const val placeId = "placeId"
    }
}

@Composable
private fun PlacesNavHost(
    places: NearbyPlaces,
    navController: NavHostController,
    startDestination: String = Routes.PlacesList.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.PlacesList.route) {
            PlacesList(places.places) {
                navController.navigate(Routes.PlacesMap.route)
            }
        }
        composable(Routes.PlacesMap.route) {
            PlacesMap(places.places, places.location) {
                navController.navigate(Routes.PlacesList.route)
            }
        }
        composable(Routes.PlaceDetail.route) {
            PlaceDetail(placeId = it.arguments!!.getString(Routes.PlaceDetail.placeId)!!)
        }
    }
}

@Composable
private fun PlaceDetail(placeId: String) {
    Text("detail for place $placeId]}")
}

@Composable
private fun PlacesMap(places: List<NearbySearchResponse.Place>, location: LatLng, onNavigateToPlacesList: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(location.toGoogleMapsLatLng(), 16f)
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = {
                val latLngs = places.mapNotNull { place ->
                    place.geometry?.location?.run {
                        GLatLng(lat, lng)
                    }
                }
                var bounds: LatLngBounds? = null
                if (latLngs.isNotEmpty()) {
                    bounds = LatLngBounds(latLngs.first(), latLngs.first())
                    for (latLng in latLngs) {
                        bounds = bounds!!.including(latLng)
                    }
                }
                if (bounds != null) {
                    // TODO: figure out how to run this on location update, in spite of rememberCameraPositionState
                    cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(bounds, 60))
                }
            }
        ) {
            for (place in places) {
                if (place.geometry != null) {
                    Marker(
                        position = GLatLng(place.geometry.location.lat, place.geometry.location.lng),
                        title = place.name,
                        snippet = place.rating?.let { "\u2605".repeat(it.toInt()) }
                    )
                }
            }
        }

        Button(
            onClick = onNavigateToPlacesList,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(text = stringResource(R.string.list))
        }
    }
}

@Composable
private fun PlacesList(places: List<NearbySearchResponse.Place>, onNavigateToPlacesMap: () -> Unit) {
    val itemHeight = 77.dp
    val photoSizePx = with(LocalDensity.current) {
        itemHeight.roundToPx()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 15.dp),
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
                            val url = "https://maps.googleapis.com/maps/api/place/photo?key=${BuildConfig.MAPS_API_KEY}" +
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
                }
            }
        }

        Button(
            onClick = onNavigateToPlacesMap,
            modifier = Modifier
                .padding(20.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(text = stringResource(R.string.map))
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
        )) {}
    }
}