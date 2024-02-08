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
package io.adrestus.erasure.code.parameters;


import io.Adrestus.erasure.code.Parsed;
import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.SerializableParameters;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.Pipe;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;


/**
 * Tests the read/write methods of class FECParameters.
 */
public class FECParametersReadWriteTest {

    private static final FECParameters PARAMS = FECParameters.newParameters(10_000, 100, 1);

    // for I/O
    private static ExecutorService executor;


    @BeforeClass
    public static void initExecutor() {

        executor = Executors.newFixedThreadPool(1);
    }

    @AfterClass
    public static void shutdownExecutor() throws InterruptedException {

        executor.shutdown();
        executor.awaitTermination(2L, TimeUnit.SECONDS);
    }

    @Test
    public void testAsSerializable() {

        final SerializableParameters serParams = PARAMS.asSerializable();
        final Parsed<FECParameters> parsed = FECParameters.parse(serParams);

        assertEquals(PARAMS, parsed.value());
    }

    @Test
    public void testAsArray() {

        final byte[] array = PARAMS.asArray();
        final Parsed<FECParameters> parsed = FECParameters.parse(array);

        assertEquals(PARAMS, parsed.value());
    }

    @Test
    public void testWriteToArray() {

        final byte[] array = new byte[12];

        PARAMS.writeTo(array);
        final Parsed<FECParameters> parsed = FECParameters.parse(array);

        assertEquals(PARAMS, parsed.value());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayException() {

        final byte[] array = new byte[11];
        PARAMS.writeTo(array);
    }

    @Test
    public void testWriteToArrayOffset() {

        final byte[] array = new byte[24];
        final int off = 12;

        PARAMS.writeTo(array, off);
        final Parsed<FECParameters> parsed = FECParameters.parse(array, off);

        assertEquals(PARAMS, parsed.value());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayOffsetExceptionNegOffset() {

        final byte[] array = new byte[24];
        final int off = -1;

        PARAMS.writeTo(array, off);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteToArrayOffsetExceptionSmallArray() {

        final byte[] array = new byte[24];
        final int off = 13;

        PARAMS.writeTo(array, off);
    }

    @Test
    public void testAsBuffer() {

        final ByteBuffer buffer = PARAMS.asBuffer();
        final Parsed<FECParameters> parsed = FECParameters.parse(buffer);

        assertEquals(PARAMS, parsed.value());
    }

    @Test
    public void testWriteToBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(12);

        PARAMS.writeTo(buffer);
        buffer.rewind();
        final Parsed<FECParameters> parsed = FECParameters.parse(buffer);

        assertEquals(PARAMS, parsed.value());
    }

    @Test(expected = ReadOnlyBufferException.class)
    public void testWriteToBufferExceptionReadOnlyBuffer() {

        final ByteBuffer buffer = ByteBuffer.allocate(12);
        PARAMS.writeTo(buffer.asReadOnlyBuffer());
    }

    @Test(expected = BufferOverflowException.class)
    public void testWriteToBufferExceptionBufferOverflow() {

        final ByteBuffer buffer = ByteBuffer.allocate(11);
        PARAMS.writeTo(buffer);
    }

    @Test(timeout = 5000)
    public void testWriteToDataOutput() throws IOException {

        final PipedInputStream in = new PipedInputStream();
        final PipedOutputStream out = new PipedOutputStream(in);

        executor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    PARAMS.writeTo(new DataOutputStream(out));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Parsed<FECParameters> parsed = FECParameters.readFrom(new DataInputStream(in));

        assertEquals(PARAMS, parsed.value());
    }

    @Test(timeout = 5000)
    public void testWriteToByteChannel() throws IOException {

        final Pipe pipe = Pipe.open();

        executor.execute(new Runnable() {

            @Override
            public void run() {

                try {
                    PARAMS.writeTo(pipe.sink());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        final Parsed<FECParameters> parsed = FECParameters.readFrom(pipe.source());

        assertEquals(PARAMS, parsed.value());
    }
}
