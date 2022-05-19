package com.galeopsis.weatherapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.galeopsis.weatherapp.databinding.WeatherMainFragmentBinding
import com.galeopsis.weatherapp.utils.LoadingState
import com.galeopsis.weatherapp.utils.unixTimestampToTimeString
import com.galeopsis.weatherapp.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.neovisionaries.i18n.CountryCode
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherMainFragment : Fragment() {

    companion object {
        fun newInstance() = WeatherMainFragment()
    }

    private val mainViewModel by viewModel<MainViewModel>()
    private var _binding: WeatherMainFragmentBinding? = null
    private val binding get() = _binding!!
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var latitude = ""
    private var longtitude = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherMainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getLastLocation()
        fetchData()
        initListeners()

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        latitude = location.latitude.toString()
                        longtitude = location.longitude.toString()
                        validate("lat=$latitude&lon=$longtitude", "coordinates")
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun getVersion(context: Context): String {
        var version = ""
        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            version = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version
    }

    private fun checkPermissions(): Boolean {
        if (
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == 42) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 42
        )
    }

    private fun updateCurrentCity() {
        getLastLocation()
    }

    private fun initListeners() {
        with(binding) {
            inputLayout
                .setEndIconOnClickListener {
                    searchByName("name")
                }

            switchCompat.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    inputEditText.inputType = InputType.TYPE_CLASS_NUMBER
                    inputLayout
                        .setEndIconOnClickListener {
                            searchByZip("zip")
                        }
                } else {
                    inputEditText.inputType = InputType.TYPE_CLASS_TEXT
                    inputLayout
                        .setEndIconOnClickListener {
                            searchByName("name")
                        }
                }
            }
        }
    }

    private fun WeatherMainFragmentBinding.searchByZip(method: String) {
        val inputText = deleteSpace()
        val inputData = "$inputText,ru"
        validate(inputData, method)
    }

    private fun WeatherMainFragmentBinding.searchByName(method: String) {
        val inputText = deleteSpace()
        if (inputText.isNotEmpty()) {
            validate(inputText, method)
        } else {
            getLastLocation()
        }
    }

    private fun WeatherMainFragmentBinding.deleteSpace(): String {
        return inputEditText.text.toString().dropLastWhile {
            it == ' '
        }
    }

    private fun validate(inputData: String, method: String) {
        activity?.let { it1 -> dismissKeyboard(it1) }
        context?.let { isOnline(it) }
        if (context?.let { isOnline(it) } == true) {
            mainViewModel.fetchData(inputData, method)
            initData()
        } else {
            fetchData()
        }
    }

    private fun dismissKeyboard(activity: Activity) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (null != activity.currentFocus) imm.hideSoftInputFromWindow(
            requireActivity().currentFocus!!.applicationWindowToken, 0
        )
    }

    private fun initData() {

        fetchData()

        mainViewModel.loadingState.observe(viewLifecycleOwner) {
            when (it.status) {
                LoadingState.Status.FAILED -> {
                    Toast.makeText(context, "Ошибка запроса, попробуйте снова", Toast.LENGTH_SHORT)
                        .show()
                    binding.loadingLayout.visibility = View.GONE
                }

                LoadingState.Status.RUNNING ->
                    binding.loadingLayout.visibility = View.VISIBLE
                LoadingState.Status.SUCCESS -> {
                    binding.inputEditText.text = null
                    binding.loadingLayout.visibility = View.GONE
                }
            }
        }
    }

    private fun fetchData() {
        mainViewModel.data.observe(viewLifecycleOwner) {
            it?.forEach { weatherData ->
                with(binding) {
                    val countryCode = CountryCode.getByCode(weatherData.sys?.country)
                    val country = countryCode.getName()
                    val textToTrim = (weatherData.weather.toString()).substringAfter("description=")
                    val description = textToTrim.substringBefore(',')
                    versionNumber.text = "Версия приложения: ${getVersion(requireContext())}"
                    currentCondition.text = description
                    cityName.text = (weatherData.name)
                    temperature.text = ((weatherData.main?.temp?.toInt()).toString() + " °С")
                    wind.text = (weatherData.wind?.speed.toString() + " м/с")
                    humidityVal.text = (weatherData.main?.humidity.toString() + " %")
                    sunriseVal.text = weatherData.timezone?.let { it1 ->
                        weatherData.sys?.sunrise?.unixTimestampToTimeString(
                            it1
                        )
                    }
                    sunsetVal.text = weatherData.timezone?.let { it1 ->
                        weatherData.sys?.sunset?.unixTimestampToTimeString(
                            it1
                        )
                    }
                    val iconToTrim = (weatherData.weather.toString()).substringAfter("icon=")
                    val iconData = iconToTrim.substringBefore(')')
                    val iconUrl = "https://openweathermap.org/img/w/$iconData.png"
                    Glide.with(this@WeatherMainFragment)
                        .load(iconUrl)
                        .into(icon)
                }
            }
        }
    }

    private fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
//                    Toast.makeText(context, "mobile", Toast.LENGTH_SHORT).show()
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
//                    Toast.makeText(context, "wifi", Toast.LENGTH_SHORT).show()
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
//                    Toast.makeText(context, "ethernet", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        Toast.makeText(context, "Проверьте подключение к интернет!", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onResume() {
        updateCurrentCity()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
