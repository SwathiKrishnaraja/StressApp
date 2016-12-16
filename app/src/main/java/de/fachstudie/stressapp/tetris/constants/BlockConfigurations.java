package de.fachstudie.stressapp.tetris.constants;

/**
 * Created by Sanjeev on 16.12.2016.
 */

public class BlockConfigurations {

    public static final int[][] SQUARE = new int[][]{{1, 1}, {1, 1}};
    public static final int[][][] SQUARE_ROTATIONS = {SQUARE, SQUARE, SQUARE, SQUARE};
    public static final int[][] SQUARE_SHIFTS = {{0, 0}, {0, 0}, {0, 0}, {0, 0}};

    //See http://tetris.wikia.com/wiki/SRS
    public static final int[][] L_1 = new int[][]{{1, 0}, {1, 0}, {1, 1}};
    public static final int[][] L_2 = new int[][]{{1, 1, 1}, {1, 0, 0}};
    public static final int[][] L_3 = new int[][]{{1, 1}, {0, 1}, {0, 1}};
    public static final int[][] L_4 = new int[][]{{0, 0, 1}, {1, 1, 1}};
    public static final int[][][] L_ROTATIONS = {L_1, L_2, L_3, L_4};
    public static final int[][] L_SHIFTS = {{1, -1}, {-1, 0}, {0, 0}, {0, 1}};

    public static final int[][] T_1 = new int[][]{{1, 1, 1}, {0, 1, 0}};
    public static final int[][] T_2 = new int[][]{{0, 1}, {1, 1}, {0, 1}};
    public static final int[][] T_3 = new int[][]{{0, 1, 0}, {1, 1, 1}};
    public static final int[][] T_4 = new int[][]{{1, 0}, {1, 1}, {1, 0}};
    public static final int[][][] T_ROTATIONS = {T_1, T_2, T_3, T_4};
    public static final int[][] T_SHIFTS = {{-1, 0}, {0, 0}, {0, 1}, {1, -1}};

    public static final int[][] I_1 = new int[][]{{1, 1, 1, 1}};
    public static final int[][] I_2 = new int[][]{{1}, {1}, {1}, {1}};
    public static final int[][][] I_ROTATIONS = {I_1, I_2, I_1, I_2};
    public static final int[][] I_SHIFTS = {{-1, 2}, {2, -2}, {-2, 1}, {1, -1}};

    public static final int[][] J_1 = new int[][]{{1, 0, 0}, {1, 1, 1}};
    public static final int[][] J_2 = new int[][]{{1, 1}, {1, 0}, {1, 0}};
    public static final int[][] J_3 = new int[][]{{1, 1, 1}, {0, 0, 1}};
    public static final int[][] J_4 = new int[][]{{0, 1}, {0, 1}, {1, 1}};
    public static final int[][][] J_ROTATIONS = {J_1, J_2, J_3, J_4};
    public static final int[][] J_SHIFTS = {{0, 1}, {1, -1}, {-1, 1}, {0, -1}};

    public static final int[][] S_1 = new int[][]{{0, 1, 1}, {1, 1, 0}};
    public static final int[][] S_2 = new int[][]{{1, 0}, {1, 1}, {0, 1}};
    public static final int[][][] S_ROTATIONS = {S_1, S_2, S_1, S_2};
    public static final int[][] S_SHIFTS = {{0, 1}, {1, -1}, {-1, 0}, {0, 0}};

    public static final int[][] Z_1 = new int[][]{{1, 1, 0}, {0, 1, 1}};
    public static final int[][] Z_2 = new int[][]{{0, 1}, {1, 1}, {1, 0}};
    public static final int[][][] Z_ROTATIONS = {Z_1, Z_2, Z_1, Z_2};
    public static final int[][] Z_SHIFTS = {{0, 1}, {1, -1}, {-1, 0}, {0, 0}};
}
