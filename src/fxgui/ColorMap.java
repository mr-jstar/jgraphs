package fxgui;

import javafx.geometry.Orientation;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 *
 * @author jstar
 */
public class ColorMap {

    private double min = 100;
    private double max = 1000;
    private final static double BLUE_HUE = Color.BLUE.getHue();
    private final static double RED_HUE = Color.RED.getHue();

    public ColorMap() {
    }

    public ColorMap(double min, double max) {
        this.min = min;
        this.max = max;
    }

    public void setMin(double newMin) {
        this.min = newMin;
    }

    public void setMax(double newMax) {
        this.max = newMax;
    }

    public Color getColorForValue(double value) {
        if (value < min || value > max) {
            return Color.BLACK;
        }
        double hue = BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - min) / (max - min);
        return Color.hsb(hue, 1.0, 1.0);
    }

    public Image createColorScaleImage(int width, int height, Orientation orientation) {
        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();
        if (orientation == Orientation.HORIZONTAL) {
            for (int x = 0; x < width; x++) {
                double value = min + (max - min) * x / width;
                Color color = getColorForValue(value);
                for (int y = 0; y < height; y++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                double value = max - (max - min) * y / height;
                Color color = getColorForValue(value);
                for (int x = 0; x < width; x++) {
                    pixelWriter.setColor(x, y, color);
                }
            }
        }
        return image;
    }
}
