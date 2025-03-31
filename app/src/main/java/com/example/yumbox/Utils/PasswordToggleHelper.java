package com.example.yumbox.Utils;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import com.example.yumbox.R;

public class PasswordToggleHelper {
    private boolean isPasswordVisible = false;

    @SuppressLint("ClickableViewAccessibility")
    public void setupPasswordToggle(final EditText editText) {
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int drawableEnd = 2;
                    if (editText.getCompoundDrawables()[drawableEnd] != null) {
                        int drawableWidth = editText.getCompoundDrawables()[drawableEnd].getBounds().width();

                        Rect touchableArea = new Rect();
                        editText.getGlobalVisibleRect(touchableArea);
                        touchableArea.left = touchableArea.right - drawableWidth - 50; // Mở rộng 50px

                        if (touchableArea.contains((int) event.getRawX(), (int) event.getRawY())) {
                            isPasswordVisible = !isPasswordVisible;

                            if (isPasswordVisible) {
                                editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eye_hide, 0);
                            } else {
                                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                editText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, R.drawable.eye, 0);
                            }

                            // Giữ con trỏ ở cuối dòng sau khi thay đổi inputType
                            editText.setSelection(editText.getText().length());
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }
}
