package jan.schuettken.bierpongleague.basic;

/**
 * Created by Jan Schüttken on 23.01.2020 at 21:09
 */
public class DoublePoint implements Comparable {

    public double x, y;
    private DoublePoint center;

    public DoublePoint(DoublePoint dp) {
        this(dp.x, dp.y);
    }

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static double getDistance(DoublePoint d1, DoublePoint d2) {
        double distance = Math.pow((d2.x - d1.x), 2) + Math.pow((d2.y - d1.y), 2);
        distance = Math.sqrt(distance);
        return distance;
    }

    public double getDistance(DoublePoint d) {
        return getDistance(this, d);
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof DoublePoint) {
            double dis1 = getDistance(this, center);
            double dis2 = getDistance((DoublePoint) o, center);
            if (dis1 < dis2)
                return -1;
            if (dis1 == dis2)
                return 0;
            if (dis1 > dis2)
                return 1;
        }
        return 0;
    }

    public void setCenter(DoublePoint center) {
        this.center = center;
    }
}
