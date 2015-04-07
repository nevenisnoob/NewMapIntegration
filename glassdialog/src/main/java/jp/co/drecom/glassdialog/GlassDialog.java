package jp.co.drecom.glassdialog;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;


/**
 * TODO: document your custom view class.
 */
public class GlassDialog extends View {

    private Context context;

    private Drawable mBackgroundDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private ObjectAnimator mScaleUpAnim;
    private PropertyValuesHolder mScaleUpX;
    private PropertyValuesHolder mScaleUpY;

//    private ScaleAnimation mScaleDownAnim;
    private ObjectAnimator mScaleDownAnim;
    private PropertyValuesHolder mScaleDownX;
    private PropertyValuesHolder mScaleDownY;


    //customize attribute
    private String mTextTitle;
    private String mTextContent;
    private float mTextSize = 0;



    private float mDialogSize = 0;
    private float mTouchPointX;
    private float mTouchPointY;

    public GlassDialog(Context context) {
        super(context);
        init(context, null, 0);
    }

    public GlassDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GlassDialog(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);

    }

    private void init(Context context,AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.GlassDialog, defStyle, 0);

        this.context = context;

        mDialogSize = a.getDimension(R.styleable.GlassDialog_glassDialogSize, 0);
        //same
//        Log.v("TAG", "mDialogSize in dp is " + a.getDimension(R.styleable.GlassDialog_glassDialogSize,0));
        Log.v("TAG", "mDialogSize in pixel is " + mDialogSize);
        mTextTitle = a.getString(
                R.styleable.GlassDialog_glassTextTitle);
        mTextContent = a.getString(
                R.styleable.GlassDialog_glassTextContent);
        mTextSize = a.getDimension(R.styleable.GlassDialog_glassTextSize, mTextSize);
        mBackgroundDrawable = context.getResources().getDrawable(R.drawable.glass);

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        //TODO
        //init the animation

        mScaleUpX = PropertyValuesHolder.ofFloat("scaleX",0.0f, 1.2f, 1.0f, 1.1f, 1.0f);
        mScaleUpY = PropertyValuesHolder.ofFloat("scaleY",0.0f, 1.2f, 1.0f, 1.1f, 1.0f);

        mScaleDownX = PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.2f, 0.0f);
        mScaleDownY = PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.2f, 0.0f);


        mScaleUpAnim = ObjectAnimator.ofPropertyValuesHolder(this,mScaleUpX,mScaleUpY);
        mScaleUpAnim.setDuration(500);

//        mScaleDownAnim = new ScaleAnimation(1.0f,0.0f,1.0f,0.0f,
//                Animation.RELATIVE_TO_SELF,
//                0.5f,
//                Animation.RELATIVE_TO_SELF,
//                0.5f);
//        mScaleDownAnim.setDuration(500);
        mScaleDownAnim = ObjectAnimator.ofPropertyValuesHolder(this, mScaleDownX, mScaleDownY);
        mScaleDownAnim.setDuration(300);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();

    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mTextSize);
        mTextWidth = mTextPaint.measureText(mTextTitle);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();

        int contentWidth = getWidth() - paddingLeft - paddingRight;
        Log.v("TAG", "width is " + getWidth() + " height is " + getHeight());
        int contentHeight = getHeight() - paddingTop - paddingBottom;

        // Draw the example drawable on top of the text.
        if (mBackgroundDrawable != null) {
            mBackgroundDrawable.setBounds(paddingLeft, paddingTop,
                    paddingLeft + contentWidth, paddingTop + contentHeight);
            mBackgroundDrawable.draw(canvas);
        }


        // Draw the text.
        canvas.drawText(mTextTitle,
                paddingLeft + (contentWidth - mTextWidth) / 2,
                paddingTop + (contentHeight + mTextHeight) / 2,
                mTextPaint);
//        canvas.drawText(mTextContent,
//                mTextWidth + paddingLeft + (contentWidth - mTextWidth) / 2,
//                mTextHeight + paddingTop + (contentHeight + mTextHeight) / 2,
//                mTextPaint);



    }

    public void startAnim(MotionEvent event) {
        if (getVisibility() == GONE) {
            setVisibility(VISIBLE);

            Log.v("TAG", "before set");
            Log.v("TAG", "left is " + getX() + " top is " + getY());
            Log.v("TAG", "width is " + getWidth() + " height is " + getHeight());
            int locationOnScreen[] = new int[2];
            getLocationOnScreen(locationOnScreen);
//        int locationInWindow[] = new int[2];
//        getLocationInWindow(locationInWindow);

            int dValueY = locationOnScreen[1] - (int)getY();

//            int dValueCenterX = getWidth()/2;


            Log.v("TAG", "location on screen is " + locationOnScreen[0] + " , " + locationOnScreen[1]);
//        Log.v("TAG", "location in window is " + locationInWindow[0] + " , " + locationInWindow[1]);

            setX((int) event.getX() - getWidth()/2);
            //should minus the action bar height + status bar height
            setY((int) event.getY() - dValueY);
            Log.v("TAG", "after set");
            Log.v("TAG", "left is " + getX() + " top is " + getY());
            getLocationOnScreen(locationOnScreen);
//        getLocationInWindow(locationInWindow);
            Log.v("TAG", "location on screen is " + locationOnScreen[0] + " , " + locationOnScreen[1]);
//        Log.v("TAG", "location in window is " + locationInWindow[0] + " , " + locationInWindow[1]);
//        startAnimation(mScaleAnim);
            mScaleUpAnim.start();
        } else {

            mScaleDownAnim.start();
            mScaleDownAnim.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {

                }


                @Override
                public void onAnimationEnd(Animator animation) {
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
        }

    }


    public String getTextTile() {
        return mTextTitle;
    }


    public void setTextTitle(String exampleString) {
        mTextTitle = exampleString;
        invalidateTextPaintAndMeasurements();
    }


    public String getTextContent() {
        return mTextContent;
    }


    public void setTextContent(String exampleString) {
        mTextContent = exampleString;
        invalidateTextPaintAndMeasurements();
    }

    public float getDialogSize() {
        return mDialogSize;
    }

    public void setDialogSize(float mDialogSize) {
        this.mDialogSize = mDialogSize;
    }



}
