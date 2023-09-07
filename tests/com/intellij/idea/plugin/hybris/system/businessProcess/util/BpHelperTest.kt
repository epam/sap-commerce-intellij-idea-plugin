/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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