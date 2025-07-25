/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Test Cases for CodeActions.
 *
 * @since 1.0.0
 */
public class ImportModuleCodeActionTest extends AbstractCodeActionTest {

    @Override
    public String getResourceDir() {
        return "import-module";
    }

    @Override
    @Test(dataProvider = "codeaction-data-provider")
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @Test(dataProvider = "negativeDataProvider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @Override
    public boolean loadMockedPackages() {
        return true;
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"importModule1a.json"},
                {"importModule1b.json"},
                {"importModule2.json"},
                {"importModule3.json"},
                {"importModuleWithModAlias1.json"},
                {"importModuleWithModAlias2.json"},
                {"importModuleWithMultipleModAliases1.json"},
                {"importModuleWithMultipleModAliases2.json"},
                {"importModuleWithLicenceHeader1.json"},
                {"importModuleWithLicenceHeader2.json"},
                {"importModuleWithIgnoredImport.json"},
                {"importModuleWithTopLevelComment.json"},
                {"importMultipleModules1.json"},
                {"importMultipleModules2.json"},
                {"importMultipleModules3.json"},
                {"importMultipleModules4.json"},
                {"importMultipleModules5.json"},
                {"importMultipleModules6.json"},
        };
    }

    @DataProvider
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"negativeImportModuleWithMultipleModAliases1.json"}
        };
    }
}
