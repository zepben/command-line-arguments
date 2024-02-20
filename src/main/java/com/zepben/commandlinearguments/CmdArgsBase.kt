/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package com.zepben.commandlinearguments

import org.apache.commons.cli.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * A base class for command line parsing the Zepben way.
 *
 * @property options The options supported by the commandline argument parser.
 * @property isHelpRequested Indicates if command line help has been requested.
 */
abstract class CmdArgsBase {

    val options: Options = createOptions()
    var isHelpRequested = true; private set

    private lateinit var cmd: CommandLine

    /**
     * Parse the [args] and extract the values into the class members.
     *
     * @param args The command line args to be parsed
     */
    fun parse(args: Array<out String>) {
        cmd = DefaultParser().parse(options, args.clone())

        isHelpRequested = hasArg("h")

        if (!isHelpRequested)
            extractCustomOptions()
    }

    /**
     * Add the options you wish to make available via the commandline.
     *
     * @param options The [Options] that will be used for parsing the commandline, to which you should add your [Option] entries.
     */
    protected abstract fun addCustomOptions(options: Options)

    /**
     * Populate your public properties via the hasArg, getOptional* and getRequired* functions.
     */
    protected abstract fun extractCustomOptions()

    /**
     * Check to see if the [option] was specified via the commandline arguments.
     *
     * @param option The name of the option to check.
     *
     * @return True if [option] was specified via the commandline arguments, otherwise false.
     */
    fun hasArg(option: String): Boolean = cmd.hasOption(option)

    /**
     * Try and extract an optional string [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments, otherwise null.
     */
    fun getOptionalStringArg(option: String): String? =
        cmd.getOptionValue(option)

    /**
     * Try and extract an optional repeatable string [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The values of the [option] if it was specified via the commandline arguments, otherwise null.
     */
    fun getOptionalStringArgList(option: String): List<String>? =
        cmd.getOptionValues(option)?.toList()

    /**
     * Try and extract an optional integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments, otherwise null.
     * @throws ParseException if the value specified was not a valid integer.
     */
    fun getOptionalIntArg(option: String): Int? =
        try {
            getOptionalStringArg(option)?.toInt()
        } catch (_: NumberFormatException) {
            throw ParseException("Invalid integer '${getOptionalStringArg(option)}' for argument $option.")
        }

    /**
     * Try and extract an optional integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     * @param minimumValue The smallest valid value that can be specified for [option].
     *
     * @return The value of the [option] if it was specified via the commandline arguments, otherwise null.
     * @throws ParseException if the value specified was not a valid integer, or was below [minimumValue].
     */
    fun getOptionalIntArg(option: String, minimumValue: Int): Int? =
        getOptionalIntArg(option)?.also {
            if (it < minimumValue)
                throw ParseException("Integer $it for argument $option is out of range. Value must be at least $minimumValue.")
        }

    /**
     * Try and extract an optional integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     * @param minimumValue The smallest valid value that can be specified for [option].
     * @param maximumValue The largest valid value that can be specified for [option].
     *
     * @return The value of the [option] if it was specified via the commandline arguments, otherwise null.
     * @throws ParseException if the value specified was not a valid integer, or was outside the range specified via [minimumValue] and [maximumValue].
     */
    fun getOptionalIntArg(option: String, minimumValue: Int, maximumValue: Int): Int? =
        getOptionalIntArg(option)?.also {
            if (it !in minimumValue..maximumValue)
                throw ParseException("Integer $it for argument $option is out of range. Expected value in range $minimumValue..$maximumValue.")
        }

    /**
     * Try and extract an optional [LocalDate] [option] from the commandline arguments. The date can be in any format supported by [LocalDate.parse].
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments, otherwise null.
     * @throws ParseException if the value specified was not a valid [LocalDate].
     */
    fun getOptionalDateArg(option: String): LocalDate? =
        try {
            getOptionalStringArg(option)?.let { LocalDate.parse(it) }
        } catch (_: DateTimeParseException) {
            throw ParseException("Invalid date '${getOptionalStringArg(option)}' for argument $option.")
        }

    /**
     * Try and extract a required string [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified.
     */
    fun getRequiredStringArg(option: String): String =
        getOptionalStringArg(option) ?: throw missingOptionException(option)

    /**
     * Try and extract a required repeatable string [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The values of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified.
     */
    fun getRequiredStringArgList(option: String): List<String> =
        getOptionalStringArgList(option)?.toList() ?: throw missingOptionException(option)

    /**
     * Try and extract a required integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified, or was not a valid integer.
     */
    fun getRequiredIntArg(option: String): Int =
        getOptionalIntArg(option) ?: throw missingOptionException(option)

    /**
     * Try and extract a required integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     * @param minimumValue The smallest valid value that can be specified for [option].
     *
     * @return The value of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified, was not a valid integer, or was below [minimumValue].
     */
    fun getRequiredIntArg(option: String, minimumValue: Int): Int =
        getOptionalIntArg(option, minimumValue) ?: throw missingOptionException(option)

    /**
     * Try and extract a required integer [option] from the commandline arguments.
     *
     * @param option The name of the option to extract.
     * @param minimumValue The smallest valid value that can be specified for [option].
     * @param maximumValue The largest valid value that can be specified for [option].
     *
     * @return The value of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified, was not a valid integer, or was outside the range specified via [minimumValue] and [maximumValue].
     */
    fun getRequiredIntArg(option: String, minimumValue: Int, maximumValue: Int): Int =
        getOptionalIntArg(option, minimumValue, maximumValue) ?: throw missingOptionException(option)

    /**
     * Try and extract a required [LocalDate] [option] from the commandline arguments. The date can be in any format supported by [LocalDate.parse].
     *
     * @param option The name of the option to extract.
     *
     * @return The value of the [option] if it was specified via the commandline arguments.
     * @throws ParseException if the value was not specified, or was not a valid [LocalDate].
     */
    fun getRequiredDateArg(option: String): LocalDate =
        getOptionalDateArg(option) ?: throw missingOptionException(option)

    private fun createOptions(): Options =
        Options().apply {
            addOption(
                Option
                    .builder("h")
                    .longOpt("help")
                    .desc("shows this help message.")
                    .build()
            )
        }.also { addCustomOptions(it) }

    private fun missingOptionException(option: String): MissingOptionException =
        MissingOptionException("Missing required option: $option.")

}
