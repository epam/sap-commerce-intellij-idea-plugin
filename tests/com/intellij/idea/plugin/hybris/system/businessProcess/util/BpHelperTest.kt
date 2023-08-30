package com.intellij.idea.plugin.hybris.system.businessProcess.util

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals


class BpHelperTest {
    private val helper = BpHelper

    @Test
    fun test_parseDuration_allTokensParsed() {
        val result = helper.parseDuration("P2Y6M5DT12H35M30S")

        assertEquals("2 years 6 months 5 days 12 hours 35 minutes 30 seconds", result)
    }

    @Test
    fun test_parseDuration_allTokensParsed_noPlural() {
        val result = helper.parseDuration("P1Y1M1DT1H1M1S")

        assertEquals("1 year 1 month 1 day 1 hour 1 minute 1 second", result)
    }

    @Test
    fun test_parseDuration_dayToken_hourToken() {
        val result = helper.parseDuration("P1DT2H")

        assertEquals("1 day 2 hours", result)
    }

    @Test
    fun test_parseDuration_monthToken() {
        val result = helper.parseDuration("P20M")

        assertEquals("20 months", result)
    }

    @Test
    fun test_parseDuration_minuteToken() {
        val result = helper.parseDuration("PT20M")

        assertEquals("20 minutes", result)
    }

    @Test
    fun test_parseDuration_zeroYearToken_monthToken_zeroDayToken() {
        val result = helper.parseDuration("P0Y20M0D")

        assertEquals("20 months", result)
    }

    @Test
    @Ignore
    fun test_parseDuration_zeroYearTokenOnly() {
        val result = helper.parseDuration("P0Y")

        assertEquals("0 years", result)
    }

    @Test
    fun test_parseDuration_yearToken() {
        val result = helper.parseDuration("P12Y")

        assertEquals("12 years", result)
    }

    @Test
    fun test_parseDuration_daysToken() {
        val result = helper.parseDuration("P60D")

        assertEquals("60 days", result)
    }

    @Test
    fun test_parseDuration_minuteToken_secondToken() {
        val result = helper.parseDuration("PT1M30.5S")

        assertEquals("1 minute 30.5 seconds", result)
    }

    @Test
    fun test_parseDuration_hourToken_minuteToken_secondToken() {
        val result = helper.parseDuration("PT30H1M30.5S")

        assertEquals("30 hours 1 minute 30.5 seconds", result)
    }
}