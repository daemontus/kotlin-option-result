package com.github.daemontus

import com.github.daemontus.Option.*
import com.github.daemontus.Result.*
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun okIterator() {
        val ok = Ok<Int, String>(2)
        val i = ok.asIterable().iterator()
        assertTrue(i.hasNext())
        assertEquals(2, i.next())
        assertFalse(i.hasNext())
        assertFailsWith<NoSuchElementException> { i.next() }
    }

    @Test
    fun errorIterator() {
        val error = Error<Int, String>("error")
        val i = error.asIterable().iterator()
        assertFalse(i.hasNext())
        assertFailsWith<NoSuchElementException> { i.next() }
    }

    @Test
    fun unwrapError() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals("error", error.unwrapError())
        assertFailsWith<IllegalStateException> {
            ok.unwrapError()
        }
    }

    @Test
    fun unwrap() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(2, ok.unwrap())
        assertFailsWith<IllegalStateException> {
            error.unwrap()
        }
    }

    @Test
    fun expect() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(2, ok.expect("custom message"))
        assertFailsWith<IllegalStateException> {
            error.expect("custom message")
        }
    }

    @Test
    fun isOk() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertTrue(ok.isOk())
        assertFalse(error.isOk())
    }

    @Test
    fun isError() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertTrue(error.isError())
        assertFalse(ok.isError())
    }

    @Test
    fun ok() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(Some(2), ok.ok())
        assertEquals(None<Int>(), error.ok())
    }

    @Test
    fun error() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(None<String>(), ok.error())
        assertEquals(Some("error"), error.error())
    }

    @Test
    fun map() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(Ok<String, String>("Value: 2"), ok.map { "Value: $it" })
        assertEquals(Error<String, String>("error"), error.map { "Value: $it" })
    }

    @Test
    fun mapError() {
        val ok = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        assertEquals(Ok<Int, Int>(2), ok.mapError { it.length })
        assertEquals(Error<Int, Int>(5), error.mapError { it.length })
    }

    @Test
    fun and() {
        val ok = Ok<Int, String>(2)
        val ok2 = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        val error2 = Error<Int, String>("error2")
        assertEquals(error, ok.and(error))
        assertEquals(error, error.and(ok))
        assertEquals(error, error.and(error2))
        assertEquals(ok2, ok.and(ok2))
    }

    @Test
    fun andThen() {
        fun sq(x: Int) = Ok<Int, Int>(x * x)
        fun err(x: Int) = Error<Int, Int>(x)
        assertEquals(Ok<Int, Int>(16), Ok<Int, Int>(2).andThen(::sq).andThen(::sq))
        assertEquals(Error<Int, Int>(4), Ok<Int, Int>(2).andThen(::sq).andThen(::err))
        assertEquals(Error<Int, Int>(2), Ok<Int, Int>(2).andThen(::err).andThen(::sq))
        assertEquals(Error<Int,Int>(3), Error<Int, Int>(3).andThen(::sq).andThen(::sq))
    }

    @Test
    fun or() {
        val ok = Ok<Int, String>(2)
        val ok2 = Ok<Int, String>(2)
        val error = Error<Int, String>("error")
        val error2 = Error<Int, String>("error2")
        assertEquals(ok, ok.or(error))
        assertEquals(ok, error.or(ok))
        assertEquals(error2, error.or(error2))
        assertEquals(ok, ok.or(ok2))
    }

    @Test
    fun orElse() {
        fun sq(x: Int) = Ok<Int, Int>(x * x)
        fun err(x: Int) = Error<Int, Int>(x)
        assertEquals(Ok<Int, Int>(2), Ok<Int, Int>(2).orElse(::sq).orElse(::sq))
        assertEquals(Ok<Int, Int>(2), Ok<Int, Int>(2).orElse(::err).orElse(::sq))
        assertEquals(Ok<Int, Int>(9), Error<Int, Int>(3).orElse(::sq).orElse(::err))
        assertEquals(Error<Int,Int>(3), Error<Int, Int>(3).orElse(::err).orElse(::err))
    }

    @Test
    fun unwrapOr() {
        assertEquals(2, Ok<Int, String>(2).unwrapOr(9))
        assertEquals(9, Error<Int, String>("foo").unwrapOr(9))
    }

    @Test
    fun unwrapOrElse() {
        assertEquals(2, Ok<Int, String>(2).unwrapOrElse { 10 })
        assertEquals(3, Error<Int, String>("foo").unwrapOrElse { it.length })
    }

}