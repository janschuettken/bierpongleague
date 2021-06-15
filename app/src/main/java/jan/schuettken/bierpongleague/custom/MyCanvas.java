package jan.schuettken.bierpongleague.custom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import jan.schuettken.bierpongleague.basic.DoublePoint;
import jan.schuettken.bierpongleague.data.Cup;

/**
 * Created by Jan Schüttken on 23.01.2020 at 22:11
 */
public class MyCanvas extends View {
    private Paint paint;
    private Cup[] originalCups, movedCups;
    private int width, height;
    private DoublePoint center;

    public MyCanvas(Context context, Cup[] originalCups, Cup[] movedCups) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.originalCups = originalCups;
        this.movedCups = movedCups;
        height = 700;
        width = 500;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(1);
        float r = 5;
        float factor = 20, xOff = width / 2f - 4f * r * 2f * factor, yOff = 0;
        xOff = 100;
        yOff = 40;
        for (Cup cup : originalCups) {
            if (cup.hit)
                paint.setColor(Color.LTGRAY);
            else
                paint.setColor(Color.GREEN);
            float x, y;
            x = (float) cup.position.x * factor + xOff;
            y = height - (float) cup.position.y * factor + yOff;
            canvas.drawCircle(x, y, r * factor, paint);
            if(!cup.hit) {
                paint.setColor(Color.BLUE);
                canvas.drawLine(x, y, (float) center.x * factor + xOff, height - (float) center.y * factor + yOff, paint);
                paint.setColor(Color.RED);
                canvas.drawText((cup.position.getDistance(center)) + "", x + 5, y + 5, paint);
            }
        }
        for (Cup cup : movedCups) {
            if (cup.hit)
                continue;
            paint.setColor(Color.BLACK);
            float x, y;
            x = (float) cup.position.x * factor + xOff;
            y = height - (float) cup.position.y * factor + yOff;
            canvas.drawCircle(x, y, (r - 2f) * factor, paint);
        }
        if (center != null) {
            paint.setColor(Color.BLUE);
            canvas.drawCircle((float) center.x * factor + xOff, height - (float) center.y * factor + yOff, 2 * factor, paint);
        }
//        paint.setColor(Color.RED);
//        canvas.drawCircle(dpToPx((int) cup.position.x), dpToPx((int) cup.position.y), dpToPx(3), paint);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public DoublePoint getCenter() {
        return center;
    }

    public void setCenter(DoublePoint center) {
        this.center = center;
    }


    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }
}
