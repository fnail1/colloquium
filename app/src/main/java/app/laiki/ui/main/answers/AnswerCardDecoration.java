package app.laiki.ui.main.answers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import static app.laiki.App.screenMetrics;


public class AnswerCardDecoration extends RecyclerView.ItemDecoration {
    private final int indentHorizontalOuter;
    private final int indentHorizontalInner;
    private final int indentTopOuter;
    private final int indentBottomOuter;
    private final int indentTopInner;

    public AnswerCardDecoration(Context context) {
        Resources resources = context.getResources();
        float density = resources.getDisplayMetrics().density;
        int indent = screenMetrics().indent;
        int shadowThicknessTop = (int) (density * 1);
        int shadowThicknessLeft = (int) (density * 2);
        int shadowThicknessBottom = (int) (density * 3);
        indentHorizontalOuter = indent - shadowThicknessLeft;
        indentHorizontalInner = indent / 2 - shadowThicknessLeft;
        indentTopOuter = indent - shadowThicknessTop;
        indentBottomOuter = indent - shadowThicknessBottom;
        indentTopInner = indent - shadowThicknessTop - shadowThicknessBottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);

        if ((position & 1) == 0) {
            outRect.left = indentHorizontalOuter;
            outRect.right = indentHorizontalInner;
            if (parent.getAdapter().getItemCount() - position <= 2)
                outRect.bottom = indentBottomOuter;
        } else {
            outRect.left = indentHorizontalInner;
            outRect.right = indentHorizontalOuter;
            if (parent.getAdapter().getItemCount() - position <= 1)
                outRect.bottom = indentBottomOuter;
        }
        if (position <= 1) {
            outRect.top = indentTopOuter;
        } else {
            outRect.top = indentTopInner;
        }
    }
}
