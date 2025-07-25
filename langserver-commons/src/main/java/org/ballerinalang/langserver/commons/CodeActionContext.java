/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.commons;

import io.ballerina.compiler.syntax.tree.Node;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents the Code action operation context.
 *
 * @since 1.0.0
 */
public interface CodeActionContext extends DocumentServiceContext {

    /**
     * Get the cursor position.
     *
     * @return {@link Position}
     */
    @Deprecated(forRemoval = true)
    Position cursorPosition();

    /**
     * Get the diagnostics of the file.
     *
     * @return {@link List} of diagnostics
     */
    List<io.ballerina.tools.diagnostics.Diagnostic> diagnostics(Path filePath);

    /**
     * Get the diagnostics at the cursor.
     *
     * @return {@link  List} of diagnostics at the cursor
     */
    List<Diagnostic> cursorDiagnostics();

    /**
     * Get the cursor position as an offset value according to the syntax tree.
     *
     * @return {@link Integer} offset of the cursor
     */
    int cursorPositionInTree();


    /**
     * Get the node at cursor.
     *
     * @return {@link Node} node at the cursor
     */
    @Deprecated(forRemoval = true)
    Node nodeAtCursor();

    /**
     * Get the selected range.
     *
     * @return {@link Range}
     */
    Range range();

    /**
     * Get the node at selected range.
     *
     * @return {@link Node}
     */
    Node nodeAtRange();
}
