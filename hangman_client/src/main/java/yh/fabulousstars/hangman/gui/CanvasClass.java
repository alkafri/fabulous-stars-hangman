package yh.fabulousstars.hangman.gui;

import javafx.scene.canvas.Canvas;

public class CanvasClass extends Canvas {
    public CanvasClass() {
    }

    public CanvasClass(double v, double v1) {
        super(v, v1);
    }

    @Override
    public double minHeight(double width)
    {
        return 376;
    }

    @Override
    public double maxHeight(double width)
    {
        return Double.MAX_VALUE;
    }

    @Override
    public double prefHeight(double width)
    {
        return minHeight(width);
    }

    @Override
    public double minWidth(double height)
    {
        return 376;
    }

    @Override
    public double maxWidth(double height)
    {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isResizable()
    {
        return true;
    }

    @Override
    public void resize(double width, double height)
    {
        super.setWidth(width);
        super.setHeight(height);

    }}