/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.graphqlmodelgenerator.core.model;

import io.ballerina.stdlib.graphql.commons.types.Position;

import java.util.List;

/**
 * Represents the Parameters of resource and remote functions.
 *
 * @since 1.0.0
 */
public class RecordComponent {
    private final String name;
    private final Position position;
    private final String description;
    private final List<RecordField> recordFields;
    private final boolean isInputObject;

    public RecordComponent(String name, Position position, String description, List<RecordField> recordFields,
                           boolean isInputObject) {
        this.name = name;
        this.position = position;
        this.description = description;
        this.recordFields = recordFields;
        this.isInputObject = isInputObject;
    }

    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public String getDescription() {
        return description;
    }

    public List<RecordField> getRecordFields() {
        return recordFields;
    }

    public boolean isInputObject() {
        return isInputObject;
    }
}
