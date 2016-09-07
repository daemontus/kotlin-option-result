package com.github.daemontus

import java.util.*

/**
 * Optional value.
 */
sealed class Option<out T> {

    abstract fun asIterable(): Iterable<T>

    class None<out T> : Option<T>() {

        override fun asIterable(): Iterable<T> = object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                override fun hasNext(): Boolean = false
                override fun next(): T = throw NoSuchElementException()
            }
        }

        override fun equals(other: Any?): Boolean = other is None<*>
        override fun hashCode(): Int = 23
        override fun toString(): String = "None"
    }

    class Some<out T>(val value: T) : Option<T>() {

        override fun asIterable(): Iterable<T> = object : Iterable<T> {
            override fun iterator(): Iterator<T> = object : Iterator<T> {
                private var next = true
                override fun hasNext(): Boolean = next

                override fun next(): T {
                    return if (next) {
                        next = false
                        value
                    } else throw NoSuchElementException()
                }
            }
        }

        override fun equals(other: Any?): Boolean = other is Some<*> && other.value == this.value
        override fun hashCode(): Int = value?.hashCode() ?: 17
        override fun toString(): String = "Some($value)"
    }

}

/**
 * True if this Option is Some.
 */
fun <T> Option<T>.isSome() = this is Option.Some<T>

/**
 * True if this Option is None.
 */
fun <T> Option<T>.isNone() = this is Option.None<T>

/**
 * Unwrap option and throw IllegalStateException with specified message in case of error.
 */
fun <T> Option<T>.expect(errorMessage: String)
        = if (this is Option.Some<T>) this.value else throw IllegalStateException(errorMessage)

/**
 * Unsafely convert option to the inner value.
 */
fun <T> Option<T>.unwrap() = expect("Optional value is None")

/**
 * Safely convert option to a value, returning default if option is none.
 */
fun <T> Option<T>.unwrapOr(default: T)
        = if (this is Option.Some<T>) this.value else default

/**
 * Safely convert option to a value, calling default if option is none.
 */
inline fun <T> Option<T>.unwrapOrElse(action: () -> T)
        = if (this is Option.Some<T>) this.value else action()

/**
 * Transform optional value (if option is some)
 */
inline fun <T, U> Option<T>.map(action: (T) -> U): Option<U>
        = if (this is Option.Some<T>) Option.Some(action(this.value)) else Option.None<U>()

/**
 * Transform optional value (if option is some) or return default if option is none.
 */
inline fun <T, U> Option<T>.mapOr(default: U, action: (T) -> U) = this.map(action).unwrapOr(default)

/**
 * Transform optional value (if option is some) or call default if option is none.
 */
inline fun <T, U> Option<T>.mapOrElse(default: () -> U, action: (T) -> U) = this.map(action).unwrapOrElse(default)

/**
 * Transform optional value into Result returning specified error if option is none.
 */
fun <T, E> Option<T>.okOr(error: E): Result<T, E>
        = if (this is Option.Some<T>) Result.Ok(this.value) else Result.Error(error)

/**
 * Transform optional value into Result calling specified error if option is none.
 */
inline fun <T, E> Option<T>.okOrElse(error: () -> E): Result<T, E>
        = if (this is Option.Some<T>) Result.Ok(this.value) else Result.Error(error())

/**
 * If option is None, return None, otherwise return optionB.
 */
fun <T> Option<T>.and(optionB: Option<T>): Option<T>
        = if (this.isSome()) optionB else this

/**
 * Returns None if the option is None, otherwise calls f with the wrapped value and returns the result. (Flat Map)
 */
inline fun <T, U> Option<T>.andThen(action: (T) -> Option<U>): Option<U>
        = if (this is Option.Some<T>) action(this.value) else Option.None()

/**
 * Returns the option if it contains a value, otherwise returns optionB.
 */
fun <T> Option<T>.or(optionB: Option<T>): Option<T>
        = if (this.isSome()) this else optionB

/**
 * Returns the option if it contains a value, otherwise calls f and returns the result.
 */
inline fun <T> Option<T>.orElse(action: () -> Option<T>): Option<T>
        = if (this.isSome()) this else action()