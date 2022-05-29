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
import com.galeopsis.weatherapp.model.FInfo
import com.galeopsis.weatherapp.utils.LoadingState
import com.galeopsis.weatherapp.utils.unixTimestampToTimeString
import com.galeopsis.weatherapp.viewmodel.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherMainFragment (): Fragment() {

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

        fetchData()
        initListeners()

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

    private fun initListeners() {
        with(binding) {
            gps.setOnClickListener {
                getLastLocation()
            }
            inputLayout
                .setEndIconOnClickListener {
                    searchByName("name")
                }
        }
    }

    private fun WeatherMainFragmentBinding.searchByName(method: String) {
        val inputText = deleteSpace()
        if (inputText.isNotEmpty()) validate(inputText, method) else return
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
            Log.d("foreInfo", "отправлен http запрос $inputData $method")
            mainViewModel.fetchData(inputData, method)
            initData()
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
        mainViewModel.loadingState.observe(viewLifecycleOwner) {
            when (it.status) {
                LoadingState.Status.FAILED -> {
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
        try {
            mainViewModel.data.observe(viewLifecycleOwner) {
                it?.forEach { weatherData ->
                    with(binding) {
                        Log.d("foretest", FInfo.dTempTomorrow!!)
                        val textToTrim = (weatherData.weather.toString()).substringAfter("description=")
                        val description = textToTrim.substringBefore(',')
                        val lat = weatherData.coord?.lat
                        val lon = weatherData.coord?.lon
                        validate("lat=$lat&lon=$lon", "forecast")
                        Thread.sleep(3000)
                        val temp = FInfo.mainTemp
                        val rWind = FInfo.wind
                        val rHumidity = FInfo.humidity
                        val dTempTomorrow = FInfo.dTempTomorrow
                        val dDescriptionTomorrow = FInfo.dDescriptionTomorrow

                        val dTempAfterTomorrow = FInfo.dTempAfterTomorrow
                        val dDescriptionAfterTomorrow = FInfo.dDescriptionAfterTomorrow
                        val dTempAfterAfterTomorrow = FInfo.dTempAfterAfterTomorrow
                        val dDescriptionAfterAfterTomorrow = FInfo.dDescriptionAfterAfterTomorrow

                        val val1 = "$dTempTomorrow°C\n$dDescriptionTomorrow"
                        val val2 = "$dTempAfterTomorrow°С\n$dDescriptionAfterTomorrow"
                        val val3 = "$dTempAfterAfterTomorrow°С\n$dDescriptionAfterAfterTomorrow"

                        tomorrowDate.text = FInfo.tomorrowDate
                        tomorrow.text = val1

                        tomorrowAfterDate.text = FInfo.tomorrowAfterDate
                        tomorrowAfter.text = val2

                        tomorrowAfterAfterDate.text = FInfo.tomorrowAfterAfterDate
                        tomorrowAfterAfter.text = val3

                        tomorrowDate.visibility = View.VISIBLE
                        tomorrow.visibility = View.VISIBLE

                        tomorrowAfterDate.visibility = View.VISIBLE
                        tomorrowAfter.visibility = View.VISIBLE

                        tomorrowAfterAfterDate.visibility = View.VISIBLE
                        tomorrowAfterAfter.visibility = View.VISIBLE

                        versionNumber.text = "Версия приложения: ${getVersion(requireContext())}"

                        currentCondition.text = description
                        currentCondition.visibility = View.VISIBLE
                        if (weatherData.name == "Бадалык") cityName.text =
                            "Красноярск" else cityName.text = weatherData.name
                        cityName.visibility = View.VISIBLE
                        temperature.text = ("+$temp°С")
                        temperature.visibility = View.VISIBLE
                        wind.text = (rWind + " м/с")
                        windSpeed.visibility = View.VISIBLE
                        wind.visibility = View.VISIBLE
                        humidityVal.text = (rHumidity + " %")
                        humidity.visibility = View.VISIBLE
                        humidityVal.visibility = View.VISIBLE
                        sunriseVal.text = weatherData.timezone?.let { it1 ->
                            weatherData.sys?.sunrise?.unixTimestampToTimeString(
                                it1
                            )
                        }
                        sunrise.visibility = View.VISIBLE
                        sunriseVal.visibility = View.VISIBLE
                        sunsetVal.text = weatherData.timezone?.let { it1 ->
                            weatherData.sys?.sunset?.unixTimestampToTimeString(
                                it1
                            )
                        }
                        sunsetVal.visibility = View.VISIBLE
                        sunset.visibility = View.VISIBLE
                        val iconToTrim = (weatherData.weather.toString()).substringAfter("icon=")
                        val iconData = iconToTrim.substringBefore(')')
                        val iconUrl = "https://openweathermap.org/img/w/$iconData.png"
                        val iconUrl1 = "https://openweathermap.org/img/w/${FInfo.tomorrowIcon}.png"
                        val iconUrl2 = "https://openweathermap.org/img/w/${FInfo.tomorrowAfterIcon}.png"
                        val iconUrl3 = "https://openweathermap.org/img/w/${FInfo.tomorrowAfterAfterIcon}.png"



                        Glide.with(this@WeatherMainFragment)
                            .load(iconUrl)
                            .into(icon)

                        Glide.with(this@WeatherMainFragment)
                            .load(iconUrl1)
                            .into(icon1)

                        Glide.with(this@WeatherMainFragment)
                            .load(iconUrl2)
                            .into(icon2)

                        Glide.with(this@WeatherMainFragment)
                            .load(iconUrl3)
                            .into(icon3)

                    }

                }
            }
        } catch (e: Exception) {
            Log.d("errtest", "exception: ${e.fillInStackTrace()}")
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
                    Log.i("errtest", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("errtest", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("errtest", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        Toast.makeText(context, "Проверьте подключение к интернет!", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
