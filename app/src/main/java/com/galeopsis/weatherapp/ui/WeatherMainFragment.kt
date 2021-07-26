package com.galeopsis.weatherapp.ui

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.galeopsis.weatherapp.databinding.WeatherMainFragmentBinding
import com.galeopsis.weatherapp.utils.LoadingState
import com.galeopsis.weatherapp.utils.unixTimestampToTimeString
import com.galeopsis.weatherapp.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherMainFragment : Fragment() {

    companion object {
        fun newInstance() = WeatherMainFragment()
    }

    private val mainViewModel by viewModel<MainViewModel>()
    private var _binding: WeatherMainFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = WeatherMainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initOfflineData()

        with(binding) {
            inputLayout
                .setEndIconOnClickListener {
                    val zipCode = inputEditText.text.toString()

                    activity?.let { it1 -> dismissKeyboard(it1) }

                    mainViewModel.fetchData(zipCode)

                    context?.let { isOnline(it) }
                    if (context?.let { isOnline(it) } == true) {
                        mainViewModel.fetchData(zipCode)
                        initData()
                    } else {
                        initOfflineData()
                    }
                }
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

        mainViewModel.data.observe(viewLifecycleOwner, {
            it?.forEach { weatherData ->
                with(binding) {
                    cityName.text = weatherData.name
                    temperature.text = ((weatherData.main?.temp?.toInt()).toString() + " °С")
                    wind.text = (weatherData.wind?.speed.toString() + " mph")
                    humidityVal.text = (weatherData.main?.humidity.toString() + " %")
                    visibilityVal.text = (weatherData.visibility.toString() + " Meters")
                    sunriseVal.text = weatherData.sys?.sunrise?.unixTimestampToTimeString()
                    sunsetVal.text = weatherData.sys?.sunset?.unixTimestampToTimeString()
                }
            }
        })

        mainViewModel.loadingState.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.FAILED ->
//                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "You've entered wrong zip code!", Toast.LENGTH_SHORT).show()
                LoadingState.Status.RUNNING ->
                    binding.loadingLayout.visibility = View.VISIBLE
                LoadingState.Status.SUCCESS ->
                    binding.loadingLayout.visibility = View.GONE
            }
        })
    }

    private fun initOfflineData() {
        mainViewModel.data.observe(viewLifecycleOwner, {
            it?.forEach { weatherData ->
                with(binding) {
                    cityName.text = weatherData.name
                    temperature.text = ((weatherData.main?.temp?.toInt()).toString() + " °С")
                    wind.text = (weatherData.wind?.speed.toString() + " mph")
                    humidityVal.text = (weatherData.main?.humidity.toString() + " %")
                    visibilityVal.text = (weatherData.visibility.toString() + " Meters")
                    sunriseVal.text = weatherData.sys?.sunrise?.unixTimestampToTimeString()
                    sunsetVal.text = weatherData.sys?.sunset?.unixTimestampToTimeString()
                }
            }
        })
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
                    Toast.makeText(context, "mobile", Toast.LENGTH_SHORT).show()
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    Toast.makeText(context, "wifi", Toast.LENGTH_SHORT).show()
                    return true
                }
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    Toast.makeText(context, "ethernet", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        Toast.makeText(context, "no internet connection!", Toast.LENGTH_SHORT).show()
        return false
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
