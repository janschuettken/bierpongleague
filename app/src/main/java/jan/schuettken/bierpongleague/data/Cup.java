package jan.schuettken.bierpongleague.data;

import jan.schuettken.bierpongleague.basic.DoublePoint;

/**
 * Created by Jan Schüttken on 23.01.2020 at 20:27
 */
public class Cup implements Comparable {
    public final int pos;
    public boolean hit = true;
    public boolean mooved = false;
    public DoublePoint position;

    public Cup(Cup c) {
        pos = c.pos;
        hit = c.hit;
        mooved = c.mooved;
        position = new DoublePoint(c.position);
    }

    public Cup(int pos) {
        this.pos = pos;
    }

    public void moveToFreeSpot(Cup[] cups, DoublePoint center) {
        double minDistance = position.getDistance(center);
        int minPos = -1;
        for (int i = 0; i < cups.length; i++) {
            if (cups[i].hit) {
                double distance = cups[i].position.getDistance(center);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPos = i;
                }
            }
        }
        if (minPos >= 0) {
            Cup tmp = cups[minPos];
            this.setPosition(getPosition(minPos));
            cups[minPos] = this;
            cups[pos] = tmp;
        }
    }

    public void setPosition(DoublePoint position) {
        this.position = position;
    }

    public void setPosition() {
        position = getPosition(this);
    }

    public static DoublePoint getPosition(Cup c) {
        return getPosition(c.pos);
    }

    public static DoublePoint getPosition(int pos) {
        double cupSize = 10;
        double lineY = 5;
        int off;
        DoublePoint p = null;
        //line 1
        if (pos == 0)
            p = new DoublePoint(20, lineY);


        //line 4
        lineY = 5;
        if (pos >= 7 - 1) {
            off = pos - 6;
            return new DoublePoint((cupSize / 2) + cupSize * off, lineY);
        }
        //line 3
        lineY += Math.sqrt(3) * (cupSize / 2);
        if (pos >= 4 - 1) {
            off = pos - 3;
            return new DoublePoint((cupSize) + cupSize * off, lineY);
        }
        lineY += Math.sqrt(3) * (cupSize / 2);
        //line 2
        if (pos >= 2 - 1) {
            off = pos - 1;
            off++;
            return new DoublePoint((cupSize / 2) + cupSize * off, lineY);
        }
        lineY += Math.sqrt(3) * (cupSize / 2);
        //line 2
        if (pos >= 0) {
            return new DoublePoint(20, lineY);
        }
        return p;
    }

    @Override
    public int compareTo(Object o) {
        return position.compareTo(((Cup) o).position);
    }

    @Override
    public Cup clone() {
        return new Cup(this);
    }
}
