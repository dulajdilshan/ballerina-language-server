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

package org.ballerinalang.debugadapter.jdi;

import com.sun.jdi.ReferenceType;

import java.util.List;

/**
 * Proxy definition for JDI virtual machine.
 *
 * @since 1.0.0
 */
public interface VirtualMachineProxy {

    List<ReferenceType> allClasses();

    boolean canGetBytecodes();

    boolean versionHigher(String version);

    boolean canWatchFieldModification();

    boolean canWatchFieldAccess();

    boolean canInvokeMethods();

    List<ReferenceType> classesByName(String s);
}
