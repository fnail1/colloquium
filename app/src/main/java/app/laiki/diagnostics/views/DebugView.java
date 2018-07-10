package app.laiki.diagnostics.views;

import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Display;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewOverlay;
import android.view.ViewParent;
import android.view.ViewPropertyAnimator;
import android.view.ViewStructure;
import android.view.ViewTreeObserver;
import android.view.WindowId;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.animation.Animation;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;
import java.util.Collection;

import static app.laiki.diagnostics.Logger.trace;

public class DebugView extends View {
    public DebugView(Context context) {
        super(context);
    }

    public DebugView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DebugView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DebugView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int getVerticalFadingEdgeLength() {
        trace();
        return super.getVerticalFadingEdgeLength();
    }

    @Override
    public void setFadingEdgeLength(int length) {
        trace();
        super.setFadingEdgeLength(length);
    }

    @Override
    public int getHorizontalFadingEdgeLength() {
        trace();
        return super.getHorizontalFadingEdgeLength();
    }

    @Override
    public int getVerticalScrollbarWidth() {
        trace();
        return super.getVerticalScrollbarWidth();
    }

    @Override
    protected int getHorizontalScrollbarHeight() {
        trace();
        return super.getHorizontalScrollbarHeight();
    }

    @Override
    public void setVerticalScrollbarPosition(int position) {
        trace();
        super.setVerticalScrollbarPosition(position);
    }

    @Override
    public int getVerticalScrollbarPosition() {
        trace();
        return super.getVerticalScrollbarPosition();
    }

    @Override
    public void setScrollIndicators(int indicators) {
        trace();
        super.setScrollIndicators(indicators);
    }

    @Override
    public void setScrollIndicators(int indicators, int mask) {
        trace();
        super.setScrollIndicators(indicators, mask);
    }

    @Override
    public int getScrollIndicators() {
        trace();
        return super.getScrollIndicators();
    }

    @Override
    public void setOnScrollChangeListener(OnScrollChangeListener l) {
        trace();
        super.setOnScrollChangeListener(l);
    }

    @Override
    public void setOnFocusChangeListener(OnFocusChangeListener l) {
        trace();
        super.setOnFocusChangeListener(l);
    }

    @Override
    public void addOnLayoutChangeListener(OnLayoutChangeListener listener) {
        trace();
        super.addOnLayoutChangeListener(listener);
    }

    @Override
    public void removeOnLayoutChangeListener(OnLayoutChangeListener listener) {
        trace();
        super.removeOnLayoutChangeListener(listener);
    }

