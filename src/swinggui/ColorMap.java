/*
 * Do whatever you want with this file
 */
package swinggui;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.SwingConstants;

/**
 *
 * @author jstar
 */
public class ColorMap {

    private double min = 100;
    private double max = 1000;
    private final static double BLUE_HUE = Color.RGBtoHSB(0, 0, 100, null)[0];
    private final static double RED_HUE = Color.RGBtoHSB(100, 0, 0, null)[0];

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
        if (max == min) {
            return Color.GRAY;
        }
        float hue = (float) (BLUE_HUE + (RED_HUE - BLUE_HUE) * (value - min) / (max - min));
        return new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
    }

    public Image createColorScaleImage(int width, int height, int orientation) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        if (orientation == SwingConstants.HORIZONTAL) {
            for (int x = 0; x < width; x++) {
                double value = min + (max - min) * x / width;
                int rgb = getColorForValue(value).getRGB();
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, rgb);
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                double value = max - (max - min) * y / height;
                int rgb = getColorForValue(value).getRGB();
                for (int x = 0; x < width; x++) {
                    image.setRGB(x, y, rgb);
                }
            }
        }
        return image;
    }
}
