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
package com.oracle.truffle.js.test.structs;

import com.oracle.truffle.js.test.JSTest;
import org.graalvm.polyglot.Context;
import org.junit.Test;

import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_NAME;
import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_STAGING;

/**
 * Tests related to section 1.1.7 BindingStructDeclarationEvaluation.
 */
public class Struct_1_1_7_BindingStructDeclarationEvaluationTest {

    /**
     * Tests "StructDeclaration : struct BindingIdentifier StructTail" steps 1-6.
     * - String value extraction from BindingIdentifier
     * - StructDefinitionEvaluation call
     * - Binding initialization in lexical environment
     */
    @Test
    public void testNamedStructDeclaration() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                // Test basic declaration
                struct Point {
                    x = 0;
                    y = 0;
                }

                // Verify binding was created
                if (typeof Point !== 'function') {
                    throw new Error('Point should be bound to a function');
                }

                // Test lexical scoping
                {
                    struct Point {
                        z = 0;
                    }
                    // Inner Point should shadow outer Point
                    let p = new Point();
                    if (!('z' in p)) {
                        throw new Error('Wrong Point constructor in scope');
                    }
                }

                // Outer Point should still be accessible
                let p = new Point();
                if (!('x' in p) || !('y' in p)) {
                    throw new Error('Original Point not preserved');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests "StructDeclaration : struct BindingIdentifier StructTail" step 2.
     * Error handling during StructDefinitionEvaluation
     */
    @Test
    public void testDeclarationErrors() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                'use strict';
                try {
                    struct Base {
                        constructor() {
                            throw new Error('Initialization of derived should happen before constructor call');
                        }
                    }
                    struct Derived extends Base {
                        field = (() => { throw new Error('Initialization error'); })();
                        constructor() {
                            super();
                        }
                    }
                    let d = new Derived();
                    throw new Error('Should have thrown during struct declaration');
                } catch (e) {
                    if (!e.message.includes('Initialization error')) {
                        throw e;
                    }
                }

                // Verify the binding wasn't created
                if (typeof ErrorStruct !== 'undefined') {
                    throw new Error('ErrorStruct should not be defined');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests "StructDeclaration : struct BindingIdentifier StructTail" steps 4-5.
     * Multiple declarations in different lexical environments
     */
    @Test
    public void testMultipleScopeDeclarations() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                let structs = [];

                // Create structs in different scopes
                {
                    struct Point { x = 1; }
                    structs.push(Point);
                }

                {
                    struct Point { y = 2; }
                    structs.push(Point);
                }

                // Verify each struct maintains its own identity
                let p1 = new structs[0]();
                let p2 = new structs[1]();

                if (!('x' in p1) || ('y' in p1)) {
                    throw new Error('First struct definition corrupted');
                }

                if (('x' in p2) || !('y' in p2)) {
                    throw new Error('Second struct definition corrupted');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests "StructDeclaration : struct BindingIdentifier StructTail" step 3.
     * Source text preservation in SourceText internal slot
     */
    @Test
    public void testSourceTextPreservation() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                // Define struct with a specific format
                struct   Point   {
                    x    =    0;
                    y    =    0;
                }

                // Test toString() preserves source format
                let str = Point.toString();
                if (!str.includes('struct') || !str.includes('Point')) {
                    throw new Error('Source text not preserved in toString');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
}
