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

/**
 * Represents the Interactions between the graphQL operations and other components.
 * Based on the component name and the path, the links will be created between the components.
 *
 * @since 1.0.0
 */
public class Interaction {
    private final String componentName;
    private final String path;

    public Interaction(String componentName, String path) {
        this.componentName = componentName;
        this.path = path;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getPath() {
        return path;
    }
}
