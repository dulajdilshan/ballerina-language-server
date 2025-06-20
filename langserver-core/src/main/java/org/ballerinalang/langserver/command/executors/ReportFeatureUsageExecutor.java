/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.command.executors;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.ExecuteCommandContext;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor;
import org.ballerinalang.langserver.telemetry.LSFeatureUsageTelemetryEvent;
import org.ballerinalang.langserver.telemetry.TelemetryUtil;

import java.util.List;
import java.util.Optional;

/**
 * The executor used to send telemetry events for quickfix codeaction usages.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.command.spi.LSCommandExecutor")
public class ReportFeatureUsageExecutor implements LSCommandExecutor {

    public static final String COMMAND = "REPORT_FEATURE_USAGE";

    @Override
    public Object execute(ExecuteCommandContext context) {
        List<CommandArgument> arguments = context.getArguments();

        if (arguments == null || arguments.isEmpty()) {
            return null;
        }

        Optional<LSFeatureUsageTelemetryEvent> telemetryEvent = 
                TelemetryUtil.featureUsageEventFromCommandArgs(arguments);
        telemetryEvent.ifPresent(lsFeatureUsageTelemetryEvent ->
                TelemetryUtil.sendTelemetryEvent(context.languageServercontext(), lsFeatureUsageTelemetryEvent));

        return null;
    }

    @Override
    public String getCommand() {
        return COMMAND;
    }
}
