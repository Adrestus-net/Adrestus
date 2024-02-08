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
package io.adrestus.erasure.code;


import io.Adrestus.erasure.code.parameters.FECParameters;
import io.Adrestus.erasure.code.parameters.ParameterChecker;

import java.util.*;

import static io.Adrestus.erasure.code.util.math.ExtraMath.integerPow;


/**
 * Contains useful constants and utility methods for the test classes.
 */
public final class TestingCommon {

    private static final long RAND_SEED = 42L;


    public static Random newRandom() {

        return new Random();
    }

    public static Random newSeededRandom() {

        return new Random(RAND_SEED);
    }

    public static byte[] randomBytes(int size, Random rand) {

        final byte[] bs = new byte[size];
        rand.nextBytes(bs);
        return bs;
    }

    public static Set<Integer> randomAnyESIs(Random rand, int numSymbols) {

        final int minESI = ParameterChecker.minEncodingSymbolID();
        final int maxESI = ParameterChecker.maxEncodingSymbolID();
        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        // Floyd's Algorithm for random sampling (uniform over all possible ESIs)
        randomUniformSample(esis, rand, minESI, maxESI, numSymbols);

        return esis;
    }

    public static Set<Integer> randomSrcRepESIs(Random rand, int numSymbols, int K) {

        final int maxESI = ParameterChecker.maxEncodingSymbolID();
        final Set<Integer> esis = new LinkedHashSet<>(); // preserve randomized ordering

        while (esis.size() < numSymbols) {
            // exponential distribution with mean K/2
            final int esi = (int) ((-K / 2D) * Math.log(1 - rand.nextDouble()));
            // repeat sampling if a repeated ESI is obtained
            esis.add(Math.min(esi, maxESI));
        }

        return esis;
    }

    public static int[] exponentialDistribution(int base, int maxExponent) {

        integerPow(base, maxExponent); // test a power calculation to validate arguments
        if (base <= 0) throw new IllegalArgumentException("base must be positive");

        final int[] distr = new int[maxExponent + 1];
        for (int exp = 0; exp <= maxExponent; exp++) {
            distr[exp] = integerPow(base, exp);
        }
        return distr;
    }

    public static long[] exponentialDistribution(long base, int maxExponent) {

        integerPow(base, maxExponent); // test a power calculation to validate arguments
        if (base <= 0) throw new IllegalArgumentException("base must be positive");

        final long[] distr = new long[maxExponent + 1];
        for (int exp = 0; exp <= maxExponent; exp++) {
            distr[exp] = integerPow(base, exp);
        }
        return distr;
    }

    public static int[] primeExponentialDistribution(int base, int maxExponent) {

        integerPow(base, maxExponent); // test a power calculation to validate arguments
        if (base <= 0) throw new IllegalArgumentException("base must be positive");

        final Sieve sieve = new Sieve();

        // distribution.size() == approx. number of primes + number of powers
        // distribution.size() == approx. 1 + 2 + ... + maxExponent + (maxExponent+1)
        final LinkedHashSet<Integer> distribution = new LinkedHashSet<>(((maxExponent + 1) * (maxExponent + 2)) / 2);

        for (int exp = 0; exp < maxExponent; exp++) {
            // collect some primes between current and next power
            final int currPower = integerPow(base, exp);
            final int nextPower = integerPow(base, exp + 1);

            // this provides a sort of uniform distribution of primes of limited size
            final int inc = (exp == 0) ? 1 : (nextPower - currPower) / exp;

            // add the current power to the distribution
            distribution.add(currPower);

            // start the prime number search at current power
            for (int n = sieve.nextPrimeInclusive(currPower); n < nextPower; n = sieve.nextPrimeInclusive(n + inc)) {
                // add the collected prime to the distribution
                distribution.add(n);
            }
        }

        // add the last power to the distribution
        distribution.add(integerPow(base, maxExponent));

        final int[] intDist = new int[distribution.size()];
        int idx = 0;
        for (int value : distribution) {
            intDist[idx++] = value;
        }
        return intDist;
    }

    private static void randomUniformSample(Set<Integer> sink, Random rand, int begin, int end, int sampleSize) {

        if (begin > end) throw new IllegalArgumentException("begin > end");
        if (end >= 0 && begin <= end - Integer.MAX_VALUE) throw new IllegalArgumentException("end - begin overflows");

        final int distrSize = 1 + (end - begin);
        if (sampleSize > distrSize) throw new IllegalArgumentException("sample size is too large");

        // Floyd's Algorithm for uniform random sampling
        for (int i = distrSize - sampleSize; i < distrSize; i++) {
            // try to add a random index between 0 and i (inclusive)
            if (!sink.add(begin + rand.nextInt(i + 1))) {
                // if already present, choose index i, which is surely not present yet
                sink.add(begin + i);
            }
        }
    }

