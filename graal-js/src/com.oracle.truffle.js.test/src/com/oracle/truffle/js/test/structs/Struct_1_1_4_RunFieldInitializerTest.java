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
 * Tests related to section 1.1.4 RunFieldInitializer.
 */
public class Struct_1_1_4_RunFieldInitializerTest {

    /**
     * Tests steps 2-3: Field initialization with explicit initializer.
     */
    @Test
    public void testExplicitInitializer() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        struct Point {
            x = 10;  // explicit initializer
            y = 20;  // explicit initializer
        }
        let p = new Point();
        if (p.x !== 10 || p.y !== 20) {
            throw new Error('Field initializers not properly executed');
        }
        """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 3.b, 3.b.i: Private field initialization.
     */
    @Test
    public void testPrivateFieldInitializer() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        struct Point {
            #x = 42;  // private field with initializer
            getX() { return this.#x; }
        }
        let p = new Point();
        if (p.getX() !== 42) {
            throw new Error('Private field initializer not properly executed');
        }
        """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 3.c.i, 3.c.ii: Public field property descriptor after initialization.
     */
    @Test
    public void testPublicFieldPropertyDescriptor() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        struct Point {
            x = 100;
        }
        let p = new Point();
        let xDesc = Object.getOwnPropertyDescriptor(p, 'x');
        if (xDesc.value !== 100 || !xDesc.writable || !xDesc.enumerable || xDesc.configurable) {
            throw new Error('Field property descriptor not properly set');
        }
        """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests step 3.a: Initializer context (this binding).
     */
    @Test
    public void testInitializerContext() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        struct Point {
            x = 1;
            y = this.x + 1;  // should have access to previously initialized x
        }
        let p = new Point();
        if (p.y !== 2) {
            throw new Error('Initializer context (this binding) not properly set');
        }
        """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests step 3.a: Initializer throwing error.
     */
    @Test
    public void testInitializerError() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        struct Point {
            x = (() => { throw new Error('Initialization error'); })();
        }
        new Point();
        """, "test").buildLiteral();
            try {
                ctx.eval(source);
                Assert.fail("Should have thrown error from initializer");
            } catch (PolyglotException e) {
                Assert.assertTrue("Should be a runtime error", e.isGuestException());
                Assert.assertEquals("Should have correct error message", "Error: Initialization error", e.getMessage());
            }
        }
    }
}
