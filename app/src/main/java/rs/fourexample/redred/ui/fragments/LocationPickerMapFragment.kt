package rs.fourexample.redred.ui.fragments


import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class LocationPickerMapFragment : MapFragment() {
    private val ARG_LOCATION = "location"

    private lateinit var marker: Marker
    private var listener: OnFragmentInteractionListener? = null

    override fun onMapReady(map: GoogleMap?) {
        super.onMapReady(map)
        googleMap.uiSettings.isZoomControlsEnabled = true

        location?.let {
            addMarkerToMap(it)
        }
        setPaddingForMapElements(googleMap, bottom = 12, top = 12)
    }

    private fun addMarkerToMap(location: Location) {
        listener?.onLocationSelected(LatLng(location.latitude, location.longitude))
        context?.let {
            marker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(location.latitude, location.longitude))
                    .draggable(true)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            )
//                    .icon(
//                        bitmapDescriptorFromVector(it, R.drawable.ic_marker_request)
//                    )
        }


        googleMap.setOnCameraMoveListener {
            marker.position = googleMap.cameraPosition.target
            listener?.onLocationSelected(googleMap.cameraPosition.target)
        }
    }

    override fun getInitZoomLevel(): Float {
        return 16f
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnFragmentInteractionListener {
        fun onLocationSelected(latLng: LatLng)
    }

    companion object {

        fun newInstance(
            location: Location?
        ): LocationPickerMapFragment {
            return LocationPickerMapFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_LOCATION, location)
                }
            }
        }
    }

}
