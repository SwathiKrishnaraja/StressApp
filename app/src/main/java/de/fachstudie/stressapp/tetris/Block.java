package de.fachstudie.stressapp.tetris;

import java.util.Random;

import de.fachstudie.stressapp.tetris.constants.BlockConfigurations;

import static de.fachstudie.stressapp.tetris.Block.Shape.I;
import static de.fachstudie.stressapp.tetris.Block.Shape.J;
import static de.fachstudie.stressapp.tetris.Block.Shape.L;
import static de.fachstudie.stressapp.tetris.Block.Shape.S;
import static de.fachstudie.stressapp.tetris.Block.Shape.SQUARE;
import static de.fachstudie.stressapp.tetris.Block.Shape.T;
import static de.fachstudie.stressapp.tetris.Block.Shape.Z;
import static de.fachstudie.stressapp.tetris.utils.ArrayUtils.indexExists;

public class Block extends Item {

    private final int[][] shift;
    private Shape type;
    private int[][][] shape;
    private int rotationIndex = 0;

    public Block(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.type = Shape.L;
        this.shape = BlockConfigurations.L_ROTATIONS;
        this.shift = BlockConfigurations.L_SHIFTS;
    }

    public Block(int x, int y, int width, int height, Shape shape) {
        super(x, y, width, height);

        this.type = shape;
        switch (shape) {
            case SQUARE:
                this.shape = BlockConfigurations.SQUARE_ROTATIONS;
                this.shift = BlockConfigurations.SQUARE_SHIFTS;
                break;
            case L:
                this.shape = BlockConfigurations.L_ROTATIONS;
                this.shift = BlockConfigurations.L_SHIFTS;
                break;
            case T:
                this.shape = BlockConfigurations.T_ROTATIONS;
                this.shift = BlockConfigurations.T_SHIFTS;
                break;
            case I:
                this.shape = BlockConfigurations.I_ROTATIONS;
                this.shift = BlockConfigurations.I_SHIFTS;
                break;
            case J:
                this.shape = BlockConfigurations.J_ROTATIONS;
                this.shift = BlockConfigurations.J_SHIFTS;
                break;
            case S:
                this.shape = BlockConfigurations.S_ROTATIONS;
                this.shift = BlockConfigurations.S_SHIFTS;
                break;
            case Z:
                this.shape = BlockConfigurations.Z_ROTATIONS;
                this.shift = BlockConfigurations.Z_SHIFTS;
                break;
            default:
                this.shape = BlockConfigurations.T_ROTATIONS;
                this.shift = BlockConfigurations.T_SHIFTS;
                break;
        }
    }

    public static Block randomItem() {
        Random r = new Random();
        int number = r.nextInt(7);
        int x = 3;
        if (number == 0) {
            return new Block(x, 0, 0, 0, L);
        } else if (number == 1) {
            return new Block(x, 0, 0, 0, T);
        } else if (number == 2) {
            return new Block(x, 0, 0, 0, SQUARE);
        } else if (number == 3) {
            return new Block(x, 0, 0, 0, I);
        } else if (number == 4) {
            return new Block(x, 0, 0, 0, J);
        } else if (number == 5) {
            return new Block(x, 0, 0, 0, S);
        } else {
            return new Block(x, 0, 0, 0, Z);
        }
    }

    public Shape getType() {
        return type;
    }

    public int[][] getShape() {
        return shape[rotationIndex % 4];
    }

    @Override
    public int getHeight() {
        return shape[rotationIndex % 4].length;
    }

    @Override
    public int getWidth() {
        return shape[rotationIndex % 4][0].length;
    }

    public void rotate() {
        rotate(1);
    }

    public void rotate(int direction) {
        if (direction > 0) {
            this.y += shift[(rotationIndex) % 4][0];
            this.x += shift[(rotationIndex) % 4][1];
        } else {
            this.y -= shift[(rotationIndex - 1) % 4][0];
            this.x -= shift[(rotationIndex - 1) % 4][1];
        }
        this.rotationIndex += direction;

        if (this.x < 0) {
            this.x = 0;
        }
    }

    public void simulateRotate(int[][] state) {
        this.rotate(1);
        computeOverlaps(state);
        this.rotate(-1);
    }

    private void computeOverlaps(int[][] state) {
        int currentY = getY();
        int currentX = getX();
        int currentWidth = getWidth();
        int currentHeight = getHeight();
        for (int j = currentY; j < currentY + currentHeight && j < state.length; j++) {
            for (int i = getX(); i < currentX + currentWidth && j >= 0 && i < state[j].length;
                 i++) {
                synchronized (this) {
                    synchronized (getShape()) {
                        int yOffset = j - currentY;
                        int xOffset = i - currentX;
                        if (indexExists(yOffset, getShape()) && indexExists(xOffset, getShape()
                                [yOffset])
                                && getShape()[yOffset][xOffset] == 1) {
                            if (indexExists(j, state) && indexExists(i, state[j]) && state[j][i]
                                    > 0) {
                                state[j][i] = -1;
                            }
                        }
                    }
                }
            }
        }
    }

    public void simulateStepDown(int[][] state) {
        this.setY(getY() + 1);
        computeOverlaps(state);
        this.setY(getY() - 1);
    }

    public void simulateStepRight(int[][] state) {
        this.moveRight();
        computeOverlaps(state);
        this.moveLeft();
    }

    public void simulateStepLeft(int[][] state) {
        this.moveLeft();
        computeOverlaps(state);
        this.moveRight();
    }

    public enum Shape {
        SQUARE(1), L(2), T(3), I(4), J(5), S(6), Z(7);

        private int n;

        Shape(int n) {
            this.n = n;
        }

        public int getN() {
            return n;
        }
    }
}
