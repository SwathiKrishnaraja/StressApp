package org.hcilab.projects.stressblocks.tetris;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Handles tetris game in an own thread.
 */
public class TetrisViewThread extends Thread {
    private TetrisView view;
    private SurfaceHolder holder;
    private boolean run = false;
    private boolean pause = false;

    public TetrisViewThread(TetrisView view, SurfaceHolder holder) {
        this.view = view;
        this.holder = holder;
    }

    @Override
    public void run() {
        Canvas canvas = null;
        while (run) {
            if (!pause) {
                try {
                    canvas = holder.lockCanvas();
                    synchronized (holder) {
                        view.draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        holder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void setPause(boolean pause) {
        this.pause = pause;
    }

    public void setRunnable(boolean run) {
        this.run = run;
    }
}
