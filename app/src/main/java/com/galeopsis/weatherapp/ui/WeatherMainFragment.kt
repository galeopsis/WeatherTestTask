package com.galeopsis.weatherapp.ui

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.galeopsis.weatherapp.databinding.WeatherMainFragmentBinding
import com.galeopsis.weatherapp.utils.LoadingState
import com.galeopsis.weatherapp.utils.unixTimestampToTimeString
import com.galeopsis.weatherapp.viewmodel.MainViewModel
import com.neovisionaries.i18n.CountryCode
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
        val inputText = inputEditText.text.toString().dropLastWhile {
            it == ' '
        }
        val inputData = "$inputText,ru"
        validate(inputData, method)
    }

    private fun WeatherMainFragmentBinding.searchByName(method: String) {
        val inputText = inputEditText.text.toString().dropLastWhile {
            it == ' '
        }
        validate(inputText, method)
    }

    private fun validate(inputData: String, method: String) {
        activity?.let { it1 -> dismissKeyboard(it1) }
        context?.let { isOnline(it) }
        if (context?.let { isOnline(it) } == true) {
            mainViewModel.fetchData(inputData, method)
            initData()
        } else {
            initOfflineData()
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

        mainViewModel.loadingState.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.FAILED ->
//                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                {
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
        })
    }

    private fun initOfflineData() {

        fetchData()

    }

    private fun fetchData() {
        mainViewModel.data.observe(viewLifecycleOwner, {
            it?.forEach { weatherData ->
                with(binding) {
                    val countryCode = CountryCode.getByCode(weatherData.sys?.country)
                    val country = countryCode.getName()
                    val textToTrim = (weatherData.weather.toString()).substringAfter("description=")
                    val description = textToTrim.substringBefore(',')
                    currentCondition.text = description
                    cityName.text = (country + ", " + weatherData.name)
                    temperature.text = ((weatherData.main?.temp?.toInt()).toString() + " °С")
                    wind.text = (weatherData.wind?.speed.toString() + " м/с")
                    humidityVal.text = (weatherData.main?.humidity.toString() + " %")
                    visibilityVal.text = (weatherData.visibility.toString() + " м.")
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
