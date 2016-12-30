package de.fachstudie.stressapp.tetris;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static de.fachstudie.stressapp.tetris.Block.Shape.I;
import static de.fachstudie.stressapp.tetris.Block.Shape.J;
import static de.fachstudie.stressapp.tetris.Block.Shape.L;
import static de.fachstudie.stressapp.tetris.Block.Shape.S;
import static de.fachstudie.stressapp.tetris.Block.Shape.SQUARE;
import static de.fachstudie.stressapp.tetris.Block.Shape.T;
import static de.fachstudie.stressapp.tetris.Block.Shape.Z;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForShape;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.indexExists;

public class TetrisWorld {

    // Various specification
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    private final int PREVIEW_WIDTH = 4;
    private final int PADDING = 140;
    private final int TOP_PADDING = 50;
    private final int PREVIEW_PADDING = 10;
    private final int TEXT_SIZE = 40;

    // Initialization of the tetris field
    private int[][] occupancy = new int[HEIGHT][WIDTH];
    private Bitmap[][] bitmaps = new Bitmap[HEIGHT][WIDTH];

    private Block currentBlock;
    private Block nextBlock;
    private Bitmap currentBitmap;
    private Bitmap nextBitmap;

    private int score;
    private int gridSize;
    private boolean dropping = false;
    private boolean blockChange = false;

    public void addItem(Block item) {
        this.currentBlock = item;
    }

    public void createNextItem() {
        this.nextBlock = randomItem();
    }

    public boolean gravityStep() {
        int[][] state = copy(occupancy);
        currentBlock.simulateStepDown(state);
        this.blockChange = false;
        if (!hasOverlap(state) && currentBlock.getY() + currentBlock.getHeight() < HEIGHT) {
            currentBlock.stepDown();
            return true;
        } else {
            freezeCurrentBlock();
            calculateScore();
            clearFullLines();
            this.currentBlock = nextBlock;
            this.nextBlock = randomItem();
            this.currentBitmap = nextBitmap;
            this.nextBitmap = null;
            this.blockChange = true;
            return false;
        }
    }

    private void calculateScore() {
        List<Integer> fullLines = getFullLines();
        if (fullLines.size() == 1) {
            this.score += 40;
        } else if (fullLines.size() == 2) {
            this.score += 100;
        } else if (fullLines.size() == 3) {
            this.score += 300;
        } else if (fullLines.size() == 4) {
            this.score += 1200;
        }
    }

    private void clearFullLines() {
        List<Integer> fullLines = getFullLines();
        for (Integer fullLine : fullLines) {
            for (int j = fullLine; j >= 0; j--) {
                for (int i = 0; i < occupancy[j].length; i++) {
                    if (j != 0) {
                        occupancy[j][i] = occupancy[j - 1][i];
                        bitmaps[j][i] = bitmaps[j - 1][i];
                    } else {
                        occupancy[j][i] = 0;
                        bitmaps[j][i] = null;
                    }
                }
            }
        }
    }

