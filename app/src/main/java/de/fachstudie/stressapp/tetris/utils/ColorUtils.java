package de.fachstudie.stressapp.tetris.utils;

import android.graphics.Color;
import android.graphics.Paint;

import de.fachstudie.stressapp.tetris.Block;
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
            case 8:
                p.setColor(Color.parseColor(BlockColors.GOLD));
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

    public static void setLightColorForShape(Paint p, int i) {
        switch (i) {
            case 1:
                p.setColor(Color.parseColor(BlockColors.LIGHT_RED));
                break;
            case 2:
                p.setColor(Color.parseColor(BlockColors.LIGHT_PURPLE));
                break;
            case 3:
                p.setColor(Color.parseColor(BlockColors.LIGHT_TEAL));
                break;
            case 4:
                p.setColor(Color.parseColor(BlockColors.LIGHT_CYAN));
                break;
            case 5:
                p.setColor(Color.parseColor(BlockColors.LIGHT_INDIGO));
                break;
            case 6:
                p.setColor(Color.parseColor(BlockColors.LIGHT_BLUE));
                break;
            case 7:
                p.setColor(Color.parseColor(BlockColors.LIGHT_ORANGE));
                break;
            case 8:
                p.setColor(Color.parseColor(BlockColors.LIGHT_GOLD));
                break;
        }
    }

    public static void setColorForBlockBitmap(Paint p, boolean blockGolden) {
        if (blockGolden)
            p.setColor(Color.parseColor(BlockColors.GOLD));
    }

    public static void setColorForBlock(Paint p, int goldBlockCount) {
        if (goldBlockCount > 0) {
            p.setColor(Color.parseColor(BlockColors.GOLD));
        } else {
            p.setColor(Color.parseColor(BlockColors.LIGHT_GRAY));
        }
    }

    public static void setColorForNotificationBlock(Paint p, int notificationCount) {
        if (notificationCount > 0) {
            p.setColor(Color.parseColor(BlockColors.LIGHT_GREEN));
        } else {
            p.setColor(Color.parseColor(BlockColors.LIGHT_GRAY));
        }
    }
}
