package com.xiangcheng.amount;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.graphics.Shader;
import android.icu.text.DecimalFormat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangcheng on 18/1/2.
 */

public class AmountView extends View {
    //默认最大的额度
    private static final int MAX_AMOUNT = 3000000;
    private static final String TAG = AmountView.class.getSimpleName();
    private int amount = MAX_AMOUNT;
    private Paint radianPaint;
    private int mMinWidth = 160;
    private int mMinHeight = 80;

    private Path radianPath;
    private Path shadowPath;
    private Path shadowProgressPath;
    private int strokeWidth;
    private int shadowOffset;
    private Paint shadowPaint;
    private int shadowStrokeWidth;
    private int shadowColor = Color.parseColor("#1E90FF");
    private int alphaShadowColor = Color.parseColor("#331E90FF");

    private List<ValueAnimator> animators;

    private Path progressPath;

    private PathMeasure progressPathMeasure;

    private PathMeasure shadowPathMeasure;

    private String amountText;
    private Paint amountPaint;
    private Paint progressPaint;
    private String hintText = "最高可借金额";
    private Paint hintPaint;
    private int hintOffset;
    private float textTop;
    private int[] amounts;

    public AmountView(Context context) {
        this(context, null);
    }

    public AmountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AmountView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initArgus(attrs, context);
    }

    private void initArgus(AttributeSet attrs, Context context) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AmountView);
        amount = array.getInt(R.styleable.AmountView_max_amount, MAX_AMOUNT);
        hintText = array.getString(R.styleable.AmountView_hint_text);
        if (TextUtils.isEmpty(hintText)) {
            hintText = "最高可借金额";
        }
        shadowColor = array.getColor(R.styleable.AmountView_shadow_color, shadowColor);
        //透明度是33
        alphaShadowColor = Color.argb(16 * 3 + 3, Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor));
        Log.d(TAG, "amount:" + amount);
        shadowOffset = dp2px(7);
        strokeWidth = dp2px(4);
        shadowStrokeWidth = dp2px(3);
        hintOffset = dp2px(3);
        mMinWidth = dp2px(mMinWidth);
        mMinHeight = dp2px(mMinHeight);
        radianPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        radianPaint.setStrokeWidth(strokeWidth);
        radianPaint.setStyle(Paint.Style.STROKE);
        radianPaint.setColor(alphaShadowColor);
        radianPath = new Path();
        shadowPath = new Path();
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setPathEffect(new DashPathEffect(new float[]{dp2px(1.5f), dp2px(4)}, 0));
        shadowPaint.setStrokeWidth(shadowStrokeWidth);
        shadowPaint.setStyle(Paint.Style.STROKE);
        amountPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        amountPaint.setColor(Color.BLACK);
        amountPaint.setTextSize(dp2sp(25));
        amountPaint.setTextAlign(Paint.Align.CENTER);
        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(Color.BLACK);
        hintPaint.setTextSize(dp2sp(13));
        hintPaint.setTextAlign(Paint.Align.CENTER);
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setStyle(Paint.Style.STROKE);
        initAnimator();
    }

    public void setAmount(int amount) {
        this.amount = amount;
        initAnimator();
    }

    private void initAnimator() {
        animators = new ArrayList<>();
        String format;
        if (amount >= 1000) {
            DecimalFormat df = new DecimalFormat("#,###");
            format = df.format(amount);
        } else {
            format = amount + "";
        }
        String[] split = format.split(",");
        amounts = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            if (split[i].equals("000")) {
                split[i] = 1000 + "";
            }
            final int index = i;
            ValueAnimator animator = ValueAnimator.ofInt(0, Integer.parseInt(split[i]));
            animator.setDuration(1500);
            animator.setInterpolator(new LinearInterpolator());

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    amounts[index] = value;
                    if (index == 0) {
                        progressPath.reset();
                        shadowProgressPath.reset();
                        Log.d(TAG, "progressPathMeasure.getLength() * animation.getAnimatedFraction():" + progressPathMeasure.getLength() * animation.getAnimatedFraction());
                        Log.d(TAG, "progressPathMeasure.getLength():" + progressPathMeasure.getLength());
                        float progress = progressPathMeasure.getLength() * animation.getAnimatedFraction();
                        progressPathMeasure.getSegment(0, progress, progressPath, true);
                        shadowPathMeasure.getSegment(0, shadowPathMeasure.getLength() * animation.getAnimatedFraction(), shadowProgressPath, true);
                    }
                    invalidate();
                }
            });
            animators.add(animator);
        }

    }

    public void start() {
        for (int i = 0; i < animators.size(); i++) {
            ValueAnimator valueAnimator = animators.get(i);
            if (valueAnimator != null && valueAnimator.isRunning()) {
                return;
            }
            valueAnimator.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawCircle(canvas);
        drawShadow(canvas);
        drawAmountText(canvas);
        drawProgress(canvas);
        drawHint(canvas);
    }

    private void drawHint(Canvas canvas) {
        Paint.FontMetrics fontMetrics = hintPaint.getFontMetrics();
        float allHeight = fontMetrics.descent - fontMetrics.ascent;
        canvas.drawText(hintText, getWidth() / 2, textTop - hintOffset - allHeight - fontMetrics.ascent, hintPaint);
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawPath(progressPath, progressPaint);
    }

    private void drawAmountText(Canvas canvas) {
        amountText = "";
        for (int i = 0; i < amounts.length; i++) {
            if (i > 0) {
                if ((amounts[i] + "").equals("1000")) {
                    amountText += "000,";
                } else {
                    amountText += amounts[i] + ",";
                }
            } else {
                amountText += amounts[i] + ",";
            }
        }
        amountText = amountText.substring(0, amountText.length() - 1);
        Paint.FontMetrics fontMetrics = amountPaint.getFontMetrics();
        float allHeight = fontMetrics.descent - fontMetrics.ascent;
        textTop = getHeight() - getPaddingBottom() - allHeight;
        canvas.drawText(amountText, getWidth() / 2, textTop - fontMetrics.ascent, amountPaint);
    }

    private void drawShadow(Canvas canvas) {
        canvas.drawPath(shadowProgressPath, shadowPaint);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawPath(radianPath, radianPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        mMinWidth = mMinWidth + getPaddingLeft() + getPaddingRight();
        mMinHeight = mMinHeight + getPaddingTop() + getPaddingBottom();
        Log.d(TAG, "getPaddingLeft:" + getPaddingLeft());
        if (widthMode == MeasureSpec.AT_MOST) {
            if (heightMode == MeasureSpec.EXACTLY) {
                width = ((height - getPaddingTop() - getPaddingBottom()) * 2) + getPaddingLeft() + getPaddingRight();
            } else {
                width = mMinWidth;
                height = mMinHeight;
            }
        } else if (widthMode == MeasureSpec.EXACTLY) {
            if (heightMode == MeasureSpec.EXACTLY) {
                width = Math.min(width - getPaddingLeft() - getPaddingRight(), ((height - getPaddingTop() - getPaddingBottom()) * 2));
                height = (int) (width * 1.0 / 2) + getPaddingTop() + getPaddingBottom();
                width = width + getPaddingLeft() + getPaddingRight();
            } else {
                height = (width - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingTop() + getPaddingBottom();
            }
        } else {
            width = mMinWidth;
            height = mMinHeight;
        }
        RectF rect = new RectF();
        rect.left = getPaddingLeft() + strokeWidth;
        rect.right = width - getPaddingRight() - strokeWidth;
        rect.top = getPaddingTop() + strokeWidth;
        rect.bottom = height - getPaddingBottom();
        radianPath.addCircle(width / 2, height - getPaddingBottom(),
                height - getPaddingTop() - getPaddingBottom() - strokeWidth, Path.Direction.CW);
        shadowPath.addCircle(width / 2, height - getPaddingBottom(),
                height - getPaddingTop() - getPaddingBottom() - strokeWidth - shadowOffset, Path.Direction.CW);
        shadowPaint.setShader(new LinearGradient(rect.left, rect.bottom, rect.right, rect.bottom, alphaShadowColor,
                shadowColor, Shader.TileMode.CLAMP));
        progressPaint.setShader(new LinearGradient(rect.left, rect.bottom, rect.right, rect.bottom, alphaShadowColor,
                shadowColor, Shader.TileMode.CLAMP));
        progressPath = new Path();
        shadowProgressPath = new Path();
        progressPathMeasure = new PathMeasure();
        progressPathMeasure.setPath(radianPath, false);
        shadowPathMeasure = new PathMeasure();
        shadowPathMeasure.setPath(shadowPath, false);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        start();
    }

    private int dp2px(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private int dp2sp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
