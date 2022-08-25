package com.alltrails.lunch.core

/**
 * Initial (have not started loading yet)
 *    |
 *    |
 *   \/
 * Loading (started loading)
 *     /       \
 *    |        |
 *   \/       \/
 * Content | Error
 */
sealed class Lce<T> {
    class Initial<T> : Lce<T>()

    class Loading<T>(val oldContent: T? = null) : Lce<T>()

    data class Content<T>(val content: T) : Lce<T>()

    data class Error<T>(val throwable: Throwable) : Lce<T>()

    companion object {
        fun <T> initial(): Initial<T> {
            return Initial()
        }

        fun <T> loading(): Loading<T> {
            return Loading()
        }
    }
}