package com.example.av_project.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.example.av_project.R;

public class MusicView extends View {

    Paint paint;
    float width,height;



    public MusicView(Context context) {
        super(context);
        initData();
    }



    public MusicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initData();
    }

    public MusicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initData();
    }

    public MusicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initData();
    }

    @SuppressLint("ResourceAsColor")
    private void initData() {
        paint = new Paint();

        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        paint.setColor(Color.rgb(123,76,129));
        for(int i=0;i<10;i++){
            double nowHeight = Math.random()*height;
            canvas.drawRect(i*width/10, (float) nowHeight,(i+1)*width/10,height,paint);
        }

        postInvalidateDelayed(300);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }
}
