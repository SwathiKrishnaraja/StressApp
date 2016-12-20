package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

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
    private boolean notificationPosted = false;
    private String notificationText = "";
    private Bitmap bitmap;
    private long lastTouchDown = -1;

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);
        thread = new TetrisViewThread(this, getHolder());
        p = new Paint();
        p.setColor(Color.GREEN);

        this.model = new TetrisWorld();
        this.model.addItem(new Block(2, 0, 0, 0));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (!swiping && !dropping) {
                this.model.rotateBlock();
            }
            if (dropping && System.currentTimeMillis() - lastTouchDown < 150) {
                this.model.hardDrop();
            } else if(dropping) {
                this.model.stopDropping();
            }
            swiping = false;
            dropping = false;
        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = event.getX();
            lastTouchY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (lastTouchX - event.getX() > 50 && lastTouchX != -1 && !dropping) {
                Log.d("Left", event.getX() + "");
                this.model.moveLeft();
                lastTouchX = event.getX();
                lastTouchY = event.getY();
                swiping = true;
            } else if (event.getX() - lastTouchX > 50 && lastTouchX != -1 && !dropping) {
                Log.d("Right", event.getX() + "");
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
                Log.d("Dropping", dropping + "");
                lastUpdate = System.currentTimeMillis();
            }
        }

        if (System.currentTimeMillis() - lastUpdateTime > 350 || lastUpdateTime == -1) {
            model.gravityStep();
            lastUpdateTime = System.currentTimeMillis();
        }
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            model.drawState(canvas, p);

            if (notificationPosted) {
                if (bitmap != null) {
                    model.setBitmap(bitmap);
                    model.drawIcon(canvas, p);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
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

    public void postNotification(String title) {
        notificationPosted = true;
        notificationText = title;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}