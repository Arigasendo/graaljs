/*
 * Copyright (c) 2018, 2025, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.js.nodes.access;

import com.oracle.truffle.api.dsl.Executed;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.objects.JSObject;

import java.util.Set;

public abstract class PrepareStructInstanceNode extends JavaScriptNode {
    protected final JSContext context;
    @Child protected JavaScriptNode targetNode;
    @Child protected JavaScriptNode constructorNode;
    @Child @Executed protected JavaScriptNode initializedTargetNode;

    protected PrepareStructInstanceNode(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode) {
        this.context = context;
        this.targetNode = targetNode;
        this.constructorNode = constructorNode;
        var structInitializedTargetNode = DefineStructInstanceFieldsAndBrandNode.create(context, targetNode, constructorNode);
        var targetIntegritySet = SetIntegrityLevelNode.create(structInitializedTargetNode);
        this.initializedTargetNode = RunStructInstanceFieldInitializersNode.create(context, targetIntegritySet, constructorNode);
    }

    public static PrepareStructInstanceNode create(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode) {
        return PrepareStructInstanceNodeGen.create(context, targetNode, constructorNode);
    }

    @Specialization
    protected Object initialize(Object initializedTarget) {
        return initializedTarget;
    }

    @Override
    protected JavaScriptNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        return create(context,
                cloneUninitialized(targetNode, materializedTags),
                cloneUninitialized(constructorNode, materializedTags));
    }

    private static final class SetIntegrityLevelNode extends JavaScriptNode {
        @Child private JavaScriptNode targetNode;

        private SetIntegrityLevelNode(JavaScriptNode targetNode) {
            this.targetNode = targetNode;
        }

        public static JavaScriptNode create(JavaScriptNode targetNode) {
            return new SetIntegrityLevelNode(targetNode);
        }

        @Override
        public Object execute(VirtualFrame frame) {
            Object target = targetNode.execute(frame);
            assert JSObject.isJSObject(target);
            JSObject targetJSObject = (JSObject) target;
            targetJSObject.setIntegrityLevel(false, true);
            assert targetJSObject.testIntegrityLevel(false);
            return targetJSObject;
        }
    }
}
