/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.inlayhint;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.common.utils.TypeResolverUtil;
import org.ballerinalang.langserver.commons.InlayHintContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents the Inlay Hint Provider.
 *
 * @since 1.0.0
 */
public final class InlayHintProvider {

    private InlayHintProvider() {
    }

    public static List<InlayHint> getInlayHint(InlayHintContext context) {
        LSClientCapabilities lsClientCapabilities = context.languageServercontext().get(LSClientCapabilities.class);
        if (!lsClientCapabilities.getInitializationOptions().isEnableInlayHints() ||
                context.currentDocument().isEmpty()) {
            return Collections.emptyList();
        }
        InvokableNodeFinder invokableNodeFinder = new InvokableNodeFinder();
        Node rootNode = context.currentDocument().get().syntaxTree().rootNode();
        rootNode.accept(invokableNodeFinder);

        // Get the method name list
        List<NonTerminalNode> invokableNodeList = invokableNodeFinder.getInvokableNodeList();
        List<InlayHint> inlayHints = new ArrayList<>();

        for (NonTerminalNode invokableNode : invokableNodeList) {
            InlayHintArgumentTypeFinder argumentTypeFinder = new InlayHintArgumentTypeFinder();
            invokableNode.accept(argumentTypeFinder);

            // Get the argument list
            List<NonTerminalNode> argList = argumentTypeFinder.getArgumentList();
            if (argList.isEmpty()) {
                continue;
            }
            Map<NonTerminalNode, LineRange> inlayHintLocations = argumentTypeFinder.getInlayHintLocations();
            Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> parameterSymbols =
                    getParameterSymbolsForInvokableNode(context, invokableNode);

            for (int argumentIndex = 0; argumentIndex < argList.size(); argumentIndex++) {
                //Find the inlay-hint location for the argument
                NonTerminalNode argument = argList.get(argumentIndex);
                if (argument.kind() == SyntaxKind.NAMED_ARG) {
                    break;
                }
                LineRange lineRange = inlayHintLocations.get(argument);
                if (lineRange == null) {
                    return Collections.emptyList();
                }
                int startLine = lineRange.endLine().line();
                int startChar = lineRange.endLine().offset();
                Position position = new Position(startLine, startChar);

                if (argument.kind() == SyntaxKind.REST_ARG) {
                    if (parameterSymbols.getRight().isEmpty() ||
                            parameterSymbols.getRight().get().getName().get().startsWith("$")) {
                        break;
                    }
                    String label = "..." + parameterSymbols.getRight().get().getName().get();
                    InlayHint inlayHint = new InlayHint(position, Either.forLeft(label + ": "));
                    inlayHints.add(inlayHint);
                    break;
                }

                //Find the corresponding parameter symbol for the argument and create inlay-hint
                if (parameterSymbols.getLeft().size() <= argumentIndex) {
                    if (parameterSymbols.getRight().isPresent()
                            && parameterSymbols.getRight().get().getName().isPresent()) {
                        String label = "..." + parameterSymbols.getRight().get().getName().get();
                        InlayHint inlayHint = new InlayHint(position, Either.forLeft(label + ": "));
                        inlayHints.add(inlayHint);
                    }
                    break;
                } else if (parameterSymbols.getLeft().get(argumentIndex).getName().isEmpty()
                        || parameterSymbols.getLeft().get(argumentIndex).getName().get().startsWith("$")) {
                    break;
                }
                String label = parameterSymbols.getLeft().get(argumentIndex).getName().get();
                InlayHint inlayHint = new InlayHint(position, Either.forLeft(label + ": "));
                inlayHints.add(inlayHint);
            }
        }
        return inlayHints;
    }

