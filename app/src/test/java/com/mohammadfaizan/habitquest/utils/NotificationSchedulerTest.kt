package com.mohammadfaizan.habitquest.utils

import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationSchedulerTest {

    @Test
    fun `parseTime returns null for null or blank input`() {
        assertNull(NotificationScheduler.parseTime(null))
        assertNull(NotificationScheduler.parseTime(""))
        assertNull(NotificationScheduler.parseTime("   "))
    }

    @Test
    fun `parseTime parses valid HH-mm strings`() {
        assertEquals(Pair(9, 0), NotificationScheduler.parseTime("09:00"))
        assertEquals(Pair(23, 59), NotificationScheduler.parseTime("23:59"))
        assertEquals(Pair(0, 0), NotificationScheduler.parseTime("00:00"))
        assertEquals(Pair(9, 5), NotificationScheduler.parseTime("9:5"))
    }

    @Test
    fun `parseTime rejects out-of-range or malformed strings`() {
        assertNull(NotificationScheduler.parseTime("24:00"))
        assertNull(NotificationScheduler.parseTime("09:60"))
        assertNull(NotificationScheduler.parseTime("-1:00"))
        assertNull(NotificationScheduler.parseTime("09"))
        assertNull(NotificationScheduler.parseTime("09:00:00"))
        assertNull(NotificationScheduler.parseTime("abc"))
    }

    @Test
    fun `calculateInitialDelay schedules a future time today within the next few minutes`() {
        val now = Calendar.getInstance()
        // Avoid the rare flaky window where "2 minutes from now" rolls past midnight.
        assumeTrue(now.get(Calendar.HOUR_OF_DAY) != 23 || now.get(Calendar.MINUTE) < 57)

        val target = (now.clone() as Calendar).apply { add(Calendar.MINUTE, 2) }
        val hour = target.get(Calendar.HOUR_OF_DAY)
        val minute = target.get(Calendar.MINUTE)

        val delay = NotificationScheduler.calculateInitialDelay(hour, minute)

        assertTrue(delay > 0)
        assertTrue(delay <= TimeUnit.MINUTES.toMillis(3))
    }

    @Test
    fun `calculateInitialDelay rolls over to tomorrow when the time has already passed today`() {
        val past = Calendar.getInstance().apply { add(Calendar.MINUTE, -5) }
        val hour = past.get(Calendar.HOUR_OF_DAY)
        val minute = past.get(Calendar.MINUTE)

        val delay = NotificationScheduler.calculateInitialDelay(hour, minute)

        // Whether this rolls to tomorrow or (near midnight) resolves later today,
        // the target is always ~5 minutes before a full day away, and never in the past.
        val expectedApprox = TimeUnit.DAYS.toMillis(1) - TimeUnit.MINUTES.toMillis(5)
        assertTrue(delay > expectedApprox - TimeUnit.MINUTES.toMillis(2))
        assertTrue(delay <= TimeUnit.DAYS.toMillis(1))
    }

    @Test
    fun `calculateInitialDelayForDay resolves to a moment that actually falls on the target day of week`() {
        val now = Calendar.getInstance()
        val targetDayOfWeek = ((now.get(Calendar.DAY_OF_WEEK) - 1 + 3) % 7) + 1

        val delay = NotificationScheduler.calculateInitialDelayForDay(targetDayOfWeek, 10, 0)

        assertTrue(delay > 0)
        assertTrue(delay <= TimeUnit.DAYS.toMillis(7))

        val resolved = Calendar.getInstance().apply { timeInMillis = now.timeInMillis + delay }
        assertEquals(targetDayOfWeek, resolved.get(Calendar.DAY_OF_WEEK))
        assertEquals(10, resolved.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resolved.get(Calendar.MINUTE))
    }

    @Test
    fun `calculateInitialDelayForDay schedules today when target day is today and time is upcoming`() {
        val now = Calendar.getInstance()
        assumeTrue(now.get(Calendar.HOUR_OF_DAY) != 23 || now.get(Calendar.MINUTE) < 57)

        val target = (now.clone() as Calendar).apply { add(Calendar.MINUTE, 2) }
        val delay = NotificationScheduler.calculateInitialDelayForDay(
            now.get(Calendar.DAY_OF_WEEK),
            target.get(Calendar.HOUR_OF_DAY),
            target.get(Calendar.MINUTE)
        )

        assertTrue(delay > 0)
        assertTrue(delay <= TimeUnit.MINUTES.toMillis(3))
    }

    @Test
    fun `calculateInitialDelayForDay rolls a full week when target day is today but time already passed`() {
        val now = Calendar.getInstance()
        val past = (now.clone() as Calendar).apply { add(Calendar.MINUTE, -5) }

        val delay = NotificationScheduler.calculateInitialDelayForDay(
            now.get(Calendar.DAY_OF_WEEK),
            past.get(Calendar.HOUR_OF_DAY),
            past.get(Calendar.MINUTE)
        )

        val expectedApprox = TimeUnit.DAYS.toMillis(7) - TimeUnit.MINUTES.toMillis(5)
        assertTrue(delay > expectedApprox - TimeUnit.MINUTES.toMillis(2))
        assertTrue(delay <= TimeUnit.DAYS.toMillis(7))
    }
}
