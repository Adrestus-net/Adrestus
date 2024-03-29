/*
 * Copyright 2014 OpenRQ Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2011-2014, by Vladimir Kostyukov and Contributors.
 *
 * This file is part of la4j project (http://la4j.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributor(s): -
 */
package io.Adrestus.erasure.code.util.linearalgebra.io;


import io.Adrestus.erasure.code.util.linearalgebra.factory.Factory;
import io.Adrestus.erasure.code.util.linearalgebra.vector.ByteVector;

public abstract class ByteVectorIterator extends CursorIterator {

    protected final int length;


    public ByteVectorIterator(int length) {

        this.length = length;
    }

    /**
     * Returns an index of the current cell.
     *
     * @return an index of the current cell
     */
    public abstract int index();

    public ByteVectorIterator orElseAdd(final ByteVectorIterator those) {

        return new CursorToVectorIterator(super.orElse(those, JoinFunction.ADD), length);
    }

    public ByteVectorIterator orElseSubtract(final ByteVectorIterator those) {

        return new CursorToVectorIterator(super.orElse(those, JoinFunction.SUB), length);
    }

    public ByteVectorIterator andAlsoMultiply(final ByteVectorIterator those) {

        return new CursorToVectorIterator(super.andAlso(those, JoinFunction.MUL), length);
    }

    public ByteVectorIterator andAlsoDivide(final ByteVectorIterator those) {

        return new CursorToVectorIterator(super.andAlso(those, JoinFunction.DIV), length);
    }

    @Override
    protected int cursor() {

        return index();
    }

    /**
     * Converts this iterator into a vector.
     *
     * @param factory that creates a new vector
     * @return a new vector
     */
    public ByteVector toVector(Factory factory) {

        ByteVector result = factory.createVector(length);
        alterVector(result);
        return result;
    }

    /**
     * Alters given {@code vector} with values from this iterator.
     *
     * @param vector the vector to alter
     */
    public void alterVector(ByteVector vector) {

        while (hasNext()) {
            next();
            vector.set(index(), get());
        }
    }
}
