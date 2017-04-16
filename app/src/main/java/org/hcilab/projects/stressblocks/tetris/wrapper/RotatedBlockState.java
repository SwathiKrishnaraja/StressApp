package org.hcilab.projects.stressblocks.tetris.wrapper;

/**
 * Created by Sanjeev on 16.04.2017.
 */

public class RotatedBlockState {
    private boolean overlappingBoundary;
    private int previousX;

    public boolean isOverlappingBoundary() {
        return overlappingBoundary;
    }

    public void setOverlappingBoundary(boolean overlappingBoundary) {
        this.overlappingBoundary = overlappingBoundary;
    }

    public int getPreviousX() {
        return previousX;
    }

    public void setPreviousX(int previousX) {
        this.previousX = previousX;
    }
}
