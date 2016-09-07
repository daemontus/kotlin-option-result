package com.github.daemontus

import org.junit.Test
import kotlin.test.assertTrue
import com.github.daemontus.Option.*
import com.github.daemontus.Result.*
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse


class OptionTest {

    @Test
    fun someIterator() {
        val some = Some(10)
        val i = some.asIterable().iterator()
        assertTrue(i.hasNext())
        assertEquals(10, i.next())
        assertFalse(i.hasNext())
        assertFailsWith<NoSuchElementException> { i.next() }
    }

    @Test
    fun noneIterator() {
        val none = None<String>()
        val i = none.asIterable().iterator()
        assertFalse(i.hasNext())
        assertFailsWith<NoSuchElementException> { i.next() }
    }

    @Test
    fun isSome() {
        assertTrue(Some(2).isSome())
        assertFalse(None<Int>().isSome())
    }

    @Test
    fun isNone() {
        assertFalse(Some(2).isNone())
        assertTrue(None<Int>().isNone())
    }

    @Test
    fun expect() {
        assertEquals(2, Some(2).expect("error"))
        assertFailsWith<IllegalStateException> {
            None<Int>().expect("error")
        }
    }

    @Test
    fun unwrap() {
        assertEquals(2, Some(2).unwrap())
        assertFailsWith<IllegalStateException> {
            None<Int>().unwrap()
        }
    }

    @Test
    fun unwrapOr() {
        assertEquals(2, Some(2).unwrapOr(4))
        assertEquals(4, None<Int>().unwrapOr(4))
    }

    @Test
    fun unwrapOrElse() {
        assertEquals(2, Some(2).unwrapOrElse { 14 })
        assertEquals(14, None<Int>().unwrapOrElse { 14 })
    }

    @Test
    fun map() {
        assertEquals(Some(4), Some(2).map { it * 2 })
        assertEquals(None(), None<Int>().map { it * 2 })
    }

    @Test
    fun mapOr() {
        assertEquals(4, Some(2).mapOr(10) { it * it })
        assertEquals(10, None<Int>().mapOr(10) { it * it })
    }

    @Test
    fun mapOrElse() {
        assertEquals(4, Some(2).mapOrElse({ 10 }, { it*it }))
        assertEquals(10, None<Int>().mapOrElse({ 10 }, { it*it }))
    }

    @Test
    fun okOr() {
        assertEquals(Ok(2), Some(2).okOr("Error"))
        assertEquals(Error("Error"), None<Int>().okOr("Error"))
    }

    @Test
    fun okOrElse() {
        assertEquals(Ok(2), Some(2).okOrElse { "Error" })
        assertEquals(Error("Error"), None<Int>().okOrElse { "Error" })
    }

    @Test
    fun and() {
        assertEquals(None(), Some(2).and(None()))
        assertEquals(None<Int>(), None<Int>().and(Some(2)))
        assertEquals(None<Int>(), None<Int>().and(None<Int>()))
        assertEquals(Some(2), Some(3).and(Some(2)))
    }

    @Test
    fun andThen() {
        fun sq(x: Int) = Some(x * x)
        val err: (Int) -> Option<Int> = { None<Int>() }
        assertEquals(Some(16), Some(2).andThen(::sq).andThen(::sq))
        assertEquals(None<Int>(), Some(2).andThen(::sq).andThen(err))
        assertEquals(None<Int>(), Some(2).andThen(err).andThen(::sq))
        assertEquals(None<Int>(), None<Int>().andThen(::sq).andThen(::sq))
    }

    @Test
    fun or() {
        assertEquals(Some(2), Some(2).or(None()))
        assertEquals(Some(2), None<Int>().or(Some(2)))
        assertEquals(None<Int>(), None<Int>().or(None<Int>()))
        assertEquals(Some(3), Some(3).or(Some(2)))
    }

    @Test
    fun orElse() {
        assertEquals(Some("foo"), Some("foo").orElse { Some("goo") })
        assertEquals(Some("foo"), Some("foo").orElse { None() })
        assertEquals(Some("goo"), None<String>().orElse { Some("goo") })
        assertEquals(None(), None<String>().orElse { None() })
    }

}