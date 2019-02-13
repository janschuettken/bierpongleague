package jan.schuettken.bierpongleague.handler;

import android.graphics.Color;

/**
 * Created by Jan Schüttken on 27.09.2018 at 02:20
 */
public class ColorFunctionProvider {
    public static int setColor(double r, double g, double b) {
        return setColor(1.0, r, g, b);
    }

    public static int setColor(double a, double r, double g, double b) {
        int A_c = (int) (a * 255.0);
        int R_c = (int) (r * 255.0);
        int G_c = (int) (g * 255.0);
        int B_c = (int) (b * 255.0);
        return setColor(A_c, R_c, G_c, B_c);
    }

    public static int setColor(int a, int r, int g, int b) {
        return (a & 0xff) << 24 | (r & 0xff) << 16 | (g & 0xff) << 8 | (b & 0xff);
    }

    public static int getRandomColor(double alpha) {
        return setColor(alpha, Math.random(), Math.random(), Math.random());
    }

    public static int setAlphaToNull(int argb) {
        return ColorFunctionProvider.setColor(
                (((double) Color.red(argb)) / 255.0) * 100.0,
                (((double) Color.green(argb)) / 255.0) * 100.0,
                (((double) Color.blue(argb)) / 255.0) * 100.0);
    }

    public static int getRandomColor() {
        return getRandomColor(1.0);
    }

    /**
     * If the platform is running O but the app is not providing AdaptiveIconDrawable, then
     * shrink the legacy icon and set it as foreground. Use color drawable as background to
     * create AdaptiveIconDrawable.
     */
}
