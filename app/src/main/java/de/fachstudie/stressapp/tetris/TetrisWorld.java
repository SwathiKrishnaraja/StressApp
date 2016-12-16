package de.fachstudie.stressapp.tetris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import de.fachstudie.stressapp.tetris.constants.BlockColors;

import static de.fachstudie.stressapp.tetris.Block.Shape.I;
import static de.fachstudie.stressapp.tetris.Block.Shape.J;
import static de.fachstudie.stressapp.tetris.Block.Shape.L;
import static de.fachstudie.stressapp.tetris.Block.Shape.S;
import static de.fachstudie.stressapp.tetris.Block.Shape.SQUARE;
import static de.fachstudie.stressapp.tetris.Block.Shape.T;
import static de.fachstudie.stressapp.tetris.Block.Shape.Z;

public class TetrisWorld {
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    int PADDING = 140;
    int TOP_PADDING = 30;
    private int[][] occupancy = new int[HEIGHT][WIDTH];
    private Block item;
    private boolean dropping = false;

    public void addItem(Block item) {
        this.item = item;
    }

    public boolean gravityStep() {
        int[][] state = copy(occupancy);
        item.simulateStepDown(state);
        if (!hasOverlap(state) && item.getY() + item.getHeight() < HEIGHT) {
            item.stepDown();
            return true;
        } else {
            freeze(this.item);
            clearFullLines();
            Block item = randomItem();
            this.item = item;
            return false;
        }
    }

    private void clearFullLines() {
        List<Integer> fullLines = new ArrayList<>();
        for (int j = 0; j < occupancy.length; j++) {
            boolean isFull = true;
            for (int i = 0; i < occupancy[j].length; i++) {
                if (occupancy[j][i] == 0) {
                    isFull = false;
                    break;
                }
            }
            if (isFull) {
                fullLines.add(j);
            }
        }
        for (Integer fullLine : fullLines) {
            for (int j = fullLine; j >= 0; j--) {
                for (int i = 0; i < occupancy[j].length; i++) {
                    if (j != 0) {
                        occupancy[j][i] = occupancy[j - 1][i];
                    } else {
                        occupancy[j][i] = 0;
                    }
                }
            }
        }
    }

    private Block randomItem() {
        Random r = new Random();
        int number = r.nextInt(7);
        int x = r.nextInt(3);
        if (number == 0) {
            return new Block(x, 0, 0, 0, L);
        } else if (number == 1) {
            return new Block(x, 0, 0, 0, T);
        } else if (number == 2) {
            return new Block(x, 0, 0, 0, SQUARE);
        } else if (number == 3) {
            return new Block(x, 0, 0, 0, I);
        } else if (number == 4) {
            return new Block(x, 0, 0, 0, J);
        } else if (number == 5) {
            return new Block(x, 0, 0, 0, S);
        } else {
            return new Block(x, 0, 0, 0, Z);
        }
    }

    private void freeze(Block item) {
        for (int j = item.getY(); j < item.getY() + item.getHeight(); j++) {
            for (int i = item.getX(); i < item.getX() + item.getWidth(); i++) {
                int yOffset = j - item.getY();
                int xOffset = i - item.getX();
                if (item.getShape()[yOffset][xOffset] == 1 && yOffset >= 0 && xOffset >= 0) {
                    occupancy[j][i] = item.getType().getN();
                }
            }
        }
    }

    private boolean hasOverlap(int[][] state) {
        for (int j = 0; j < state.length; j++) {
            for (int i = 0; i < state[j].length; i++) {
                if (state[j][i] == -1) {
                    return true;
                }
            }
        }
        return false;
    }

    public int[][] copy(int[][] input) {
        int[][] target = new int[input.length][];
        for (int i = 0; i < input.length; i++) {
            target[i] = Arrays.copyOf(input[i], input[i].length);
        }
        return target;
    }

    public void drawState(Canvas canvas, Paint p) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        canvas.drawRect(PADDING, TOP_PADDING, PADDING + WIDTH * gridSize, TOP_PADDING + HEIGHT *
                gridSize, p);
        p.setStyle(Paint.Style.FILL);

        for (int j = 0; j < occupancy.length; j++) {
            for (int i = 0; i < occupancy[j].length; i++) {
                if (j >= item.getY() && j < item.getY() + item.getHeight() && i >= item.getX()
                        && i < item.getX() + item.getWidth()) {
                    int yOffset = j - item.getY();
                    int xOffset = i - item.getX();
                    if (item.getShape()[yOffset][xOffset] == 1) {
                        switch (item.getType()) {
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
                        canvas.drawRect(i * gridSize + PADDING + 1, j * gridSize + TOP_PADDING +
                                1, (i +
                                1) * gridSize
                                + PADDING - 1, (j + 1) *
                                gridSize + TOP_PADDING - 1, p);
                    }
                }
                if (occupancy[j][i] != 0) {
                    switch (occupancy[j][i]) {
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
                    canvas.drawRect(i * gridSize + PADDING + 1, j * gridSize + TOP_PADDING + 1,
                            (i + 1) *
                                    gridSize +
                                    PADDING - 1, (j + 1) *
                                    gridSize + TOP_PADDING - 1, p);
                }
            }
        }
    }

    public void rotateBlock() {
        int[][] state = copy(occupancy);
        this.item.simulateRotate(state);

        if (!hasOverlap(state)) {
            this.item.rotate();
        }

        if (this.item.getX() + this.item.getWidth() >= WIDTH) {
            this.item.setX(WIDTH - this.item.getWidth());
        }
    }

    public void drop() {
        dropping = true;
    }

    public void moveRight() {
        int[][] state = copy(occupancy);
        this.item.simulateStepRight(state);

        if (!hasOverlap(state)) {
            this.item.moveRight();
        }

        if (item.getX() + item.getWidth() >= WIDTH) {
            item.setX(WIDTH - item.getWidth());
        }
    }

    public void moveLeft() {
        int[][] state = copy(occupancy);
        this.item.simulateStepLeft(state);

        if (!hasOverlap(state)) {
            this.item.moveLeft();
        }

        Log.d("X", item.getX() + "");
    }

    public void stopDropping() {
        this.dropping = false;
    }

    public boolean isDropping() {
        return dropping;
    }

    public void setDropping(boolean dropping) {
        this.dropping = dropping;
    }

    public void drawIcon(Canvas canvas, Bitmap bitmap, Paint p) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
        bitmap = getResizedBitmap(bitmap, gridSize, gridSize);

        for (int j = item.getY(); j < item.getY() + item.getHeight(); j++) {
            for (int i = item.getX(); i < item.getX() + item.getWidth(); i++) {
                int yOffset = j - item.getY();
                int xOffset = i - item.getX();
                if (yOffset >= 0 && xOffset >= 0 && yOffset < item.getShape().length && xOffset <
                        item.getShape()[yOffset].length && item
                        .getShape()[yOffset][xOffset] == 1) {
                    canvas.drawBitmap(bitmap, i * gridSize + PADDING + 1, j * gridSize +
                            TOP_PADDING +
                            1, p);
                }
            }
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        return resizedBitmap;
    }
}
