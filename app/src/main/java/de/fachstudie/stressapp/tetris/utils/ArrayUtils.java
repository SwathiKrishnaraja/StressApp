package de.fachstudie.stressapp.tetris.utils;

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
}
