/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.shell.service.test.getresult;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * getResult End point tests for errors and error handling.
 *
 * @since 1.0.0
 */
public class ErrorTests extends AbstractGetResultTest {
    @Test(description = "Test for parsing fails")
    public void testParsingErrors() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("errors.parse.json");
    }

    @Test(description = "Test for checkpanic")
    public void testErrorsCheckPanic() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("errors.checkpanic.json");
    }

    @Test(description = "Test for check")
    public void testErrorsCheck() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("errors.check.json");
    }

    @Test(description = "Test for trap")
    public void testErrorsTrap() throws ExecutionException, IOException, InterruptedException {
        runGetResultTest("errors.trap.json");
    }
}
