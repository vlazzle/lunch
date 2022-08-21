package com.alltrails.lunch.core

sealed class Lce<T> {
    class Initial<T> : Lce<T>()

    class Loading<T> : Lce<T>()

    data class Content<T>(val content: T) : Lce<T>()

    data class Error<T>(val throwable: Throwable) : Lce<T>()

    companion object {
        private val initial = Initial<Any>()
        private val loadingLoading = Loading<Any>()

        fun <T> initial(): Initial<T> {
            return Initial()
        }

        fun <T> loading(): Loading<T> {
            return Loading()
        }
    }
}