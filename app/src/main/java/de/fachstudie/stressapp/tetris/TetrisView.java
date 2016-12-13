package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import de.fachstudie.stressapp.R;

public class TetrisView extends SurfaceView implements SurfaceHolder.Callback {
    private final TetrisWorld model;
    private TetrisViewThread thread = null;
    private Bitmap bitMap;
    private float x, y = 0;
    private Paint p;
    private long lastUpdateTime = -1;

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        bitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        getHolder().addCallback(this);
        thread = new TetrisViewThread(this, getHolder());
        p = new Paint();
        p.setColor(Color.GREEN);

        setLongClickable(true);
        setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return TetrisView.this.onLongPress();
            }
        });

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return TetrisView.this.onTouch(motionEvent);
            }
        });

        this.model = new TetrisWorld();
        this.model.addItem(new Block(2, 0, 0, 0));
    }

    private boolean onLongPress() {
        model.drop();
        return true;
    }

    public boolean onTouch(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            this.model.rotateBlock();
        }
        return false;
    }

    public void draw(Canvas canvas) {
        if (System.currentTimeMillis() - lastUpdateTime > 500 || lastUpdateTime == -1) {
            model.gravityStep();
            lastUpdateTime = System.currentTimeMillis();
        }
        if (canvas != null) {
            canvas.drawColor(Color.WHITE);
            model.drawState(canvas, p);
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
}