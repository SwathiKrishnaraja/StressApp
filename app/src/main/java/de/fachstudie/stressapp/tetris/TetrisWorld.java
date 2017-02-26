package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import de.fachstudie.stressapp.R;
import de.fachstudie.stressapp.db.DatabaseService;
import de.fachstudie.stressapp.model.StressNotification;
import de.fachstudie.stressapp.networking.StressAppClient;
import de.fachstudie.stressapp.tetris.constants.BlockColors;
import de.fachstudie.stressapp.tetris.utils.ArrayUtils;
import de.fachstudie.stressapp.tetris.utils.ColorUtils;

import static de.fachstudie.stressapp.tetris.Block.randomItem;
import static de.fachstudie.stressapp.tetris.constants.StringConstants.GOLD_BLOCKS;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.copy;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.indexExists;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.drawableToBitmap;
import static de.fachstudie.stressapp.tetris.utils.BitmapUtils.getResizedBitmap;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForBlockBitmap;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setColorForShape;
import static de.fachstudie.stressapp.tetris.utils.ColorUtils.setLightColorForShape;

public class TetrisWorld {

    public static final float CLEAR_TIME = 250f;
    // Various specification
    private final int WIDTH = 10;
    private final int HEIGHT = 20;
    private final int FULL_HEIGHT = 22;
    private final int PREVIEW_WIDTH = 4;
    private int TEXT_SIZE = 40;
    private int PADDING = 0;
    private int TOP_PADDING = 0;
    private int PREVIEW_PADDING = 0;

    // Initialization of the tetris field
    private int[][] occupancy = new int[FULL_HEIGHT][WIDTH];
    private Bitmap[][] bitmaps = new Bitmap[FULL_HEIGHT][WIDTH];
    private int[][] highlightings = new int[FULL_HEIGHT][WIDTH];

    private Block currentBlock;
    private Block nextBlock;
    private Bitmap currentBlockIcon;
    private Bitmap nextBlockIcon;
    private Bitmap notificationIcon;
    private Bitmap arrowDownIcon;

    private int score;
    private int gridSize;
    private int notificationCount = 0;
    private boolean dropping = false;
    private boolean blockChange = false;
    private boolean gameOver = false;
    private boolean notificationPosted = false;
    private boolean highlighting = false;
    private boolean currentBlockGolden = false;
    private boolean nextBlockGolden = false;
    private AtomicBoolean overlapsBoundary = new AtomicBoolean(false);

    private List<StressNotification> notifications = new ArrayList<>();
    private StressNotification loadedNotification;
    private DatabaseService dbService;
    private Context context;
    private StressAppClient client;
    private String scoreString = "0000";
    private long lastScoreChange = -1;
    private long lastFrozenTime = -1;
    private long lastShowCombo = -1;
    private int lastScoreDelta = 0;
    private int comboCount = 0;
    private int lastDroppedRows = 0;
    private int goldBlockCount = 0;
    private float stressLevel = 0;
    private boolean clearedLastTime;

    private SharedPreferences prefs;
    private boolean clearFullLinesNext = false;
    private List<Integer> currentFullLines;
    private long lastClearLinesTime = -1;

    public TetrisWorld(Context context) {
        this.context = context;
        dbService = DatabaseService.getInstance(this.context);
        client = new StressAppClient(this.context);
        setEventIcons(context);
        prefs = context.getSharedPreferences("de.fachstudie.stressapp.preferences",
                Context.MODE_PRIVATE);
        goldBlockCount = prefs.getInt(GOLD_BLOCKS, 0);
    }

    private void setEventIcons(Context context) {
        notificationIcon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_event_available);
        arrowDownIcon = BitmapFactory.decodeResource(context.getResources(),
                R.mipmap.ic_arrow_down);
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

        if (nextBlockIcon == null || notificationPosted) {
            notifications = dbService.getSpecificNotifications("false");
            notificationCount = notifications.size();
            notificationPosted = false;
            setNextNotificationBitmap();
        }

