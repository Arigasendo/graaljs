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
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.dsl.Cached.Shared;
import com.oracle.truffle.api.dsl.Executed;
import com.oracle.truffle.api.dsl.NeverDefault;
import com.oracle.truffle.api.dsl.Specialization;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.instrumentation.Tag;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.js.nodes.JavaScriptBaseNode;
import com.oracle.truffle.js.nodes.JavaScriptNode;
import com.oracle.truffle.js.nodes.function.ClassElementDefinitionRecord;
import com.oracle.truffle.js.runtime.JSContext;
import com.oracle.truffle.js.runtime.builtins.JSFunction;
import com.oracle.truffle.js.runtime.builtins.JSFunctionObject;
import com.oracle.truffle.js.runtime.objects.JSClassObject;
import com.oracle.truffle.js.runtime.objects.JSDynamicObject;
import com.oracle.truffle.js.runtime.objects.JSObject;
import com.oracle.truffle.js.runtime.objects.PropertyDescriptor;
import com.oracle.truffle.js.runtime.objects.Undefined;

import java.util.Set;

public abstract class DefineStructInstanceFieldsAndBrandNode extends JavaScriptNode {
    protected final JSContext context;
    @Child PropertySetNode structBrandsSetNode;
    @Child PropertyNode structBrandsGetNode;
    @Child @Executed protected JavaScriptNode targetNode;
    @Child @Executed protected JavaScriptNode constructorNode;
    @Child @Executed(with = "constructorNode") protected JSTargetableNode elementsNode;
    @Child @Executed(with = "constructorNode") protected JSTargetableNode constructorProtoNode;
    @CompilationFinal private JSDynamicObject functionPrototype;
    private final boolean isRecursiveNode;

    protected DefineStructInstanceFieldsAndBrandNode(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode, boolean isRecursiveNode) {
        this.context = context;
        this.targetNode = targetNode;
        this.constructorNode = constructorNode;
        this.isRecursiveNode = isRecursiveNode;

        if (constructorNode != null) {
            this.elementsNode = PropertyNode.createGetHidden(context, null, JSFunction.CLASS_ELEMENTS_ID);
            this.constructorProtoNode = PropertyNode.createProperty(context, null, JSObject.PROTO);
        }
        if (!isRecursiveNode) {
            this.structBrandsSetNode = PropertySetNode.createSetHidden(JSObject.STRUCT_BRANDS, context);
            this.structBrandsGetNode = PropertyNode.createGetHidden(context, null, JSObject.STRUCT_BRANDS);
        }
    }

    protected static DefineStructInstanceFieldsAndBrandNode create(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode, boolean isRecursiveNode) {
        return DefineStructInstanceFieldsAndBrandNodeGen.create(context, targetNode, constructorNode, isRecursiveNode);
    }

    public static DefineStructInstanceFieldsAndBrandNode create(JSContext context, JavaScriptNode targetNode, JavaScriptNode constructorNode) {
        return create(context, targetNode, constructorNode, false);
    }

