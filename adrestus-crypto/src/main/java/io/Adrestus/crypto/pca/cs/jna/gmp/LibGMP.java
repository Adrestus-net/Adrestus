package io.Adrestus.crypto.pca.cs.jna.gmp;

import com.sun.jna.Library;
import com.sun.jna.NativeLong;

public interface LibGMP extends Library {
    void __gmpz_init(mpz_t integer);//limp space, initial value 0

    void __gmpz_init2(mpz_t x, NativeLong n);//space for n-bit numbers, initial value 0

    void __gmpz_clear(mpz_t x);//Free the space occupied by x

    void __gmpz_abs(mpz_t rop, mpz_t op);

    void __gmpz_neg(mpz_t rop, mpz_t op);

    void __gmpz_add(mpz_t rop, mpz_t op1, mpz_t op2);

    void __gmpz_sub(mpz_t rop, mpz_t op1, mpz_t op2);

    void __gmpz_mul(mpz_t rop, mpz_t op1, mpz_t op2);

    void __gmpz_tdiv_q(mpz_t q, mpz_t n, mpz_t d);

    void __gmpz_tdiv_r(mpz_t r, mpz_t n, mpz_t d);

    void __gmpz_divexact(mpz_t q, mpz_t n, mpz_t d);

    void __gmpz_gcd(mpz_t rop, mpz_t op1, mpz_t op2);

    void __gmpz_gcdext(mpz_t gcd, mpz_t s, mpz_t t, mpz_t a, mpz_t b);

    void __gmpz_fdiv_q(mpz_t q, mpz_t n, mpz_t d);

    void __gmpz_fdiv_q_ui(mpz_t rop, mpz_t op1, NativeLong op2);

    int __gmpz_cmp_si(mpz_t op1, NativeLong op2);

    void __gmpz_mod(mpz_t r, mpz_t n, mpz_t d);

    void __gmpz_powm(mpz_t rop, mpz_t base, mpz_t exp, mpz_t mod);

    void __gmpz_powm_sec(mpz_t rop, mpz_t base, mpz_t exp, mpz_t mod);

    int __gmpz_invert(mpz_t rop, mpz_t op1, mpz_t op2);//modpowinv

    int __gmpz_legendre(mpz_t a, mpz_t p);

    int __gmpz_jacobi(mpz_t a, mpz_t p);

    int __gmpz_probab_prime_p(mpz_t a, int reps);

    void __gmpz_nextprime(mpz_t rop, mpz_t op);

}
