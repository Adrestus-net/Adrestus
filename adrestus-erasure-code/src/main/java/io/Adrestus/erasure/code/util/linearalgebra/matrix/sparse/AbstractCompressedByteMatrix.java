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
 * Contributor(s): Ewald Grusk
 */
package io.Adrestus.erasure.code.util.linearalgebra.matrix.sparse;


import io.Adrestus.erasure.code.util.linearalgebra.factory.Factory;
import io.Adrestus.erasure.code.util.linearalgebra.matrix.AbstractByteMatrix;
import io.Adrestus.erasure.code.util.linearalgebra.matrix.ByteMatrices;

import static io.Adrestus.erasure.code.util.math.OctetOps.aIsGreaterThanB;
import static io.Adrestus.erasure.code.util.math.OctetOps.aIsLessThanB;


public abstract class AbstractCompressedByteMatrix extends AbstractByteMatrix implements SparseByteMatrix {

    public AbstractCompressedByteMatrix(Factory factory, int rows, int columns) {

        super(factory, rows, columns);
    }

    @Override
    public double density() {

        return (double) cardinality() / capacity();
    }

    protected final long capacity() {

        return ((long) rows()) * columns();
    }

    @Override
    public final int nonZeros() {

        return cardinality();
    }

    @Override
    public final boolean isZeroAt(int i, int j) {

        return !nonZeroAt(i, j);
    }

    @Override
    public abstract boolean nonZeroAt(int i, int j);

    @Override
    public final byte max() {

        byte max = foldNonZero(ByteMatrices.mkMaxAccumulator());
        if (cardinality() == capacity() || aIsGreaterThanB(max, (byte) 0)) {
            return max;
        } else {
            return 0;
        }
    }

    @Override
    public final byte min() {

        byte min = foldNonZero(ByteMatrices.mkMinAccumulator());
        if (cardinality() == capacity() || aIsLessThanB(min, (byte) 0)) {
            return min;
        } else {
            return 0;
        }
    }
}
