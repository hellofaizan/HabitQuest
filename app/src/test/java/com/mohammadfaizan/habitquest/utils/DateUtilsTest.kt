package com.mohammadfaizan.habitquest.utils

import org.junit.Assert.*
import org.junit.Test

class DateUtilsTest {

    @Test
    fun `parseReminderDays returns empty set for null or blank`() {
        assertEquals(emptySet<Int>(), DateUtils.parseReminderDays(null))
        assertEquals(emptySet<Int>(), DateUtils.parseReminderDays(""))
        assertEquals(emptySet<Int>(), DateUtils.parseReminderDays("   "))
    }

    @Test
    fun `parseReminderDays parses comma-separated days`() {
        assertEquals(setOf(1, 2, 3, 4, 5, 6, 7), DateUtils.parseReminderDays("1,2,3,4,5,6,7"))
        assertEquals(setOf(2, 4, 6), DateUtils.parseReminderDays("2,4,6"))
        assertEquals(setOf(2, 4, 6), DateUtils.parseReminderDays(" 2 , 4 , 6 "))
    }

    @Test
    fun `formatReminderDays produces sorted comma-separated output`() {
        assertEquals("1,2,3,4,5,6,7", DateUtils.formatReminderDays(setOf(7, 3, 1, 5, 2, 6, 4)))
        assertEquals("2,4,6", DateUtils.formatReminderDays(setOf(6, 2, 4)))
        assertEquals("", DateUtils.formatReminderDays(emptySet()))
    }

    @Test
    fun `parseReminderDays and formatReminderDays round-trip`() {
        val days = setOf(1, 3, 5, 7)
        assertEquals(days, DateUtils.parseReminderDays(DateUtils.formatReminderDays(days)))
    }
}
