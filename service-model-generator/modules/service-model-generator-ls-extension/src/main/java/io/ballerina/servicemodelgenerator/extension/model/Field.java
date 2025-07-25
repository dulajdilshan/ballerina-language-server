/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.servicemodelgenerator.extension.model;

/**
 * Field class to hold the object field information.
 *
 * @since 1.0.0
 */
public class Field extends Parameter {
    private final boolean isPrivate;
    private final boolean isFinal;
    private final Codedata codedata;

    public Field(Parameter parameter, boolean isPrivate, boolean isFinal, Codedata codedata) {
        super(parameter);
        this.isPrivate = isPrivate;
        this.isFinal = isFinal;
        this.codedata = codedata;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public boolean isFinal() {
        return isFinal;
    }

    public Codedata codedata() {
        return codedata;
    }
}