    @Specialization(guards = {"isDerived(constructor, constructorProto)", "isUndefined(elements)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeWithInheritanceNoElements(
            VirtualFrame frame,
            @SuppressWarnings("unused") Object target,
            JSFunctionObject constructor,
            @SuppressWarnings("unused") Object elements,
            @SuppressWarnings("unused") Object constructorProto,
            @Cached("createParentInitNode(constructorProto)") DefineStructInstanceFieldsAndBrandNode parentNode) {
        Object initializedTarget = parentNode.execute(frame);

        if (!this.isRecursiveNode) {
            setTargetStructBrands(initializedTarget, constructor);
        }

        return initializedTarget;
    }

    @Specialization(guards = {"isDerived(constructor, constructorProto)", "!isUndefined(elements)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeWithInheritance(
            VirtualFrame frame,
            @SuppressWarnings("unused") Object target,
            JSFunctionObject constructor,
            ClassElementDefinitionRecord[] elements,
            @SuppressWarnings("unused") Object constructorProto,
            @Cached("createParentInitNode(constructorProto)") DefineStructInstanceFieldsAndBrandNode parentNode,
            @Cached("createFieldInitializationNodes(elements)") @Shared StructInstanceInitializerNode[] initializerNodes) {
        var initializedTarget = parentNode.execute(frame);

        if (!this.isRecursiveNode) {
            setTargetStructBrands(initializedTarget, constructor);
        }

        int size = initializerNodes.length;
        assert size == elements.length;
        for (int i = 0; i < size; i++) {
            initializerNodes[i].define(initializedTarget, elements[i]);
        }

        return initializedTarget;
    }

    @Specialization(guards = {"!isDerived(constructor, constructorProto)", "isUndefined(elements)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeNoInheritanceNoElements(
            @SuppressWarnings("unused") VirtualFrame frame,
            JSObject target,
            JSFunctionObject constructor,
            @SuppressWarnings("unused") Object elements,
            @SuppressWarnings("unused") Object constructorProto) {
        if (!this.isRecursiveNode) {
            setTargetStructBrands(target, constructor);
        }
        return target;
    }

    @Specialization(guards = {"!isDerived(constructor, constructorProto)", "!isUndefined(elements)"})
    @ExplodeLoop(kind = ExplodeLoop.LoopExplosionKind.FULL_UNROLL)
    protected Object initializeNoInheritance(
            @SuppressWarnings("unused") VirtualFrame frame,
            JSObject target,
            JSFunctionObject constructor,
            ClassElementDefinitionRecord[] elements,
            @SuppressWarnings("unused") Object constructorProto,
            @Cached("createFieldInitializationNodes(elements)") @Shared StructInstanceInitializerNode[] initializerNodes) {
        if (!this.isRecursiveNode) {
            setTargetStructBrands(target, constructor);
        }
        int size = initializerNodes.length;
        assert size == elements.length;
        for (int i = 0; i < size; i++) {
            initializerNodes[i].define(target, elements[i]);
        }
        return target;
    }

    private void setTargetStructBrands(Object target, JSFunctionObject constructor) {
        Object structBrands = this.structBrandsGetNode.executeWithTarget(constructor);
        this.structBrandsSetNode.setValue(target, structBrands);
    }

    protected static boolean isUndefined(Object obj) {
        return obj == Undefined.instance;
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
    protected DefineStructInstanceFieldsAndBrandNode createParentInitNode(Object prototype) {
        JavaScriptNode prototypeNode = JSConstantNode.create(prototype);
        return DefineStructInstanceFieldsAndBrandNode.create(context, targetNode,
                prototypeNode, true);
    }

    @NeverDefault
    protected StructInstanceInitializerNode[] createFieldInitializationNodes(ClassElementDefinitionRecord[] fields) {
        int size = fields.length;
        StructInstanceInitializerNode[] nodes = new StructInstanceInitializerNode[size];
        for (int i = 0; i < size; i++) {
            ClassElementDefinitionRecord field = fields[i];
            if (field.isField()) {
                if (field.isPrivate()) {
                    assert field.getBackingStorageKey() != null : field.getKey();
                    nodes[i] = StructInstanceFieldInitializerNode.createPrivateField();
                } else {
                    nodes[i] = StructInstanceFieldInitializerNode.createNonPrivateField();
                }
            } else if (field.isAutoAccessor()) {
                assert field.getBackingStorageKey() != null : field.getKey();
                nodes[i] = StructInstanceFieldInitializerNode.createPrivateField();
            } else {
                nodes[i] = StructNonFieldInitializerNode.createNonField();
            }
        }
        return nodes;
    }

    @Override
    protected JavaScriptNode copyUninitialized(Set<Class<? extends Tag>> materializedTags) {
        return create(context,
                cloneUninitialized(targetNode, materializedTags),
                cloneUninitialized(constructorNode, materializedTags));
    }

    static final class StructInstanceFieldInitializerNode extends StructInstanceInitializerNode {
        @Child private PrivateFieldAddNode writeNode;

        private StructInstanceFieldInitializerNode(PrivateFieldAddNode writeNode) {
            this.writeNode = writeNode;
        }
        public static StructInstanceFieldInitializerNode createPrivateField() {
            return new StructInstanceFieldInitializerNode(PrivateFieldAddNode.create());
        }

        public static StructInstanceFieldInitializerNode createNonPrivateField() {
            return new StructInstanceFieldInitializerNode(null);
        }

        @Override
        public void define(Object target, ClassElementDefinitionRecord record) {
            assert record.isField() || record.isAutoAccessor();

            if (writeNode != null) {
                assert record.isPrivate();
                writeNode.execute(target, record.getKey(), Undefined.instance);
            } else {
                assert target instanceof JSClassObject : target;
                JSClassObject targetObject = (JSClassObject) target;
                PropertyDescriptor desc = PropertyDescriptor.createData(
                        Undefined.instance,
                        true,
                        true,
                        false
                );
                boolean success = JSObject.getJSClass(targetObject).defineOwnProperty(targetObject, record.getKey(), desc, true);
                assert success : "defineOwnProperty should always succeed";
            }
        }
    }

    static final class StructNonFieldInitializerNode extends StructInstanceInitializerNode {
        public static StructNonFieldInitializerNode createNonField() {
            return new StructNonFieldInitializerNode();
        }
        private StructNonFieldInitializerNode() {
        }
        @Override
        public void define(Object targetObject, ClassElementDefinitionRecord record) {
            assert !record.isField();
        }
    }

    protected abstract static sealed class StructInstanceInitializerNode extends JavaScriptBaseNode permits StructInstanceFieldInitializerNode, StructNonFieldInitializerNode {
        abstract void define(Object targetObject, ClassElementDefinitionRecord record);
    }
}
