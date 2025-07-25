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

package org.ballerinalang.debugadapter.utils;

import com.sun.jdi.Location;

import static io.ballerina.identifier.Utils.decodeIdentifier;
import static org.ballerinalang.debugadapter.utils.PackageUtils.TEST_PKG_POSTFIX;
import static org.ballerinalang.debugadapter.utils.PackageUtils.getQModuleNameParts;

/**
 * Holds source information of a given JDI source location.
 *
 * @since 1.0.0
 */
public class DebugSourceLocation {

    private final Location jdiLocation;
    private boolean isValid;
    private String orgName;
    private String moduleName;
    private String moduleVersion;
    private String fileName;

    DebugSourceLocation(Location frameLocation) {
        this.jdiLocation = frameLocation;
        resolveLocation();
    }

    public boolean isValid() {
        return isValid;
    }

    public String orgName() {
        return orgName;
    }

    public String moduleName() {
        return moduleName;
    }

    public String moduleVersion() {
        return moduleVersion;
    }

    public String fileName() {
        return fileName;
    }

    private void resolveLocation() {
        try {
            String sourcePath = jdiLocation.sourcePath();
            String[] moduleParts = getQModuleNameParts(sourcePath);

            orgName = moduleParts[0];
            moduleName = decodeIdentifier(moduleParts[1]);
            // need to remove the auto generated "$test" prefix from the package name when converting back class names
            // into source paths.
            if (moduleName.endsWith(TEST_PKG_POSTFIX) || (moduleName.contains(TEST_PKG_POSTFIX + "."))) {
                moduleName = moduleName.replace(TEST_PKG_POSTFIX, "");
            }
            moduleVersion = moduleParts[2];
            fileName = moduleParts[3];

            isValid = true;
        } catch (Exception e) {
            isValid = false;
        }
    }
}
