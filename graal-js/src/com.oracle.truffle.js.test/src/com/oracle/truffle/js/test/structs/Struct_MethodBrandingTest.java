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

public class Struct_MethodBrandingTest {
    @Test
    public void testStructMethodValidCall() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            p.x(); // Should not throw
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithPlainObject() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            let plainObj = {};
            try {
                p.x.call(plainObj);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithConstructorObject() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            let plainObj = {};
            try {
                p.x.call(Point.prototype.constructor);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithNull() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                p.x.call(null);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithUndefined() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                p.x.call(undefined);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithNumber() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                p.x.call(42);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodWithString() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                p.x.call("test");
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodBoundToOtherObject() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            let plainObj = {};
            try {
                let bound = p.x.bind(plainObj);
                bound();
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodApply() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                p.x.apply({});
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodBindAndCall() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            let p = new Point();
            try {
                let bound = p.x.bind({});
                bound.call(p); // Even calling with correct this won't work after wrong bind
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodApplyWithArrayArgs() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x(arg) {
                    return 'Point.x' + arg;
                }
            }
            let p = new Point();
            try {
                p.x.apply({}, [42]);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceValidCall() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                y() {
                    return 'Point3D.y';
                }
            }
            let p3d = new Point3D();
            // Should be able to call both parent and child methods
            p3d.x();
            p3d.y();
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceWithWrongThis() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                y() {
                    return 'Point3D.y';
                }
            }
            let p = new Point();
            let p3d = new Point3D();
            try {
                // Point instance shouldn't be able to call Point3D method
                p3d.y.call(p);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceParentMethodWithChild() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                y() {
                    return 'Point3D.y';
                }
            }
            let p = new Point();
            let p3d = new Point3D();
            // Child should be able to use parent method
            p.x.call(p3d); // This should work
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceOverride() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                x() {
                    return 'Point3D.x';
                }
            }
            let p = new Point();
            let p3d = new Point3D();
            // Should be possible according to proposal
            p.x.call(p3d);
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceMultiLevel() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                y() {
                    return 'Point3D.y';
                }
            }
            struct ColorPoint3D extends Point3D {
                z() {
                    return 'ColorPoint3D.z';
                }
            }

            let cp3d = new ColorPoint3D();
            // Should be able to call all inherited methods

            let p = new Point();
            let p3d = new Point3D();

            try {
                cp3d.z.call(p3d);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw e;
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceSideways() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
            'use strict';
            struct Point {
                x() {
                    return 'Point.x';
                }
            }
            struct Point3D extends Point {
                y() {
                    return 'Point3D.y';
                }
            }
            struct ColorPoint extends Point {
                color() {
                    return 'ColorPoint.color';
                }
            }
            let p3d = new Point3D();
            let cp = new ColorPoint();

            try {
                // Sibling struct methods shouldn't work on each other
                p3d.y.call(cp);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }

            try {
                cp.color.call(p3d);
                throw new Error('Should have thrown TypeError');
            } catch(e) {
                if (!(e instanceof TypeError)) throw new Error('Expected TypeError');
            }
            """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

    @Test
    public void testStructMethodInheritanceParallelChainsWithBaseCalls() {
        try (Context ctx = JSTest.newContextBuilder().option(ECMASCRIPT_VERSION_NAME, ECMASCRIPT_VERSION_STAGING).build()) {
            org.graalvm.polyglot.Source source = org.graalvm.polyglot.Source.newBuilder("js", """
        'use strict';
        // First inheritance chain
        struct Point {
            x() {
                return 'Point.x';
            }
        }
        struct Point3D extends Point {
            y() {
                return 'Point3D.y';
            }
        }
        struct ColorPoint3D extends Point3D {
            z() {
                return 'ColorPoint3D.z';
            }
        }

        // Second inheritance chain
        struct Shape {
            area() {
                return 'Shape.area';
            }
        }
        struct Rectangle extends Shape {
            width() {
                return 'Rectangle.width';
            }
        }
        struct ColorRectangle extends Rectangle {
            color() {
                return 'ColorRectangle.color';
            }
        }

        // Create instances from all levels

        let cp3d = new ColorPoint3D();
        let cr = new ColorRectangle();
        let p3d = new Point3D();
        let rect = new Rectangle();

        let point = new Point();
        let shape = new Shape();

        // Test base class interactions
        try {
            point.x.call(shape);  // Point method with Shape instance
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }

        try {
            shape.area.call(point);  // Shape method with Point instance
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }

        // Test derived-to-base class calls
        try {
            cp3d.z.call(point);  // ColorPoint3D method with Point instance
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }

        try {
            cr.color.call(shape);  // ColorRectangle method with Shape instance
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }

        // Test base-to-derived class calls
        try {
            point.x.call(cp3d);  // This should work - parent method on child instance
            point.x.call(p3d);   // This should work - parent method on child instance
        } catch(e) {
            throw new Error('Base class methods should work on derived instances');
        }

        try {
            shape.area.call(cr);   // This should work - parent method on child instance
            shape.area.call(rect); // This should work - parent method on child instance
        } catch(e) {
            throw new Error('Base class methods should work on derived instances');
        }

        // Verify that cross-hierarchy calls still fail at base level
        try {
            point.x.call(shape);
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }

        try {
            shape.area.call(point);
            throw new Error('Should have thrown TypeError');
        } catch(e) {
            if (!(e instanceof TypeError)) throw e;
        }
        """, "test").buildLiteral();
            ctx.eval(source);
        }
    }

}
