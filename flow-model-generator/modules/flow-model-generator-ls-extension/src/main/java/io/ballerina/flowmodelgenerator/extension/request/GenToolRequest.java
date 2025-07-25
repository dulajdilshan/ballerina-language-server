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

package io.ballerina.flowmodelgenerator.extension.request;

import com.google.gson.JsonElement;

/**
 * A request to retrieve tools of the agent.
 *
 * @param filePath    path of the file
 * @param flowNode    diagram node
 * @param toolName    name of the tool
 * @param toolParameters tool function parameters property node
 * @param description description of the tool
 * @param connection  name of the connection
 *                    
 * @since 1.0.0
 */
public record GenToolRequest(String filePath, JsonElement flowNode, String toolName, JsonElement toolParameters,
                             String description, String connection) {
}
