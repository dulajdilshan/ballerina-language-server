/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Tests ignore unused variable code action.
 *
 * @since 1.0.0
 */
public class IgnoreUnusedVariableCodeActionTest extends AbstractCodeActionTest {

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Override
    @Test(dataProvider = "negative-test-data-provider")
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"ignore_unused_var1.json"},
                {"ignore_unused_var2.json"},
                {"ignore_unused_var3.json"},
                {"ignore_unused_var4.json"},
                {"ignore_unused_var5.json"},
                {"ignore_unused_var6.json"},
                {"ignore_unused_var7.json"},
                {"ignore_unused_var_tuple1.json"},
        };
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"ignore_unused_var8.json"},
                {"ignore_unused_var_assignment1.json"},
                {"ignore_unused_var_assignment2.json"},
                {"negative_ignore_unused_var_with_error1.json"},
        };
    }

    @Override
    public String getResourceDir() {
        return "ignore-unused-var";
    }
}
