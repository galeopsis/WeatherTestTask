package com.galeopsis.weatherapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.galeopsis.weatherapp.databinding.WeatherMainFragmentBinding
import com.galeopsis.weatherapp.utils.LoadingState
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

        /*with(binding) {
            inputLayout
                .setEndIconOnClickListener {
                    val data = inputEditText.text.toString()
                    initData(data)
                }
        }*/
        initData()
    }

    private fun initData() {
        mainViewModel.data.observe(viewLifecycleOwner, {
            it?.forEach { weatherData ->
                with(binding) {
                    location.text = weatherData.name
                    currentTemp.text = weatherData.main?.temp.toString()
                }
            }
        })

        mainViewModel.loadingState.observe(viewLifecycleOwner, {
            when (it.status) {
                LoadingState.Status.FAILED ->
                    Toast.makeText(context, it.msg, Toast.LENGTH_SHORT).show()
                LoadingState.Status.RUNNING ->
                    binding.loadingLayout.visibility = View.VISIBLE
                LoadingState.Status.SUCCESS ->
                    binding.loadingLayout.visibility = View.GONE
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
