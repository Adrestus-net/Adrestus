package io.Adrestus.util;

public final class MathOperationUtil {


    private static int countNumberDigits(int number) {
        int length = String.valueOf(number).length();
        return length - 1;
    }


    private static int multipli(int iteration) {
        if (iteration == 1 || iteration == 0)
            return 10;
        int sum = 1;
        int count = 1;
        while (count <= iteration) {
            sum = sum * 10;
            count++;
        }
        return sum;
    }


    public static int multiplication(int number) {
        number = Math.abs(number);
        int digits = countNumberDigits(number);
        if (digits == 0)
            return number * 100;
        else if (digits == 1)
            return number * 10;
        else {
            int first_n = (int) (number / Math.pow(10, Math.floor(Math.log10(number)) - (3) + 1));
            if (first_n % 2 != 0)
                first_n = first_n + 1;
            return first_n;
        }
    }

}
