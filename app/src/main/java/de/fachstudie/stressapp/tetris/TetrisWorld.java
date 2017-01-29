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
import java.util.concurrent.atomic.AtomicBoolean;

import de.fachstudie.stressapp.R;
import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.model.StressNotification;
import de.fachstudie.stressapp.networking.StressAppClient;
import de.fachstudie.stressapp.tetris.utils.ArrayUtils;

import static de.fachstudie.stressapp.tetris.Block.randomItem;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.copy;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.indexExists;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.drawableToBitmap;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.getResizedBitmap;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForShape;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForShapeAlpha;

public class TetrisWorld {

    // Various specification
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    private final int FULL_HEIGHT = 22;
    private final int PREVIEW_WIDTH = 4;
    private final int PADDING = 150;
    private final int NEXT_BLOCK_PREVIEW_PADDING = 10;
    private final int NOTIFICATIONS_SIZE_PREVIEW_PADDING = 20;
    private final int TEXT_SIZE = 40;
    private int TOP_PADDING = 0;
    // Initialization of the tetris field
    private int[][] occupancy = new int[FULL_HEIGHT][WIDTH];
    private Bitmap[][] bitmaps = new Bitmap[FULL_HEIGHT][WIDTH];
    private int[][] highlightings = new int[FULL_HEIGHT][WIDTH];

    private Block currentBlock;
    private Block nextBlock;
    private Bitmap currentBitmap;
    private Bitmap nextBitmap;
    private Bitmap notificationBitmap;

    private int score;
    private int gridSize;
    private int notificationsCount = 0;
    private boolean dropping = false;
    private boolean blockChange = false;
    private boolean gameOver = false;
    private boolean notificationPosted = false;
    private boolean highlighting = false;
    private AtomicBoolean overlapsBoundary = new AtomicBoolean(false);

    private List<StressNotification> notifications = new ArrayList<>();
    private DatabaseService dbService;
    private Context context;
    private StressAppClient client;
    private String scoreString = "0000";
    private long lastScoreChange = -1;
    private long lastFreezedTime = -1;
    private int lastScoreDelta = 0;
    private boolean clearedLastTime;
    private long lastShowCombo = -1;
    private int comboCount = 0;