        if (!currentBlockGolden && !hasOverlap(state) &&
                currentBlock.getY() + currentBlock.getHeight() < FULL_HEIGHT) {
            currentBlock.stepDown();
            if (dropping) {
                currentBlock.increaseDroppedRows();
            }
            return true;

        } else if (currentBlockGolden && currentBlock.getY() + currentBlock.getHeight() <
                FULL_HEIGHT) {
            currentBlock.stepDown();
            currentBlock.deleteOverlaps(occupancy, bitmaps);
            return true;
        } else {
            freezeCurrentBlock();
            calculateScore();

            this.currentFullLines = getFullLines();
            if (currentFullLines.size() > 0) {
                lastClearLinesTime = System.currentTimeMillis();
                clearFullLinesNext = true;
            }

            checkGameOver();
            this.currentBlockGolden = false;

            if (!gameOver) {
                this.currentBlock = nextBlock;
                this.nextBlock = randomItem();
                this.blockChange = true;
                this.currentBlockGolden = false;

                if (nextBlockGolden) {
                    this.currentBlockGolden = true;
                    this.nextBlockGolden = false;
                } else {
                    this.updateNotificationIsLoaded(loadedNotification);
                    this.notificationCount = (notificationCount != 0) ? notificationCount - 1
                            : 0;
                }

                this.setBitmaps();
                if (currentBlockIcon != null && !currentBlockGolden) {
                    this.stressLevel += 5;
                    if (stressLevel > 100) {
                        this.stressLevel = 100;
                    }
                }
            }
            return false;
        }
    }

    private void setNextNotificationBitmap() {
        if (!notifications.isEmpty() && !nextBlockGolden) {
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
                this.nextBlockIcon = bitmap;
                loadedNotification = notification;
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
        if (nextBlockIcon != null) {
            this.currentBlockIcon = nextBlockIcon;
            this.nextBlockIcon = null;
            setNextNotificationBitmap();
        } else {
            this.currentBlockIcon = null;
        }
    }

    private void updateNotificationIsLoaded(StressNotification notification) {
        if (notification != null) {
            dbService.updateNotificationIsLoaded(notification.getId());
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

        this.score += this.lastDroppedRows;
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
        lastFrozenTime = System.currentTimeMillis();
        for (int j = currentBlock.getY(); j < currentBlock.getY() + currentBlock.getHeight(); j++) {
            for (int i = currentBlock.getX(); i < currentBlock.getX() + currentBlock.getWidth();
                 i++) {

                int yOffset = j - currentBlock.getY();
                int xOffset = i - currentBlock.getX();
                if (indexExists(j, occupancy) && indexExists(i, occupancy[j]) &&
                        currentBlock.getShape()[yOffset][xOffset] == 1) {
                    if (currentBlockGolden) {
                        occupancy[j][i] = 8;
                    } else {
                        occupancy[j][i] = currentBlock.getType().getN();
                    }
                    bitmaps[j][i] = currentBlockIcon;
                    highlightings[j][i] = 1;
                }
            }
        }
        lastDroppedRows = currentBlock.getDroppedRows();
        this.stressLevel += 0.5;
        if (stressLevel > 100) {
            stressLevel = 100;
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

        Log.d("canvasHeigth", canvasHeight + " ");
        Log.d("canvasWidth", canvasWidth + " ");

        Log.d("PADDING", PADDING + "");
        Log.d("TOP_PADDING", TOP_PADDING + "");
        Log.d("PREVIEW_PADDING", PREVIEW_PADDING + "");

        gridSize = (canvasWidth - 2 * PADDING) / WIDTH;

        int iconSize = gridSize - 2 * (gridSize / 8);
        int previewGridSize = (PADDING - 2 * PREVIEW_PADDING) / PREVIEW_WIDTH;

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

        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.DKGRAY);
        canvas.drawRect(PADDING, TOP_PADDING, PADDING + WIDTH * gridSize, TOP_PADDING + HEIGHT *
                gridSize, p);

        // Draw boundary of the golden blocks preview
        canvas.drawRect(PREVIEW_PADDING,
                (canvasHeight / 2) + 50,
                PADDING - PREVIEW_PADDING, (canvasHeight / 2) + 50 + PADDING - HEIGHT, p);

        // Draw boundary of the next block preview
        canvas.drawRect(PADDING + WIDTH * gridSize + PREVIEW_PADDING,
                TOP_PADDING,
                PADDING + WIDTH * gridSize + PADDING - PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT, p);

        // Draw boundary of the notifications number preview
        canvas.drawRect(PADDING + WIDTH * gridSize + PREVIEW_PADDING,
                TOP_PADDING + PADDING - HEIGHT + 20,
                PADDING + WIDTH * gridSize + PADDING - PREVIEW_PADDING,
                TOP_PADDING + 2 * PADDING - HEIGHT, p);

        p.setStyle(Paint.Style.FILL);

        // First draw the shadow
        p.setColor(Color.parseColor(BlockColors.LIGHT_GRAY));
        drawShadow(canvas, p, currentBlock);

        // Then the actual block, so it obscures the shadow if necessary
        setColorForShape(p, currentBlock.getType());
        setColorForBlockBitmap(p, currentBlockGolden);

        drawCurrentItem(canvas, p, currentBlock);
        drawGoldBlockInPreview(canvas, p, previewGridSize);
        drawNotificationBlockInPreview(canvas, p, previewGridSize);
        drawNextItem(canvas, p, previewGridSize);

        for (int j = 2; j < occupancy.length; j++) {
            for (int i = 0; i < occupancy[j].length; i++) {
                if (occupancy[j][i] != 0) {
                    setColorForShape(p, occupancy[j][i]);

                    float left = i * gridSize + PADDING + 1;
                    float top = (j - 2) * gridSize + TOP_PADDING + 1;
                    float right = (i + 1) * gridSize + PADDING - 1;
                    float bottom = (j - 1) * gridSize + TOP_PADDING - 1;

                    if (currentFullLines != null) {
                        if (currentFullLines.contains(j)) {
                            float dx = (right - left) / 2;
                            float dy = (bottom - top) / 2;
                            float dt = System.currentTimeMillis() - lastClearLinesTime;
                            float change = dt / CLEAR_TIME * dx;
                            if (change > dx) {
                                change = dx;
                            }
                            if (clearFullLinesNext && dt > CLEAR_TIME) {
                                clearFullLines();
                                currentFullLines = null;
                                lastClearLinesTime = -1;
                                clearFullLinesNext = false;
                            }
                            left += change;
                            top += change;
                            right -= change;
                            bottom -= change;
                        }
                    }
                    canvas.drawRect(left, top, right, bottom, p);

                    if (highlightings[j][i] == 1) {
                        if (highlighting && System.currentTimeMillis() - lastFrozenTime < 500) {
                            setLightColorForShape(p, occupancy[j][i]);
                            canvas.drawRect(left, top, right, bottom, p);
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

        // Cache old values
        Paint.Align align = p.getTextAlign();
        float sw = p.getStrokeWidth();
        drawStressLevel(canvas, p);

        p.setTextSize(TEXT_SIZE);
        p.setAntiAlias(true);
        p.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        p.setTextAlign(Paint.Align.CENTER);
        if (System.currentTimeMillis() - lastScoreChange < 1500 && System.currentTimeMillis() -
                lastScoreChange > 100) {
            p.setStrokeWidth(5);
            p.setColor(context.getResources().getColor(R.color.white));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawText("+" + lastScoreDelta, PADDING + (WIDTH * gridSize / 2), 2 * canvasHeight
                    / 3, p);
            p.setColor(context.getResources().getColor(R.color.black));
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(sw);
            canvas.drawText("+" + lastScoreDelta, PADDING + (WIDTH * gridSize / 2), 2 * canvasHeight
                    / 3, p);
        }
        if (System.currentTimeMillis() - lastShowCombo < 1500 && lastScoreChange > 100) {
            p.setStrokeWidth(5);
            p.setColor(context.getResources().getColor(R.color.white));
            p.setStyle(Paint.Style.STROKE);
            canvas.drawText("Combo x" + this.comboCount, PADDING + (WIDTH * gridSize / 2),
                    2 * canvasHeight / 3 + 100, p);
            p.setStrokeWidth(sw);
            p.setColor(context.getResources().getColor(R.color.black));
            p.setStyle(Paint.Style.FILL);
            canvas.drawText("Combo x" + this.comboCount, PADDING + (WIDTH * gridSize / 2),
                    2 * canvasHeight / 3 + 100, p);
        }
        p.setStrokeWidth(sw);
        p.setTextAlign(align);
    }

    private void drawStressLevel(Canvas canvas, Paint p) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(context.getResources().getColor(R.color.black));
        int stressLevelPadding = 50;
        p.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Stress", PADDING / 2, TOP_PADDING + 10, p);

        float barHeight = canvas.getHeight() * 0.322f;
        // Map stress level 0 - 100 to height of bar
        p.setColor(context.getResources().getColor(android.R.color.holo_red_dark));

        for (int i = 0; i < 10; i++) {
            if (this.stressLevel > 0 && i > 8) {
                p.setColor(context.getResources().getColor(R.color.yellow));
            } else if (this.stressLevel > 10 && i > 7) {
                p.setColor(context.getResources().getColor(R.color.amber));
            } else if (this.stressLevel > 20 && i > 6) {
                p.setColor(context.getResources().getColor(R.color.orange));
            } else if (this.stressLevel > 30 && i > 5) {
                p.setColor(context.getResources().getColor(R.color.darkorange));
            } else if (this.stressLevel > 40 && i > 4) {
                p.setColor(context.getResources().getColor(R.color.darkdarkorange));
            } else if (this.stressLevel > 50 && i > 3) {
                p.setColor(context.getResources().getColor(R.color.red));
            } else if (this.stressLevel > 60 && i > 2) {
                p.setColor(context.getResources().getColor(R.color.darkred));
            } else if (this.stressLevel > 70 && i > 1) {
                p.setColor(context.getResources().getColor(R.color.darkdarkred));
            } else if (this.stressLevel > 80 && i > 0) {
                p.setColor(context.getResources().getColor(R.color.darkdarkdarkred));
            } else if (this.stressLevel > 90 && i == 0) {
                p.setColor(context.getResources().getColor(R.color.veryred));
            } else {
                p.setColor(context.getResources().getColor(R.color.white));
            }
            float currTop = (barHeight / 10) * i + TOP_PADDING + 20;
            canvas.drawRect(stressLevelPadding, currTop, PADDING - stressLevelPadding,
                    currTop + (barHeight / 10), p);
        }

        p.setStyle(Paint.Style.STROKE);
        p.setColor(context.getResources().getColor(android.R.color.black));
        canvas.drawRect(stressLevelPadding, TOP_PADDING + 20, PADDING - stressLevelPadding,
                TOP_PADDING + 20 + canvas.getHeight() * 0.322f, p);
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

    private void drawGoldBlockInPreview(Canvas canvas, Paint p, int previewGridSize) {
        int canvasHeight = canvas.getHeight();
        int previewIconSize = previewGridSize - 2 * (previewGridSize / 8);

        p.setTextSize(TEXT_SIZE - 10);

        ColorUtils.setColorForBlock(p, goldBlockCount);

        Bitmap bitmap = getResizedBitmap(this.arrowDownIcon, previewIconSize, previewIconSize);

        for (int j = 1; j < 3; j++) {
            for (int i = 1; i < 3; i++) {
                canvas.drawRect(i * previewGridSize + PREVIEW_PADDING + 1,
                        j * previewGridSize + (canvasHeight / 2) + 50 + 1,
                        (i + 1) * previewGridSize + PREVIEW_PADDING - 1,
                        (j + 1) * previewGridSize + (canvasHeight / 2) + 50 - 1, p);

                if (bitmap != null)
                    canvas.drawBitmap(bitmap,
                            i * previewGridSize + PREVIEW_PADDING + 1
                                    + (previewGridSize / 8),
                            j * previewGridSize + (canvasHeight / 2) + 50 +
                                    1 + (previewGridSize / 8), p);
            }
        }

        p.setTextAlign(Paint.Align.CENTER);

        p.setStyle(Paint.Style.STROKE);
        float sw = p.getStrokeWidth();
        p.setStrokeWidth(5);
        p.setColor(Color.parseColor("white"));
        canvas.drawText("" + goldBlockCount,
                PREVIEW_PADDING + ((PADDING - 2 * PREVIEW_PADDING) / 2),
                (canvasHeight / 2) + 60 + ((PADDING - HEIGHT) / 2),
                p);

        p.setColor(Color.parseColor("black"));
        p.setStyle(Paint.Style.FILL);

        // Draw number of golden blocks
        canvas.drawText("" + goldBlockCount,
                PREVIEW_PADDING + ((PADDING - 2 * PREVIEW_PADDING) / 2),
                (canvasHeight / 2) + 60 + ((PADDING - HEIGHT) / 2),
                p);

        p.setTextAlign(Paint.Align.LEFT);
        p.setStrokeWidth(sw);
    }

    private void drawNotificationBlockInPreview(Canvas canvas, Paint p, int previewGridSize) {
        int previewIconSize = previewGridSize - 2 * (previewGridSize / 8);

        ColorUtils.setColorForNotificationBlock(p, notificationCount);

        Bitmap bitmap = getResizedBitmap(this.notificationIcon, previewIconSize, previewIconSize);

        for (int j = 1; j < 3; j++) {
            for (int i = 1; i < 3; i++) {
                canvas.drawRect(i * previewGridSize + PADDING + WIDTH * gridSize +
                                PREVIEW_PADDING + 1,
                        j * previewGridSize + TOP_PADDING + PADDING - HEIGHT + 20 + 1,
                        (i + 1) * previewGridSize + PADDING + WIDTH * gridSize +
                                PREVIEW_PADDING - 1,
                        (j + 1) * previewGridSize + TOP_PADDING + PADDING - HEIGHT + 20 - 1, p);

                if (bitmap != null)
                    canvas.drawBitmap(bitmap,
                            i * previewGridSize + PADDING + WIDTH * gridSize +
                                    PREVIEW_PADDING + 1 + (previewGridSize / 8),
                            j * previewGridSize + TOP_PADDING + PADDING - HEIGHT + 20 + 1
                                    + (previewGridSize / 8), p);
            }
        }

        p.setTextAlign(Paint.Align.CENTER);

        p.setStyle(Paint.Style.STROKE);
        float sw = p.getStrokeWidth();
        p.setStrokeWidth(5);
        p.setColor(Color.parseColor("white"));
        // Draw number of notifications
        canvas.drawText("" + notificationCount,
                PADDING + WIDTH * gridSize + PREVIEW_PADDING + ((PADDING - 2 * PREVIEW_PADDING) / 2),
                TOP_PADDING + PADDING - HEIGHT + 30 + ((PADDING - 20) / 2), p);

        p.setColor(Color.parseColor("black"));
        p.setStyle(Paint.Style.FILL);

        // Draw number of notifications
        canvas.drawText("" + notificationCount,
                PADDING + WIDTH * gridSize + PREVIEW_PADDING + ((PADDING - 2 * PREVIEW_PADDING) / 2),
                TOP_PADDING + PADDING - HEIGHT + 30 + ((PADDING - 20) / 2), p);


        p.setTextAlign(Paint.Align.LEFT);
        p.setStrokeWidth(sw);
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

        setColorForShape(p, nextBlock.getType());
        setColorForBlockBitmap(p, nextBlockGolden);

        if (nextBlockIcon != null && !nextBlockGolden) {
            bitmap = getResizedBitmap(this.nextBlockIcon, previewIconSize, previewIconSize);
        } else if (nextBlockGolden) {
            this.nextBlockIcon = arrowDownIcon;
            bitmap = getResizedBitmap(this.nextBlockIcon, previewIconSize, previewIconSize);
        }

        for (int j = yStart; j < yLimit; j++) {
            for (int i = xStart; i < xLimit; i++) {
                int yOffset = j - yStart;
                int xOffset = i - xStart;

                if (nextBlock.getShape()[yOffset][xOffset] == 1) {
                    canvas.drawRect(i * previewGridSize + PADDING + WIDTH * gridSize +
                                    PREVIEW_PADDING + 1,
                            j * previewGridSize + TOP_PADDING + 1,
                            (i + 1) * previewGridSize + PADDING + WIDTH * gridSize +
                                    PREVIEW_PADDING - 1,
                            (j + 1) * previewGridSize + TOP_PADDING - 1, p);

                    if (bitmap != null)
                        canvas.drawBitmap(bitmap,
                                i * previewGridSize + PADDING + WIDTH * gridSize +
                                        PREVIEW_PADDING + 1
                                        + (previewGridSize / 8),
                                j * previewGridSize + TOP_PADDING + 1 + (previewGridSize / 8), p);
                }
            }
        }
    }

    public void drawIcon(Canvas canvas, Paint p) {
        if (currentBlock.getY() > 1) {
            int canvasWidth = canvas.getWidth();

            gridSize = (canvasWidth - 2 * PADDING) / WIDTH;

            int iconSize = gridSize - 2 * (gridSize / 8);
            Bitmap bitmap = getResizedBitmap(this.currentBlockIcon, iconSize, iconSize);

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
            if (!currentBlockGolden && !hasOverlap(state) && currentBlock.getY() + currentBlock
                    .getHeight() <
                    FULL_HEIGHT) {
                currentBlock.stepDown();
                currentBlock.increaseDroppedRows();
            } else if (currentBlockGolden && currentBlock.getY() + currentBlock.getHeight() <
                    FULL_HEIGHT) {
                currentBlock.stepDown();
                currentBlock.deleteOverlaps(occupancy, bitmaps);
            } else {
                dropping = false;
                return;
            }
        }
    }

    public void rotateBlock() {
        synchronized (currentBlock) {
            int[][] state = copy(occupancy);
            this.currentBlock.simulateRotate(state, overlapsBoundary);

            if (!hasOverlap(state) && !overlapsBoundary.get()) {
                this.currentBlock.rotate();
            }

            if (this.currentBlock.getX() + this.currentBlock.getWidth() >= WIDTH) {
                this.currentBlock.setX(WIDTH - this.currentBlock.getWidth());
            }
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
        this.stressLevel = 0;
    }

    public boolean isBlockVisible() {
        if (currentBlock.getY() > 1) {
            return true;
        }
        return false;
    }

    public int getHighScore() {
        int score = dbService.getHighScore();
        return score;
    }

    public int getScore() {
        return score;
    }

    public void saveScore() {
        if (score != 0) {
            dbService.saveScore(score);
            if (!prefs.getString("username", "").isEmpty()) {
                client.sendScore(this.context, score, prefs.getString("username", ""));
            }
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

    public Bitmap getCurrentBlockIcon() {
        return currentBlockIcon;
    }

    public void notificationPosted() {
        notificationPosted = true;
    }

    public float getStressLevel() {
        return stressLevel;
    }

    public boolean isNextBlockGolden() {
        return nextBlockGolden;
    }

    public void setNextBlockGolden(boolean nextBlockGolden) {
        this.nextBlockGolden = nextBlockGolden;
    }

    public int getGoldBlockCount() {
        return goldBlockCount;
    }

    public void decreaseGoldBlockCount() {
        if (goldBlockCount != 0) {
            this.goldBlockCount--;
            prefs.edit().putInt(GOLD_BLOCKS, goldBlockCount).commit();
        }
    }

    public void increaseGoldBlockCount(int count) {
        int golden_blocks = prefs.getInt(GOLD_BLOCKS, 0);
        this.goldBlockCount = golden_blocks + count;
        prefs.edit().putInt(GOLD_BLOCKS, goldBlockCount).commit();
    }

    public void setPaddings(Canvas canvas) {
        if (canvas != null) {
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();

            PADDING = (int) (canvasWidth * 0.2);
            PREVIEW_PADDING = (int) (canvasWidth * 0.013);
            TOP_PADDING = (int) (canvasHeight * 0.15);
        }
    }

    public void setTextSize(Canvas canvas) {
        if (canvas != null) {
            int canvasHeight = canvas.getHeight();
            if (canvasHeight > 1400 && canvasHeight < 2000) {
                TEXT_SIZE = 50;
            } else if (canvasHeight > 2000 && canvasHeight < 2500) {
                TEXT_SIZE = 55;
            } else if (canvasHeight > 2500) {
                TEXT_SIZE = 60;
            }
        }
    }
}
