package de.fachstudie.stressapp.tetris;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import de.fachstudie.stressapp.R;

public class TetrisView extends SurfaceView implements SurfaceHolder.Callback {
    private TetrisViewThread thread = null;
    private Bitmap bitMap;
    private float x,y = 0;

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);

        bitMap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        getHolder().addCallback(this);
        thread = new TetrisViewThread(this, getHolder());
    }

    public void draw(Canvas canvas) {
        if(Math.abs(bitMap.getHeight() + y) > canvas.getHeight()){
            thread.setRunnable(false);
        }else {
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitMap, x, y, null);

            y += 10;
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
        while(true){
            try{
                thread.join();
                thread = null;
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}