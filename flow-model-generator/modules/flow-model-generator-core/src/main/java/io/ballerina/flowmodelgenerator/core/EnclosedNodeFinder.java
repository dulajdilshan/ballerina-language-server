/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.flowmodelgenerator.core;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.langserver.common.utils.PositionUtil;

import java.util.function.Predicate;

/**
 * Enclosed node finder.
 *
 * @since 1.0.0
 */
public class EnclosedNodeFinder {

    private final Document document;
    private final LinePosition position;
    private final boolean findClass;

    public EnclosedNodeFinder(Document document, LinePosition position, boolean findClass) {
        this.document = document;
        this.position = position;
        this.findClass = findClass;
    }

    public LineRange findEnclosedNode() {
        ModulePartNode modulePartNode = document.syntaxTree().rootNode();
        int positionOffset = PositionUtil.getPositionOffset(PositionUtil.toPosition(position), document.syntaxTree());
        TextRange textRange = TextRange.from(positionOffset, 1);
        NonTerminalNode nonTerminalNode = modulePartNode.findNode(textRange);

        Predicate<NonTerminalNode> nodeMatcher;
        if (findClass) {
            nodeMatcher = node -> !(node instanceof ClassDefinitionNode) && !(node instanceof ServiceDeclarationNode);
        } else {
            nodeMatcher = node -> !(node instanceof FunctionDefinitionNode);
        }

        while (nonTerminalNode != null && nodeMatcher.test(nonTerminalNode)) {
            nonTerminalNode = nonTerminalNode.parent();
        }

        if (nonTerminalNode == null) {
            throw new RuntimeException("No enclosed node found");
        }
        return nonTerminalNode.lineRange();
    }
}
