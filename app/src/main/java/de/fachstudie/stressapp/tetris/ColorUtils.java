package de.fachstudie.stressapp.tetris;

import android.graphics.Color;
import android.graphics.Paint;

import de.fachstudie.stressapp.tetris.constants.BlockColors;

public class ColorUtils {
    public static void setColorForShape(Paint p, int i) {
        switch (i) {
            case 1:
                p.setColor(Color.parseColor(BlockColors.RED));
                break;
            case 2:
                p.setColor(Color.parseColor(BlockColors.PURPLE));
                break;
            case 3:
                p.setColor(Color.parseColor(BlockColors.TEAL));
                break;
            case 4:
                p.setColor(Color.parseColor(BlockColors.CYAN));
                break;
            case 5:
                p.setColor(Color.parseColor(BlockColors.INDIGO));
                break;
            case 6:
                p.setColor(Color.parseColor(BlockColors.BLUE));
                break;
            case 7:
                p.setColor(Color.parseColor(BlockColors.ORANGE));
                break;
        }
    }

    public static void setColorForShape(Paint p, Block.Shape type) {
        switch (type) {
            case SQUARE:
                p.setColor(Color.parseColor(BlockColors.RED));
                break;
            case L:
                p.setColor(Color.parseColor(BlockColors.PURPLE));
                break;
            case T:
                p.setColor(Color.parseColor(BlockColors.TEAL));
                break;
            case I:
                p.setColor(Color.parseColor(BlockColors.CYAN));
                break;
            case J:
                p.setColor(Color.parseColor(BlockColors.INDIGO));
                break;
            case S:
                p.setColor(Color.parseColor(BlockColors.BLUE));
                break;
            case Z:
                p.setColor(Color.parseColor(BlockColors.ORANGE));
                break;
        }
    }
}
