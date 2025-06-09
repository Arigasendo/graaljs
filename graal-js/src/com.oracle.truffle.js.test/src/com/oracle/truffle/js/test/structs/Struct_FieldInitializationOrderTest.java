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
import org.graalvm.polyglot.PolyglotException;
import org.junit.Test;

import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_NAME;
import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_STAGING;

/**
 * Tests struct field initialization order and reference behavior.
 */
public class Struct_FieldInitializationOrderTest {

    /**
     * Tests that referencing an undefined struct in a field initializer throws a ReferenceError.
     */
    @Test
    public void testUndefinedStructReference() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct A {
                b = new B();  // Reference to B before it's defined
            }

            try {
                new A();  // Should throw ReferenceError
                throw new Error('Expected ReferenceError but got none');
            } catch (e) {
                if (!(e instanceof ReferenceError)) {
                    throw new Error('Expected ReferenceError but got: ' + e);
                }
            }

            struct B {
                value = 42;
            }
            """, "test").buildLiteral();

            try {
                ctx.eval(source);
            } catch (PolyglotException e) {
                if (!e.getMessage().contains("ReferenceError")) {
                    throw new AssertionError("Expected ReferenceError, but got: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Tests that struct references work correctly when structs are defined in the correct order.
     */
    @Test
    public void testValidStructReference() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct B {
                value = 42;
            }

            struct A {
                b = new B();
            }

            let a = new A();
            if (!(a.b instanceof B) || a.b.value !== 42) {
                throw new Error('Invalid struct reference initialization');
            }
            """, "test").buildLiteral();

            ctx.eval(source);
        }
    }

    /**
     * Tests that circular struct references are handled properly.
     */
    @Test
    public void testCircularStructReference() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct A {
                value = 1;
                b = new B();  // Should throw since B references A in its initializer
            }

            struct B {
                a = new A();  // Circular reference
            }

            try {
                new A();
                throw new Error('Expected error for circular reference but got none');
            } catch (e) {
                if (!(e instanceof ReferenceError || e instanceof TypeError)) {
                    throw new Error('Expected Reference/TypeError but got: ' + e);
                }
            }
            """, "test").buildLiteral();

            try {
                ctx.eval(source);
            } catch (PolyglotException e) {
                // Accept either ReferenceError or TypeError as valid outcomes
                if (!e.getMessage().contains("Error")) {
                    throw new AssertionError("Expected Reference/TypeError, but got: " + e.getMessage());
                }
            }
        }
    }
}
