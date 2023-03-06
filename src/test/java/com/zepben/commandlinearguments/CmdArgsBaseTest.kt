/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.commandlinearguments

import com.zepben.testutils.exception.ExpectException.Companion.expect
import io.mockk.spyk
import io.mockk.verify
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CmdArgsBaseTest {

    private val cmdArgs = spyk(
        object : CmdArgsBase() {

            override fun addCustomOptions(options: Options) {
                options.addOption("a", true, "")
                options.addOption(Option.builder("b").hasArgs().desc("").build())
            }

            public override fun extractCustomOptions() {}

        }
    )

    @Test
    fun options() {
        assertThat(cmdArgs.options.options.size, equalTo(3))

        assertThat(cmdArgs.options.getOption("h"), notNullValue())
        assertThat(cmdArgs.options.getOption("a"), notNullValue())
        assertThat(cmdArgs.options.getOption("b"), notNullValue())
    }

    @Test
    fun extractsCustomOptionsCalledOnlyWithNoHelpRequested() {
        cmdArgs.parse(arrayOf("-h")).also {
            verify(exactly = 0) { cmdArgs.extractCustomOptions() }
        }

        cmdArgs.parse(arrayOf("")).also {
            verify(exactly = 1, timeout = 1) { cmdArgs.extractCustomOptions() }
        }
    }

    @Test
    fun supportsHelp() {
        cmdArgs.parse(arrayOf("-h")).also {
            assertThat(cmdArgs.isHelpRequested, equalTo(true))
        }

        cmdArgs.parse(arrayOf()).also {
            assertThat(cmdArgs.isHelpRequested, equalTo(false))
        }
    }

    @Test
    fun `hasArgs works`() {
        parseArgs("abc")

        assertThat(cmdArgs.hasArg("a"), equalTo(true))
        assertThat(cmdArgs.hasArg("b"), equalTo(true))
        assertThat(cmdArgs.hasArg("c"), equalTo(false))

        cmdArgs.parse(arrayOf())

        assertThat(cmdArgs.hasArg("a"), equalTo(false))
    }

    @Test
    internal fun `getOptionalStringArg works`() {
        parseArgs("abc")

        assertThat(cmdArgs.getOptionalStringArg("a"), equalTo("abc"))
        assertThat(cmdArgs.getOptionalStringArg("b"), equalTo("123"))
        assertThat(cmdArgs.getOptionalStringArg("c"), nullValue())

        cmdArgs.parse(arrayOf())
        assertThat(cmdArgs.getOptionalStringArg("a"), nullValue())
    }


    @Test
    internal fun `getOptionalStringArgList works`() {
        parseArgs("abc")

        assertThat(cmdArgs.getOptionalStringArgList("a"), contains("abc"))
        assertThat(cmdArgs.getOptionalStringArgList("b"), contains("123", "456"))
        assertThat(cmdArgs.getOptionalStringArgList("c"), nullValue())

        cmdArgs.parse(arrayOf())
        assertThat(cmdArgs.getOptionalStringArgList("a"), nullValue())
    }


    @Test
    internal fun `getOptionalIntArg works`() {
        parseArgs("abc")

        expect { cmdArgs.getOptionalIntArg("a") }
            .toThrow<ParseException>()
            .withMessage("Invalid integer 'abc' for argument a.")

        assertThat(cmdArgs.getOptionalIntArg("b"), equalTo(123))

        assertThat(cmdArgs.getOptionalIntArg("b", 100), equalTo(123))
        expect { cmdArgs.getOptionalIntArg("b", 200) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Value must be at least 200.")

        assertThat(cmdArgs.getOptionalIntArg("b", 100, 200), equalTo(123))
        expect { cmdArgs.getOptionalIntArg("b", 200, 220) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 200..220.")
        expect { cmdArgs.getOptionalIntArg("b", 100, 120) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 100..120.")

        assertThat(cmdArgs.getOptionalIntArg("c"), nullValue())
        assertThat(cmdArgs.getOptionalIntArg("c", 100), nullValue())
        assertThat(cmdArgs.getOptionalIntArg("c", 100, 200), nullValue())
    }


    @Test
    internal fun `getOptionalDateArg works`() {
        parseArgs("2018-12-03")

        assertThat(cmdArgs.getOptionalDateArg("a"), equalTo(LocalDate.of(2018, 12, 3)))

        expect { cmdArgs.getRequiredDateArg("b") }
            .toThrow<ParseException>()
            .withMessage("Invalid date '123' for argument b.")

        assertThat(cmdArgs.getOptionalDateArg("c"), nullValue())
    }

    @Test
    internal fun `getRequiredStringArg works`() {
        parseArgs("abc")

        assertThat(cmdArgs.getRequiredStringArg("a"), equalTo("abc"))
        assertThat(cmdArgs.getRequiredStringArg("b"), equalTo("123"))

        expect { cmdArgs.getRequiredStringArg("c") }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
    }

    @Test
    internal fun `getRequiredStringArgList works`() {
        parseArgs("abc")

        assertThat(cmdArgs.getRequiredStringArgList("a"), contains("abc"))
        assertThat(cmdArgs.getRequiredStringArgList("b"), contains("123", "456"))

        expect { cmdArgs.getRequiredStringArgList("c") }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
    }

    @Test
    internal fun `getRequiredIntArg works`() {
        parseArgs("abc")

        expect { cmdArgs.getRequiredIntArg("a") }
            .toThrow<ParseException>()
            .withMessage("Invalid integer 'abc' for argument a.")

        assertThat(cmdArgs.getRequiredIntArg("b"), equalTo(123))

        assertThat(cmdArgs.getRequiredIntArg("b", 100), equalTo(123))
        expect { cmdArgs.getRequiredIntArg("b", 200) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Value must be at least 200.")

        assertThat(cmdArgs.getRequiredIntArg("b", 100, 200), equalTo(123))
        expect { cmdArgs.getRequiredIntArg("b", 200, 220) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 200..220.")
        expect { cmdArgs.getRequiredIntArg("b", 100, 120) }
            .toThrow<ParseException>()
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 100..120.")

        expect { cmdArgs.getRequiredIntArg("c") }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
        expect { cmdArgs.getRequiredIntArg("c", 100) }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
        expect { cmdArgs.getRequiredIntArg("c", 100, 200) }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
    }

    @Test
    internal fun getRequiredDateArgsHelperWorks() {
        parseArgs("2018-12-03")

        assertThat(cmdArgs.getRequiredDateArg("a"), equalTo(LocalDate.of(2018, 12, 3)))

        expect { cmdArgs.getRequiredDateArg("b") }
            .toThrow<ParseException>()
            .withMessage("Invalid date '123' for argument b.")

        expect { cmdArgs.getRequiredDateArg("c") }
            .toThrow<ParseException>()
            .withMessage("Missing required option: c.")
    }

    private fun parseArgs(argA: String) {
        cmdArgs.parse(arrayOf("-a", argA, "-b", "123", "-b", "456"))
    }

}
