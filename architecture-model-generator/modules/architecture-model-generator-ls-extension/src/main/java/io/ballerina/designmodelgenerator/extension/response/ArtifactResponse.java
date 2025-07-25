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

package io.ballerina.designmodelgenerator.extension.response;

import io.ballerina.artifactsgenerator.Artifact;

import java.util.Map;

/**
 * Represents the response for artifact related operations. s
 *
 * @since 1.0.0
 */
public class ArtifactResponse extends AbstractResponse {

    private String uri;
    private Map<String, Map<String, Artifact>> artifacts;

    public Map<String, Map<String, Artifact>> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Map<String, Map<String, Artifact>> artifacts) {
        this.artifacts = artifacts;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
