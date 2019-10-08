package jan.schuettken.bierpongleague.handler;

import android.app.DatePickerDialog;
import android.content.Context;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;

import java.util.Objects;

import jan.schuettken.bierpongleague.R;

/**
 * Created by Jan Schüttken on 21.12.2017 at 15:12
 */

public class DialogHandler {

    public DatePickerDialog createDialogWithoutDateField(AppCompatActivity parent) {
        DateFunctionHandler dateFunctionHandler = new DateFunctionHandler();
        return createDialogWithoutDateField(parent, DateFunctionHandler.getThisYear(), DateFunctionHandler.getThisMonth(), DateFunctionHandler.getThisDay());
    }

    public DatePickerDialog createDialogWithoutDateField(AppCompatActivity parent, int year, int month, int day) {
        if (parent == null)
            return null;
        DatePickerDialog dpd = new DatePickerDialog(parent, null, year, month - 1, day);
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        //Log.i("test", datePickerField.getName());
                        if ("mDaySpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return dpd;
    }

    public AlertDialog.Builder getAlterDialogYesCancel(String title, String body, final ActionInterface pos_a, Context context) {
        return getAlterDialog(title, body, context.getString(R.string.button_yes), pos_a, null, null, context.getString(R.string.button_cancel), null, context);
    }

    public AlertDialog.Builder getAlterDialogYesCancel(int title, int body, final ActionInterface pos, Context context) {
        return getAlterDialog(title, body, R.string.button_yes, pos, -1, null, R.string.button_cancel, null, context);
    }

    public AlertDialog.Builder getAlterDialogOk(String title, String body, ActionInterface ac, Context context) {
        return getAlterDialog(title, body, context.getString(R.string.button_ok), ac, null, null, null, null, context);
    }

    public AlertDialog.Builder getAlterDialogOk(int title, int body, ActionInterface ac, Context context) {
        return getAlterDialog(title, body, R.string.button_ok, ac, -1, null, -1, null, context);
    }

    public void showAlterDialogOk(int title, int body, ActionInterface ac, Context context) {
        getAlterDialog(title, body, R.string.button_ok, ac, -1, null, -1, null, context).show();
    }

    public AlertDialog.Builder getAlterDialogOk(String title, String body, Context context) {
        return getAlterDialogOk(title, body, null, context);
//        return getAlterDialog(title, body, context.getString(R.string.button_ok), null, null, null, null, null, context);
    }

    public void showAlterDialogOk(String title, String body, Context context) {
        getAlterDialogOk(title, body, null, context).show();
    }

    public void showAlterDialogOk(int title, int body, Context context) {
        getAlterDialogOk(title, body, null, context).show();
    }

    public AlertDialog.Builder getAlterDialogOk(int title, int body, Context context) {
        return getAlterDialog(title, body, R.string.button_ok, null, -1, null, -1, null, context);
    }

    public AlertDialog.Builder getAlterDialogYesCancel(int title, int body, final ActionInterface pos, final ActionInterface can, Context context) {
        return getAlterDialog(title, body, R.string.button_yes, pos, -1, null, R.string.button_cancel, can, context);
    }

    public AlertDialog.Builder getAlterDialog
            (String title, String body, String pos, final ActionInterface pos_a, String neg, final ActionInterface neg_a, String can, final ActionInterface can_a, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(body);
        // Set up the buttons
        if (pos != null && !Objects.equals(pos, ""))
            builder.setPositiveButton(pos, (dialog, which) -> {
                if (pos_a == null)
                    dialog.cancel();
                else
                    pos_a.run();
            });
        if (neg != null && !Objects.equals(neg, ""))
            builder.setNeutralButton(neg, (dialog, which) -> {
                if (neg_a == null)
                    dialog.cancel();
                else
                    neg_a.run();
            });
        if (can != null && !Objects.equals(can, ""))
            builder.setNegativeButton(can, (dialog, which) -> {
                if (can_a == null)
                    dialog.cancel();
                else
                    can_a.run();

            });
        return builder;
    }

    public AlertDialog.Builder getAlterDialog
            (int title, int body, int positive, final ActionInterface pos_a, int negative, final ActionInterface neg_a, int cancel, final ActionInterface can_a, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (title != -1)
            builder.setTitle(title);
        if (body != -1)
            builder.setMessage(body);
        // Set up the buttons
        if (positive != -1)
            builder.setPositiveButton(positive, (dialog, which) -> {
                if (pos_a == null)
                    dialog.cancel();
                else
                    pos_a.run();
            });
        if (negative != -1)
            builder.setNeutralButton(negative, (dialog, which) -> {
                if (neg_a == null)
                    dialog.cancel();
                else
                    neg_a.run();
            });
        if (cancel != -1)
            builder.setNegativeButton(cancel, (dialog, which) -> {
                if (can_a == null)
                    dialog.cancel();
                else
                    can_a.run();

            });
        return builder;
    }

    public Snackbar createSnakbar(View parent, int message, View.OnClickListener action, int actionText) {
        Snackbar mySnackbar = Snackbar.make(parent,
                message, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(actionText, action);
        return mySnackbar;
    }
}
