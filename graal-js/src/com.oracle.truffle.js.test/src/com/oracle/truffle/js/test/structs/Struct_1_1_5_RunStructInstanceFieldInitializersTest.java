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

public class Struct_1_1_5_RunStructInstanceFieldInitializersTest {

    /**
     * Tests step 1: Constructor inheritance chain.
     * Verifies that field initializers are run in correct order when inheritance is involved
     */
    @Test
    public void testInheritanceChainInitialization() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Base {
                    baseValue = 1;
                }
                struct Derived extends Base {
                    derivedValue = this.baseValue + 1;
                }
                let d = new Derived();
                if (d.baseValue !== 1 || d.derivedValue !== 2) {
                    throw new Error('Field initializers not executed in correct order');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 2-3: Field initialization for multiple fields.
     */
    @Test
    public void testMultipleFieldInitializers() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    field1 = 1;
                    field2 = 2;
                    field3 = 3;
                }
                let t = new Test();
                if (t.field1 !== 1 || t.field2 !== 2 || t.field3 !== 3) {
                    throw new Error('Multiple field initializers not executed correctly');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests the interaction between private and public fields in the initialization process.
     */
    @Test
    public void testPrivatePublicFieldInitialization() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    #privateField = 42;
                    publicField = this.#privateField + 1;
                    getPrivate() { return this.#privateField; }
                }
                let t = new Test();
                if (t.getPrivate() !== 42 || t.publicField !== 43) {
                    throw new Error('Private and public field initialization failed');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests initialization order in a complex inheritance chain.
     */
    @Test
    public void testComplexInheritanceInitialization() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct A {
                    a = 1;
                }
                struct B extends A {
                    b = this.a + 1;
                }
                struct C extends B {
                    c = this.b + 1;
                }
                let c = new C();
                if (c.a !== 1 || c.b !== 2 || c.c !== 3) {
                    throw new Error('Complex inheritance initialization failed');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests that field initializers have access to instance methods.
     */
    @Test
    public void testFieldInitializerWithInstanceMethods() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Calculator {
                    getValue() { return 42; }
                    field = this.getValue();
                }
                let calc = new Calculator();
                if (calc.field !== 42) {
                    throw new Error('Field initializer could not access instance method');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
}