    private static Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> getParameterSymbolsForInvokableNode(
            InlayHintContext context,
            NonTerminalNode invokableNode) {
        if (invokableNode.kind() == SyntaxKind.METHOD_CALL) {
            MethodCallExpressionNode methodCallExpressionNode = (MethodCallExpressionNode) invokableNode;
            Optional<FunctionTypeSymbol> libFunction = TypeResolverUtil
                    .findMethodInLangLibFunctions(methodCallExpressionNode, context);
            if (libFunction.isEmpty() || libFunction.get().params().isEmpty()) {
                return Pair.of(Collections.emptyList(), Optional.empty());
            }
            // Since the method is a lang-lib function, skip the first parameter
            return Pair.of(libFunction.get().params().get().stream().skip(1).toList(),
                    libFunction.get().restParam());
        } else if (invokableNode.kind() == SyntaxKind.CLIENT_RESOURCE_ACCESS_ACTION) {
            return context.currentSemanticModel().get()
                    .symbol(invokableNode)
                    .map(symbol -> ((ResourceMethodSymbol) symbol).typeDescriptor())
                    .map(typeSymbol -> Pair.of(typeSymbol.params().orElse(Collections.emptyList()),
                            typeSymbol.restParam()))
                    .orElse(Pair.of(Collections.emptyList(), Optional.empty()));
        } else if (invokableNode.kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
            return context.currentSemanticModel().get()
                    .symbol(invokableNode)
                    .map(symbol -> ((MethodSymbol) symbol).typeDescriptor())
                    .map(typeSymbol -> Pair.of(typeSymbol.params().orElse(Collections.emptyList()),
                            typeSymbol.restParam()))
                    .orElse(Pair.of(Collections.emptyList(), Optional.empty()));
        } else if (invokableNode.kind() == SyntaxKind.IMPLICIT_NEW_EXPRESSION || invokableNode.kind() ==
                SyntaxKind.EXPLICIT_NEW_EXPRESSION) {
            return getParameterSymbolsForNewExpression(context, invokableNode);
        } else {
            FunctionCallExpressionNode functionCallExpressionNode = (FunctionCallExpressionNode) invokableNode;
            return getParameterSymbolsForFunctionCall(context, functionCallExpressionNode);
        }
    }

    private static Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> getParameterSymbolsForNewExpression(
            InlayHintContext context,
            NonTerminalNode node) {
        Optional<TypeSymbol> symbol = context.currentSemanticModel()
                .flatMap(semanticModel -> semanticModel.typeOf(node))
                .flatMap(typeSymbol -> Optional.of(CommonUtil.getRawType(typeSymbol))).stream().findFirst();
        if (symbol.isEmpty()) {
            return Pair.of(Collections.emptyList(), Optional.empty());
        }
        return getParametersOfNewExpression(symbol.get());
    }

    private static Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> getParametersOfNewExpression(
            TypeSymbol symbol) {
        if (symbol.typeKind() == TypeDescKind.UNION) {
            symbol = ((UnionTypeSymbol) symbol).memberTypeDescriptors().stream()
                    .filter(typeSymbol ->
                            CommonUtil.getRawType(typeSymbol).typeKind() == TypeDescKind.OBJECT)
                    .map(CommonUtil::getRawType).findFirst().get();
        }
        TypeSymbol typeSymbol = CommonUtil.getRawType(symbol);
        Optional<MethodSymbol> methodSymbol = ((ClassSymbol) typeSymbol).initMethod();
        return methodSymbol.map(value -> Pair.of(value.typeDescriptor().params()
                .orElse(Collections.emptyList()), value.typeDescriptor().restParam())).orElseGet(() ->
                Pair.of(Collections.emptyList(), Optional.empty()));
    }

