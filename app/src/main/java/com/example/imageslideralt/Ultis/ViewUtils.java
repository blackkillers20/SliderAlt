package com.example.imageslideralt.Ultis;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.example.imageslideralt.CameraActivity;
import com.example.imageslideralt.MainActivity;
import com.example.imageslideralt.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.function.Function;


public class ViewUtils {
    // show a toast message
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // show snackbar message
    public static void showSnackbar(Context context, String message) {
        Snackbar.make(((CameraActivity) context).findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .show();
    }

    // show snackbar message with action
    public static void showSnackbar(Context context, String message, String action, View.OnClickListener listener) {
        Snackbar.make(((MainActivity) context).findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setAction(action, listener)
                .show();
    }

    public interface InputDialogCallback {
        void onConfirm(String input) throws Exception;
        void onException(Exception e);
    }
    public static void showInputDialog(Context context, String title, String hint,
                                       String defaultValue,
                                       InputDialogCallback callback) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        EditText editText = layout.findViewById(R.id.input);
        editText.setHint(hint);
        editText.setText(defaultValue);
        dialog.setView(layout);
        dialog.setPositiveButton("Confirm", (dialogInterface, i) -> {
            AlertDialog alertDialog = (AlertDialog) dialogInterface;
            String input = ((EditText)alertDialog.findViewById(R.id.input)).getText().toString();
            try {
                callback.onConfirm(input);
            } catch (Exception e) {
                callback.onException(e);
            }
            alertDialog.dismiss();
        });
        dialog.show();
    }

    public static void showInputDialog(Context context, String title, String hint,
                                       String defaultValue,
                                       InputDialogCallback callback, Function<EditText, Boolean> validator) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        View layout = LayoutInflater.from(context).inflate(R.layout.dialog_input, null);
        EditText editText = layout.findViewById(R.id.input);
        editText.setHint(hint);
        editText.setText(defaultValue);
        dialog.setView(layout);
        dialog.setPositiveButton("Confirm", null);
        AlertDialog alertDialog = dialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String input = editText.getText().toString();
            if (validator.apply(editText)) {
                try {
                    callback.onConfirm(input);
                } catch (Exception e) {
                    callback.onException(e);
                }
                alertDialog.dismiss();
            }
        });
    }

    public static void showDialog(Context context, String title, String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setPositiveButton("OK", null);
        dialog.show();
    }
}
