/*
 * Copyright (C) 2013 UCWeb Inc. All rights reserved
 * 本代码版权归UC优视科技所有。
 * UC游戏交易平台为优视科技（UC）旗下的手机游戏交易平台产品
 *
 *
 */

package com.dalimao.corelibrary;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;


public class VerificationCodeInput extends ViewGroup {

    private final static String TYPE_NUMBER = "number";
    private final static String TYPE_TEXT = "text";
    private final static String TYPE_PASSWORD = "password";
    private final static String TYPE_PHONE = "phone";

    private static final String TAG = "VerificationCodeInput";
    private int box = 4;
    private int boxWidth = 120;
    private int boxHeight = 120;
    private int childHPadding = 7;
    private int childVPadding = 10;
    private int textColor;
    private int textSize;
    private String inputType = TYPE_PASSWORD;
    private int boxBgFocus;
    private int boxBgNormal;
    private Listener listener;
    private boolean isDelete = false;

    private String content;

    public VerificationCodeInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.vericationCodeInput);
        box = a.getInt(R.styleable.vericationCodeInput_box, 4);
        childHPadding = (int) a.getDimension(R.styleable.vericationCodeInput_child_h_padding, 0);
        childVPadding = (int) a.getDimension(R.styleable.vericationCodeInput_child_v_padding, 0);
        boxBgFocus = a.getResourceId(R.styleable.vericationCodeInput_box_bg_focus, R.drawable.verification_edit_bg_focus);
        boxBgNormal = a.getResourceId(R.styleable.vericationCodeInput_box_bg_normal, R.drawable.verification_edit_bg_normal);
        inputType = a.getString(R.styleable.vericationCodeInput_inputType);
        boxWidth = (int) a.getDimension(R.styleable.vericationCodeInput_child_width, boxWidth);
        boxHeight = (int) a.getDimension(R.styleable.vericationCodeInput_child_height, boxHeight);
        textColor = a.getColor(R.styleable.vericationCodeInput_textColor, Color.parseColor("#FFFFFF"));
        textSize = a.getDimensionPixelSize(R.styleable.vericationCodeInput_textSize, 14);
        a.recycle();
        initViews();
    }


    private void initViews() {
        OnKeyListener onKeyListener = new OnKeyListener() {
            @Override
            public synchronized boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    isDelete = true;
                }
                return false;
            }
        };
        for (int i = 0; i < box; i++) {
            final EditText editText = new EditText(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(boxWidth, boxHeight);
            layoutParams.leftMargin = childHPadding;
            layoutParams.rightMargin = childHPadding;
            layoutParams.gravity = Gravity.CENTER;
//            editText.setOnKeyListener(onKeyListener);
            setBg(editText, false);
            editText.setTextColor(textColor);
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            editText.setLayoutParams(layoutParams);
            editText.setGravity(Gravity.CENTER);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

            if (TYPE_NUMBER.equals(inputType)) {
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else if (TYPE_PASSWORD.equals(inputType)) {
                editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            } else if (TYPE_TEXT.equals(inputType)) {
                editText.setInputType(InputType.TYPE_CLASS_TEXT);
            } else if (TYPE_PHONE.equals(inputType)) {
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
            }
            editText.setId(i);
            editText.setEms(1);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        setBg(editText, false);
                        isDelete = true;
                    } else {
                        isDelete = false;
                        setBg(editText, true);
                        checkAndCommit();
                    }
                    next();
                }
            });
            addView(editText, i);
        }
    }

    private void next() {
        int count = getChildCount();
        EditText editText;
        if (isDelete) {
            isDelete = false;
            backFocus();
        } else {
            focus();
        }
    }

    private void backFocus() {
        int count = getChildCount();
        EditText editText;
        for (int i = count - 1; i >= 0; i--) {
            editText = (EditText) getChildAt(i);
            if (editText.getText().length() == 1) {
                editText.requestFocus();
                editText.setSelection(1);
                return;
            } else {
                setBg(editText, false);
            }
        }
    }

    private void focus() {
        int count = getChildCount();
        EditText editText;
        for (int i = 0; i < count; i++) {
            editText = (EditText) getChildAt(i);
            if (editText.getText().length() < 1) {
                editText.requestFocus();
                return;
            }
        }
    }

    private void setBg(EditText editText, boolean focus) {
        editText.setBackgroundResource(focus ? boxBgFocus : boxBgNormal);
        if (listener != null && !focus) {
            listener.onComplete(focus, "");
        }
    }

    private void checkAndCommit() {
        StringBuilder stringBuilder = new StringBuilder();
        boolean full = true;
        for (int i = 0; i < box; i++) {
            EditText editText = (EditText) getChildAt(i);
            String content = editText.getText().toString();
            if (content.length() == 0) {
                full = false;
                break;
            } else {
                stringBuilder.append(content);
            }

        }
        Log.d(TAG, "checkAndCommit:" + stringBuilder.toString());
        if (full) {
            if (listener != null) {
                content = stringBuilder.toString();
                listener.onComplete(true, stringBuilder.toString());
                setEnabled(false);
            }
        }
    }

    public String getContent() {
        return content;
    }

    @Override
    public void setEnabled(boolean enabled) {
        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            child.setEnabled(enabled);
        }
    }

    public void setOnCompleteListener(Listener listener) {
        this.listener = listener;
    }

    @Override

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LinearLayout.LayoutParams(getContext(), attrs);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = getMeasuredWidth();
        if (parentWidth == LayoutParams.MATCH_PARENT) {
            parentWidth = getScreenWidth();
        }
        Log.d(getClass().getName(), "onMeasure width " + parentWidth);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            this.measureChild(child, widthMeasureSpec, heightMeasureSpec);
        }
        if (count > 0) {
            View child = getChildAt(0);
            int cWidth = child.getMeasuredWidth();
            if (parentWidth != LayoutParams.WRAP_CONTENT) {
                // 重新计算padding
                childHPadding = (parentWidth - cWidth * count) / (count + 1);
            }

            int cHeight = child.getMeasuredHeight();

            int maxH = cHeight;
//            int maxH = cHeight + 2 * childVPadding;
            int maxW = (cWidth) * count + childHPadding * (count + 1);
            setMeasuredDimension(resolveSize(maxW, widthMeasureSpec),
                    resolveSize(maxH, heightMeasureSpec));
        }


    }

    private int getScreenWidth() {

        Resources resources = this.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();

        return dm.widthPixels;


    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.d(getClass().getName(), "onLayout width = " + getMeasuredWidth());

        int childCount = getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);

            child.setVisibility(View.VISIBLE);
            int cWidth = child.getMeasuredWidth();
            int cHeight = child.getMeasuredHeight();
            int cl = childHPadding + (i) * (cWidth + childHPadding);
            int cr = cl + cWidth;
            int ct = childVPadding;
            int cb = ct + cHeight;
            child.layout(cl, ct, cr, cb);
        }


    }

    public interface Listener {
        void onComplete(boolean complete, String content);
    }

}