    private static Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> getParameterSymbolsForFunctionCall(
            InlayHintContext context,
            FunctionCallExpressionNode node) {
        LineRange lineRange = node.lineRange();
        List<Symbol> visibleSymbols = context.visibleSymbols(new Position(
                lineRange.startLine().line(), lineRange.startLine().offset()));
        List<FunctionSymbol> functionSymbols;
        String functionName;
        if (node.functionName().kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            functionName = ((QualifiedNameReferenceNode) node.functionName()).identifier().text();
            functionSymbols = visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.MODULE)
                    .map(symbol -> (ModuleSymbol) symbol)
                    .filter(moduleSymbol -> moduleSymbol.getName().isPresent())
                    .filter(moduleSymbol -> moduleSymbol.getName().get().equals(
                            ((QualifiedNameReferenceNode) node.functionName()).modulePrefix().toString().strip()))
                    .findFirst()
                    .map(ModuleSymbol::functions)
                    .orElse(Collections.emptyList());

        } else if (node.functionName().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            functionSymbols = visibleSymbols.stream()
                    .filter(symbol -> symbol.kind() == SymbolKind.FUNCTION)
                    .map(symbol -> (FunctionSymbol) symbol).toList();
            functionName = ((SimpleNameReferenceNode) node.functionName()).name().text();
        } else {
            return Pair.of(Collections.emptyList(), Optional.empty());
        }
        return findParameterSymbols(functionSymbols, functionName);
    }

    private static Pair<List<ParameterSymbol>, Optional<ParameterSymbol>> findParameterSymbols(
            List<FunctionSymbol> functionSymbols,
            String functionName) {
        Optional<FunctionTypeSymbol> functionTypeSymbol = functionSymbols.stream()
                .filter(functionSymbol -> functionSymbol.getName().isPresent()
                        && functionSymbol.getName().get().equals(functionName.strip()))
                .findFirst()
                .flatMap(SymbolUtil::getTypeDescriptor)
                .filter(typeDescriptor -> typeDescriptor instanceof FunctionTypeSymbol)
                .map(typeDescriptor -> (FunctionTypeSymbol) typeDescriptor);

        return functionTypeSymbol.map(symbol -> Pair.of(symbol.params().orElse(Collections.emptyList()),
                symbol.restParam())).orElse(Pair.of(Collections.emptyList(), Optional.empty()));
    }

    /**
     * Visitor to find function call and method call expression nodes.
     */
    private static class InvokableNodeFinder extends NodeVisitor {
        List<NonTerminalNode> invokableNodeList = new ArrayList<>();

        public InvokableNodeFinder() {
        }

        public List<NonTerminalNode> getInvokableNodeList() {
            return invokableNodeList;
        }

        @Override
        public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
            invokableNodeList.add(functionCallExpressionNode);
        }

        @Override
        public void visit(MethodCallExpressionNode methodCallExpressionNode) {
            invokableNodeList.add(methodCallExpressionNode);
        }

        @Override
        public void visit(ClientResourceAccessActionNode clientResourceAccessActionNode) {
            invokableNodeList.add(clientResourceAccessActionNode);
        }

        @Override
        public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
            invokableNodeList.add(remoteMethodCallActionNode);
        }

        @Override
        public void visit(ImplicitNewExpressionNode implicitNewExpressionNode) {
            invokableNodeList.add(implicitNewExpressionNode);
        }

        @Override
        public void visit(ExplicitNewExpressionNode explicitNewExpressionNode) {
            invokableNodeList.add(explicitNewExpressionNode);
        }
    }

    /**
     * Visitor to find argument list and comma list of a function call or method call expression node.
     */
    private static class InlayHintArgumentTypeFinder extends NodeVisitor {
        List<NonTerminalNode> argumentList = new ArrayList<>();
        Map<NonTerminalNode, LineRange> inlayHintLocations = new HashMap<>();

        public InlayHintArgumentTypeFinder() {
        }

        public List<NonTerminalNode> getArgumentList() {
            return argumentList;
        }

        public Map<NonTerminalNode, LineRange> getInlayHintLocations() {
            return inlayHintLocations;
        }

        @Override
        public void visit(FunctionCallExpressionNode functionCallExpressionNode) {
            findInlayHintLocationsFromArgs(functionCallExpressionNode.arguments(),
                    functionCallExpressionNode.openParenToken());
        }

        @Override
        public void visit(MethodCallExpressionNode methodCallExpressionNode) {
            findInlayHintLocationsFromArgs(methodCallExpressionNode.arguments(),
                    methodCallExpressionNode.openParenToken());
        }

        @Override
        public void visit(ParenthesizedArgList parenthesizedArgList) {
            findInlayHintLocationsFromArgs(parenthesizedArgList.arguments(),
                    parenthesizedArgList.openParenToken());
        }

        @Override
        public void visit(RemoteMethodCallActionNode remoteMethodCallActionNode) {
            findInlayHintLocationsFromArgs(remoteMethodCallActionNode.arguments(),
                    remoteMethodCallActionNode.openParenToken());
        }

        @Override
        public void visit(PositionalArgumentNode positionalArgumentNode) {
            argumentList.add(positionalArgumentNode);
        }

        @Override
        public void visit(RestArgumentNode restArgumentNode) {
            argumentList.add(restArgumentNode);
        }

        private void findInlayHintLocationsFromArgs(SeparatedNodeList<FunctionArgumentNode> argumentNodes,
                                                    Token openParenToken) {
            for (int i = 0; i < argumentNodes.size(); i++) {
                FunctionArgumentNode argumentNode = argumentNodes.get(i);
                argumentNode.accept(this);
                if (!argumentList.contains(argumentNode)) {
                    continue;
                }
                if (i == 0) {
                    inlayHintLocations.put(argumentNode,
                            openParenToken.lineRange());
                    continue;
                }
                inlayHintLocations.put(argumentNode,
                        argumentNodes.getSeparator(i - 1).lineRange());
            }
        }
    }
}
