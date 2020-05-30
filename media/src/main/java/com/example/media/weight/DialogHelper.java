package com.example.media.weight;

import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.media.R;

public class DialogHelper {
    private DialogHelper() {
    }

    public static DialogHelper with() {
        return new DialogHelper();
    }

    public AlertDialog createDialog(@NonNull Context context, @NonNull String title, @NonNull String message,
                                  @NonNull DialogInterface.OnClickListener onNegativeListener, @NonNull DialogInterface.OnClickListener onPositiveListener) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(message)
                .setNegativeButton(R.string.cancel, onNegativeListener).setPositiveButton(R.string.confirm, onPositiveListener)
                .create();
    }
}