    private List<Integer> getFullLines() {
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

        return fullLines;
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

    private void freezeCurrentBlock() {
        for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
            for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth(); i++) {

                int yOffset = j - currentBlock.getY();
                int xOffset = i - currentBlock.getX();
                if (currentBlock.getShape()[yOffset][xOffset] == 1 && yOffset >= 0 && xOffset >= 0 &&
                        indexExists(j, occupancy) && indexExists(i, occupancy[j])) {
                    occupancy[j][i] = currentBlock.getType().getN();
                    bitmaps[j][i] = currentBitmap;
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

        gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
        int previewGridSize = (PADDING - 2 * PREVIEW_PADDING) / PREVIEW_WIDTH;

        // Font settings
        p.setTextSize(TEXT_SIZE);
        p.setColor(Color.BLACK);
        p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        canvas.drawText("" + score, PADDING + (WIDTH * gridSize / 2), (TOP_PADDING / 2) + 10, p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        canvas.drawRect(PADDING, TOP_PADDING, PADDING + WIDTH * gridSize, TOP_PADDING + HEIGHT *
                gridSize, p);

        // Draw boundary of the preview
        canvas.drawRect(PADDING + WIDTH * gridSize + PREVIEW_PADDING,
                TOP_PADDING,
                PADDING + WIDTH * gridSize + PADDING - PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT, p);

        p.setStyle(Paint.Style.FILL);

        // First draw the shadow
        p.setColor(Color.parseColor("#D3D3D3"));
        drawShadow(canvas, p, currentBlock);

        // Then the actual block, so it obscures the shadow if necessary
        setColorForShape(p, currentBlock.getType());
        drawCurrentItem(canvas, p, currentBlock);

        drawNextItem(canvas, p, previewGridSize);

        for (int j = 0; j < occupancy.length; j++) {
            for (int i = 0; i < occupancy[j].length; i++) {
                if (occupancy[j][i] != 0) {
                    setColorForShape(p, occupancy[j][i]);
                    canvas.drawRect(i * gridSize + PADDING + 1, j * gridSize + TOP_PADDING + 1,
                            (i + 1) * gridSize + PADDING - 1, (j + 1) * gridSize + TOP_PADDING -
                                    1, p);

                    if (bitmaps[j][i] != null) {
                        canvas.drawBitmap(bitmaps[j][i], i * gridSize + PADDING + 1, j * gridSize +
                                TOP_PADDING +
                                1, p);
                    }
                }
            }
        }
    }

    public void drawCurrentItem(Canvas canvas, Paint p, Block item) {
        int currentY = item.getY();
        int currentX = item.getX();
        int currentWidth = item.getWidth();
        int currentHeight = item.getHeight();
        for (int j = item.getY(); j < currentY + currentHeight; j++) {
            for (int i = item.getX(); i < currentX + currentWidth; i++) {
                int yOffset = j - currentY;
                int xOffset = i - currentX;
                if (indexExists(yOffset, currentBlock.getShape()) && indexExists(xOffset, currentBlock.getShape()[yOffset])
                        && item.getShape()[yOffset][xOffset] == 1) {
                    canvas.drawRect(i * gridSize + PADDING + 1, j * gridSize + TOP_PADDING + 1,
                            (i + 1) * gridSize + PADDING - 1,
                            (j + 1) * gridSize + TOP_PADDING - 1, p);
                }
            }
        }
    }

    private void drawNextItem(Canvas canvas, Paint p, int previewGridSize) {
        int yStart = 1;
        int xStart = 0;
        int yLimit = 3;
        int xLimit = 3;

        switch (nextBlock.getType()) {
            case SQUARE:
                xStart = 1;
                break;
            case I:
                yStart = 2;
                xLimit = 4;
                break;
        }

        if (nextBitmap != null)
            this.nextBitmap = getResizedBitmap(this.nextBitmap, previewGridSize, previewGridSize);

        for (int j = yStart; j < yLimit; j++) {
            for (int i = xStart; i < xLimit; i++) {
                int yOffset = j - yStart;
                int xOffset = i - xStart;

                if (nextBlock.getShape()[yOffset][xOffset] == 1) {
                    setColorForShape(p, nextBlock.getType());
                    canvas.drawRect(i * previewGridSize + PADDING + WIDTH * gridSize + PREVIEW_PADDING + 1,
                            j * previewGridSize + TOP_PADDING + 1,
                            (i + 1) * previewGridSize + PADDING + WIDTH * gridSize + PREVIEW_PADDING - 1,
                            (j + 1) * previewGridSize + TOP_PADDING - 1, p);

                    if (nextBitmap != null)
                        canvas.drawBitmap(nextBitmap,
                                i * previewGridSize + PADDING + WIDTH * gridSize + PREVIEW_PADDING + 1,
                                j * previewGridSize + TOP_PADDING + 1, p);
                }
            }
        }
    }

    public void drawShadow(Canvas canvas, Paint p, Block item) {
        int oldY = item.getY();
        int[][] state = copy(occupancy);

        while (true) {
            item.simulateStepDown(state);
            if (!hasOverlap(state) && item.getY() + item.getHeight() < HEIGHT) {
                item.stepDown();
            } else {
                drawCurrentItem(canvas, p, item);
                item.setY(oldY);
                return;
            }
        }
    }

    public void hardDrop() {
        int oldY = currentBlock.getY();
        int[][] state = copy(occupancy);

        while (true) {
            currentBlock.simulateStepDown(state);
            if (!hasOverlap(state) && currentBlock.getY() + currentBlock.getHeight() < HEIGHT) {
                currentBlock.stepDown();
            } else {
                dropping = false;
                return;
            }
        }
    }

    public void rotateBlock() {
        int[][] state = copy(occupancy);
        this.currentBlock.simulateRotate(state);

        if (!hasOverlap(state)) {
            this.currentBlock.rotate();
        }

        if (this.currentBlock.getX() + this.currentBlock.getWidth() >= WIDTH) {
            this.currentBlock.setX(WIDTH - this.currentBlock.getWidth());
        }
    }

    public void drop() {
        dropping = true;
    }

    public void moveRight() {
        int[][] state = copy(occupancy);
        this.currentBlock.simulateStepRight(state);

        if (!hasOverlap(state)) {
            this.currentBlock.moveRight();
        }

        if (currentBlock.getX() + currentBlock.getWidth() >= WIDTH) {
            currentBlock.setX(WIDTH - currentBlock.getWidth());
        }
    }

    public void moveLeft() {
        int[][] state = copy(occupancy);
        this.currentBlock.simulateStepLeft(state);

        if (!hasOverlap(state)) {
            this.currentBlock.moveLeft();
        }

        Log.d("X", currentBlock.getX() + "");
    }

    public void drawIcon(Canvas canvas, Paint p) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
        this.currentBitmap = getResizedBitmap(this.currentBitmap, gridSize, gridSize);

        for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
            for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth(); i++) {

                int yOffset = j - currentBlock.getY();
                int xOffset = i - currentBlock.getX();
                if (indexExists(yOffset, currentBlock.getShape()) && indexExists(xOffset, currentBlock.getShape()[yOffset]) &&
                        currentBlock
                                .getShape()[yOffset][xOffset] == 1) {
                    canvas.drawBitmap(this.currentBitmap, i * gridSize + PADDING + 1, j * gridSize +
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

    public void setNextBitmap(Bitmap nextBitmap) {
        this.nextBitmap = nextBitmap;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
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

    public boolean isBlockChange() {
        return blockChange;
    }
}