    public static <C extends Collection<Integer>> C addInts(C col, int... ints) {

        for (int i : ints) {
            col.add(i);
        }
        return col;
    }

    public static <C extends Collection<Long>> C addIntsL(C col, int... ints) {

        for (int i : ints) {
            col.add((long) i);
        }
        return col;
    }

    public static <C extends Collection<Long>> C addLongs(C col, long... longs) {

        for (long eL : longs) {
            col.add(eL);
        }
        return col;
    }

    public static void checkParamsForSingleSourceBlockData(long datalen, int srcsymbs) {

        if (ParameterChecker.isDataLengthOutOfBounds(datalen)) {
            throw new IllegalArgumentException(
                    String.format(
                            "by default, the data length (%d) must be within [%d, %d] bytes",
                            datalen,
                            ParameterChecker.minDataLength(),
                            ParameterChecker.maxDataLength()));
        }

        if (ParameterChecker.isNumSourceSymbolsPerBlockOutOfBounds(srcsymbs)) {
            throw new IllegalArgumentException(
                    String.format(
                            "by default, the number of source symbols (%d) must be within [%d, %d]",
                            srcsymbs,
                            ParameterChecker.minNumSourceSymbolsPerBlock(),
                            ParameterChecker.maxNumSourceSymbolsPerBlock()));
        }

        final long minF = (long) srcsymbs * ParameterChecker.minSymbolSize();
        final long maxF = (long) srcsymbs * ParameterChecker.maxSymbolSize();
        if (datalen < minF || maxF < datalen) {
            throw new IllegalArgumentException(
                    String.format(
                            "%d source symbol(s) can only support a data length within [%d, %d] bytes",
                            srcsymbs,
                            minF,
                            maxF));
        }
    }


    /**
     * Class that implements Sieve of Eratosthenes to check and find prime numbers.
     */
    private static final class Sieve {

        // Bit set where 0 means prime, 1 means composite
        // Keeps odd numbers only, so index == n / 2
        // First index is never accessed, so there is no error for n = {0, 1, 2}
        private final BitSet bitset;

        // The last number in the bit set (not necessarily the last index in the bit set)
        private int last;


        Sieve() {

            // we expect isPrime queries for N <= 2^^15
            this.bitset = new BitSet(1 >> 15);
            this.last = 3;
        }

        int nextPrimeInclusive(int n) {

            if (n <= 2) return 2;

            // begin at an odd number
            for (int nn = (n % 2 == 0) ? n + 1 : n; true; nn += 2) {
                if (isPrime(nn)) {
                    return nn;
                }
            }
        }

        // A note on micro-optimizations: division and modulo by 2 are usually
        // converted by the compiler to an unsigned right shift and an AND 1,
        // respectively.
        boolean isPrime(final int n) {

            // trivial cases
            if (n == 2) return true;
            if (n % 2 == 0 || n < 3) return false;

            // not yet cached
            if (n > last) {
                // mark in the bit set composite numbers that are multiples of ii
                for (int ii = 3; ii <= n / 2; ii += 2) {
                    // if ii is prime, then mark all multiples of ii as composite in the bit set
                    if (isCachedPrime(ii)) {
                        // find the greatest multiple of ii, that is <= last (to avoid repeating markings)
                        int firstMultiple = (last / ii) * ii;
                        if (firstMultiple <= ii) { // at first this will be true
                            firstMultiple = ii * 2;
                        }

                        markMultiples(ii, firstMultiple, n);
                    }
                }
                // update the last number that was processed
                last = n;
            }

            return isCachedPrime(n);
        }

        private void markMultiples(int prime, int startAt, int endAt) {

            for (int mult = startAt; mult <= endAt; mult += prime) {
                markAsComposite(mult);
            }
        }

        private void markAsComposite(final int n) {

            // only odd numbers in bit set
            if (n % 2 != 0) {
                bitset.set(n / 2, true);
            }
        }

        private boolean isCachedPrime(int n) {

            return !bitset.get(n / 2);
        }
    }

    /**
     * Returns minimal values of FEC parameters and array sizes.
     */
    public static final class Minimal {

        public static final long F = ParameterChecker.minDataLength();
        public static final int T = ParameterChecker.minSymbolSize();
        public static final int Z = ParameterChecker.minNumSourceBlocks();


        public static FECParameters fecParameters() {

            return FECParameters.newParameters(F, T, Z);
        }

        public static byte[] data() {

            return new byte[(int) F];
        }
    }


    private TestingCommon() {

        // not instantiable
    }
}
