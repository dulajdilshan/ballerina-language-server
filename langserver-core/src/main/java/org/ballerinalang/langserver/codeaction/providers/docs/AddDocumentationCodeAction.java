/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers.docs;

import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.hasDocs;

/**
 * Code Action for adding single documentation.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class AddDocumentationCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Add Documentation";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return Arrays.asList(SyntaxKind.FUNCTION_DEFINITION,
                SyntaxKind.OBJECT_TYPE_DESC,
                SyntaxKind.CLASS_DEFINITION,
                SyntaxKind.SERVICE_DECLARATION,
                SyntaxKind.RESOURCE_ACCESSOR_DEFINITION,
                SyntaxKind.RECORD_TYPE_DESC,
                SyntaxKind.METHOD_DECLARATION,
                SyntaxKind.OBJECT_METHOD_DEFINITION,
                SyntaxKind.ANNOTATION_DECLARATION,
                SyntaxKind.MODULE_VAR_DECL,
                SyntaxKind.ENUM_DECLARATION,
                SyntaxKind.CONST_DECLARATION,
                SyntaxKind.UNION_TYPE_DESC,
                SyntaxKind.NIL_TYPE_DESC,
                SyntaxKind.ARRAY_TYPE_DESC,
                SyntaxKind.INT_TYPE_DESC,
                SyntaxKind.BYTE_TYPE_DESC,
                SyntaxKind.FLOAT_TYPE_DESC,
                SyntaxKind.DECIMAL_TYPE_DESC,
                SyntaxKind.STRING_TYPE_DESC,
                SyntaxKind.BOOLEAN_TYPE_DESC,
                SyntaxKind.XML_TYPE_DESC,
                SyntaxKind.JSON_TYPE_DESC,
                SyntaxKind.HANDLE_TYPE_DESC,
                SyntaxKind.ANY_TYPE_DESC,
                SyntaxKind.ANYDATA_TYPE_DESC,
                SyntaxKind.NEVER_TYPE_DESC,
                SyntaxKind.MAP_TYPE_DESC,
                SyntaxKind.ERROR_TYPE_DESC,
                SyntaxKind.STREAM_TYPE_DESC,
                SyntaxKind.TABLE_TYPE_DESC,
                SyntaxKind.FUNCTION_TYPE_DESC,
                SyntaxKind.TUPLE_TYPE_DESC,
                SyntaxKind.READONLY_TYPE_DESC,
                SyntaxKind.FUTURE_TYPE_DESC,
                SyntaxKind.SINGLETON_TYPE_DESC,
                SyntaxKind.INTERSECTION_TYPE_DESC,
                SyntaxKind.QUALIFIED_NAME_REFERENCE,
                SyntaxKind.SIMPLE_NAME_REFERENCE,
                SyntaxKind.TYPEDESC_TYPE_DESC,
                SyntaxKind.DISTINCT_TYPE_DESC,
                SyntaxKind.OPTIONAL_TYPE_DESC,
                SyntaxKind.PARENTHESISED_TYPE_DESC
        );
    }

    @Override
    public int priority() {
        return 999;
    }

    @Override
    public boolean validate(CodeActionContext context, RangeBasedPositionDetails positionDetails) {
        return positionDetails.matchedDocumentableNode().isPresent() 
                && !hasDocs(positionDetails.matchedDocumentableNode().get())
                && CodeActionNodeValidator.validate(positionDetails.matchedCodeActionNode());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context, RangeBasedPositionDetails posDetails) {
        String docUri = context.fileUri();
        Optional<NonTerminalNode> documentableNode = posDetails.matchedDocumentableNode();

        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE,
                PositionUtil.toRange(documentableNode.get().lineRange()));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));

        CodeAction action = new CodeAction(CommandConstants.ADD_DOCUMENTATION_TITLE);
        Command command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE, AddDocumentationExecutor.COMMAND, args);
        action.setCommand(command);
        return Collections.singletonList(action);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
