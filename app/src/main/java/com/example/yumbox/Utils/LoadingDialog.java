package com.example.yumbox.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.example.yumbox.R;

public class LoadingDialog {
    public static Dialog create(Context context, String message) {
        Dialog loadingDialog = new Dialog(context);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView messageTextView = loadingDialog.findViewById(R.id.txtMessage);
        messageTextView.setText(message);
        loadingDialog.setCancelable(false); // Can't cancel dialog by back, tapping outside
        loadingDialog.create();
        return loadingDialog;
    }
}
