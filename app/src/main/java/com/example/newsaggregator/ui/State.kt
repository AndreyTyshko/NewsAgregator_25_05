package com.example.newsaggregator.ui

sealed class State{
    object ColdStart:State()
    object Completed:State()
    object Wait:State()
    object Error:State()
}