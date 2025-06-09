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
 * Tests related to section 1.1.6 StructDefinitionEvaluation.
 */
public class Struct_1_1_6_StructDefinitionEvaluationTest {

    /**
     * Tests steps 3-4: Struct binding and private environment creation.
     */
    @Test
    public void testStructBindingImmutable() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            class TestClass {
                constructor() {
                    try {
                        TestClass = "modified";
                        throw new Error('Should not be able to assign to class name');
                    } catch (e) {
                        if (!(e instanceof TypeError)) {
                            throw new Error('Expected TypeError when assigning to class name');
                        }
                    }
                }
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
    /**
     * Tests steps 21c If newTarget is not the struct constructor, throw TypeError.
     */
    @Test
    public void testNewTargetNotStructConstructor() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                'use strict'
                struct MyStruct {
                   constructor() {
                     // F is MyStruct's constructor.
                     // NewTarget is MyOtherConstructor.
                     // They are not the same, so this throws.
                   }
                 }

                 function MyOtherConstructor() { /* ... */ }

                try {
                    Reflect.construct(MyStruct, [], MyOtherConstructor);
                    throw new Error('Should throw TypeError when newTarget is not the struct constructor');
                } catch (e) {
                    if (!(e instanceof TypeError)) { throw e }
                }

            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }


