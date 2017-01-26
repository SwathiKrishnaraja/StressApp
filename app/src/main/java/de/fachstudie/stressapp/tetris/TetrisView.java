package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import de.fachstudie.stressapp.R;

public class TetrisView extends SurfaceView implements SurfaceHolder.Callback {
    private final TetrisWorld model;
    private TetrisViewThread thread = null;
    private float x, y = 0;
    private Paint p;
    private long lastUpdateTime = -1;

    private float lastTouchX = 0;
    private float lastTouchY = 0;
    private boolean swiping = false;
    private boolean dropping = false;
    private long lastUpdate = -1;
    private long lastTouchDown = -1;
    private int gravityTime = 20;

    private Handler handler;

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);
        thread = new TetrisViewThread(this, getHolder());
        p = new Paint();
        p.setColor(Color.GREEN);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_event_available);

        this.model = new TetrisWorld(context);
        this.model.addItem(new Block(3, 0, 0, 0));
        this.model.createNextItem();
        this.model.setNotificationBitmap(bitmap);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!swiping && !dropping) {
                this.model.rotateBlock();
            }
            if (dropping && System.currentTimeMillis() - lastTouchDown < 150 && !this.model.isBlockChange()) {
                this.model.hardDrop();

            } else if (dropping) {
                this.model.stopDropping();
            }
            swiping = false;
            dropping = false;

        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getX();
            lastTouchY = event.getY();

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (lastTouchX - event.getX() > 60 && lastTouchX != -1 && !dropping) {
                this.model.moveLeft();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                swiping = true;
            } else if (event.getX() - lastTouchX > 60 && lastTouchX != -1 && !dropping) {
                this.model.moveRight();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                swiping = true;
            }
            if (event.getY() - lastTouchY > 60 && !dropping) {
                dropping = true;
                this.model.drop();
                lastTouchY = 0;
                lastTouchDown = System.currentTimeMillis();
            }
        }
        return true;
    }

    public void draw(Canvas canvas) {
        if (this.model.isDropping()) {
            if (System.currentTimeMillis() - lastUpdate > 70 && lastUpdate != 0) {
                dropping = this.model.gravityStep();
                this.model.setDropping(dropping);
                lastUpdate = System.currentTimeMillis();
            }
        }

        setGravityTime();

        if (!model.isDropping() && (System.currentTimeMillis() - lastUpdateTime > gravityTime || lastUpdateTime == -1)) {
            model.gravityStep();
            lastUpdateTime = System.currentTimeMillis();
        }
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            model.drawState(canvas, p);

            if (model.getCurrentBitmap() != null)
                model.drawIcon(canvas, p);
        }

        if (this.model.isGameOver()) {
            this.pauseGame();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    final Message msg = new Message();
                    final Bundle bundle = new Bundle();
                    model.saveScore();
                    model.setGameOver(false);
                    bundle.putInt("highscore", model.getHighScore());
                    bundle.putInt("score", model.getScore());
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });
        }
    }

    private void setGravityTime() {
        if (model.isBlockVisible()) {
            gravityTime = 800;
        } else {
            gravityTime = 20;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (thread == null) {
            thread = new TetrisViewThread(this, surfaceHolder);
        }
        thread.setRunnable(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        thread.setRunnable(false);
        while (true) {
            try {
                thread.join();
                thread = null;
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void startNewGame() {
        this.model.startNewGame();
        thread.setPause(false);
    }

    public void pauseGame() {
        createNewThread();
        thread.setPause(true);
    }

    public void resumeGame() {
        createNewThread();
        thread.setPause(false);
    }

    public boolean isPause() {
        createNewThread();
        return thread.isPause();
    }

    public void createNewThread() {
        if (thread == null) {
            thread = new TetrisViewThread(this, getHolder());
        }
    }

    public void notificationPosted() {
        model.notificationPosted();
    }

    public void setHeight(int heightPixels) {
        model.setTopPadding(heightPixels);
    }
}