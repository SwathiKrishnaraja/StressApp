package de.fachstudie.stressapp.tetris;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Arrays;
import java.util.Random;

import static de.fachstudie.stressapp.tetris.Block.Shape.L;
import static de.fachstudie.stressapp.tetris.Block.Shape.SQUARE;
import static de.fachstudie.stressapp.tetris.Block.Shape.T;

public class TetrisWorld {
    private final int WIDTH = 8;
    private final int HEIGHT = 20;
    private int[][] occupancy = new int[HEIGHT][WIDTH];
    private Block item;

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

        int gridSize = canvasWidth / HEIGHT;

        for (int j = 0; j < occupancy.length; j++) {
            for (int i = 0; i < occupancy[j].length; i++) {
                if (j >= item.getY() && j < item.getY() + item.getHeight() && i >= item.getX()
                        && i < item.getX() + item.getWidth()) {
                    int yOffset = j - item.getY();
                    int xOffset = i - item.getX();
                    if (item.getShape()[yOffset][xOffset] == 1) {
                        switch (item.getType()) {
                            case SQUARE:
                                p.setColor(Color.YELLOW);
                                break;
                            case L:
                                p.setColor(Color.BLUE);
                                break;
                            case T:
                                p.setColor(Color.RED);
                                break;
                        }
                        canvas.drawRect(i * gridSize, j * gridSize, (i + 1) * gridSize, (j + 1) *
                                gridSize, p);
                    }
                }
                if (occupancy[j][i] == 1) {
                    p.setColor(Color.GRAY);
                    canvas.drawRect(i * gridSize, j * gridSize, (i + 1) * gridSize, (j + 1) *
                            gridSize, p);
                }
            }
        }
    }

    public void rotateBlock() {
        this.item.rotate();
    }

    public void drop() {
        long lastUpdate = -1;
        while (true) {
            if (System.currentTimeMillis() - lastUpdate > 70) {
                boolean canDrop = gravityStep();
                if (!canDrop) {
                    return;
                }
                lastUpdate = System.currentTimeMillis();
            }
        }
    }
}
