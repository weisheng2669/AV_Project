package com.example.av_project.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

public class ClockView extends View {

    Paint paintForQuarter,paintForMinute,paintForCircle,paintForHandsOfClock,paintForMinuteHand,paintForNum;
    int width,height,radius;

    public ClockView(Context context) {
        super(context);
        initData();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public ClockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initData();
    }
    private void initData() {
        //画外圆
        paintForCircle  = new Paint();
        paintForCircle.setColor(Color.BLACK);
        paintForCircle.setStyle(Paint.Style.STROKE);
        paintForCircle.setStrokeWidth(3);
        //画刻度
        paintForQuarter = new Paint();
        paintForQuarter.setColor(Color.CYAN);
        paintForQuarter.setStrokeWidth(1);
        //画字
        paintForNum = new Paint();
        paintForNum.setColor(Color.BLACK);
        //画时针
        paintForHandsOfClock = new Paint();
        paintForHandsOfClock.setColor(Color.RED);
        paintForHandsOfClock.setStrokeWidth(10);
        //画分针
        paintForMinute = new Paint();
        paintForMinute.setColor(Color.GREEN);
        paintForMinute.setStrokeWidth(5);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //1.画圆
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        radius = Math.min(width,height)/2-5;
        canvas.drawCircle(width/2,height/2,radius,paintForCircle);
        //2.画刻钟
       for(int i=0;i<12;i++){
           canvas.drawLine(width/2,0,width/2,60,paintForQuarter);
           canvas.rotate(30,width/2,height/2);
           canvas.drawText(String.valueOf(i+1),width/2,65,paintForNum);
       }
       //3.画中心
       canvas.drawRect(width/2-5,height/2-5,width/2+5,height/2+5,paintForNum);
       canvas.translate(width/2,height/2);
       //4.画时钟
        canvas.drawLine(0,0,100,100,paintForHandsOfClock);

        //5.画分针
        canvas.drawLine(0,0,100,200,paintForMinute);


    }
}
