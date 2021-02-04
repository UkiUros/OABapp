package rs.fourexample.redred.ui.fragments

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import rs.fourexample.redred.R
import rs.fourexample.redred.data.CurrentSession
import rs.fourexample.redred.databinding.FragmentMapBinding
import rs.fourexample.redred.helper.dpToPx
import rs.fourexample.redred.helper.extractCountryCodeFromLocation

abstract class MapFragment : Fragment(), OnMapReadyCallback {


    protected var location: Location? = null
    protected var countryCode: String? = null

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    protected lateinit var googleMap: GoogleMap

    private lateinit var mapFragment: SupportMapFragment

    private val ARG_LOCATION = "location"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            location = bundle.getParcelable(ARG_LOCATION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.isEnabled = false
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        if (location == null) {
            countryCode = CurrentSession.getInstance().countryCode
            countryCode?.let { onCountryCodeExtracted() }
        } else {
            location?.let { location ->
                countryCode = extractCountryCodeFromLocation(requireActivity(), location)
                countryCode?.let {
                    CurrentSession.getInstance().countryCode = it
                    onCountryCodeExtracted()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap?) {
        map?.let {
            googleMap = it
            googleMap.uiSettings.isCompassEnabled = true
            googleMap.uiSettings.setAllGesturesEnabled(true)
            googleMap.uiSettings.isZoomGesturesEnabled = true
            googleMap.uiSettings.isRotateGesturesEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false

            val zoomLevel = if (location == null) {
                location = Location("")
                location!!.latitude = googleMap.cameraPosition.target.latitude
                location!!.longitude = googleMap.cameraPosition.target.longitude
                googleMap.minZoomLevel
            } else {
                getInitZoomLevel()
            }

            googleMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(location!!.latitude, location!!.longitude), zoomLevel
                )
            )
//            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.custom_map_style))

        }
    }

    protected fun setPaddingForMapElements(map: GoogleMap?, top: Int = 0, bottom: Int = 0, left: Int = 0, right: Int = 0) {
        context?.let {
            map?.setPadding(
                dpToPx(left, it),
                dpToPx(top, it), // top
                dpToPx(right, it),
                dpToPx(bottom, it) // bottom
            )
        }
    }

    abstract fun getInitZoomLevel(): Float

    protected fun showRefreshAnimation() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    protected fun hideRefreshAnimation() {
        binding.swipeRefreshLayout.isRefreshing = false
    }

//    protected fun showRefreshButton(){
//        buttonRefresh.visibility = View.VISIBLE
//    }
//
//    protected fun hideRefreshButton(){
//        buttonRefresh.visibility = View.GONE
//    }

    private fun onCountryCodeExtracted() {
        mapFragment.getMapAsync(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
