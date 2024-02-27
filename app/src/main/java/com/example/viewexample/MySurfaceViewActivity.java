package com.example.viewexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;


public class MySurfaceViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MySurfaceView(this));
    }

    public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

        private final List<Circle> circles = new ArrayList<>();
        private final Paint paint;
        private DrawThread drawThread;

        public MySurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
            paint = new Paint();
            paint.setColor(Color.RED);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            drawThread = new DrawThread(holder);
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }


        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent (MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                circles.add(new Circle(event.getX(), event.getY()));
            }
            return false;
        }

        private class DrawThread extends Thread {
            private boolean running = false;
            private final SurfaceHolder surfaceHolder;

            public DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
            }

            public void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                Canvas canvas;
                while (running) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        if (canvas == null)
                            continue;
                        canvas.drawColor(Color.WHITE);
                        for (Circle circle : circles) {
                            circle.move(canvas);
                            canvas.drawCircle(circle.x, circle.y, circle.radius, paint);
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }

        private class Circle {
            float x, y;
            int radius = 50;
            int dx = 10, dy = 10;

            public Circle(float x, float y) {
                this.x = x;
                this.y = y;
            }

            public void move(Canvas canvas) {
                if (x + dx < radius || x + dx > canvas.getWidth() - radius) dx = -dx;
                if (y + dy < radius || y + dy > canvas.getHeight() - radius) dy = -dy;
                x += dx;
                y += dy;
            }
        }
    }
}
