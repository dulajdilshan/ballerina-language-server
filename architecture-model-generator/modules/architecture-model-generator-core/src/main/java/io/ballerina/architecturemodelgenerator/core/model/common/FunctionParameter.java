/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package io.ballerina.architecturemodelgenerator.core.model.common;

import io.ballerina.architecturemodelgenerator.core.diagnostics.ArchitectureModelDiagnostic;
import io.ballerina.architecturemodelgenerator.core.model.ModelElement;
import io.ballerina.architecturemodelgenerator.core.model.SourceLocation;

import java.util.List;

/**
 * Represent a parameter of a Ballerina Object Method.
 *
 * @since 1.0.0
 */
public class FunctionParameter extends ModelElement {

    private final List<String> type;
    private final String name;
    private final boolean isRequired;

    public FunctionParameter(List<String> type, String name, boolean isRequired, SourceLocation elementLocation,
                             List<ArchitectureModelDiagnostic> diagnostics) {
        super(elementLocation, diagnostics);
        this.type = type;
        this.name = name;
        this.isRequired = isRequired;
    }

    public List<String> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return isRequired;
    }
}
