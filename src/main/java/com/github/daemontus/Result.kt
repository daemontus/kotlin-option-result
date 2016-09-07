package com.github.daemontus

import java.util.*

sealed class Result<out T, E> {

    abstract fun asIterable(): Iterable<T>

    class Ok<out T, E>(val ok: T) : Result<T, E>() {

        override fun asIterable(): Iterable<T> = object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                private var next: Boolean = true
                override fun hasNext(): Boolean = next
                override fun next(): T = if (next) {
                    next = false
                    ok
                } else throw NoSuchElementException()
            }
        }

        override fun equals(other: Any?): Boolean = other is Ok<*, *> && other.ok == this.ok
        override fun hashCode(): Int = ok?.hashCode() ?: 17
        override fun toString(): String = "Ok($ok)"
    }

    class Error<out T, E>(val error: E) : Result<T, E>() {

        override fun asIterable(): Iterable<T> = object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                override fun hasNext(): Boolean = false
                override fun next(): T = throw NoSuchElementException()
            }
        }

        override fun equals(other: Any?): Boolean = other is Error<*,*> && other.error == this.error
        override fun hashCode(): Int = error?.hashCode() ?: 23
        override fun toString(): String = "Error($error)"
    }

}

/**
 * Unwraps a result, yielding the content of an Err.
 * Throws IllegalStateException if the value is an Ok, with a custom message provided by the Ok's value.
 */
fun <T, E> Result<T, E>.unwrapError(): E = when (this) {
    is Result.Ok<T,E> -> throw IllegalStateException(this.toString())
    is Result.Error<T,E> -> this.error
}

/**
 * Unwraps a result, yielding the content of an Ok.
 * Throws IllegalStateException if the value is an Err, with a custom message provided by the Err's value.
 */
fun <T, E> Result<T, E>.unwrap(): T = when (this) {
    is Result.Ok<T,E> -> this.ok
    is Result.Error<T,E> -> throw IllegalStateException(this.toString())
}

/**
 * Unwraps a result, yielding the content of an Ok.
 * Throws IllegalStateException if the value is an Err, with a custom message provided by the Err's value.
 */
fun <T, E> Result<T, E>.expect(message: String): T = when (this) {
    is Result.Ok<T,E> -> this.ok
    is Result.Error<T,E> -> throw IllegalStateException(message)
}

fun <T, E> Result<T, E>.isOk() = this is Result.Ok<T, E>
fun <T, E> Result<T, E>.isError() = this is Result.Error<T, E>

/**
 * Converts from Result<T, E> to Option<T>.
 * Converts self into an Option<T>, consuming self, and discarding the error, if any.
 */
fun <T, E> Result<T, E>.ok(): Option<T> = if (this is Result.Ok<T, E>) Option.Some(this.ok) else Option.None()

/**
 * Converts from Result<T, E> to Option<E>.
 * Converts self into an Option<E>, consuming self, and discarding the value, if any.
 */
fun <T, E> Result<T, E>.error(): Option<E> = if (this is Result.Error<T, E>) Option.Some(this.error) else Option.None()

/**
 * Maps a Result<T, E> to Result<U, E> by applying a function to a contained Ok value, leaving an Err value untouched.
 * This function can be used to compose the results of two functions.
 */
inline fun <T, U, E> Result<T, E>.map(action: (T) -> U): Result<U, E> = when (this) {
    is Result.Ok<T, E> -> Result.Ok(action(this.ok))
    is Result.Error<T, E> -> Result.Error(this.error)
}

/**
 * Maps a Result<T, E> to Result<T, F> by applying a function to a contained Err value, leaving an Ok value untouched.
 * This function can be used to pass through a successful result while handling an error.
 */
inline fun <T, E1, E2> Result<T, E1>.mapError(action: (E1) -> E2): Result<T, E2> = when (this) {
    is Result.Ok<T, E1> -> Result.Ok(this.ok)
    is Result.Error<T, E1> -> Result.Error(action(this.error))
}

/**
 * Returns resultB if the result is Ok, otherwise returns the Err value of self.
 */
fun <T, U, E> Result<T, E>.and(resultB: Result<U, E>): Result<U, E> = when (this) {
    is Result.Ok<T, E> -> resultB
    is Result.Error<T, E> -> Result.Error(this.error)
}

/**
 * Returns resultB if the result is Ok, otherwise returns the Err value of self.
 */
inline fun <T, U, E> Result<T, E>.andThen(resultB: (T) -> Result<U, E>): Result<U, E> = when (this) {
    is Result.Ok<T, E> -> resultB(this.ok)
    is Result.Error<T, E> -> Result.Error(this.error)
}

/**
 * Returns resultB if the result is Err, otherwise returns the Ok value of self.
 */
fun <T, E1, E2> Result<T, E1>.or(resultB: Result<T, E2>): Result<T, E2> = when (this) {
    is Result.Ok<T, E1> -> Result.Ok(this.ok)
    is Result.Error<T, E1> -> resultB
}

/**
 * Returns resultB if the result is Err, otherwise returns the Ok value of self.
 */
inline fun <T, E1, E2> Result<T, E1>.orElse(resultB: (E1) -> Result<T, E2>): Result<T, E2> = when (this) {
    is Result.Ok<T, E1> -> Result.Ok(this.ok)
    is Result.Error<T, E1> -> resultB(this.error)
}

/**
 * Unwraps a result, yielding the content of an Ok. Else it returns default.
 */
fun <T, E> Result<T, E>.unwrapOr(default: T) = when (this) {
    is Result.Ok<T, E> -> this.ok
    is Result.Error<T, E> -> default
}

/**
 * Unwraps a result, yielding the content of an Ok. Else it returns default.
 */
fun <T, E> Result<T, E>.unwrapOrElse(default: (E) -> T) = when (this) {
    is Result.Ok<T, E> -> this.ok
    is Result.Error<T, E> -> default(this.error)
}