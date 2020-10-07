/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.commandlinearguments;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.zepben.testutils.exception.ExpectException.expect;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

public class CmdArgsBaseTest {

    private final TestCmdArgs cmdArgs = spy(new TestCmdArgs());

    @Test
    public void options() {
        @EverythingIsNonnullByDefault
        CmdArgsBase cmdArgsBase = new CmdArgsBase() {
            @Override
            protected void addCustomOptions(Options options) {
            }

            @Override
            protected void extractCustomOptions() {
            }
        };

        assertThat(cmdArgsBase.options().getOptions().size(), equalTo(1));
        assertThat(cmdArgsBase.options().getOption("h"), notNullValue());

        assertThat(cmdArgs.options().getOptions().size(), equalTo(3));
        assertThat(cmdArgs.options().getOption("h"), notNullValue());
        assertThat(cmdArgs.options().getOption("a"), notNullValue());
        assertThat(cmdArgs.options().getOption("b"), notNullValue());
    }

    @Test
    public void extractsCustomOptionsCalledOnlyWithNoHelpRequested() throws Exception {
        verify(cmdArgs, never()).extractCustomOptions();

        cmdArgs.parse(new String[]{"-h"});

        verify(cmdArgs, never()).extractCustomOptions();

        cmdArgs.parse(new String[]{""});

        verify(cmdArgs, timeout(1)).extractCustomOptions();
    }

    @Test
    public void supportsHelp() throws Exception {
        cmdArgs.parse(new String[]{"-h"});
        assertThat(cmdArgs.isHelpRequested(), equalTo(true));

        cmdArgs.parse(new String[]{});
        assertThat(cmdArgs.isHelpRequested(), equalTo(false));
    }

    @Test
    public void ensureOptionInitialisedWorks() {
        cmdArgs.ensureOptionInitialised("value");

        expect(() -> cmdArgs.ensureOptionInitialised(null))
            .toThrow(IllegalStateException.class)
            .withMessage("INTERNAL ERROR: You called an option getter before you parsed the options or when help was requested.");
    }

    @Test
    public void hasArgsHelperWorks() throws Exception {
        parseArgs("abc");

        assertThat(cmdArgs.hasArg("a"), equalTo(true));
        assertThat(cmdArgs.hasArg("b"), equalTo(true));
        assertThat(cmdArgs.hasArg("c"), equalTo(false));
    }

    @Test
    public void getRequiredStringArgsHelperWorks() throws Exception {
        parseArgs("abc");

        assertThat(cmdArgs.getRequiredStringArg("a"), equalTo("abc"));
        assertThat(cmdArgs.getRequiredStringArg("b"), equalTo("123"));
        expect(() -> cmdArgs.getRequiredStringArg("c"))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getOptionalStringArgsHelperWorks() throws Exception {
        parseArgs("abc");

        assertThat(cmdArgs.getOptionalStringArg("a"), isPresentAnd(equalTo("abc")));
        assertThat(cmdArgs.getOptionalStringArg("b"), isPresentAnd(equalTo("123")));
        assertThat(cmdArgs.getOptionalStringArg("c"), isEmpty());
    }

    @Test
    public void getRequiredArgsListHelperWorks() throws Exception {
        parseArgs("abc");

        assertThat(cmdArgs.getRequiredStringArgList("a"), contains("abc"));
        assertThat(cmdArgs.getRequiredStringArgList("b"), contains("123", "456"));
        expect(() -> cmdArgs.getRequiredStringArgList("c"))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getOptionalArgsListHelperWorks() throws Exception {
        parseArgs("abc");

        assertThat(cmdArgs.getOptionalStringArgList("a"), isPresentAnd(contains("abc")));
        assertThat(cmdArgs.getOptionalStringArgList("b"), isPresentAnd(contains("123", "456")));
        assertThat(cmdArgs.getOptionalStringArgList("c"), isEmpty());
    }

    @Test
    public void getRequiredIntArgsHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getRequiredIntArg("a"))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getRequiredIntArg("b"), equalTo(123));
        expect(() -> cmdArgs.getRequiredIntArg("c"))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getRequiredIntArgsWithMinHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getRequiredIntArg("a", 200))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getRequiredIntArg("b", 100), equalTo(123));
        expect(() -> cmdArgs.getRequiredIntArg("b", 200))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Value must be at least 200.");
        expect(() -> cmdArgs.getRequiredIntArg("c", 200))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getRequiredIntArgsWithMinMaxHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getRequiredIntArg("a", 100, 120))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getRequiredIntArg("b", 100, 200), equalTo(123));
        expect(() -> cmdArgs.getRequiredIntArg("b", 200, 220))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 200..220.");
        expect(() -> cmdArgs.getRequiredIntArg("b", 100, 120))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 100..120.");
        expect(() -> cmdArgs.getRequiredIntArg("c", 100, 120))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getOptionalIntArgsHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getOptionalIntArg("a"))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getOptionalIntArg("b"), isPresentAnd(equalTo(123)));
        assertThat(cmdArgs.getOptionalIntArg("c"), isEmpty());
    }

    @Test
    public void getOptionalIntArgsWithMinHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getOptionalIntArg("a", 200))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getOptionalIntArg("b", 100), isPresentAnd(equalTo(123)));
        expect(() -> cmdArgs.getOptionalIntArg("b", 200))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Value must be at least 200.");
        assertThat(cmdArgs.getOptionalIntArg("c"), isEmpty());
    }

    @Test
    public void getOptionalIntArgsWithMinMaxHelperWorks() throws Exception {
        parseArgs("abc");

        expect(() -> cmdArgs.getOptionalIntArg("a", 100, 120))
            .toThrow(ParseException.class)
            .withMessage("Invalid integer 'abc' for argument a.");
        assertThat(cmdArgs.getOptionalIntArg("b", 100, 200), isPresentAnd(equalTo(123)));
        expect(() -> cmdArgs.getOptionalIntArg("b", 200, 220))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 200..220.");
        expect(() -> cmdArgs.getOptionalIntArg("b", 100, 120))
            .toThrow(ParseException.class)
            .withMessage("Integer 123 for argument b is out of range. Expected value in range 100..120.");
        assertThat(cmdArgs.getOptionalIntArg("c"), isEmpty());
    }

    @Test
    public void getRequiredDateArgsHelperWorks() throws Exception {
        parseArgs("2018-12-03");

        assertThat(cmdArgs.getRequiredDateArg("a"), equalTo(LocalDate.of(2018, 12, 3)));
        expect(() -> cmdArgs.getRequiredDateArg("b"))
            .toThrow(ParseException.class)
            .withMessage("Invalid date '123' for argument b.");
        expect(() -> cmdArgs.getRequiredDateArg("c"))
            .toThrow(ParseException.class)
            .withMessage("Missing required option: c.");
    }

    @Test
    public void getOptionalDateArgsHelperWorks() throws Exception {
        parseArgs("2018-12-03");

        assertThat(cmdArgs.getOptionalDateArg("a"), isPresentAnd(equalTo(LocalDate.of(2018, 12, 3))));
        expect(() -> cmdArgs.getRequiredDateArg("b"))
            .toThrow(ParseException.class)
            .withMessage("Invalid date '123' for argument b.");
        assertThat(cmdArgs.getOptionalDateArg("c"), isEmpty());
    }

    private void parseArgs(String argA) throws Exception {
        cmdArgs.parse(new String[]{"-a", argA, "-b", "123", "-b", "456"});
    }

}
