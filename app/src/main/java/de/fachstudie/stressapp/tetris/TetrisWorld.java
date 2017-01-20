package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.model.StressNotification;
import de.fachstudie.stressapp.tetris.utils.ArrayUtils;

import static de.fachstudie.stressapp.tetris.Block.randomItem;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.copy;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.indexExists;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.drawableToBitmap;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.getResizedBitmap;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForShape;

public class TetrisWorld {

    // Various specification
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    private final int FULL_HEIGHT = 22;
    private final int PREVIEW_WIDTH = 4;
    private final int PADDING = 140;
    private final int TOP_PADDING = 160;
    private final int PREVIEW_PADDING = 10;
    private final int TEXT_SIZE = 40;

    // Initialization of the tetris field
    private int[][] occupancy = new int[FULL_HEIGHT][WIDTH];
    private Bitmap[][] bitmaps = new Bitmap[FULL_HEIGHT][WIDTH];

    private Block currentBlock;
    private Block nextBlock;
    private Bitmap currentBitmap;
    private Bitmap nextBitmap;

    private int score;
    private int gridSize;
    private boolean dropping = false;
    private boolean blockChange = false;
    private boolean gameOver = false;

    private int notificationsIndex = 0;
    private List<StressNotification> notifications = new ArrayList<>();
    private DatabaseService dbService;
    private Context context;

    public TetrisWorld(Context context) {
        this.context = context;
        dbService = DatabaseService.getInstance(this.context);
    }

    public void addItem(Block item) {
        this.currentBlock = item;
    }

    public void createNextItem() {
        this.nextBlock = randomItem();
    }

    public synchronized boolean gravityStep() {
        int[][] state = copy(occupancy);
        currentBlock.simulateStepDown(state);
        this.blockChange = false;

        if (nextBitmap == null) {
            notifications = dbService.getSpecificNotifications("false");
            setNextBitmap();
        }

        if (!hasOverlap(state) && currentBlock.getY() + currentBlock.getHeight() < FULL_HEIGHT) {
            currentBlock.stepDown();
            return true;

        } else {
            freezeCurrentBlock();
            calculateScore();
            clearFullLines();
            checkGameOver();

            if (!gameOver) {
                this.currentBlock = nextBlock;
                this.nextBlock = randomItem();
                this.setBitmaps();
                this.blockChange = true;
            }
            return false;
        }
    }

