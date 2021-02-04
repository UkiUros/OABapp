package rs.fourexample.redred.ui.fragments

import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class RequestDetailMapFragment : MapFragment() {

    private val ARG_LOCATION = "location"

    private lateinit var marker: Marker

    override fun onMapReady(map: GoogleMap?) {
        super.onMapReady(map)
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        location?.let {
            addMarkerToMap(it)
        }

        setPaddingForMapElements(map, top = 12, bottom = 70)
    }

    private fun addMarkerToMap(location: Location) {
        context?.let {
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
        }
    }

    override fun getInitZoomLevel(): Float {
        return 16f
    }

    companion object {

        fun newInstance(
            location: Location?
        ): RequestDetailMapFragment {
            return RequestDetailMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LOCATION, location)
                }
            }
        }
    }

}
