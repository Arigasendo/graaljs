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
 * Tests related to section 1.1.2 DefineStructField abstract operation and struct method definitions.
 */
public class Struct_1_1_2_DefineStructFieldTest {

    /**
     * Tests step 2-3: If fieldName is a regular property key (not Private Name).
     * it must be defined with PropertyDescriptor { [[Value]]: undefined, [[Writable]]: true,
     * [[Enumerable]]: true, [[Configurable]]: false }
     */
    @Test
    public void testRegularField() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                x;  // regular field
                y;  // regular field
                test(){
                    console.log(this.x, this.y);  // should not throw - verifies regular fields were defined
                }
            }
            let p = new Point();
            p.test()
            // Verify field properties
            let xDesc = Object.getOwnPropertyDescriptor(p, 'x');

            if (!xDesc.configurable && xDesc.enumerable && xDesc.writable) {
                // success
            } else {
                console.log(xDesc.configurable, xDesc.enumerable, xDesc.writable);
                x = !xDesc.configurable ? "" : "configurable ";
                y = xDesc.enumerable ? "" : "enumerable ";
                z = xDesc.writable ? "" : "writable ";
                message = `Field '${x}${y}${z}' has wrong value`;
                throw new Error('Field descriptor has wrong attributes. ' + message);
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testRegularDerivedField() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Base {
                x;  // regular field
            }
            struct Derived extends Base {
                y;  // regular field
            }
            let d = new Derived();
            // Verify field properties
            let xDesc = Object.getOwnPropertyDescriptor(d, 'x');
            let yDesc = Object.getOwnPropertyDescriptor(d, 'y');

            if (!xDesc.configurable && xDesc.enumerable && xDesc.writable) {
                // success
            } else {
                console.log(xDesc.configurable, xDesc.enumerable, xDesc.writable);
                x = !xDesc.configurable ? "" : "configurable ";
                y = xDesc.enumerable ? "" : "enumerable ";
                z = xDesc.writable ? "" : "writable ";
                message = `Field '${x}${y}${z}' has wrong value`;
                throw new Error('Field descriptor of base has wrong attributes. ' + message);
            }
            if (!yDesc.configurable && yDesc.enumerable && yDesc.writable) {
                // success
            } else {
                console.log(yDesc.configurable, yDesc.enumerable, yDesc.writable);
                x = !yDesc.configurable ? "" : "configurable ";
                y = yDesc.enumerable ? "" : "enumerable ";
                z = yDesc.writable ? "" : "writable ";
                message = `Field '${x}${y}${z}' has wrong value`;
                throw new Error('Field descriptor of derived has wrong attributes. ' + message);
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testRegularDerivedField2() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Base {
                x;  // regular field
                constructor() {
                }
            }
            struct Derived extends Base {
                y;  // regular field
                constructor() {
                    super();
                }
            }
            let d = new Derived();
            // Verify field properties
            let xDesc = Object.getOwnPropertyDescriptor(d, 'x');
            let yDesc = Object.getOwnPropertyDescriptor(d, 'y');

            if (!xDesc.configurable && xDesc.enumerable && xDesc.writable) {
                // success
            } else {
                console.log(xDesc.configurable, xDesc.enumerable, xDesc.writable);
                x = !xDesc.configurable ? "" : "configurable ";
                y = xDesc.enumerable ? "" : "enumerable ";
                z = xDesc.writable ? "" : "writable ";
                message = `Field '${x}${y}${z}' has wrong value`;
                throw new Error('Field descriptor of base has wrong attributes. ' + message);
            }
            if (!yDesc.configurable && yDesc.enumerable && yDesc.writable) {
                // success
            } else {
                console.log(yDesc.configurable, yDesc.enumerable, yDesc.writable);
                x = !yDesc.configurable ? "" : "configurable ";
                y = yDesc.enumerable ? "" : "enumerable ";
                z = yDesc.writable ? "" : "writable ";
                message = `Field '${x}${y}${z}' has wrong value`;
                throw new Error('Field descriptor of derived has wrong attributes. ' + message);
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests step 2.a: If fieldName is a Private Name, perform PrivateFieldAdd(receiver, fieldName, undefined).
     */
    @Test
    public void testPrivateField() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                #x;  // private field
                get x() { return this.#x; }  // accessor to verify private field exists
            }
            let p = new Point();
            p.x;  // should not throw - verifies private field was added
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests steps 2.a and 3.b: Both private and regular fields must be initialized with undefined.
     */
    @Test
    public void testInitialValue() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            struct Point {
                x;      // regular field, should be undefined
                #y;    // private field, should be undefined
                get y() { return this.#y; }
            }
            let p = new Point();
            if (p.x !== undefined || p.y !== undefined) {
                throw new Error('Fields should be initialized to undefined');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    /**
     * Tests that struct methods are properly defined and can be executed.
     */
    @Test
    public void testStructMethod() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
                    struct Point {
                        x;
                        y;
                        setCoords(x, y) {
                            this.x = x;
                            this.y = y;
                            return this;
                        }
                        getSum() {
                            return this.x + this.y;
                        }
                    }
                    let p = new Point();
                    p.setCoords(10, 20);
                    if (p.getSum() !== 30) {
                        throw new Error('Struct method returned incorrect result');
                    }
                    """, "test").buildLiteral();
            ctx.eval(source);
        }
    }
}