    public TetrisWorld(Context context) {
        this.context = context;
        dbService = DatabaseService.getInstance(this.context);
        client = new StressAppClient(this.context);
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

        if (nextBitmap == null || notificationPosted) {
            notifications = dbService.getSpecificNotifications("false");
            notificationsCount = notifications.size();
            notificationPosted = false;
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
                notificationsCount = (notificationsCount != 0) ? notificationsCount - 1 : 0;
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
                applicationIcon = context.getPackageManager().getApplicationIcon(notification
                        .getApplication());
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
        int notificationBlocks = getFullNotificationBlocks();
        int oldScore = this.score;
        if (fullLines.size() == 1) {
            this.score += 40;
        } else if (fullLines.size() == 2) {
            this.score += 100;
        } else if (fullLines.size() == 3) {
            this.score += 300;
        } else if (fullLines.size() == 4) {
            this.score += 1200;
        }
        if (fullLines.size() > 0) {
            if (clearedLastTime) {
                this.lastShowCombo = System.currentTimeMillis();
                this.comboCount++;
                this.score += 50 * this.comboCount;
            }
            clearedLastTime = true;
        } else {
            clearedLastTime = false;
            this.comboCount = 0;
        }
        this.score += 10 * notificationBlocks;
        if (oldScore != this.score) {
            scoreString = String.valueOf(score);
            while (scoreString.length() < 4) {
                scoreString = "0" + scoreString;
            }
            lastScoreChange = System.currentTimeMillis();
            lastScoreDelta = this.score - oldScore;
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

    private int getFullNotificationBlocks() {
        int result = 0;
        for (int j = 0; j < occupancy.length; j++) {
            boolean isFull = true;
            int notificationCount = 0;
            for (int i = 0; i < occupancy[j].length; i++) {
                if (occupancy[j][i] == 0) {
                    isFull = false;
                    break;
                } else if (bitmaps[j][i] != null) {
                    notificationCount++;
                }
            }
            if (isFull) {
                result += notificationCount;
            }
        }
        return result;
    }

    private void freezeCurrentBlock() {
        highlighting = true;
        lastFreezedTime = System.currentTimeMillis();
        for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
            for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth();
                 i++) {

                int yOffset = j - currentBlock.getY();
                int xOffset = i - currentBlock.getX();
                if (indexExists(j, occupancy) && indexExists(i, occupancy[j]) &&
                        currentBlock.getShape()[yOffset][xOffset] == 1) {
                    occupancy[j][i] = currentBlock.getType().getN();
                    bitmaps[j][i] = currentBitmap;
                    highlightings[j][i] = 1;
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
        int previewGridSize = (PADDING - 2 * NEXT_BLOCK_PREVIEW_PADDING) / PREVIEW_WIDTH;

        // Font settings
        p.setTextSize(TEXT_SIZE);
        p.setColor(Color.parseColor("#D3D3D3"));
        p.setStyle(Paint.Style.FILL);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        p.setStrokeWidth(2);

        boolean notZero = false;
        for (int i = 0; i < 4; i++) {
            if (scoreString.charAt(i) != '0') {
                notZero = true;
            }
            if (notZero) {
                if (score >= 4000) {
                    p.setColor(context.getResources().getColor(R.color.black));
                } else if (score >= 3000) {
                    p.setColor(context.getResources().getColor(R.color.colorPrimary));
                } else if (score >= 2000) {
                    p.setColor(context.getResources().getColor(R.color.colorAccent));
                } else if (score >= 1000) {
                    p.setColor(context.getResources().getColor(R.color.red));
                } else if (score >= 300) {
                    p.setColor(context.getResources().getColor(R.color.orange));
                } else if (score >= 100) {
                    p.setColor(context.getResources().getColor(R.color.amber));
                } else {
                    p.setColor(context.getResources().getColor(R.color.yellow));
                }
            } else {
                p.setColor(Color.parseColor("#D3D3D3"));
            }
            canvas.drawRect(
                    PADDING + (WIDTH * gridSize / 2) + i * (gridSize + 5) - 2 * gridSize,
                    TOP_PADDING - 5 - gridSize,
                    PADDING + (WIDTH * gridSize / 2) + i * (gridSize + 5) - gridSize,
                    TOP_PADDING - 5, p);
            p.setColor(Color.parseColor("white"));
            canvas.drawText(scoreString.charAt(i) + "",
                    PADDING + (WIDTH * gridSize / 2) + i * (gridSize + 5) - 2 * gridSize + 0.25f *
                            gridSize,
                    TOP_PADDING - 10, p);
        }
        p.setColor(Color.parseColor("black"));

        // Draw number of notifications
        p.setTextSize(TEXT_SIZE - 10);
        canvas.drawText("" + notificationsCount,
                PADDING + WIDTH * gridSize + NEXT_BLOCK_PREVIEW_PADDING + 50,
                TOP_PADDING + PADDING - HEIGHT + NOTIFICATIONS_SIZE_PREVIEW_PADDING + (PADDING / 2),
                p);

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        canvas.drawRect(PADDING, TOP_PADDING, PADDING + WIDTH * gridSize, TOP_PADDING + HEIGHT *
                gridSize, p);

        // Draw boundary of the next block preview
        canvas.drawRect(PADDING + WIDTH * gridSize + NEXT_BLOCK_PREVIEW_PADDING,
                TOP_PADDING,
                PADDING + WIDTH * gridSize + PADDING - NEXT_BLOCK_PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT, p);

        // Draw boundary of the notifications number preview
        canvas.drawRect(PADDING + WIDTH * gridSize + NEXT_BLOCK_PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT + 20,
                PADDING + WIDTH * gridSize + PADDING - NEXT_BLOCK_PREVIEW_PADDING,
                TOP_PADDING + 2 * PADDING - HEIGHT, p);

        // Draw icon for number of notifications preview
        notificationBitmap = getResizedBitmap(notificationBitmap, TEXT_SIZE + 5, TEXT_SIZE + 5);
        canvas.drawBitmap(notificationBitmap, PADDING + WIDTH * gridSize +
                        NEXT_BLOCK_PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT + 20 + 40, p);

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
                    canvas.drawRect(i * gridSize + PADDING + 1, (j - 2) * gridSize + TOP_PADDING
                                    + 1,
                            (i + 1) * gridSize + PADDING - 1, (j - 1) * gridSize + TOP_PADDING -
                                    1, p);

                    if (highlightings[j][i] == 1) {
                        if (highlighting && System.currentTimeMillis() - lastFreezedTime < 500) {
                            setColorForShapeAlpha(p, occupancy[j][i]);
                            canvas.drawRect(i * gridSize + PADDING + 1, (j - 2) * gridSize +
                                            TOP_PADDING
                                            + 1,
                                    (i + 1) * gridSize + PADDING - 1, (j - 1) * gridSize +
                                            TOP_PADDING -
                                            1, p);
                        } else {
                            highlighting = false;
                            ArrayUtils.resetArray(highlightings);
                        }
                    }

                    if (bitmaps[j][i] != null) {
                        Bitmap bitmap = getResizedBitmap(bitmaps[j][i], iconSize, iconSize);
                        canvas.drawBitmap(bitmap, i * gridSize + PADDING + 1 + (gridSize / 8),
                                (j - 2) * gridSize + TOP_PADDING + 1 + (gridSize / 8), p);
                    }
                }
            }
        }

        p.setTextSize(40);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        float sw = p.getStrokeWidth();
        Paint.Align align = p.getTextAlign();
        p.setTextAlign(Paint.Align.CENTER);
        if (System.currentTimeMillis() - lastScoreChange < 1500 && System.currentTimeMillis() -
                lastScoreChange > 100) {
            p.setStrokeWidth(5);
            p.setColor(context.getResources().getColor(R.color.white));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawText("+" + lastScoreDelta, PADDING + (WIDTH * gridSize / 2), 500, p);
            p.setColor(context.getResources().getColor(R.color.red));
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(sw);
            canvas.drawText("+" + lastScoreDelta, PADDING + (WIDTH * gridSize / 2), 500, p);
        }
        if (System.currentTimeMillis() - lastShowCombo < 1500) {
            p.setStrokeWidth(5);
            p.setColor(context.getResources().getColor(R.color.white));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawText("Combo x" + this.comboCount + "!", PADDING + (WIDTH * gridSize / 2),
                    600, p);
            p.setStrokeWidth(sw);
            p.setColor(context.getResources().getColor(R.color.red));
            p.setStyle(Paint.Style.FILL);
            canvas.drawText("Combo x" + this.comboCount + "!", PADDING + (WIDTH * gridSize / 2),
                    600, p);
        }
        p.setStrokeWidth(sw);
        p.setTextAlign(align);
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
                        synchronized (currentBlock) {
                            int yOffset = j - currentY;
                            int xOffset = i - currentX;
                            if (indexExists(yOffset, currentBlock.getShape()) && indexExists
                                    (xOffset,
                                            currentBlock.getShape()[yOffset])
                                    && item.getShape()[yOffset][xOffset] == 1) {
                                canvas.drawRect(i * gridSize + PADDING + 1, (j - 2) * gridSize +
                                                TOP_PADDING + 1,
                                        (i + 1) * gridSize + PADDING - 1,
                                        (j - 1) * gridSize + TOP_PADDING - 1, p);
                            }
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

        if (nextBitmap != null)
            bitmap = getResizedBitmap(this.nextBitmap, previewIconSize, previewIconSize);

        for (int j = yStart; j < yLimit; j++) {
            for (int i = xStart; i < xLimit; i++) {
                int yOffset = j - yStart;
                int xOffset = i - xStart;

                if (nextBlock.getShape()[yOffset][xOffset] == 1) {
                    setColorForShape(p, nextBlock.getType());
                    canvas.drawRect(i * previewGridSize + PADDING + WIDTH * gridSize +
                                    NEXT_BLOCK_PREVIEW_PADDING + 1,
                            j * previewGridSize + TOP_PADDING + 1,
                            (i + 1) * previewGridSize + PADDING + WIDTH * gridSize +
                                    NEXT_BLOCK_PREVIEW_PADDING - 1,
                            (j + 1) * previewGridSize + TOP_PADDING - 1, p);

                    if (bitmap != null)
                        canvas.drawBitmap(bitmap,
                                i * previewGridSize + PADDING + WIDTH * gridSize +
                                        NEXT_BLOCK_PREVIEW_PADDING + 1
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

            for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight();
                 j++) {
                for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth
                        (); i++) {

                    int yOffset = j - currentBlock.getY();
                    int xOffset = i - currentBlock.getX();
                    if (indexExists(yOffset, currentBlock.getShape()) && indexExists(xOffset,
                            currentBlock.getShape()[yOffset]) &&
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
            if (!hasOverlap(state) && currentBlock.getY() + currentBlock.getHeight() <
                    FULL_HEIGHT) {
                currentBlock.stepDown();
            } else {
                dropping = false;
                return;
            }
        }
    }

    public void rotateBlock() {
        int[][] state = copy(occupancy);
        this.currentBlock.simulateRotate(state, overlapsBoundary);

        if (!hasOverlap(state) && !overlapsBoundary.get()) {
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
        scoreString = "0000";
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

    public int getScore() {
        return score;
    }

    public void saveScore() {
        if (score != 0) {
            dbService.saveScore(score);
            client.sendScore(this.context, score,
                    context.getSharedPreferences("de.fachstudie.stressapp.preferences", Context
                            .MODE_PRIVATE).getString("username", "You"));
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

    public void notificationPosted() {
        notificationPosted = true;
    }

    public void setNotificationBitmap(Bitmap notificationBitmap) {
        this.notificationBitmap = notificationBitmap;
    }

    public void setTopPadding(int heightPixels) {
        TOP_PADDING = (int) (heightPixels * 0.15);
    }
}
