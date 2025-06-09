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

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Executed;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.objects.JSDynamicObject;
import com.oracle.truffle.js.runtime.objects.JSObject;

import java.util.Set;

public abstract class RunStructInstanceFieldInitializersNode extends JavaScriptNode {
    protected final JSContext context;
    @Child protected JavaScriptNode targetNode;
    @Child @Executed protected JavaScriptNode constructorNode;
    @Child @Executed protected JavaScriptNode constructorProtoNode;
    @CompilerDirectives.CompilationFinal  private JSDynamicObject functionPrototype;

    protected RunStructInstanceFieldInitializersNode(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode) {
        this.context = context;
        this.targetNode = targetNode;
        this.constructorNode = constructorNode;
        this.constructorProtoNode = PropertyNode.createProperty(context, constructorNode, JSObject.PROTO);
    }

    public static JavaScriptNode create(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode) {
        return RunStructInstanceFieldInitializersNodeGen.create(context, targetNode, constructorNode);
    }

    @Specialization(guards = {"isDerived(constructor, constructorProto)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeWithInheritance(
            VirtualFrame frame,
            @SuppressWarnings("unused") Object constructor,
            @SuppressWarnings("unused") Object constructorProto,
            @Cached("createDerivedInitializeNode(context, targetNode, constructorNode, constructorProtoNode)") JavaScriptNode initializeInstanceElementsNode) {
        return initializeInstanceElementsNode.execute(frame);
    }

    @Specialization(guards = {"!isDerived(constructor, constructorProto)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeNoInheritance(
            VirtualFrame frame,
            @SuppressWarnings("unused") Object constructor,
            @SuppressWarnings("unused") Object constructorProto,
            @Cached("createInitializeInstanceElementsNode(context, targetNode, constructorNode)") JavaScriptNode initializeInstanceElementsNode) {
        return initializeInstanceElementsNode.execute(frame);
    }

    protected boolean isFunctionPrototype(Object prototype) {
        if (functionPrototype == null) {
            CompilerDirectives.transferToInterpreterAndInvalidate();
            functionPrototype = getRealm().getFunctionPrototype();
        }
        return functionPrototype == prototype;
    }

    protected boolean isDerived(Object constructor, Object prototype) {
        return JSFunction.isDerived(constructor) || !isFunctionPrototype(prototype);
    }

    @NeverDefault
    protected JavaScriptNode createDerivedInitializeNode(JSContext ctx, JavaScriptNode target, JavaScriptNode constructor, JavaScriptNode constructorProto) {
        JavaScriptNode initializedByParentTarget = create(ctx, target, constructorProto);
        return createInitializeInstanceElementsNode(ctx, initializedByParentTarget, constructor);
    }

    @NeverDefault
    protected JavaScriptNode createInitializeInstanceElementsNode(JSContext ctx, JavaScriptNode target, JavaScriptNode constructor) {
        return InitializeInstanceElementsNode.create(ctx, target, constructor);
    }

    @Override
    protected JavaScriptNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        return create(context, cloneUninitialized(targetNode, materializedTags), cloneUninitialized(constructorNode, materializedTags));
    }
}
