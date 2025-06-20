/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
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

package io.ballerina.modelgenerator.commons;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a search result containing package information, name, description, and additional attributes.
 *
 * @param packageInfo The package information containing organization, name and version
 * @param name        The name of the component
 * @param description The description of the component
 * @param attributes  Additional attributes as key-value pairs
 * @since 1.0.0
 */
public record SearchResult(Package packageInfo, String name, String description, Map<String, String> attributes) {

    public static SearchResult from(Package packageInfo, String name, String description) {
        return new SearchResult(packageInfo, name, description, new HashMap<>());
    }

    public static SearchResult from(String packageOrg, String packageName, String moduleName,
                                    String packageVersion, String name,
                                    String description) {
        return from(new Package(packageOrg, packageName, moduleName, packageVersion), name, description);
    }

    /**
     * Sorts the given list of SearchResult based on the order of package names in packageList.
     *
     * @param searchResults the list of SearchResult to sort
     * @param packageList   the list of package identifiers
     */
    public static void sortByPackageListOrder(List<SearchResult> searchResults, List<String> packageList) {
        Map<String, Integer> packageOrder = new HashMap<>(packageList.size());
        for (int i = 0; i < packageList.size(); i++) {
            String packageName = packageList.get(i).split(":", 2)[0];
            packageOrder.put(packageName, i);
        }
        searchResults.sort(Comparator.comparingInt(
                a -> packageOrder.getOrDefault(a.packageInfo().packageName(), Integer.MAX_VALUE)));
    }

    public record Package(String org, String packageName, String moduleName, String version) {
    }
}
