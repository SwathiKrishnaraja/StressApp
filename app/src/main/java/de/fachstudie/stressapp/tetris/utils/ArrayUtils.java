package de.fachstudie.stressapp.tetris.utils;

import java.util.Arrays;

/**
 * Created by Sanjeev on 30.12.2016.
 */

public class ArrayUtils {

    public static boolean indexExists(int index, int[][] array) {
        if (index >= 0 && index < array.length)
            return true;
        return false;
    }

    public static boolean indexExists(int index, int[] array) {
        if (index >= 0 && index < array.length)
            return true;
        return false;
    }

    public static void resetArray(int[][] array){
        for(int j = 0; j < array.length; j++){
            for(int i = 0; i < array[j].length; i++){
                array[j][i] = 0;
            }
        }
    }

    private static void printRow(int[] row) {
        for (int i : row) {
            System.out.print(i);
            System.out.print("\t");
        }
        System.out.println();
    }

    public static void print2DArray(int[][] array) {
        for (int[] row : array) {
            printRow(row);
        }
    }

    public static int[][] copy(int[][] input) {
        int[][] target = new int[input.length][];
        for (int i = 0; i < input.length; i++) {
            target[i] = Arrays.copyOf(input[i], input[i].length);
        }
        return target;
    }
}
