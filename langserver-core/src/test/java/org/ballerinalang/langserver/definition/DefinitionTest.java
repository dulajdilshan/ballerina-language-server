/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.definition;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PathUtil;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Test goto definition language server feature.
 */
public class DefinitionTest {
    protected Path configRoot;
    protected Path sourceRoot;
    protected Gson gson = new Gson();
    protected Endpoint serviceEndpoint;
    private static final Logger log = LoggerFactory.getLogger(DefinitionTest.class);

    @BeforeClass
    public void init() {
        configRoot = FileUtils.RES_DIR.resolve("definition").resolve("expected");
        sourceRoot = FileUtils.RES_DIR.resolve("definition").resolve("sources");
        this.serviceEndpoint = getServiceEndpoint();
    }

    @Test(description = "Test goto definitions", dataProvider = "testDataProvider")
    public void test(String configPath, String configDir) throws IOException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRoot.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);
        this.compareResults(sourcePath, position, configObject, sourceRoot);
    }

    @Test(description = "Test goto definitions for standard libs", dataProvider = "testStdLibDataProvider")
    public void testStdLibDefinition(String configPath, String configDir) throws IOException, URISyntaxException {
        performStdLibDefinitionTest(sourceRoot, configPath, configDir, true);
        performStdLibDefinitionTest(sourceRoot, configPath, configDir, false);
    }

    @Test(dataProvider = "testInterStdLibDataProvider")
    public void testInterStdLibDefinition(String configPath, String configDir) throws IOException, URISyntaxException {
        Path ballerinaHome = Path.of(CommonUtil.BALLERINA_HOME);
        performStdLibDefinitionTest(ballerinaHome, configPath, configDir, true);
        performStdLibDefinitionTest(ballerinaHome, configPath, configDir, false);
    }

    /**
     * Perform goto def tests for std lib files.
     *
     * @param withBalaScheme Whether to use bala scheme or not when fetching definition.
     */
    private void performStdLibDefinitionTest(Path sourceRootPath, String configPath, String configDir,
                                             boolean withBalaScheme)
            throws IOException, URISyntaxException {
        JsonObject configObject = FileUtils.fileContentAsObject(configRoot.resolve(configDir)
                .resolve(configPath).toString());
        JsonObject source = configObject.getAsJsonObject("source");
        Path sourcePath = sourceRootPath.resolve(source.get("file").getAsString());
        Position position = gson.fromJson(configObject.get("position"), Position.class);

        String fileUri = sourcePath.toUri().toString();
        if (withBalaScheme) {
            fileUri = PathUtil.getUriForPath(sourcePath, CommonUtil.URI_SCHEME_BALA);
        }

        byte[] encodedContent = Files.readAllBytes(sourcePath);
        TestUtil.openDocument(serviceEndpoint, fileUri, new String(encodedContent));
        String actualStr = TestUtil.getDefinitionResponse(fileUri, position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, fileUri);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = JsonParser.parseString(actualStr).getAsJsonObject().getAsJsonObject("result")
                .getAsJsonArray("left");
        this.alterExpectedStdlibUri(expected);
        this.alterActualStdLibUri(actual);
        Assert.assertEquals(actual, expected);
    }

    protected void compareResults(Path sourcePath, Position position, JsonObject configObject, Path root)
            throws IOException {
        TestUtil.openDocument(serviceEndpoint, sourcePath);
        String actualStr = TestUtil.getDefinitionResponse(sourcePath.toUri().toString(), position, serviceEndpoint);
        TestUtil.closeDocument(serviceEndpoint, sourcePath);

        JsonArray expected = configObject.getAsJsonArray("result");
        JsonArray actual = JsonParser.parseString(actualStr).getAsJsonObject().getAsJsonObject("result")
                .getAsJsonArray("left");
        this.alterExpectedUri(expected, root);
        this.alterActualUri(actual);
        Assert.assertEquals(actual, expected);
    }

    @DataProvider
    protected Object[][] testDataProvider() {
        log.info("Test textDocument/definition for Basic Cases");
        return new Object[][]{
                {"defProject1.json", "project"},
                {"defProject2.json", "project"},
                {"defProject3.json", "project"},
                {"defProject4.json", "project"},
                {"defProject5.json", "project"},
                {"defProject6.json", "project"},
                {"defProject7.json", "project"},
                {"defProject9.json", "project"},
                {"defProject10.json", "project"},
                {"defProject11.json", "project"},
                {"defProject12.json", "project"},
                {"def_record_config1.json", "project"},
                {"def_error_config1.json", "project"},
                {"def_annotation_on_obj_func_config1.json", "project"},
                {"def_typereference.json", "project"},
                {"def_typereference2.json", "project"},
                {"def_typereference3.json", "project"},
                {"defProject15.json", "project"},
                {"defProject16.json", "project"},
                {"defProject17.json", "project"},
                {"defProject18.json", "project"},
                {"defProject19.json", "project"},
                {"defProject19.json", "project"},
                {"defProject20.json", "project"}
        };
    }

    @DataProvider
    protected Object[][] testStdLibDataProvider() {
        log.info("Test textDocument/definition for Std Lib Cases");
        return new Object[][]{
                {"defProject8.json", "project"},
                {"def_error_config2.json", "project"},
                {"def_retry_spec_config1.json", "project"},
                {"defProject14.json", "project"}
        };
    }

    @DataProvider
    protected Object[][] testInterStdLibDataProvider() {
        log.info("Test textDocument/definition for Inter Std Lib Cases");
        return new Object[][]{
                {"inter_stdlib_config1.json", "stdlib"},
                {"inter_stdlib_config2.json", "stdlib"}
        };
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
        this.serviceEndpoint = null;
    }

    protected Endpoint getServiceEndpoint() {
        return TestUtil.newLanguageServer()
                .withInitOption(InitializationOptions.KEY_BALA_SCHEME_SUPPORT, false)
                .build();
    }

    protected void alterExpectedUri(JsonArray expected, Path root) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");
            Path expectedPath = Path.of(root.toUri());
            for (String uriComponent : uriComponents) {
                expectedPath = expectedPath.resolve(uriComponent);
            }
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getPath());
        }
    }

    protected void alterExpectedStdlibUri(JsonArray expected) throws IOException {
        for (JsonElement jsonElement : expected) {
            JsonObject item = jsonElement.getAsJsonObject();
            String[] uriComponents = item.get("uri").toString().replace("\"", "").split("/");

            Path expectedPath = getPath(uriComponents);
            item.remove("uri");
            item.addProperty("uri", expectedPath.toFile().getPath());
        }
    }

    private static Path getPath(String[] uriComponents) {
        int startIndex = 0;
        for (int i = 0; i < uriComponents.length; i++) {
            if (uriComponents[i].equals("repo")) {
                startIndex = i;
                break;
            }
        }
        Path expectedPath = Path.of("/");
        for (int i = startIndex; i < uriComponents.length; i++) {
            if (!uriComponents[i].isEmpty()) {
                expectedPath = expectedPath.resolve(uriComponents[i]);
            }
        }
        return expectedPath;
    }

    protected void alterActualStdLibUri(JsonArray actual) throws IOException, URISyntaxException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String fileUri = item.get("uri").toString().replace("\"", "");

            // Check bala URI scheme
            URI uri = new URI(fileUri);
            Assert.assertEquals(uri.getScheme(), getExpectedUriScheme(),
                    String.format("Expected %s: URI scheme", getExpectedUriScheme()));
            fileUri = PathUtil.convertUriSchemeFromBala(fileUri);
            uri = new URI(fileUri);
            Assert.assertEquals(uri.getScheme(), CommonUtil.URI_SCHEME_FILE,
                    "Expected file URI scheme after conversion");

            String canonicalPath = new File(URI.create(fileUri)).getCanonicalPath();
            if (canonicalPath.contains("repo")) {
                canonicalPath = "/repo" + canonicalPath.substring(canonicalPath.lastIndexOf("repo") + 4);
            }
            // Remove version directory (d.d.d pattern) from canonical path if it exists
            String[] pathParts = canonicalPath.split("/");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < pathParts.length; i++) {
                String part = pathParts[i];
                result.append("/").append(part);
            }
            canonicalPath = result.toString().replaceFirst("^/", "");

            item.remove("uri");
            item.addProperty("uri", new File(canonicalPath).getPath());
        }
    }

    protected void alterActualUri(JsonArray actual) throws IOException {
        for (JsonElement jsonElement : actual) {
            JsonObject item = jsonElement.getAsJsonObject();
            String uri = item.get("uri").toString().replace("\"", "");
            String canonicalPath = new File(URI.create(uri)).getCanonicalPath();
            item.remove("uri");
            item.addProperty("uri", canonicalPath);
        }
    }

    protected String getExpectedUriScheme() {
        return CommonUtil.URI_SCHEME_FILE;
    }
}
