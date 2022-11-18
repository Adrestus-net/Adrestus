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

        int digits = countNumberDigits(number);
        if (digits == 0)
            return number * 100;
        else if (digits == 1)
            return number * 10;
        else {
            int div = number / 100;
            return number / div;
        }

    }

}
