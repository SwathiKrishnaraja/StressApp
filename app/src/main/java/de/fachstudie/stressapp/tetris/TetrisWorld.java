package de.fachstudie.stressapp.tetris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import java.util.Arrays;
import java.util.Random;

import static de.fachstudie.stressapp.tetris.Block.Shape.L;
import static de.fachstudie.stressapp.tetris.Block.Shape.SQUARE;
import static de.fachstudie.stressapp.tetris.Block.Shape.T;

public class TetrisWorld {
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
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
            Block item = randomItem();
            this.item = item;
            return false;
        }
    }

    private Block randomItem() {
        Random r = new Random();
        int number = r.nextInt(3);
        int x = r.nextInt(3);
        if (number == 0) {
            return new Block(x, 0, 0, 0, L);
        } else if (number == 1) {
            return new Block(x, 0, 0, 0, T);
        } else {
            return new Block(x, 0, 0, 0, SQUARE);
        }
    }

    private void freeze(Block item) {
        for (int j = item.getY(); j < item.getY() + item.getHeight(); j++) {
            for (int i = item.getX(); i < item.getX() + item.getWidth(); i++) {
                int yOffset = j - item.getY();
                int xOffset = i - item.getX();
                if (item.getShape()[yOffset][xOffset] == 1 && yOffset >= 0 && xOffset >= 0) {
                    occupancy[j][i] = 1;
                }
            }
        }
    }

    private boolean hasOverlap(int[][] state) {
        for (int j = 0; j < state.length; j++) {
            for (int i = 0; i < state[j].length; i++) {
                if (state[j][i] > 1) {
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

        int PADDING = 120;
        int TOP_PADDING = 30;
        int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        canvas.drawRect(PADDING, TOP_PADDING, canvasWidth - PADDING, TOP_PADDING + HEIGHT *
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
                                p.setColor(Color.parseColor("#f44336"));
                                break;
                            case L:
                                p.setColor(Color.parseColor("#2196f3"));
                                break;
                            case T:
                                p.setColor(Color.parseColor("#009688"));
                                break;
                        }
                        canvas.drawRect(i * gridSize + PADDING, j * gridSize + TOP_PADDING, (i +
                                1) * gridSize
                                + PADDING, (j + 1) *
                                gridSize + TOP_PADDING, p);
                    }
                }
                if (occupancy[j][i] == 1) {
                    p.setColor(Color.GRAY);
                    canvas.drawRect(i * gridSize + PADDING, j * gridSize + TOP_PADDING, (i + 1) *
                            gridSize +
                            PADDING, (j + 1) *
                            gridSize + TOP_PADDING, p);
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

        if (this.item.getX() >= WIDTH) {
            this.item.setX(WIDTH - 1);
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

        int PADDING = 120;
        int TOP_PADDING = 30;
        int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
        bitmap = getResizedBitmap(bitmap, gridSize, gridSize);
        canvas.drawBitmap(bitmap, item.getX() * gridSize + PADDING, item.getY() * gridSize +
                TOP_PADDING, p);
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
