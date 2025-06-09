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
import org.junit.Assert;
import org.junit.Test;

import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_NAME;
import static com.oracle.truffle.js.runtime.JSContextOptions.ECMASCRIPT_VERSION_STAGING;

/**
 * Tests related to section 1.1.1 Static Semantics: Early Errors for StructBody.
 */
public class Struct_1_1_1_EarlyErrorsTest {

    @Test
    public void testMultipleConstructors() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source invalidSource = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                constructor() { this.x = 0; }
                constructor() { this.x = 1; }
            }
            """, "test").buildLiteral();
            ctx.eval(invalidSource);
            Assert.fail("Should have thrown SyntaxError for multiple constructors (1.1.1)");
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error", e.isSyntaxError());
            Assert.assertFalse("Should not be incomplete source", e.isIncompleteSource());
        }
    }

    @Test
    public void testDuplicatePrivateFields() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source invalidSource = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                #x;
                #x;
            }
            """, "test").buildLiteral();
            ctx.eval(invalidSource);
            Assert.fail("Should have thrown SyntaxError for duplicate private fields (1.1.1)");
        } catch (PolyglotException e) {
            Assert.assertTrue("Should be a syntax error", e.isSyntaxError());
            Assert.assertFalse("Should not be incomplete source", e.isIncompleteSource());
        }
    }

    /**
     * Verifies the exception for getter/setter pairs.
     */
    @Test
    public void testValidGetterSetterPair() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source validSource = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                #x = 0;
                get #coord() { return this.#x; }
                set #coord(value) { this.#x = value; }
            }
            """, "test").buildLiteral();
            ctx.eval(validSource);
            // If we reach here, the parsing was successful
        }
    }
}