    private void setNextBitmap() {
        if (!notifications.isEmpty()) {
            StressNotification notification = notifications.get(0);
            Drawable applicationIcon = null;
            try {
                applicationIcon = context.getPackageManager().getApplicationIcon(notification.getApplication());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (applicationIcon != null) {
                Bitmap bitmap = drawableToBitmap(applicationIcon);
                this.nextBitmap = bitmap;
                this.updateNotificationIsLoaded(notification);
            }
            notifications.remove(0);
        }
    }

    private void checkGameOver() {
        int lowerBound = 3;
        int upperBound = 7;
        boolean cellIsOccupied = false;
        for (int i = lowerBound; i < upperBound; i++) {
            if (occupancy[0][i] != 0) {
                cellIsOccupied = true;
                break;
            }
        }
        if (cellIsOccupied) {
            gameOver = true;
        }
    }


    private void setBitmaps() {
        if (nextBitmap != null) {
            this.currentBitmap = nextBitmap;
            this.nextBitmap = null;
            setNextBitmap();
        } else {
            this.currentBitmap = null;
        }
    }

    private void updateNotificationIsLoaded(StressNotification notification) {
        if (notification != null) {
            notification.setLoaded(true);
            dbService.updateNotificationIsLoaded(notification);
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

    private void freezeCurrentBlock() {
        for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
            for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth(); i++) {

                int yOffset = j - currentBlock.getY();
                int xOffset = i - currentBlock.getX();
                if (indexExists(j, occupancy) && indexExists(i, occupancy[j]) &&
                        currentBlock.getShape()[yOffset][xOffset] == 1) {
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

    public void drawState(Canvas canvas, Paint p) {
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
        int iconSize = gridSize - 2 * (gridSize / 8);
        int previewGridSize = (PADDING - 2 * PREVIEW_PADDING) / PREVIEW_WIDTH;

        // Font settings
        p.setTextSize(TEXT_SIZE);
        p.setColor(Color.BLACK);
        p.setTypeface(Typeface.create("Arial", Typeface.BOLD));

        canvas.drawText("" + score, PADDING + (WIDTH * gridSize / 2), TOP_PADDING - 10, p);

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

        for (int j = 2; j < occupancy.length; j++) {
            for (int i = 0; i < occupancy[j].length; i++) {
                if (occupancy[j][i] != 0) {
                    setColorForShape(p, occupancy[j][i]);
                    canvas.drawRect(i * gridSize + PADDING + 1, (j - 2) * gridSize + TOP_PADDING + 1,
                            (i + 1) * gridSize + PADDING - 1, (j - 1) * gridSize + TOP_PADDING -
                                    1, p);

                    if (bitmaps[j][i] != null) {
                        Bitmap bitmap = getResizedBitmap(bitmaps[j][i], iconSize, iconSize);
                        canvas.drawBitmap(bitmap, i * gridSize + PADDING + 1 + (gridSize / 8),
                                (j - 2) * gridSize + TOP_PADDING + 1 + (gridSize / 8), p);
                    }
                }
            }
        }
    }

    public void drawCurrentItem(Canvas canvas, Paint p, Block item) {
        if (item.getY() > 1) {
            int currentY = item.getY();
            int currentX = item.getX();
            int currentWidth = item.getWidth();
            int currentHeight = item.getHeight();
            for (int j = item.getY(); j < currentY + currentHeight; j++) {
                for (int i = item.getX(); i < currentX + currentWidth; i++) {
                    synchronized (item) {
                        int yOffset = j - currentY;
                        int xOffset = i - currentX;
                        if (indexExists(yOffset, currentBlock.getShape()) && indexExists(xOffset, currentBlock.getShape()[yOffset])
                                && item.getShape()[yOffset][xOffset] == 1) {
                            canvas.drawRect(i * gridSize + PADDING + 1, (j - 2) * gridSize + TOP_PADDING + 1,
                                    (i + 1) * gridSize + PADDING - 1,
                                    (j - 1) * gridSize + TOP_PADDING - 1, p);
                        }
                    }
                }
            }
        }
    }

    private void drawNextItem(Canvas canvas, Paint p, int previewGridSize) {
        int yStart = 1;
        int xStart = 0;
        int yLimit = 3;
        int xLimit = 3;
        int previewIconSize = previewGridSize - 2 * (previewGridSize / 8);
        Bitmap bitmap = null;


        switch (nextBlock.getType()) {
            case SQUARE:
                xStart = 1;
                break;
            case I:
                yStart = 2;
                xLimit = 4;
                break;
        }

        if(nextBitmap != null)
            bitmap = getResizedBitmap(this.nextBitmap, previewIconSize, previewIconSize);

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

                    if (bitmap != null)
                        canvas.drawBitmap(bitmap,
                                i * previewGridSize + PADDING + WIDTH * gridSize + PREVIEW_PADDING + 1
                                        + (previewGridSize / 8),
                                j * previewGridSize + TOP_PADDING + 1 + (previewGridSize / 8), p);
                }
            }
        }
    }

    public void drawIcon(Canvas canvas, Paint p) {
        if (currentBlock.getY() > 1) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            int gridSize = (canvasWidth - 2 * PADDING) / WIDTH;
            int iconSize = gridSize - 2 * (gridSize / 8);
            Bitmap bitmap = getResizedBitmap(this.currentBitmap, iconSize, iconSize);

            for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
                for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth(); i++) {

                    int yOffset = j - currentBlock.getY();
                    int xOffset = i - currentBlock.getX();
                    if (indexExists(yOffset, currentBlock.getShape()) && indexExists(xOffset, currentBlock.getShape()[yOffset]) &&
                            currentBlock
                                    .getShape()[yOffset][xOffset] == 1) {
                        canvas.drawBitmap(bitmap, i * gridSize + PADDING + 1 + (gridSize / 8),
                                (j - 2) * gridSize + TOP_PADDING + 1 + (gridSize / 8), p);
                    }
                }
            }
        }
    }

    public void drawShadow(Canvas canvas, Paint p, Block item) {
        if (item.getY() > 1) {
            int oldY = item.getY();
            int[][] state = copy(occupancy);

            while (true) {
                item.simulateStepDown(state);
                if (!hasOverlap(state) && item.getY() + item.getHeight() < FULL_HEIGHT) {
                    item.stepDown();
                } else {
                    drawCurrentItem(canvas, p, item);
                    item.setY(oldY);
                    return;
                }
            }
        }
    }

    public synchronized void hardDrop() {
        int[][] state = copy(occupancy);

        while (true) {
            currentBlock.simulateStepDown(state);
            if (!hasOverlap(state) && currentBlock.getY() + currentBlock.getHeight() < FULL_HEIGHT) {
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

    public void moveRight() {
        int[][] state = ArrayUtils.copy(occupancy);
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
    }

    public void startNewGame() {
        occupancy = new int[FULL_HEIGHT][WIDTH];
        bitmaps = new Bitmap[FULL_HEIGHT][WIDTH];
        score = 0;
    }

    public boolean isBlockVisible() {
        if (currentBlock.getY() > 1) {
            return true;
        }
        return false;
    }

    public int getHighScore() {
        int score = dbService.getHighScore();
        return (score != 0 ? score : 0);
    }

    public void saveScore() {
        if (score != 0) {
            dbService.saveScore(score);
        }
    }

    public void drop() {
        dropping = true;
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

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public void notificationReceived() {
        nextBitmap = null;
    }
}
