/*
 * Copyright 2020 Zeppelin Bend Pty Ltd
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.zepben.commandlinearguments;

import com.zepben.annotations.EverythingIsNonnullByDefault;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

@EverythingIsNonnullByDefault
class TestCmdArgs extends CmdArgsBase {

    @Override
    protected void addCustomOptions(Options options) {
        options.addOption("a", true, "");
        options.addOption(Option.builder("b").hasArgs().desc("").build());
    }

    @Override
    protected void extractCustomOptions() {
    }

}