    /**
     * Tests steps 7-8: Inheritance handling for non-struct parent.
     */
    @Test
    public void testInvalidInheritance() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                class NotAStruct {}
                struct Point extends NotAStruct {}
                """, "test").buildLiteral();
            try {
                ctx.eval(source);
                Assert.fail("Should have thrown TypeError for extending non-struct");
            } catch (PolyglotException e) {
                Assert.assertTrue("Should be a TypeError", e.isGuestException());
                Assert.assertTrue(e.getMessage().contains("TypeError"));
            }
        }
    }

    /**
     * Tests steps 7-8: Inheritance from null.
     */
    @Test
    public void testNullInheritance() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Point {
                    x = 1;
                    static #test() {
                        return "";
                    }
                }
                let p = new Point();
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 9-12: Struct brand and serial number assignment.
     */
    @Test
    public void testStructBrand() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct A {}
                struct B {}
                let a1 = new A();
                let a2 = new A();
                let b = new B();

                // Test that instances of same struct share brand
                let a1proto = Object.getPrototypeOf(a1);
                let a2proto = Object.getPrototypeOf(a2);
                if (a1proto !== a2proto) {
                    throw new Error('Instances of same struct should share prototype');
                }

                // Test that different structs have different brands
                let bproto = Object.getPrototypeOf(b);
                if (a1proto === bproto) {
                    throw new Error('Different structs should have different prototypes');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 17-20: Constructor method handling.
     */
    @Test
    public void testConstructorMethod() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Point {
                    x = 0;
                    y = 0;
                    constructor(x, y) {
                        this.x = x;
                        this.y = y;
                    }
                }
                let p = new Point(10, 20);
                if (p.x !== 10 || p.y !== 20) {
                    throw new Error('Constructor not properly executed');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests step 21.c: NewTarget validation.
     */
    @Test
    public void testNewTargetValidation() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Point {
                    x = 0;
                }
                try {
                    Point(); // Call without new
                    throw new Error('Should throw when called without new');
                } catch (e) {
                    if (!(e instanceof TypeError)) {
                        throw e;
                    }
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 21.g-h: Instance sealing.
     */
    @Test
    public void testInstanceSealing() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                'use strict';
                struct Point {
                    x = 1;
                }
                let p = new Point();

                // Test that existing properties are writable
                p.x = 2;
                if (p.x !== 2) {
                    throw new Error('Should be able to modify existing properties');
                }

                // Test that new properties cannot be added
                try {
                    p.y = 3;
                    throw new Error('Should not be able to add new properties');
                } catch (e) {
                    if (!(e instanceof TypeError)) {
                        throw e;
                    }
                }

                // Test that properties cannot be deleted
                try {
                    delete p.x;
                    console.log("Jou2")
                    throw new Error('Should not be able to delete properties');
                } catch (e) {
                    if (!(e instanceof TypeError)) {
                        throw e;
                    }
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 22-27: Constructor function creation and initialization.
     */
    @Test
    public void testConstructorCreation() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Point {
                    x = 0;
                    constructor(x) {
                        this.x = x;
                    }
                }
                // Test constructor properties
                if (typeof Point !== 'function') {
                    throw new Error('Constructor should be a function');
                }
                if (Point.name !== 'Point') {
                    throw new Error('Constructor should have correct name');
                }
                if (!Point.prototype) {
                    throw new Error('Constructor should have prototype');
                }
                if (!(new Point(5) instanceof Point)) {
                    throw new Error('instanceof should work with constructor');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 24, 34.a: Derived constructor handling.
     */
    @Test
    public void testDerivedConstructor() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Base {
                    x = 1;
                }
                struct Derived extends Base {
                    y = 2;
                }
                let d = new Derived();
                if (d.x !== 1 || d.y !== 2) {
                    throw new Error('Derived constructor not properly initialized');
                }
                if (!(d instanceof Base) || !(d instanceof Derived)) {
                    throw new Error('Inheritance chain not properly set up');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 30-34: Private methods and fields handling.
     */
    @Test
    public void testPrivateMethodsAndFields() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    #privateField = 42;
                    #privateMethod() { return this.#privateField; }
                    getPrivate() { return this.#privateMethod(); }
                }
                let t = new Test();
                if (t.getPrivate() !== 42) {
                    throw new Error('Private methods not properly initialized');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 34.b-34.g: Static elements handling.
     */
    @Test
    public void testStaticElements() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    static staticField = 'static';
                    static #privateStatic = 42;
                    static getPrivateStatic() { return this.#privateStatic; }
                    static {
                        if (this.staticField !== 'static') {
                            throw new Error('Static field not initialized in static block');
                        }
                    }
                }
                if (Test.staticField !== 'static') {
                    throw new Error('Static field not properly initialized');
                }
                if (Test.getPrivateStatic() !== 42) {
                    throw new Error('Static private field not properly initialized');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 34.e: Private accessor combination.
     */
    @Test
    public void testPrivateAccessors() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    #value = 0;
                    get #field() { return this.#value; }
                    set #field(x) { this.#value = x; }

                    getValue() { return this.#field; }
                    setValue(x) { this.#field = x; }
                }
                let t = new Test();
                t.setValue(42);
                if (t.getValue() !== 42) {
                    throw new Error('Private accessors not properly combined');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
    /**
     * Tests step 34.c: Error handling during element evaluation.
     */
    @Test
    public void testElementEvaluationError() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                struct Test {
                    field = (() => { throw new Error('Field initialization error'); })();
                }
                let t = new Test();
                """, "test").buildLiteral();
            try {
                ctx.eval(source);
                Assert.fail("Should have thrown error during field initialization");
            } catch (PolyglotException e) {
                Assert.assertTrue("Should be a guest exception", e.isGuestException());
                Assert.assertTrue(e.getMessage().contains("Field initialization error"));
            }
        }
    }

    /**
     * Tests steps 42-43: Prototype sealing and final state.
     */
    @Test
    public void testPrototypeSealing() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                'use strict';
                struct Test {
                    method() {}
                }
                let proto = Test.prototype;

                // Test that prototype is sealed
                try {
                    proto.newMethod = function() {};
                    throw new Error('Should not be able to add methods to prototype');
                } catch (e) {
                    if (!(e instanceof TypeError)) throw e;
                }

                try {
                    delete proto.method;
                    throw new Error('Should not be able to delete methods from prototype');
                } catch (e) {
                    if (!(e instanceof TypeError)) throw e;
                }

                // Verify existing method still works
                let t = new Test();
                if (typeof t.method !== 'function') {
                    throw new Error('Original method should still exist');
                }
                """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
}
