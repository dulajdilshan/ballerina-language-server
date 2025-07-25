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

package io.ballerina.jsonmapper;

import io.ballerina.jsonmapper.diagnostic.JsonToRecordMapperDiagnostic;

import java.util.ArrayList;
import java.util.List;

/**
 * Response format for JsonToBalRecord endpoint.
 *
 * @since 1.0.0
 */
public class JsonToRecordResponse {

    private String codeBlock = "";
    private List<JsonToRecordMapperDiagnostic> diagnostics = new ArrayList<>();

    public String getCodeBlock() {
        return codeBlock;
    }

    public void setCodeBlock(String codeBlock) {
        this.codeBlock = codeBlock;
    }

    public List<JsonToRecordMapperDiagnostic> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<JsonToRecordMapperDiagnostic> diagnostic) {
        this.diagnostics = diagnostic;
    }
}
