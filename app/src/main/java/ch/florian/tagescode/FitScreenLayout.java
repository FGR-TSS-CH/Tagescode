package ch.florian.tagescode;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/** Fits the complete single-screen content without hiding controls or scrolling. */
public final class FitScreenLayout extends FrameLayout {
    public FitScreenLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        if (getChildCount() == 0) {
            setMeasuredDimension(width, height);
            return;
        }
        View content = getChildAt(0);
        int availableWidth = Math.max(0, width - getPaddingLeft() - getPaddingRight());
        content.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        if (MeasureSpec.getMode(heightSpec) == MeasureSpec.UNSPECIFIED) {
            height = content.getMeasuredHeight() + getPaddingTop() + getPaddingBottom();
        }
        setMeasuredDimension(width, height);
    }

    @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() == 0) return;
        View content = getChildAt(0);
        int availableHeight = Math.max(0, getHeight() - getPaddingTop() - getPaddingBottom());
        float scale = content.getMeasuredHeight() == 0 ? 1f
                : Math.min(1f, (float) availableHeight / content.getMeasuredHeight());
        int x = getPaddingLeft() + Math.round((getWidth() - getPaddingLeft()
                - getPaddingRight() - content.getMeasuredWidth() * scale) / 2f);
        content.layout(x, getPaddingTop(), x + content.getMeasuredWidth(),
                getPaddingTop() + content.getMeasuredHeight());
        content.setPivotX(0);
        content.setPivotY(0);
        content.setScaleX(scale);
        content.setScaleY(scale);
    }
}
