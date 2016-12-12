package de.fachstudie.stressapp.tetris;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by Sanjeev on 12.12.2016.
 */

public class TetrisViewThread extends Thread {
    private TetrisView view;
    private SurfaceHolder holder;
    private boolean run = false;

    public TetrisViewThread(TetrisView view, SurfaceHolder holder) {
        this.view = view;
        this.holder = holder;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while(run){
            try{
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    view.draw(canvas);
                }

            }finally {
                if(canvas != null){
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void setRunnable(boolean run){
        this.run = run;
    }
}
