package ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class FontFitTextView extends TextView {
    private float mHi;
    private float mLo;

    // Attributes
    private Paint mTestPaint;

    public FontFitTextView(Context context) {
        super(context);
        initialise(context);
    }

    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise(context);
    }

    private void initialise(Context context) {
        mTestPaint = new Paint();
        mHi = getPaint().getTextSize();
        setSingleLine(true);
        mTestPaint.set(this.getPaint());
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        mLo = (int) (20 * scale + 0.5f);
        // max size defaults to the initially specified text size unless it is
        // too small
    }

    /*
     * Re size the font so the specified text fits in the text box assuming the
     * text box is the specified width.
     */
    private void refitText(String text, int textWidth) {
        if (textWidth <= 0)
            return;
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = mHi;
        float lo = mLo;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while ((hi - lo) > threshold) {
            float size = (hi + lo) / 2;
            mTestPaint.setTextSize(size);
            if (mTestPaint.measureText(text) >= targetWidth)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

}