    @Override
    public void addOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        trace();
        super.addOnAttachStateChangeListener(listener);
    }

    @Override
    public void removeOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
        trace();
        super.removeOnAttachStateChangeListener(listener);
    }

    @Override
    public OnFocusChangeListener getOnFocusChangeListener() {
        trace();
        return super.getOnFocusChangeListener();
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        trace();
        super.setOnClickListener(l);
    }

    @Override
    public boolean hasOnClickListeners() {
        trace();
        return super.hasOnClickListeners();
    }

    @Override
    public void setOnLongClickListener(@Nullable OnLongClickListener l) {
        trace();
        super.setOnLongClickListener(l);
    }

    @Override
    public void setOnContextClickListener(@Nullable OnContextClickListener l) {
        trace();
        super.setOnContextClickListener(l);
    }

    @Override
    public void setOnCreateContextMenuListener(OnCreateContextMenuListener l) {
        trace();
        super.setOnCreateContextMenuListener(l);
    }

    @Override
    public boolean performClick() {
        trace();
        return super.performClick();
    }

    @Override
    public boolean callOnClick() {
        trace();
        return super.callOnClick();
    }

    @Override
    public boolean performLongClick() {
        trace();
        return super.performLongClick();
    }

    @Override
    public boolean performLongClick(float x, float y) {
        trace();
        return super.performLongClick(x, y);
    }

    @Override
    public boolean performContextClick(float x, float y) {
        trace();
        return super.performContextClick(x, y);
    }

    @Override
    public boolean performContextClick() {
        trace();
        return super.performContextClick();
    }

    @Override
    public boolean showContextMenu() {
        trace();
        return super.showContextMenu();
    }

    @Override
    public boolean showContextMenu(float x, float y) {
        trace();
        return super.showContextMenu(x, y);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback) {
        trace();
        return super.startActionMode(callback);
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback, int type) {
        trace();
        return super.startActionMode(callback, type);
    }

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        trace();
        super.setOnKeyListener(l);
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        trace();
        super.setOnTouchListener(l);
    }

    @Override
    public void setOnGenericMotionListener(OnGenericMotionListener l) {
        trace();
        super.setOnGenericMotionListener(l);
    }

    @Override
    public void setOnHoverListener(OnHoverListener l) {
        trace();
        super.setOnHoverListener(l);
    }

    @Override
    public void setOnDragListener(OnDragListener l) {
        trace();
        super.setOnDragListener(l);
    }

    @Override
    public boolean requestRectangleOnScreen(Rect rectangle) {
        trace();
        return super.requestRectangleOnScreen(rectangle);
    }

    @Override
    public boolean requestRectangleOnScreen(Rect rectangle, boolean immediate) {
        trace();
        return super.requestRectangleOnScreen(rectangle, immediate);
    }

    @Override
    public void clearFocus() {
        trace();
        super.clearFocus();
    }

    @Override
    public boolean hasFocus() {
        trace();
        return super.hasFocus();
    }

    @Override
    public boolean hasFocusable() {
        trace();
        return super.hasFocusable();
    }

    @Override
    public boolean hasExplicitFocusable() {
        trace();
        return super.hasExplicitFocusable();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        trace();
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public void sendAccessibilityEvent(int eventType) {
        trace();
        super.sendAccessibilityEvent(eventType);
    }

    @Override
    public void announceForAccessibility(CharSequence text) {
        trace();
        super.announceForAccessibility(text);
    }

    @Override
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        trace();
        super.sendAccessibilityEventUnchecked(event);
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        trace();
        return super.dispatchPopulateAccessibilityEvent(event);
    }

    @Override
    public void onPopulateAccessibilityEvent(AccessibilityEvent event) {
        trace();
        super.onPopulateAccessibilityEvent(event);
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        trace();
        super.onInitializeAccessibilityEvent(event);
    }

    @Override
    public AccessibilityNodeInfo createAccessibilityNodeInfo() {
        trace();
        return super.createAccessibilityNodeInfo();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        trace();
        super.onInitializeAccessibilityNodeInfo(info);
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        trace();
        return super.getAccessibilityClassName();
    }

    @Override
    public void onProvideStructure(ViewStructure structure) {
        trace();
        super.onProvideStructure(structure);
    }

    @Override
    public void onProvideAutofillStructure(ViewStructure structure, int flags) {
        trace();
        super.onProvideAutofillStructure(structure, flags);
    }

    @Override
    public void onProvideVirtualStructure(ViewStructure structure) {
        trace();
        super.onProvideVirtualStructure(structure);
    }

    @Override
    public void onProvideAutofillVirtualStructure(ViewStructure structure, int flags) {
        trace();
        super.onProvideAutofillVirtualStructure(structure, flags);
    }

    @Override
    public void autofill(AutofillValue value) {
        trace();
        super.autofill(value);
    }

    @Override
    public void autofill(@NonNull SparseArray<AutofillValue> values) {
        trace();
        super.autofill(values);
    }

    @Override
    public int getAutofillType() {
        trace();
        return super.getAutofillType();
    }

    @Nullable
    @Override
    public String[] getAutofillHints() {
        trace();
        return super.getAutofillHints();
    }

    @Nullable
    @Override
    public AutofillValue getAutofillValue() {
        trace();
        return super.getAutofillValue();
    }

    @Override
    public int getImportantForAutofill() {
        trace();
        return super.getImportantForAutofill();
    }

    @Override
    public void setImportantForAutofill(int mode) {
        trace();
        super.setImportantForAutofill(mode);
    }

    @Override
    public void dispatchProvideStructure(ViewStructure structure) {
        trace();
        super.dispatchProvideStructure(structure);
    }

    @Override
    public void dispatchProvideAutofillStructure(@NonNull ViewStructure structure, int flags) {
        trace();
        super.dispatchProvideAutofillStructure(structure, flags);
    }

    @Override
    public void addExtraDataToAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info, @NonNull String extraDataKey, @Nullable Bundle arguments) {
        trace();
        super.addExtraDataToAccessibilityNodeInfo(info, extraDataKey, arguments);
    }

    @Override
    public void setAccessibilityDelegate(@Nullable AccessibilityDelegate delegate) {
        trace();
        super.setAccessibilityDelegate(delegate);
    }

    @Override
    public AccessibilityNodeProvider getAccessibilityNodeProvider() {
        trace();
        return super.getAccessibilityNodeProvider();
    }

    @SuppressLint("GetContentDescriptionOverride")
    @Override
    public CharSequence getContentDescription() {
        trace();
        return super.getContentDescription();
    }

    @Override
    public void setContentDescription(CharSequence contentDescription) {
        trace();
        super.setContentDescription(contentDescription);
    }

    @Override
    public void setAccessibilityTraversalBefore(int beforeId) {
        trace();
        super.setAccessibilityTraversalBefore(beforeId);
    }

    @Override
    public int getAccessibilityTraversalBefore() {
        trace();
        return super.getAccessibilityTraversalBefore();
    }

    @Override
    public void setAccessibilityTraversalAfter(int afterId) {
        trace();
        super.setAccessibilityTraversalAfter(afterId);
    }

    @Override
    public int getAccessibilityTraversalAfter() {
        trace();
        return super.getAccessibilityTraversalAfter();
    }

    @Override
    public int getLabelFor() {
        trace();
        return super.getLabelFor();
    }

    @Override
    public void setLabelFor(int id) {
        trace();
        super.setLabelFor(id);
    }

    @Override
    public boolean isFocused() {
        trace();
        return super.isFocused();
    }

    @Override
    public View findFocus() {
        trace();
        return super.findFocus();
    }

    @Override
    public boolean isScrollContainer() {
        trace();
        return super.isScrollContainer();
    }

    @Override
    public void setScrollContainer(boolean isScrollContainer) {
        trace();
        super.setScrollContainer(isScrollContainer);
    }

    @Override
    public int getDrawingCacheQuality() {
        trace();
        return super.getDrawingCacheQuality();
    }

    @Override
    public void setDrawingCacheQuality(int quality) {
        trace();
        super.setDrawingCacheQuality(quality);
    }

    @Override
    public boolean getKeepScreenOn() {
        trace();
        return super.getKeepScreenOn();
    }

    @Override
    public void setKeepScreenOn(boolean keepScreenOn) {
        trace();
        super.setKeepScreenOn(keepScreenOn);
    }

    @Override
    public int getNextFocusLeftId() {
        trace();
        return super.getNextFocusLeftId();
    }

    @Override
    public void setNextFocusLeftId(int nextFocusLeftId) {
        trace();
        super.setNextFocusLeftId(nextFocusLeftId);
    }

    @Override
    public int getNextFocusRightId() {
        trace();
        return super.getNextFocusRightId();
    }

    @Override
    public void setNextFocusRightId(int nextFocusRightId) {
        trace();
        super.setNextFocusRightId(nextFocusRightId);
    }

    @Override
    public int getNextFocusUpId() {
        trace();
        return super.getNextFocusUpId();
    }

    @Override
    public void setNextFocusUpId(int nextFocusUpId) {
        trace();
        super.setNextFocusUpId(nextFocusUpId);
    }

    @Override
    public int getNextFocusDownId() {
        trace();
        return super.getNextFocusDownId();
    }

    @Override
    public void setNextFocusDownId(int nextFocusDownId) {
        trace();
        super.setNextFocusDownId(nextFocusDownId);
    }

    @Override
    public int getNextFocusForwardId() {
        trace();
        return super.getNextFocusForwardId();
    }

    @Override
    public void setNextFocusForwardId(int nextFocusForwardId) {
        trace();
        super.setNextFocusForwardId(nextFocusForwardId);
    }

    @Override
    public int getNextClusterForwardId() {
        trace();
        return super.getNextClusterForwardId();
    }

    @Override
    public void setNextClusterForwardId(int nextClusterForwardId) {
        trace();
        super.setNextClusterForwardId(nextClusterForwardId);
    }

    @Override
    public boolean isShown() {
        trace();
        return super.isShown();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    protected boolean fitSystemWindows(Rect insets) {
        trace();
        return super.fitSystemWindows(insets);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        trace();
        return super.onApplyWindowInsets(insets);
    }

    @Override
    public void setOnApplyWindowInsetsListener(OnApplyWindowInsetsListener listener) {
        trace();
        super.setOnApplyWindowInsetsListener(listener);
    }

    @Override
    public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
        trace();
        return super.dispatchApplyWindowInsets(insets);
    }

    @Override
    public WindowInsets getRootWindowInsets() {
        trace();
        return super.getRootWindowInsets();
    }

    @Override
    public WindowInsets computeSystemWindowInsets(WindowInsets in, Rect outLocalInsets) {
        trace();
        return super.computeSystemWindowInsets(in, outLocalInsets);
    }

    @Override
    public void setFitsSystemWindows(boolean fitSystemWindows) {
        trace();
        super.setFitsSystemWindows(fitSystemWindows);
    }

    @Override
    public boolean getFitsSystemWindows() {
        trace();
        return super.getFitsSystemWindows();
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void requestFitSystemWindows() {
        trace();
        super.requestFitSystemWindows();
    }

    @Override
    public void requestApplyInsets() {
        trace();
        super.requestApplyInsets();
    }

    @Override
    public int getVisibility() {
        trace();
        return super.getVisibility();
    }

    @Override
    public void setVisibility(int visibility) {
        trace();
        super.setVisibility(visibility);
    }

    @Override
    public boolean isEnabled() {
        trace();
        return super.isEnabled();
    }

    @Override
    public void setEnabled(boolean enabled) {
        trace();
        super.setEnabled(enabled);
    }

    @Override
    public void setFocusable(boolean focusable) {
        trace();
        super.setFocusable(focusable);
    }

    @Override
    public void setFocusable(int focusable) {
        trace();
        super.setFocusable(focusable);
    }

    @Override
    public void setFocusableInTouchMode(boolean focusableInTouchMode) {
        trace();
        super.setFocusableInTouchMode(focusableInTouchMode);
    }

    @Override
    public void setAutofillHints(@Nullable String... autofillHints) {
        trace();
        super.setAutofillHints(autofillHints);
    }

    @Override
    public void setSoundEffectsEnabled(boolean soundEffectsEnabled) {
        trace();
        super.setSoundEffectsEnabled(soundEffectsEnabled);
    }

    @Override
    public boolean isSoundEffectsEnabled() {
        trace();
        return super.isSoundEffectsEnabled();
    }

    @Override
    public void setHapticFeedbackEnabled(boolean hapticFeedbackEnabled) {
        trace();
        super.setHapticFeedbackEnabled(hapticFeedbackEnabled);
    }

    @Override
    public boolean isHapticFeedbackEnabled() {
        trace();
        return super.isHapticFeedbackEnabled();
    }

    @Override
    public void setLayoutDirection(int layoutDirection) {
        trace();
        super.setLayoutDirection(layoutDirection);
    }

    @Override
    public int getLayoutDirection() {
        trace();
        return super.getLayoutDirection();
    }

    @Override
    public boolean hasTransientState() {
        trace();
        return super.hasTransientState();
    }

    @Override
    public void setHasTransientState(boolean hasTransientState) {
        trace();
        super.setHasTransientState(hasTransientState);
    }

    @Override
    public boolean isAttachedToWindow() {
        trace();
        return super.isAttachedToWindow();
    }

    @Override
    public boolean isLaidOut() {
        trace();
        return super.isLaidOut();
    }

    @Override
    public void setWillNotDraw(boolean willNotDraw) {
        trace();
        super.setWillNotDraw(willNotDraw);
    }

    @Override
    public boolean willNotDraw() {
        trace();
        return super.willNotDraw();
    }

    @Override
    public void setWillNotCacheDrawing(boolean willNotCacheDrawing) {
        trace();
        super.setWillNotCacheDrawing(willNotCacheDrawing);
    }

    @Override
    public boolean willNotCacheDrawing() {
        trace();
        return super.willNotCacheDrawing();
    }

    @Override
    public boolean isClickable() {
        trace();
        return super.isClickable();
    }

    @Override
    public void setClickable(boolean clickable) {
        trace();
        super.setClickable(clickable);
    }

    @Override
    public boolean isLongClickable() {
        trace();
        return super.isLongClickable();
    }

    @Override
    public void setLongClickable(boolean longClickable) {
        trace();
        super.setLongClickable(longClickable);
    }

    @Override
    public boolean isContextClickable() {
        trace();
        return super.isContextClickable();
    }

    @Override
    public void setContextClickable(boolean contextClickable) {
        trace();
        super.setContextClickable(contextClickable);
    }

    @Override
    public void setPressed(boolean pressed) {
        trace();
        super.setPressed(pressed);
    }

    @Override
    protected void dispatchSetPressed(boolean pressed) {
        trace();
        super.dispatchSetPressed(pressed);
    }

    @Override
    public boolean isPressed() {
        trace();
        return super.isPressed();
    }

    @Override
    public boolean isSaveEnabled() {
        trace();
        return super.isSaveEnabled();
    }

    @Override
    public void setSaveEnabled(boolean enabled) {
        trace();
        super.setSaveEnabled(enabled);
    }

    @Override
    public boolean getFilterTouchesWhenObscured() {
        trace();
        return super.getFilterTouchesWhenObscured();
    }

    @Override
    public void setFilterTouchesWhenObscured(boolean enabled) {
        trace();
        super.setFilterTouchesWhenObscured(enabled);
    }

    @Override
    public boolean isSaveFromParentEnabled() {
        trace();
        return super.isSaveFromParentEnabled();
    }

    @Override
    public void setSaveFromParentEnabled(boolean enabled) {
        trace();
        super.setSaveFromParentEnabled(enabled);
    }

    @Override
    public int getFocusable() {
        trace();
        return super.getFocusable();
    }

    @Override
    public View focusSearch(int direction) {
        trace();
        return super.focusSearch(direction);
    }

    @Override
    public void setKeyboardNavigationCluster(boolean isCluster) {
        trace();
        super.setKeyboardNavigationCluster(isCluster);
    }

    @Override
    public void setFocusedByDefault(boolean isFocusedByDefault) {
        trace();
        super.setFocusedByDefault(isFocusedByDefault);
    }

    @Override
    public View keyboardNavigationClusterSearch(View currentCluster, int direction) {
        trace();
        return super.keyboardNavigationClusterSearch(currentCluster, direction);
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        trace();
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void setDefaultFocusHighlightEnabled(boolean defaultFocusHighlightEnabled) {
        trace();
        super.setDefaultFocusHighlightEnabled(defaultFocusHighlightEnabled);
    }

    @Override
    public ArrayList<View> getFocusables(int direction) {
        trace();
        return super.getFocusables(direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction) {
        trace();
        super.addFocusables(views, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
        trace();
        super.addFocusables(views, direction, focusableMode);
    }

    @Override
    public void addKeyboardNavigationClusters(@NonNull Collection<View> views, int direction) {
        trace();
        super.addKeyboardNavigationClusters(views, direction);
    }

    @Override
    public void findViewsWithText(ArrayList<View> outViews, CharSequence searched, int flags) {
        trace();
        super.findViewsWithText(outViews, searched, flags);
    }

    @Override
    public ArrayList<View> getTouchables() {
        trace();
        return super.getTouchables();
    }

    @Override
    public void addTouchables(ArrayList<View> views) {
        trace();
        super.addTouchables(views);
    }

    @Override
    public boolean isAccessibilityFocused() {
        trace();
        return super.isAccessibilityFocused();
    }

    @Override
    public boolean restoreDefaultFocus() {
        trace();
        return super.restoreDefaultFocus();
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        trace();
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    public int getImportantForAccessibility() {
        trace();
        return super.getImportantForAccessibility();
    }

    @Override
    public void setAccessibilityLiveRegion(int mode) {
        trace();
        super.setAccessibilityLiveRegion(mode);
    }

    @Override
    public int getAccessibilityLiveRegion() {
        trace();
        return super.getAccessibilityLiveRegion();
    }

    @Override
    public void setImportantForAccessibility(int mode) {
        trace();
        super.setImportantForAccessibility(mode);
    }

    @Override
    public boolean isImportantForAccessibility() {
        trace();
        return super.isImportantForAccessibility();
    }

    @Override
    public ViewParent getParentForAccessibility() {
        trace();
        return super.getParentForAccessibility();
    }

    @Override
    public void addChildrenForAccessibility(ArrayList<View> outChildren) {
        trace();
        super.addChildrenForAccessibility(outChildren);
    }

    @Override
    public boolean dispatchNestedPrePerformAccessibilityAction(int action, Bundle arguments) {
        trace();
        return super.dispatchNestedPrePerformAccessibilityAction(action, arguments);
    }

    @Override
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        trace();
        return super.performAccessibilityAction(action, arguments);
    }

    @Override
    public void dispatchStartTemporaryDetach() {
        trace();
        super.dispatchStartTemporaryDetach();
    }

    @Override
    public void onStartTemporaryDetach() {
        trace();
        super.onStartTemporaryDetach();
    }

    @Override
    public void dispatchFinishTemporaryDetach() {
        trace();
        super.dispatchFinishTemporaryDetach();
    }

    @Override
    public void onFinishTemporaryDetach() {
        trace();
        super.onFinishTemporaryDetach();
    }

    @Override
    public KeyEvent.DispatcherState getKeyDispatcherState() {
        trace();
        return super.getKeyDispatcherState();
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        trace();
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        trace();
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        trace();
        return super.dispatchKeyShortcutEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        trace();
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        trace();
        return super.onFilterTouchEventForSecurity(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        trace();
        return super.dispatchTrackballEvent(event);
    }

    @Override
    public boolean dispatchCapturedPointerEvent(MotionEvent event) {
        trace();
        return super.dispatchCapturedPointerEvent(event);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        trace();
        return super.dispatchGenericMotionEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        trace();
        return super.dispatchHoverEvent(event);
    }

    @Override
    protected boolean dispatchGenericPointerEvent(MotionEvent event) {
        trace();
        return super.dispatchGenericPointerEvent(event);
    }

    @Override
    protected boolean dispatchGenericFocusedEvent(MotionEvent event) {
        trace();
        return super.dispatchGenericFocusedEvent(event);
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        trace();
        super.dispatchWindowFocusChanged(hasFocus);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        trace();
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    public boolean hasWindowFocus() {
        trace();
        return super.hasWindowFocus();
    }

    @Override
    protected void dispatchVisibilityChanged(@NonNull View changedView, int visibility) {
        trace();
        super.dispatchVisibilityChanged(changedView, visibility);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        trace();
        super.onVisibilityChanged(changedView, visibility);
    }

    @Override
    public void dispatchDisplayHint(int hint) {
        trace();
        super.dispatchDisplayHint(hint);
    }

    @Override
    protected void onDisplayHint(int hint) {
        trace();
        super.onDisplayHint(hint);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility) {
        trace();
        super.dispatchWindowVisibilityChanged(visibility);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        trace();
        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        trace();
        super.onVisibilityAggregated(isVisible);
    }

    @Override
    public int getWindowVisibility() {
        trace();
        return super.getWindowVisibility();
    }

    @Override
    public void getWindowVisibleDisplayFrame(Rect outRect) {
        trace();
        super.getWindowVisibleDisplayFrame(outRect);
    }

    @Override
    public void dispatchConfigurationChanged(Configuration newConfig) {
        trace();
        super.dispatchConfigurationChanged(newConfig);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        trace();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean isInTouchMode() {
        trace();
        return super.isInTouchMode();
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        trace();
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        trace();
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        trace();
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        trace();
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        trace();
        return super.onKeyMultiple(keyCode, repeatCount, event);
    }

    @Override
    public boolean onKeyShortcut(int keyCode, KeyEvent event) {
        trace();
        return super.onKeyShortcut(keyCode, event);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        trace();
        return super.onCheckIsTextEditor();
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        trace();
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    public boolean checkInputConnectionProxy(View view) {
        trace();
        return super.checkInputConnectionProxy(view);
    }

    @Override
    public void createContextMenu(ContextMenu menu) {
        trace();
        super.createContextMenu(menu);
    }

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        trace();
        return super.getContextMenuInfo();
    }

    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        trace();
        super.onCreateContextMenu(menu);
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        trace();
        return super.onTrackballEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        trace();
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        trace();
        return super.onHoverEvent(event);
    }

    @Override
    public boolean isHovered() {
        trace();
        return super.isHovered();
    }

    @Override
    public void setHovered(boolean hovered) {
        trace();
        super.setHovered(hovered);
    }

    @Override
    public void onHoverChanged(boolean hovered) {
        trace();
        super.onHoverChanged(hovered);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        trace();
        return super.onTouchEvent(event);
    }

    @Override
    public void cancelLongPress() {
        trace();
        super.cancelLongPress();
    }

    @Override
    public void setTouchDelegate(TouchDelegate delegate) {
        trace();
        super.setTouchDelegate(delegate);
    }

    @Override
    public TouchDelegate getTouchDelegate() {
        trace();
        return super.getTouchDelegate();
    }

    @Override
    public void bringToFront() {
        trace();
        super.bringToFront();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        trace();
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        trace();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        trace();
        super.dispatchDraw(canvas);
    }

    @Override
    public void setScrollX(int value) {
        trace();
        super.setScrollX(value);
    }

    @Override
    public void setScrollY(int value) {
        trace();
        super.setScrollY(value);
    }

    @Override
    public void getDrawingRect(Rect outRect) {
        trace();
        super.getDrawingRect(outRect);
    }

    @Override
    public Matrix getMatrix() {
        trace();
        return super.getMatrix();
    }

    @Override
    public float getCameraDistance() {
        trace();
        return super.getCameraDistance();
    }

    @Override
    public void setCameraDistance(float distance) {
        trace();
        super.setCameraDistance(distance);
    }

    @Override
    public float getRotation() {
        trace();
        return super.getRotation();
    }

    @Override
    public void setRotation(float rotation) {
        trace();
        super.setRotation(rotation);
    }

    @Override
    public float getRotationY() {
        trace();
        return super.getRotationY();
    }

    @Override
    public void setRotationY(float rotationY) {
        trace();
        super.setRotationY(rotationY);
    }

    @Override
    public float getRotationX() {
        trace();
        return super.getRotationX();
    }

    @Override
    public void setRotationX(float rotationX) {
        trace();
        super.setRotationX(rotationX);
    }

    @Override
    public float getScaleX() {
        trace();
        return super.getScaleX();
    }

    @Override
    public void setScaleX(float scaleX) {
        trace();
        super.setScaleX(scaleX);
    }

    @Override
    public float getScaleY() {
        trace();
        return super.getScaleY();
    }

    @Override
    public void setScaleY(float scaleY) {
        trace();
        super.setScaleY(scaleY);
    }

    @Override
    public float getPivotX() {
        trace();
        return super.getPivotX();
    }

    @Override
    public void setPivotX(float pivotX) {
        trace();
        super.setPivotX(pivotX);
    }

    @Override
    public float getPivotY() {
        trace();
        return super.getPivotY();
    }

    @Override
    public void setPivotY(float pivotY) {
        trace();
        super.setPivotY(pivotY);
    }

    @Override
    public float getAlpha() {
        trace();
        return super.getAlpha();
    }

    @Override
    public void forceHasOverlappingRendering(boolean hasOverlappingRendering) {
        trace();
        super.forceHasOverlappingRendering(hasOverlappingRendering);
    }

    @Override
    public boolean hasOverlappingRendering() {
        trace();
        return super.hasOverlappingRendering();
    }

    @Override
    public void setAlpha(float alpha) {
        trace();
        super.setAlpha(alpha);
    }

    @Override
    public boolean isDirty() {
        trace();
        return super.isDirty();
    }

    @Override
    public float getX() {
        trace();
        return super.getX();
    }

    @Override
    public void setX(float x) {
        trace();
        super.setX(x);
    }

    @Override
    public float getY() {
        trace();
        return super.getY();
    }

    @Override
    public void setY(float y) {
        trace();
        super.setY(y);
    }

    @Override
    public float getZ() {
        trace();
        return super.getZ();
    }

    @Override
    public void setZ(float z) {
        trace();
        super.setZ(z);
    }

    @Override
    public float getElevation() {
        trace();
        return super.getElevation();
    }

    @Override
    public void setElevation(float elevation) {
        trace();
        super.setElevation(elevation);
    }

    @Override
    public float getTranslationX() {
        trace();
        return super.getTranslationX();
    }

    @Override
    public void setTranslationX(float translationX) {
        trace();
        super.setTranslationX(translationX);
    }

    @Override
    public float getTranslationY() {
        trace();
        return super.getTranslationY();
    }

    @Override
    public void setTranslationY(float translationY) {
        trace();
        super.setTranslationY(translationY);
    }

    @Override
    public float getTranslationZ() {
        trace();
        return super.getTranslationZ();
    }

    @Override
    public void setTranslationZ(float translationZ) {
        trace();
        super.setTranslationZ(translationZ);
    }

    @Override
    public StateListAnimator getStateListAnimator() {
        trace();
        return super.getStateListAnimator();
    }

    @Override
    public void setStateListAnimator(StateListAnimator stateListAnimator) {
        trace();
        super.setStateListAnimator(stateListAnimator);
    }

    @Override
    public void setClipToOutline(boolean clipToOutline) {
        trace();
        super.setClipToOutline(clipToOutline);
    }

    @Override
    public void setOutlineProvider(ViewOutlineProvider provider) {
        trace();
        super.setOutlineProvider(provider);
    }

    @Override
    public ViewOutlineProvider getOutlineProvider() {
        trace();
        return super.getOutlineProvider();
    }

    @Override
    public void invalidateOutline() {
        trace();
        super.invalidateOutline();
    }

    @Override
    public void getHitRect(Rect outRect) {
        trace();
        super.getHitRect(outRect);
    }

    @Override
    public void getFocusedRect(Rect r) {
        trace();
        super.getFocusedRect(r);
    }

    @Override
    public boolean getGlobalVisibleRect(Rect r, Point globalOffset) {
        trace();
        return super.getGlobalVisibleRect(r, globalOffset);
    }

    @Override
    public void offsetTopAndBottom(int offset) {
        trace();
        super.offsetTopAndBottom(offset);
    }

    @Override
    public void offsetLeftAndRight(int offset) {
        trace();
        super.offsetLeftAndRight(offset);
    }

    @Override
    public ViewGroup.LayoutParams getLayoutParams() {
        trace();
        return super.getLayoutParams();
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        trace();
        super.setLayoutParams(params);
    }

    @Override
    public void scrollTo(int x, int y) {
        trace();
        super.scrollTo(x, y);
    }

    @Override
    public void scrollBy(int x, int y) {
        trace();
        super.scrollBy(x, y);
    }

    @Override
    protected boolean awakenScrollBars() {
        trace();
        return super.awakenScrollBars();
    }

    @Override
    protected boolean awakenScrollBars(int startDelay) {
        trace();
        return super.awakenScrollBars(startDelay);
    }

    @Override
    protected boolean awakenScrollBars(int startDelay, boolean invalidate) {
        trace();
        return super.awakenScrollBars(startDelay, invalidate);
    }

    @Override
    public void invalidate(Rect dirty) {
        trace();
        super.invalidate(dirty);
    }

    @Override
    public void invalidate(int l, int t, int r, int b) {
        trace();
        super.invalidate(l, t, r, b);
    }

    @Override
    public void invalidate() {
        trace();
        super.invalidate();
    }

    @Override
    public boolean isOpaque() {
        trace();
        return super.isOpaque();
    }

    @Override
    public Handler getHandler() {
        trace();
        return super.getHandler();
    }

    @Override
    public boolean post(Runnable action) {
        trace();
        return super.post(action);
    }

    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        trace();
        return super.postDelayed(action, delayMillis);
    }

    @Override
    public void postOnAnimation(Runnable action) {
        trace();
        super.postOnAnimation(action);
    }

    @Override
    public void postOnAnimationDelayed(Runnable action, long delayMillis) {
        trace();
        super.postOnAnimationDelayed(action, delayMillis);
    }

    @Override
    public boolean removeCallbacks(Runnable action) {
        trace();
        return super.removeCallbacks(action);
    }

    @Override
    public void postInvalidate() {
        trace();
        super.postInvalidate();
    }

    @Override
    public void postInvalidate(int left, int top, int right, int bottom) {
        trace();
        super.postInvalidate(left, top, right, bottom);
    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds) {
        trace();
        super.postInvalidateDelayed(delayMilliseconds);
    }

    @Override
    public void postInvalidateDelayed(long delayMilliseconds, int left, int top, int right, int bottom) {
        trace();
        super.postInvalidateDelayed(delayMilliseconds, left, top, right, bottom);
    }

    @Override
    public void postInvalidateOnAnimation() {
        trace();
        super.postInvalidateOnAnimation();
    }

    @Override
    public void postInvalidateOnAnimation(int left, int top, int right, int bottom) {
        trace();
        super.postInvalidateOnAnimation(left, top, right, bottom);
    }

    @Override
    public void computeScroll() {
        trace();
        super.computeScroll();
    }

    @Override
    public boolean isHorizontalFadingEdgeEnabled() {
        trace();
        return super.isHorizontalFadingEdgeEnabled();
    }

    @Override
    public void setHorizontalFadingEdgeEnabled(boolean horizontalFadingEdgeEnabled) {
        trace();
        super.setHorizontalFadingEdgeEnabled(horizontalFadingEdgeEnabled);
    }

    @Override
    public boolean isVerticalFadingEdgeEnabled() {
        trace();
        return super.isVerticalFadingEdgeEnabled();
    }

    @Override
    public void setVerticalFadingEdgeEnabled(boolean verticalFadingEdgeEnabled) {
        trace();
        super.setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled);
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        trace();
        return super.getTopFadingEdgeStrength();
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        trace();
        return super.getBottomFadingEdgeStrength();
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        trace();
        return super.getLeftFadingEdgeStrength();
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        trace();
        return super.getRightFadingEdgeStrength();
    }

    @Override
    public boolean isHorizontalScrollBarEnabled() {
        trace();
        return super.isHorizontalScrollBarEnabled();
    }

    @Override
    public void setHorizontalScrollBarEnabled(boolean horizontalScrollBarEnabled) {
        trace();
        super.setHorizontalScrollBarEnabled(horizontalScrollBarEnabled);
    }

    @Override
    public boolean isVerticalScrollBarEnabled() {
        trace();
        return super.isVerticalScrollBarEnabled();
    }

    @Override
    public void setVerticalScrollBarEnabled(boolean verticalScrollBarEnabled) {
        trace();
        super.setVerticalScrollBarEnabled(verticalScrollBarEnabled);
    }

    @Override
    public void setScrollbarFadingEnabled(boolean fadeScrollbars) {
        trace();
        super.setScrollbarFadingEnabled(fadeScrollbars);
    }

    @Override
    public boolean isScrollbarFadingEnabled() {
        trace();
        return super.isScrollbarFadingEnabled();
    }

    @Override
    public int getScrollBarDefaultDelayBeforeFade() {
        trace();
        return super.getScrollBarDefaultDelayBeforeFade();
    }

    @Override
    public void setScrollBarDefaultDelayBeforeFade(int scrollBarDefaultDelayBeforeFade) {
        trace();
        super.setScrollBarDefaultDelayBeforeFade(scrollBarDefaultDelayBeforeFade);
    }

    @Override
    public int getScrollBarFadeDuration() {
        trace();
        return super.getScrollBarFadeDuration();
    }

    @Override
    public void setScrollBarFadeDuration(int scrollBarFadeDuration) {
        trace();
        super.setScrollBarFadeDuration(scrollBarFadeDuration);
    }

    @Override
    public int getScrollBarSize() {
        trace();
        return super.getScrollBarSize();
    }

    @Override
    public void setScrollBarSize(int scrollBarSize) {
        trace();
        super.setScrollBarSize(scrollBarSize);
    }

    @Override
    public void setScrollBarStyle(int style) {
        trace();
        super.setScrollBarStyle(style);
    }

    @Override
    public int getScrollBarStyle() {
        trace();
        return super.getScrollBarStyle();
    }

    @Override
    protected int computeHorizontalScrollRange() {
        trace();
        return super.computeHorizontalScrollRange();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        trace();
        return super.computeHorizontalScrollOffset();
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        trace();
        return super.computeHorizontalScrollExtent();
    }

    @Override
    protected int computeVerticalScrollRange() {
        trace();
        return super.computeVerticalScrollRange();
    }

    @Override
    protected int computeVerticalScrollOffset() {
        trace();
        return super.computeVerticalScrollOffset();
    }

    @Override
    protected int computeVerticalScrollExtent() {
        trace();
        return super.computeVerticalScrollExtent();
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        trace();
        return super.canScrollHorizontally(direction);
    }

    @Override
    public boolean canScrollVertically(int direction) {
        trace();
        return super.canScrollVertically(direction);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        trace();
        super.onDraw(canvas);
    }

    @Override
    protected void onAttachedToWindow() {
        trace();
        super.onAttachedToWindow();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        trace();
        super.onScreenStateChanged(screenState);
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
        trace();
        super.onRtlPropertiesChanged(layoutDirection);
    }

    @Override
    public boolean canResolveLayoutDirection() {
        trace();
        return super.canResolveLayoutDirection();
    }

    @Override
    public boolean isLayoutDirectionResolved() {
        trace();
        return super.isLayoutDirectionResolved();
    }

    @Override
    protected void onDetachedFromWindow() {
        trace();
        super.onDetachedFromWindow();
    }

    @Override
    protected int getWindowAttachCount() {
        trace();
        return super.getWindowAttachCount();
    }

    @Override
    public IBinder getWindowToken() {
        trace();
        return super.getWindowToken();
    }

    @Override
    public WindowId getWindowId() {
        trace();
        return super.getWindowId();
    }

    @Override
    public IBinder getApplicationWindowToken() {
        trace();
        return super.getApplicationWindowToken();
    }

    @Override
    public Display getDisplay() {
        trace();
        return super.getDisplay();
    }

    @Override
    public void onCancelPendingInputEvents() {
        trace();
        super.onCancelPendingInputEvents();
    }

    @Override
    public void saveHierarchyState(SparseArray<Parcelable> container) {
        trace();
        super.saveHierarchyState(container);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        trace();
        super.dispatchSaveInstanceState(container);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        trace();
        return super.onSaveInstanceState();
    }

    @Override
    public void restoreHierarchyState(SparseArray<Parcelable> container) {
        trace();
        super.restoreHierarchyState(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        trace();
        super.dispatchRestoreInstanceState(container);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        trace();
        super.onRestoreInstanceState(state);
    }

    @Override
    public long getDrawingTime() {
        trace();
        return super.getDrawingTime();
    }

    @Override
    public void setDuplicateParentStateEnabled(boolean enabled) {
        trace();
        super.setDuplicateParentStateEnabled(enabled);
    }

    @Override
    public boolean isDuplicateParentStateEnabled() {
        trace();
        return super.isDuplicateParentStateEnabled();
    }

    @Override
    public void setLayerType(int layerType, @Nullable Paint paint) {
        trace();
        super.setLayerType(layerType, paint);
    }

    @Override
    public void setLayerPaint(@Nullable Paint paint) {
        trace();
        super.setLayerPaint(paint);
    }

    @Override
    public int getLayerType() {
        trace();
        return super.getLayerType();
    }

    @Override
    public void buildLayer() {
        trace();
        super.buildLayer();
    }

    @Override
    public void setDrawingCacheEnabled(boolean enabled) {
        trace();
        super.setDrawingCacheEnabled(enabled);
    }

    @Override
    public boolean isDrawingCacheEnabled() {
        trace();
        return super.isDrawingCacheEnabled();
    }

    @Override
    public Bitmap getDrawingCache() {
        trace();
        return super.getDrawingCache();
    }

    @Override
    public Bitmap getDrawingCache(boolean autoScale) {
        trace();
        return super.getDrawingCache(autoScale);
    }

    @Override
    public void destroyDrawingCache() {
        trace();
        super.destroyDrawingCache();
    }

    @Override
    public void setDrawingCacheBackgroundColor(int color) {
        trace();
        super.setDrawingCacheBackgroundColor(color);
    }

    @Override
    public int getDrawingCacheBackgroundColor() {
        trace();
        return super.getDrawingCacheBackgroundColor();
    }

    @Override
    public void buildDrawingCache() {
        trace();
        super.buildDrawingCache();
    }

    @Override
    public void buildDrawingCache(boolean autoScale) {
        trace();
        super.buildDrawingCache(autoScale);
    }

    @Override
    public boolean isInEditMode() {
        trace();
        return super.isInEditMode();
    }

    @Override
    protected boolean isPaddingOffsetRequired() {
        trace();
        return super.isPaddingOffsetRequired();
    }

    @Override
    protected int getLeftPaddingOffset() {
        trace();
        return super.getLeftPaddingOffset();
    }

    @Override
    protected int getRightPaddingOffset() {
        trace();
        return super.getRightPaddingOffset();
    }

    @Override
    protected int getTopPaddingOffset() {
        trace();
        return super.getTopPaddingOffset();
    }

    @Override
    protected int getBottomPaddingOffset() {
        trace();
        return super.getBottomPaddingOffset();
    }

    @Override
    public boolean isHardwareAccelerated() {
        trace();
        return super.isHardwareAccelerated();
    }

    @Override
    public void setClipBounds(Rect clipBounds) {
        trace();
        super.setClipBounds(clipBounds);
    }

    @Override
    public Rect getClipBounds() {
        trace();
        return super.getClipBounds();
    }

    @Override
    public boolean getClipBounds(Rect outRect) {
        trace();
        return super.getClipBounds(outRect);
    }

    @Override
    public void draw(Canvas canvas) {
        trace();
        super.draw(canvas);
    }

    @Override
    public ViewOverlay getOverlay() {
        trace();
        return super.getOverlay();
    }

    @Override
    public int getSolidColor() {
        trace();
        return super.getSolidColor();
    }

    @Override
    public boolean isLayoutRequested() {
        trace();
        return super.isLayoutRequested();
    }

    @Override
    public void layout(int l, int t, int r, int b) {
        trace();
        super.layout(l, t, r, b);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        trace();
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onFinishInflate() {
        trace();
        super.onFinishInflate();
    }

    @Override
    public Resources getResources() {
        trace();
        return super.getResources();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable drawable) {
        trace();
        super.invalidateDrawable(drawable);
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        trace();
        super.scheduleDrawable(who, what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        trace();
        super.unscheduleDrawable(who, what);
    }

    @Override
    public void unscheduleDrawable(Drawable who) {
        trace();
        super.unscheduleDrawable(who);
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        trace();
        return super.verifyDrawable(who);
    }

    @Override
    protected void drawableStateChanged() {
        trace();
        super.drawableStateChanged();
    }

    @Override
    public void drawableHotspotChanged(float x, float y) {
        trace();
        super.drawableHotspotChanged(x, y);
    }

    @Override
    public void dispatchDrawableHotspotChanged(float x, float y) {
        trace();
        super.dispatchDrawableHotspotChanged(x, y);
    }

    @Override
    public void refreshDrawableState() {
        trace();
        super.refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        trace();
        return super.onCreateDrawableState(extraSpace);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        trace();
        super.jumpDrawablesToCurrentState();
    }

    @Override
    public void setBackgroundColor(int color) {
        trace();
        super.setBackgroundColor(color);
    }

    @Override
    public void setBackgroundResource(int resid) {
        trace();
        super.setBackgroundResource(resid);
    }

    @Override
    public void setBackground(Drawable background) {
        trace();
        super.setBackground(background);
    }

    @SuppressWarnings("deprecation")
    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        trace();
        super.setBackgroundDrawable(background);
    }

    @Override
    public Drawable getBackground() {
        trace();
        return super.getBackground();
    }

    @Override
    public void setBackgroundTintList(@Nullable ColorStateList tint) {
        trace();
        super.setBackgroundTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getBackgroundTintList() {
        trace();
        return super.getBackgroundTintList();
    }

    @Override
    public void setBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        trace();
        super.setBackgroundTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getBackgroundTintMode() {
        trace();
        return super.getBackgroundTintMode();
    }

    @Override
    public Drawable getForeground() {
        trace();
        return super.getForeground();
    }

    @Override
    public void setForeground(Drawable foreground) {
        trace();
        super.setForeground(foreground);
    }

    @Override
    public int getForegroundGravity() {
        trace();
        return super.getForegroundGravity();
    }

    @Override
    public void setForegroundGravity(int gravity) {
        trace();
        super.setForegroundGravity(gravity);
    }

    @Override
    public void setForegroundTintList(@Nullable ColorStateList tint) {
        trace();
        super.setForegroundTintList(tint);
    }

    @Nullable
    @Override
    public ColorStateList getForegroundTintList() {
        trace();
        return super.getForegroundTintList();
    }

    @Override
    public void setForegroundTintMode(@Nullable PorterDuff.Mode tintMode) {
        trace();
        super.setForegroundTintMode(tintMode);
    }

    @Nullable
    @Override
    public PorterDuff.Mode getForegroundTintMode() {
        trace();
        return super.getForegroundTintMode();
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        trace();
        super.onDrawForeground(canvas);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        trace();
        super.setPadding(left, top, right, bottom);
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom) {
        trace();
        super.setPaddingRelative(start, top, end, bottom);
    }

    @Override
    public int getPaddingTop() {
        trace();
        return super.getPaddingTop();
    }

    @Override
    public int getPaddingBottom() {
        trace();
        return super.getPaddingBottom();
    }

    @Override
    public int getPaddingLeft() {
        trace();
        return super.getPaddingLeft();
    }

    @Override
    public int getPaddingStart() {
        trace();
        return super.getPaddingStart();
    }

    @Override
    public int getPaddingRight() {
        trace();
        return super.getPaddingRight();
    }

    @Override
    public int getPaddingEnd() {
        trace();
        return super.getPaddingEnd();
    }

    @Override
    public boolean isPaddingRelative() {
        trace();
        return super.isPaddingRelative();
    }

    @Override
    public void setSelected(boolean selected) {
        trace();
        super.setSelected(selected);
    }

    @Override
    protected void dispatchSetSelected(boolean selected) {
        trace();
        super.dispatchSetSelected(selected);
    }

    @Override
    public boolean isSelected() {
        trace();
        return super.isSelected();
    }

    @Override
    public void setActivated(boolean activated) {
        trace();
        super.setActivated(activated);
    }

    @Override
    protected void dispatchSetActivated(boolean activated) {
        trace();
        super.dispatchSetActivated(activated);
    }

    @Override
    public boolean isActivated() {
        trace();
        return super.isActivated();
    }

    @Override
    public ViewTreeObserver getViewTreeObserver() {
        trace();
        return super.getViewTreeObserver();
    }

    @Override
    public View getRootView() {
        trace();
        return super.getRootView();
    }

    @Override
    public void getLocationOnScreen(int[] outLocation) {
        trace();
        super.getLocationOnScreen(outLocation);
    }

    @Override
    public void getLocationInWindow(int[] outLocation) {
        trace();
        super.getLocationInWindow(outLocation);
    }

    @Override
    public void setId(int id) {
        trace();
        super.setId(id);
    }

    @Override
    public int getId() {
        trace();
        return super.getId();
    }

    @Override
    public Object getTag() {
        trace();
        return super.getTag();
    }

    @Override
    public void setTag(Object tag) {
        trace();
        super.setTag(tag);
    }

    @Override
    public Object getTag(int key) {
        trace();
        return super.getTag(key);
    }

    @Override
    public void setTag(int key, Object tag) {
        trace();
        super.setTag(key, tag);
    }

    @Override
    public int getBaseline() {
        trace();
        return super.getBaseline();
    }

    @Override
    public boolean isInLayout() {
        trace();
        return super.isInLayout();
    }

    @Override
    public void requestLayout() {
        trace();
        super.requestLayout();
    }

    @Override
    public void forceLayout() {
        trace();
        super.forceLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        trace();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        trace();
        return super.getSuggestedMinimumHeight();
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        trace();
        return super.getSuggestedMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        trace();
        return super.getMinimumHeight();
    }

    @Override
    public void setMinimumHeight(int minHeight) {
        trace();
        super.setMinimumHeight(minHeight);
    }

    @Override
    public int getMinimumWidth() {
        trace();
        return super.getMinimumWidth();
    }

    @Override
    public void setMinimumWidth(int minWidth) {
        trace();
        super.setMinimumWidth(minWidth);
    }

    @Override
    public Animation getAnimation() {
        trace();
        return super.getAnimation();
    }

    @Override
    public void startAnimation(Animation animation) {
        trace();
        super.startAnimation(animation);
    }

    @Override
    public void clearAnimation() {
        trace();
        super.clearAnimation();
    }

    @Override
    public void setAnimation(Animation animation) {
        trace();
        super.setAnimation(animation);
    }

    @Override
    protected void onAnimationStart() {
        trace();
        super.onAnimationStart();
    }

    @Override
    protected void onAnimationEnd() {
        trace();
        super.onAnimationEnd();
    }

    @Override
    protected boolean onSetAlpha(int alpha) {
        trace();
        return super.onSetAlpha(alpha);
    }

    @Override
    public void playSoundEffect(int soundConstant) {
        trace();
        super.playSoundEffect(soundConstant);
    }

    @Override
    public boolean performHapticFeedback(int feedbackConstant) {
        trace();
        return super.performHapticFeedback(feedbackConstant);
    }

    @Override
    public boolean performHapticFeedback(int feedbackConstant, int flags) {
        trace();
        return super.performHapticFeedback(feedbackConstant, flags);
    }

    @Override
    public void setSystemUiVisibility(int visibility) {
        trace();
        super.setSystemUiVisibility(visibility);
    }

    @Override
    public int getSystemUiVisibility() {
        trace();
        return super.getSystemUiVisibility();
    }

    @Override
    public int getWindowSystemUiVisibility() {
        trace();
        return super.getWindowSystemUiVisibility();
    }

    @Override
    public void onWindowSystemUiVisibilityChanged(int visible) {
        trace();
        super.onWindowSystemUiVisibilityChanged(visible);
    }

    @Override
    public void dispatchWindowSystemUiVisiblityChanged(int visible) {
        trace();
        super.dispatchWindowSystemUiVisiblityChanged(visible);
    }

    @Override
    public void setOnSystemUiVisibilityChangeListener(OnSystemUiVisibilityChangeListener l) {
        trace();
        super.setOnSystemUiVisibilityChangeListener(l);
    }

    @Override
    public void dispatchSystemUiVisibilityChanged(int visibility) {
        trace();
        super.dispatchSystemUiVisibilityChanged(visibility);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        trace();
        return super.onDragEvent(event);
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        trace();
        return super.dispatchDragEvent(event);
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        trace();
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        trace();
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    @Override
    public int getOverScrollMode() {
        trace();
        return super.getOverScrollMode();
    }

    @Override
    public void setOverScrollMode(int overScrollMode) {
        trace();
        super.setOverScrollMode(overScrollMode);
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        trace();
        super.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        trace();
        return super.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        trace();
        return super.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        trace();
        super.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        trace();
        return super.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable int[] offsetInWindow) {
        trace();
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable int[] consumed, @Nullable int[] offsetInWindow) {
        trace();
        return super.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        trace();
        return super.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        trace();
        return super.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void setTextDirection(int textDirection) {
        trace();
        super.setTextDirection(textDirection);
    }

    @Override
    public int getTextDirection() {
        trace();
        return super.getTextDirection();
    }

    @Override
    public boolean canResolveTextDirection() {
        trace();
        return super.canResolveTextDirection();
    }

    @Override
    public boolean isTextDirectionResolved() {
        trace();
        return super.isTextDirectionResolved();
    }

    @Override
    public void setTextAlignment(int textAlignment) {
        trace();
        super.setTextAlignment(textAlignment);
    }

    @Override
    public int getTextAlignment() {
        trace();
        return super.getTextAlignment();
    }

    @Override
    public boolean canResolveTextAlignment() {
        trace();
        return super.canResolveTextAlignment();
    }

    @Override
    public boolean isTextAlignmentResolved() {
        trace();
        return super.isTextAlignmentResolved();
    }

    @Override
    public PointerIcon onResolvePointerIcon(MotionEvent event, int pointerIndex) {
        trace();
        return super.onResolvePointerIcon(event, pointerIndex);
    }

    @Override
    public void setPointerIcon(PointerIcon pointerIcon) {
        trace();
        super.setPointerIcon(pointerIcon);
    }

    @Override
    public PointerIcon getPointerIcon() {
        trace();
        return super.getPointerIcon();
    }

    @Override
    public boolean hasPointerCapture() {
        trace();
        return super.hasPointerCapture();
    }

    @Override
    public void requestPointerCapture() {
        trace();
        super.requestPointerCapture();
    }

    @Override
    public void releasePointerCapture() {
        trace();
        super.releasePointerCapture();
    }

    @Override
    public void onPointerCaptureChange(boolean hasCapture) {
        trace();
        super.onPointerCaptureChange(hasCapture);
    }

    @Override
    public void dispatchPointerCaptureChanged(boolean hasCapture) {
        trace();
        super.dispatchPointerCaptureChanged(hasCapture);
    }

    @Override
    public boolean onCapturedPointerEvent(MotionEvent event) {
        trace();
        return super.onCapturedPointerEvent(event);
    }

    @Override
    public void setOnCapturedPointerListener(OnCapturedPointerListener l) {
        trace();
        super.setOnCapturedPointerListener(l);
    }

    @Override
    public ViewPropertyAnimator animate() {
        trace();
        return super.animate();
    }

    @Override
    public String getTransitionName() {
        trace();
        return super.getTransitionName();
    }

    @Override
    public void setTooltipText(@Nullable CharSequence tooltipText) {
        trace();
        super.setTooltipText(tooltipText);
    }

    @Nullable
    @Override
    public CharSequence getTooltipText() {
        trace();
        return super.getTooltipText();
    }
}
