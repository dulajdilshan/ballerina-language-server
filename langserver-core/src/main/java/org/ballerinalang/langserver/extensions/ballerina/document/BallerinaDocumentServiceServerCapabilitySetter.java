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
package org.ballerinalang.langserver.extensions.ballerina.document;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter;

import java.util.Optional;

/**
 * Capability setter for the {@link BallerinaDocumentService}.
 * 
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.registration.BallerinaServerCapabilitySetter")
public class BallerinaDocumentServiceServerCapabilitySetter
        extends BallerinaServerCapabilitySetter<BallerinaDocumentServerCapabilities> {
    @Override
    public Optional<BallerinaDocumentServerCapabilities> build() {
        BallerinaDocumentServerCapabilities capabilities = new BallerinaDocumentServerCapabilities();
        capabilities.setDiagnostics(true);
        capabilities.setSyntaxTree(true);
        capabilities.setExecutorPositions(true);
        capabilities.setSyntaxApiCalls(true);
        capabilities.setSyntaxTreeByRange(true);
        capabilities.setSyntaxTreeLocate(true);
        capabilities.setSyntaxTreeModify(true);
        capabilities.setTriggerModify(true);
        capabilities.setSyntaxTreeNode(true);
        capabilities.setResolveMissingDependencies(true);
        capabilities.setSyntaxTreeByName(true);
        capabilities.setSyntaxTreeNodeByPosition(true);

        return Optional.of(capabilities);
    }
    
    @Override
    public String getCapabilityName() {
        return Constants.CAPABILITY_NAME;
    }

    @Override
    public Class<BallerinaDocumentServerCapabilities> getCapability() {
        return BallerinaDocumentServerCapabilities.class;
    }
}
