package com.example.newsaggregator.ui

import android.util.Log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.example.newsaggregator.data.rss.dto.RssDto
import com.example.newsaggregator.domain.ConnectivityUseCase

import com.example.newsaggregator.domain.UseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@OptIn(FlowPreview::class)
class ViewModel @Inject constructor(

    private val useCase: UseCase,
    private val connectivityUseCase: ConnectivityUseCase
) : ViewModel() {


    private val _rssFeed = MutableStateFlow<RssDto?>(null)
    val rssFeed: StateFlow<RssDto?> = _rssFeed

    private val _state = MutableStateFlow<State>(State.ColdStart)
    val state = _state.asStateFlow()

    private val isConnect = connectivityUseCase.isConnected as StateFlow<Boolean>


    init {
        viewModelScope.launch {
            start()
        }

    }


    fun loadRssFeed() {
        viewModelScope.launch {

            try {
                _rssFeed.value = useCase.getData()
            } catch (e: Exception) {
                _state.value = State.Error
            }
        }
    }

    fun start() {
        viewModelScope.launch {
            loadRssFeed()
            _state.value = State.Wait
            isConnect.collect { isOnline ->
                if (isOnline) {
                    _state.value = State.Wait
                    runCatching {
                        _rssFeed.value = useCase.getData()
                        _state.value = State.Wait

                    }.fold(
                        onSuccess = { response ->
                            _state.value = State.Wait
                            loadRssFeed()
                            Log.e("TAG", "$response, ${_state.value}")
                            _state.value = State.Completed

                        },
                        onFailure = {
                            _state.value = State.Error
                        }
                    )
                } else {
                    delay(5000)
                    _state.value = State.Error
                }

            }

        }
    }

}