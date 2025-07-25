/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context;

import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.TextRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionException;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SnippetCompletionItem;
import org.ballerinalang.langserver.completions.providers.AbstractCompletionProvider;
import org.ballerinalang.langserver.completions.util.CompletionUtil;
import org.ballerinalang.langserver.completions.util.Snippet;

import java.util.Collections;
import java.util.List;

/**
 * Completion provider for {@link NamedWorkerDeclarationNode} context.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.completion.spi.BallerinaCompletionProvider")
public class NamedWorkerDeclarationNodeContext extends AbstractCompletionProvider<NamedWorkerDeclarationNode> {
    public NamedWorkerDeclarationNodeContext() {
        super(NamedWorkerDeclarationNode.class);
    }

    @Override
    public List<LSCompletionItem> getCompletions(BallerinaCompletionContext context, NamedWorkerDeclarationNode node)
            throws LSCompletionException {
        boolean inReturnContext = this.withinReturnTypeContext(context, node);
        if (!inReturnContext) {
            return Collections.emptyList();
        }

        if (node.returnTypeDesc().isEmpty()) {
            return Collections.singletonList(new SnippetCompletionItem(context, Snippet.KW_RETURNS.get()));
        }
        return CompletionUtil.route(context, node.returnTypeDesc().get());
    }

    private boolean withinReturnTypeContext(BallerinaCompletionContext context, NamedWorkerDeclarationNode node) {
        int textPosition = context.getCursorPositionInTree();
        TextRange nameRange = node.workerName().textRange();
        TextRange bodyStart = node.workerBody().openBraceToken().textRange();
        return nameRange.endOffset() < textPosition && textPosition < bodyStart.startOffset();
    }

    @Override
    public boolean onPreValidation(BallerinaCompletionContext context, NamedWorkerDeclarationNode node) {
        /*
        Added the check to validate the following and to route to the fork statement body.
        eg: 
        fork {
            worker w1 {   
            }
            w<cursor>
        }
         */
        int cursor = context.getCursorPositionInTree();
        Token closeBraceToken = node.workerBody().closeBraceToken();
        
        return cursor < closeBraceToken.textRange().endOffset();
    }
}
