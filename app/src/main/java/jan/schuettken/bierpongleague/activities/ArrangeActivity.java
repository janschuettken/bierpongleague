package jan.schuettken.bierpongleague.activities;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.Objects;

import jan.schuettken.bierpongleague.R;
import jan.schuettken.bierpongleague.basic.BasicDrawerPage;
import jan.schuettken.bierpongleague.basic.DoublePoint;
import jan.schuettken.bierpongleague.custom.MyCanvas;
import jan.schuettken.bierpongleague.data.Cup;
import jan.schuettken.bierpongleague.handler.ApiHandler;

/**
 * Created by Jan Schüttken on 15.02.2019 at 09:30
 */
public class ArrangeActivity extends BasicDrawerPage {
    private Handler handler;
    private ApiHandler apiHandler;
    private Cup[] cups;
    private Cup[] moved;
    private MyCanvas canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrange);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.arrange);
        handler = new Handler();
        cups = new Cup[10];
        for (int i = 0; i < cups.length; i++) {
            cups[i] = new Cup(i);
        }
        setupClickListener();
        Button b = findViewById(R.id.button_arrange);
        b.setOnClickListener(v -> arrangeCups());
    }

    private void setupClickListener() {
        RelativeLayout rl;
        rl = findViewById(R.id.cup1);
        rl.setOnClickListener(v -> cupChange(v, 0));
        rl = findViewById(R.id.cup2);
        rl.setOnClickListener(v -> cupChange(v, 1));
        rl = findViewById(R.id.cup3);
        rl.setOnClickListener(v -> cupChange(v, 2));
        rl = findViewById(R.id.cup4);
        rl.setOnClickListener(v -> cupChange(v, 3));
        rl = findViewById(R.id.cup5);
        rl.setOnClickListener(v -> cupChange(v, 4));
        rl = findViewById(R.id.cup6);
        rl.setOnClickListener(v -> cupChange(v, 5));
        rl = findViewById(R.id.cup7);
        rl.setOnClickListener(v -> cupChange(v, 6));
        rl = findViewById(R.id.cup8);
        rl.setOnClickListener(v -> cupChange(v, 7));
        rl = findViewById(R.id.cup9);
        rl.setOnClickListener(v -> cupChange(v, 8));
        rl = findViewById(R.id.cup10);
        rl.setOnClickListener(v -> cupChange(v, 9));

    }

    private void cupChange(View v, int id) {
        cups[id].hit = !cups[id].hit;
        RelativeLayout rl = (RelativeLayout) v;
        Drawable d = getDrawable(!cups[id].hit ? R.drawable.round_red : R.drawable.round_white);
        rl.setBackground(d);
    }

    @Override
    protected void selectPage() {
        selectPage(R.id.nav_arrange);
    }

    private void arrangeCups() {
        DoublePoint center;
        for (Cup c : cups) {
            c.setPosition();
            System.out.println("Cup(" + c.pos + "), X:" + c.position.x + ", Y:" + c.position.y);
        }
        center = getCenter(cups);
        System.out.println("Center X:" + center.x + " Y:" + center.y);
        moveCups(center);
        canvas = new MyCanvas(this, cups, moved);
        canvas.setCenter(center);
        drawCups();
    }

    private void moveCups(DoublePoint center) {
        moved = cups.clone();
        moved = new Cup[cups.length];
        for(int i =0;i<cups.length;i++){
            moved[i]=cups[i].clone();
        }
//        Cup[] sort = cups.clone();
//        for (Cup c : sort) {
//            c.position.setCenter(center);
//        }
//        Arrays.sort(sort);
        for (Cup c : moved) {
            c.moveToFreeSpot(moved, center);
        }
    }

    private DoublePoint getCenter(Cup[] cups) {
        double x = 0, y = 0;
        for (Cup c : cups) {
            if (!c.hit) {
                x += c.position.x;
                y += c.position.y;
            }
        }
        int length = leftCups();
        x = x / (double) length;
        y = y / (double) length;
        return new DoublePoint(x, y);
    }

    private int leftCups() {
        int length = 0;
        for (Cup c : cups)
            if (!c.hit)
                length++;
        return length;
    }

    private double getDistance(Cup c1, Cup c2) {
        return DoublePoint.getDistance(c1.position, c2.position);
    }

    private void drawCups() {
        RelativeLayout rl = findViewById(R.id.region_cups);
        canvas.setBackgroundColor(Color.WHITE);
        rl.addView(canvas);
//        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        canvas.drawCircle((float) p.x, (float) p.y, 3, textPaint);
//        rl.draw(canvas);
    }
}
