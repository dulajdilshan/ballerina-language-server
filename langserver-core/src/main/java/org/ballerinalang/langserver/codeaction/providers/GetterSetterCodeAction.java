/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Code Action for getters and setters.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class GetterSetterCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Getter and Setter";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return List.of(SyntaxKind.OBJECT_FIELD);
    }

    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        if (CodeActionUtil.getObjectFieldNode(context, posDetails).isEmpty()) {
            return Collections.emptyList();
        }

        ObjectFieldNode objectFieldNode = CodeActionUtil.getObjectFieldNode(context, posDetails).get();
        if (CodeActionUtil.isImmutableObjectField(objectFieldNode)) {
            return Collections.emptyList();
        }

        String fieldName = String.valueOf(objectFieldNode.fieldName().text());
        String commandTitle = String.format("Create getter and setter for \"%s\"", fieldName);
        String typeName = String.valueOf(objectFieldNode.typeName()).trim();
        String extractedFieldName = CodeActionUtil.removeQuotedIdentifier(fieldName);
        String functionName = "get" + extractedFieldName.substring(0, 1).toUpperCase(Locale.ROOT) +
                extractedFieldName.substring(1);
        Optional<FunctionDefinitionNode> initNode = CodeActionUtil.getInitNode(objectFieldNode);
        if (CodeActionUtil.isFunctionDefined(functionName, objectFieldNode)) {
            return Collections.emptyList();
        }

        functionName = "set" + extractedFieldName.substring(0, 1).toUpperCase(Locale.ROOT) +
                extractedFieldName.substring(1);
        if (CodeActionUtil.isFunctionDefined(functionName, objectFieldNode)) {
            return Collections.emptyList();
        }

        List<TextEdit> edits = CodeActionUtil.getGetterSetterCodeEdits(objectFieldNode, initNode, fieldName, typeName,
                NAME);
        return Collections.singletonList(CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                CodeActionKind.Source));
    }

    @Override
    public String getName() {
        return NAME;
    }

}
