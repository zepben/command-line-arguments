/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.commandlinearguments;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.apache.commons.cli.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@EverythingIsNonnullByDefault
@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public abstract class CmdArgsBase {

    @Nullable private Options options = null;
    @Nullable private CommandLine cmd = null;

    private boolean helpRequested = true;

    public CmdArgsBase() {
    }

    /**
     * @return The supported options.
     */
    public Options options() {
        if (options == null)
            options = createOptions();
        return options;
    }

    /**
     * @return If help has been requested.
     */
    public boolean isHelpRequested() {
        return helpRequested;
    }

    /**
     * @param args The command line args to be parsed
     * @throws ParseException if the args cannot be parsed
     */
    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        cmd = parser.parse(options(), args.clone());

        helpRequested = hasArg("h");

        if (!isHelpRequested())
            extractCustomOptions();
    }

    protected abstract void addCustomOptions(Options options);

    protected abstract void extractCustomOptions() throws ParseException;

    protected <T> T ensureOptionInitialised(@Nullable T option) {
        if (option == null)
            throw new IllegalStateException("INTERNAL ERROR: You called an option getter before you parsed the options or when help was requested.");
        return option;
    }

    protected boolean hasArg(String arg) throws ParseException {
        return cmd().hasOption(arg);
    }

    protected String getRequiredStringArg(String arg) throws ParseException {
        String value = cmd().getOptionValue(arg);
        if (value == null)
            throw new ParseException(String.format("Missing required option: %s.", arg));
        return value;
    }

    protected Optional<String> getOptionalStringArg(String arg) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredStringArg(arg)) : Optional.empty();
    }

    protected List<String> getRequiredStringArgList(String arg) throws ParseException {
        String[] values = cmd().getOptionValues(arg);
        if (values == null)
            throw new ParseException(String.format("Missing required option: %s.", arg));
        return Arrays.asList(values);
    }

    protected Optional<List<String>> getOptionalStringArgList(String arg) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredStringArgList(arg)) : Optional.empty();
    }

    protected int getRequiredIntArg(String arg) throws ParseException {
        String value = getRequiredStringArg(arg);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            throw new ParseException(String.format("Invalid integer '%s' for argument %s.", value, arg));
        }
    }

    protected int getRequiredIntArg(String arg, int minimumValue) throws ParseException {
        int value = getRequiredIntArg(arg);
        if (value < minimumValue)
            throw new ParseException(String.format("Integer %s for argument %s is out of range. Value must be at least %d.", value, arg, minimumValue));
        return value;
    }

    protected int getRequiredIntArg(String arg, int minimumValue, int maximumValue) throws ParseException {
        int value = getRequiredIntArg(arg);
        if ((value < minimumValue) || (value > maximumValue))
            throw new ParseException(String.format("Integer %s for argument %s is out of range. Expected value in range %d..%d.", value, arg, minimumValue, maximumValue));
        return value;
    }

    protected Optional<Integer> getOptionalIntArg(String arg) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredIntArg(arg)) : Optional.empty();
    }

    protected Optional<Integer> getOptionalIntArg(String arg, int minimumValue) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredIntArg(arg, minimumValue)) : Optional.empty();
    }

    protected Optional<Integer> getOptionalIntArg(String arg, int minimumValue, int maximumValue) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredIntArg(arg, minimumValue, maximumValue)) : Optional.empty();
    }

    protected LocalDate getRequiredDateArg(String arg) throws ParseException {
        String value = getRequiredStringArg(arg);

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
            throw new ParseException(String.format("Invalid date '%s' for argument %s.", value, arg));
        }
    }

    protected Optional<LocalDate> getOptionalDateArg(String arg) throws ParseException {
        return cmd().hasOption(arg) ? Optional.of(getRequiredDateArg(arg)) : Optional.empty();
    }

    private Options createOptions() {
        Options options = new Options();

        options.addOption(Option
            .builder("h")
            .longOpt("help")
            .desc("shows this help message.")
            .build());

        addCustomOptions(options);

        return options;
    }

    private CommandLine cmd() throws ParseException {
        if (cmd == null)
            throw new ParseException("You must parse the command line arguments before they can be used.");
        return cmd;
    }

}
