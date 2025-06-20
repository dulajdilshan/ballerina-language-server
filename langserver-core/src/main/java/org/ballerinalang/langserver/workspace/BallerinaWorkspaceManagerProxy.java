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
package org.ballerinalang.langserver.workspace;

import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManagerProxy;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;

/**
 * A proxy implementation for the ballerina workspace manager.
 *
 * @since 1.0.0
 */
public interface BallerinaWorkspaceManagerProxy extends WorkspaceManagerProxy {
    /**
     * Handle the document open event.
     *
     * @param params {@link DidOpenTextDocumentParams}
     * @throws WorkspaceDocumentException on failure
     */
    void didOpen(DidOpenTextDocumentParams params) throws WorkspaceDocumentException;

    /**
     * Handle the document change event.
     *
     * @param params {@link DidChangeTextDocumentParams}
     * @throws WorkspaceDocumentException on failure
     */
    void didChange(DidChangeTextDocumentParams params) throws WorkspaceDocumentException;

    /**
     * Handle the document close event.
     *
     * @param params {@link DidCloseTextDocumentParams}
     */
    void didClose(DidCloseTextDocumentParams params);
}
