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
package org.ballerinalang.langserver.codeaction.providers.imports;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeFactory;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Code Action for optimizing all imports.
 *
 * @since 1.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class OptimizeImportsCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Optimize Imports";
    public static final String UNUSED_IMPORT_DIAGNOSTIC_CODE = "BCE2002";
    public static final String REDECLARED_IMPORT_DIAGNOSTIC_CODE = "BCE2004";

    @Override
    public List<SyntaxKind> getSyntaxKinds() {
        return Collections.singletonList(SyntaxKind.IMPORT_DECLARATION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        List<CodeAction> actions = new ArrayList<>();
        String uri = context.fileUri();
        SyntaxTree syntaxTree = context.currentSyntaxTree().orElseThrow();
        // Copying to a separate list since there's side effects when modifying same node-list
        List<ImportDeclarationNode> fileImports = new ArrayList<>();
        ((ModulePartNode) syntaxTree.rootNode()).imports().stream().forEach(fileImports::add);

        List<LineRange> toBeRemovedImportsLocations = context.diagnostics(context.filePath()).stream()
                .filter(diag -> UNUSED_IMPORT_DIAGNOSTIC_CODE.equals(diag.diagnosticInfo().code()))
                .map(diag -> diag.location().lineRange())
                .collect(Collectors.toList());

        // Skip, when nothing to remove and only single import pending
        if (fileImports.isEmpty() || (fileImports.size() <= 1 && toBeRemovedImportsLocations.isEmpty())) {
            return actions;
        }

        // Find the imports range
        int importSLine = fileImports.get(0).lineRange().startLine().line();
        int importELine = fileImports.get(0).lineRange().endLine().line();
        for (int i = 0; i < fileImports.size(); i++) {
            ImportDeclarationNode importPkg = fileImports.get(i);
            LineRange pos = importPkg.lineRange();

            // Get imports starting line
            if (importSLine > pos.startLine().line()) {
                importSLine = pos.startLine().line();
            }

            // Get imports ending position
            if (importELine < pos.endLine().line()) {
                importELine = pos.endLine().line();
            }

            // Remove any matching imports on-the-go
            for (int j = 0; j < toBeRemovedImportsLocations.size(); j++) {
                LineRange rmLineRange = toBeRemovedImportsLocations.get(j);
                LineRange prefixLineRange = importPkg.prefix().isPresent()
                        ? importPkg.prefix().get().prefix().lineRange()
                        : importPkg.moduleName().get(importPkg.moduleName().size() - 1).lineRange();
                if (prefixLineRange.equals(rmLineRange)) {
                    fileImports.remove(i);
                    toBeRemovedImportsLocations.remove(j);
                    i--;
                    break;
                }
            }
        }
        
        // Perform any additional filtering
        processFileImports(fileImports, context);

        // Re-create imports list text
        StringBuilder editText = new StringBuilder();
        organizeFileImports(fileImports).forEach(importNode -> buildEditText(editText, importNode));

        Position importStart = new Position(importSLine, 0);
        Position importEnd = new Position(importELine + 1, 0);
        TextEdit textEdit = new TextEdit(new Range(importStart, importEnd), editText.toString());
        List<TextEdit> edits = Collections.singletonList(textEdit);
        actions.add(CodeActionUtil.createCodeAction(getCodeActionTitle(), edits, uri,
                getCodeActionKind()));
        return actions;
    }

    /**
     * Given filtered file imports, this method should perform any filtering on the finalized imports. Useful for the
     * child classes to perform additional checks and filter the imports, etc.
     *
     * @param fileImports Filtered imports list
     * @param context     Code action context
     */
    protected void processFileImports(List<ImportDeclarationNode> fileImports, CodeActionContext context) {
        List<LineRange> reDeclaredImportLocations = context.diagnostics(context.filePath()).stream()
                .filter(diag -> REDECLARED_IMPORT_DIAGNOSTIC_CODE.equals(diag.diagnosticInfo().code()))
                .map(diag -> diag.location().lineRange())
                .toList();

        Iterator<ImportDeclarationNode> iterator = fileImports.iterator();
        while (iterator.hasNext()) {
            ImportDeclarationNode importDeclarationNode = iterator.next();
            boolean redeclared = reDeclaredImportLocations.stream()
                    .anyMatch(lineRange -> lineRange.equals(importDeclarationNode.location().lineRange()));
            if (redeclared) {
                iterator.remove();
            }
        }
    }

    protected String getCodeActionKind() {
        return CodeActionKind.SourceOrganizeImports;
    }

    protected String getCodeActionTitle() {
        return CommandConstants.OPTIMIZE_IMPORTS_TITLE;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * @param fileImports list of file imports
     * @return sorted file imports list
     */
    protected List<ImportDeclarationNode> organizeFileImports(List<ImportDeclarationNode> fileImports) {
        return fileImports.stream()
                .sorted(Comparator.comparing((Function<ImportDeclarationNode, String>) o -> o.orgName().isPresent() ?
                                o.orgName().get().orgName().text()
                                : o.moduleName().stream().map(Token::text).collect(Collectors.joining(".")))
                        .thenComparing(
                                o -> o.moduleName().stream().map(Token::text).collect(Collectors.joining(".")))
                        .thenComparing(o -> o.prefix().isPresent() ? o.prefix().get().prefix().text() : ""))
                .toList();
    }

    protected static void buildEditText(StringBuilder editText, ImportDeclarationNode importNode) {
        MinutiaeList leadingMinutiae = NodeFactory.createEmptyMinutiaeList();
        MinutiaeList trailingMinutiae = importNode.importKeyword().trailingMinutiae();
        Token modifiedImportKeyword = importNode.importKeyword().modify(leadingMinutiae, trailingMinutiae);

        ImportDeclarationNode.ImportDeclarationNodeModifier importModifier = importNode.modify();
        importModifier.withImportKeyword(modifiedImportKeyword);
        if (importNode.orgName().isPresent()) {
            importModifier.withOrgName(importNode.orgName().get());
        }
        importModifier.withModuleName(importNode.moduleName());
        if (importNode.prefix().isPresent()) {
            importModifier.withPrefix(importNode.prefix().get());
        }
        importNode.semicolon();
        importModifier.withSemicolon(importNode.semicolon());

        editText.append(importModifier.apply().toSourceCode());
    }
}
