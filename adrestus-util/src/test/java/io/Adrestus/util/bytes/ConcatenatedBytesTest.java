/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.Adrestus.util.bytes;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InOrder;

import java.security.MessageDigest;
import java.util.stream.Stream;

import static org.apache.tuweni.bytes.Bytes.fromHexString;
import static org.apache.tuweni.bytes.Bytes.wrap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class ConcatenatedBytesTest {

    @SuppressWarnings("UnusedMethod")
    private static Stream<Arguments> concatenatedWrapProvider() {
        return Stream
                .of(
                        Arguments.of(new byte[]{}, new byte[]{}),
                        Arguments.of(new byte[]{}, new byte[]{1, 2, 3}),
                        Arguments.of(new byte[]{1, 2, 3}, new byte[]{}),
                        Arguments.of(new byte[]{1, 2, 3}, new byte[]{4, 5}));
    }

    @Test
    void testConcatenatedWrapReflectsUpdates() {
        byte[] first = new byte[]{1, 2, 3};
        byte[] second = new byte[]{4, 5};
        byte[] expected1 = new byte[]{1, 2, 3, 4, 5};
        org.apache.tuweni.bytes.Bytes res = wrap(wrap(first), wrap(second));
        assertArrayEquals(res.toArray(), expected1);

        first[1] = 42;
        second[0] = 42;
        byte[] expected2 = new byte[]{1, 42, 3, 42, 5};
        assertArrayEquals(res.toArray(), expected2);
    }

    @Test
    void shouldReadConcatenatedValue() {
        org.apache.tuweni.bytes.Bytes bytes = wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF"));
        assertEquals(8, bytes.size());
        assertEquals("0x0123456789abcdef", bytes.toHexString());
    }

    @Test
    void shouldSliceConcatenatedValue() {
        org.apache.tuweni.bytes.Bytes bytes = wrap(
                fromHexString("0x01234567"),
                fromHexString("0x89ABCDEF"),
                fromHexString("0x01234567"),
                fromHexString("0x89ABCDEF"));
        assertEquals("0x", bytes.slice(4, 0).toHexString());
        assertEquals("0x0123456789abcdef0123456789abcdef", bytes.slice(0, 16).toHexString());
        assertEquals("0x01234567", bytes.slice(0, 4).toHexString());
        assertEquals("0x0123", bytes.slice(0, 2).toHexString());
        assertEquals("0x6789", bytes.slice(3, 2).toHexString());
        assertEquals("0x89abcdef", bytes.slice(4, 4).toHexString());
        assertEquals("0xabcd", bytes.slice(5, 2).toHexString());
        assertEquals("0xef012345", bytes.slice(7, 4).toHexString());
        assertEquals("0x01234567", bytes.slice(8, 4).toHexString());
        assertEquals("0x456789abcdef", bytes.slice(10, 6).toHexString());
        assertEquals("0x89abcdef", bytes.slice(12, 4).toHexString());
    }

    @Test
    void shouldReadDeepConcatenatedValue() {
        org.apache.tuweni.bytes.Bytes bytes = wrap(
                wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF")),
                wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF")),
                fromHexString("0x01234567"),
                fromHexString("0x89ABCDEF"));
        assertEquals(24, bytes.size());
        assertEquals("0x0123456789abcdef0123456789abcdef0123456789abcdef", bytes.toHexString());
    }

    @Test
    void testCopy() {
        org.apache.tuweni.bytes.Bytes bytes = wrap(fromHexString("0x01234567"), fromHexString("0x89ABCDEF"));
        assertEquals(bytes, bytes.copy());
        assertEquals(bytes, bytes.mutableCopy());
    }

    @Test
    void testCopyTo() {
        org.apache.tuweni.bytes.Bytes bytes = wrap(fromHexString("0x0123"), fromHexString("0x4567"));
        org.apache.tuweni.bytes.MutableBytes dest = org.apache.tuweni.bytes.MutableBytes.create(32);
        bytes.copyTo(dest, 10);
        assertEquals(org.apache.tuweni.bytes.Bytes.fromHexString("0x0000000000000000000001234567000000000000000000000000000000000000"), dest);
    }

    @Test
    void testHashcodeUpdates() {
        org.apache.tuweni.bytes.MutableBytes dest = MutableBytes.create(32);
        org.apache.tuweni.bytes.Bytes bytes = wrap(dest, fromHexString("0x4567"));
        int hashCode = bytes.hashCode();
        dest.set(1, (byte) 123);
        assertNotEquals(hashCode, bytes.hashCode());
    }

    @Test
    void shouldUpdateMessageDigest() {
        org.apache.tuweni.bytes.Bytes value1 = fromHexString("0x01234567");
        org.apache.tuweni.bytes.Bytes value2 = fromHexString("0x89ABCDEF");
        org.apache.tuweni.bytes.Bytes value3 = fromHexString("0x01234567");
        Bytes bytes = wrap(value1, value2, value3);
        MessageDigest digest = mock(MessageDigest.class);
        bytes.update(digest);

        final InOrder inOrder = inOrder(digest);
        inOrder.verify(digest).update(value1.toArrayUnsafe(), 0, 4);
        inOrder.verify(digest).update(value2.toArrayUnsafe(), 0, 4);
        inOrder.verify(digest).update(value3.toArrayUnsafe(), 0, 4);
    }
}
