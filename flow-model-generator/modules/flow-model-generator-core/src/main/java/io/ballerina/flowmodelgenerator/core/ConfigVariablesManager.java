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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypedBindingPatternNode;
import io.ballerina.flowmodelgenerator.core.model.FlowNode;
import io.ballerina.flowmodelgenerator.core.model.NodeBuilder;
import io.ballerina.flowmodelgenerator.core.model.NodeKind;
import io.ballerina.flowmodelgenerator.core.model.Property;
import io.ballerina.modelgenerator.commons.CommonUtils;
import io.ballerina.projects.Document;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.eclipse.lsp4j.TextEdit;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage the configurable variables.
 *
 * @since 1.0.0
 */
public class ConfigVariablesManager {

    public static final String DEFAULTABLE = "defaultable";
    private final Gson gson;

    public ConfigVariablesManager() {
        this.gson = new Gson();
    }

    public JsonElement get(Map<Document, SemanticModel> documentSemanticModelMap) {
        List<FlowNode> configVariables = new ArrayList<>();
        for (Map.Entry<Document, SemanticModel> entry : documentSemanticModelMap.entrySet()) {
            SyntaxTree syntaxTree = entry.getKey().syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            SemanticModel semanticModel = entry.getValue();
            for (Node node : modulePartNode.children()) {
                if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
                    ModuleVariableDeclarationNode modVarDeclarationNode = (ModuleVariableDeclarationNode) node;
                    if (hasConfigurableQualifier(modVarDeclarationNode)) {
                        configVariables.add(genConfigVariable(modVarDeclarationNode, semanticModel));
                    }
                }
            }
        }
        return gson.toJsonTree(configVariables);
    }

    private static boolean hasConfigurableQualifier(ModuleVariableDeclarationNode modVarDeclarationNode) {
        return modVarDeclarationNode.qualifiers()
                .stream().anyMatch(q -> q.text().equals(Qualifier.CONFIGURABLE.getValue()));
    }

    private FlowNode genConfigVariable(ModuleVariableDeclarationNode modVarDeclNode, SemanticModel semanticModel) {
        DiagnosticHandler diagnosticHandler = new DiagnosticHandler(semanticModel);
        NodeBuilder nodeBuilder = NodeBuilder.getNodeFromKind(NodeKind.CONFIG_VARIABLE)
                .semanticModel(semanticModel)
                .diagnosticHandler(diagnosticHandler)
                .defaultModuleName(null);
        diagnosticHandler.handle(nodeBuilder, modVarDeclNode.lineRange(), false);

        TypedBindingPatternNode typedBindingPattern = modVarDeclNode.typedBindingPattern();
        return nodeBuilder
                .metadata()
                .label("Config variables")
                .stepOut()
                .codedata()
                .node(NodeKind.CONFIG_VARIABLE)
                .lineRange(modVarDeclNode.lineRange())
                .stepOut()
                .properties()
                .type(typedBindingPattern.typeDescriptor(), true)
                .variableName(typedBindingPattern.bindingPattern().toSourceCode().trim())
                .defaultValue(modVarDeclNode.initializer().orElse(null))
                .stepOut()
                .build();
    }

    public JsonElement update(Document document, Path configFile, FlowNode configVariable) {
        LineRange lineRange = configVariable.codedata().lineRange();
        Map<String, Property> properties = configVariable.properties();
        String configStatement = constructConfigStatement(properties);

        List<TextEdit> textEdits = new ArrayList<>();
        if (isNew(configVariable) || lineRange == null) {
            SyntaxTree syntaxTree = document.syntaxTree();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            LinePosition startPos = LinePosition.from(modulePartNode.lineRange().endLine().line() + 1, 0);
            textEdits.add(new TextEdit(CommonUtils.toRange(startPos), configStatement));
        } else {
            textEdits.add(new TextEdit(CommonUtils.toRange(lineRange), configStatement));
        }

        Map<Path, List<TextEdit>> textEditsMap = new HashMap<>();
        textEditsMap.put(configFile, textEdits);
        return gson.toJsonTree(textEditsMap);
    }

    private String constructConfigStatement(Map<String, Property> properties) {
        String value = properties.get(DEFAULTABLE).toSourceCode();
        return String.format("configurable %s %s = %s;",
                properties.get(Property.TYPE_KEY).toSourceCode(),
                properties.get(Property.VARIABLE_KEY).toSourceCode(),
                value.isEmpty() ? "?" : value
        );
    }

    private boolean isNew(FlowNode configVariable) {
        return configVariable.codedata().isNew() != null && configVariable.codedata().isNew();
    }
}
