package com.example.newsaggregator.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.newsaggregator.AppModule.RssFeed
import com.example.newsaggregator.data.rss.dto.RssDto
//import com.example.newsaggregator.domain.ConnectivityUseCase
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

    val useCase: UseCase,
  //  val connectivityUseCase: ConnectivityUseCase
) : ViewModel() {



    private val _rssFeed = MutableStateFlow<RssDto?>(null)
    val rssFeed: StateFlow<RssDto?> = _rssFeed

    private val _state = MutableStateFlow<State>(State.ColdStart)
    val state = _state.asStateFlow()

   // private val isConnect = connectivityUseCase.isConnected as StateFlow<Boolean>


    init {
        viewModelScope.launch {
            loadRssFeed()
            //start()
        }

    }


    fun loadRssFeed() {
        viewModelScope.launch {

            try {
                _rssFeed.value = useCase.getData()
            } catch (e: Exception) {
                // Обработка ошибок
            }
        }
    }

   /* fun start() {
        viewModelScope.launch {
            _state.value = State.Wait
            isConnect.collect { isOnline ->
                if(isOnline){
                    delay(500)
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
                }else{
                    _state.value = State.Error
                }

            }

    }
    }*/

}