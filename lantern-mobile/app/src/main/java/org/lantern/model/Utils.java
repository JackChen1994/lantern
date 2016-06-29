package org.lantern.model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.analytics.HitBuilders;

import java.io.File;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lantern.R;
import org.lantern.fragment.ErrorDialogFragment;
import org.lantern.mobilesdk.Lantern;

public class Utils {
    private static final String PREFS_NAME = "LanternPrefs";
    private static final String TAG = "Utils";
    private static final String PREF_USE_VPN = "pref_vpn";

    // update START/STOP power Lantern button
    // according to our stored preference
    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
    }

    public static void clearPreferences(Context context) {
        SharedPreferences mPrefs = getSharedPrefs(context);

        if (mPrefs != null) {
            mPrefs.edit().remove(PREF_USE_VPN).commit();
        }
    }

    // isDebuggable checks the debuggable flag of the package
    // to determine if the current build is a debug build
    public static boolean isDebuggable(Context context) {
        try {
            return (context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0).applicationInfo.flags &
                    ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error fetching package information: " + e.getMessage());
        }
        return false;
    }

    public static void copyToClipboard(Context context, String label, 
            String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE); 
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    public static void showToastMessage(LayoutInflater inflater, Activity activity, Context context, String message) {
        View statusLayout = inflater.inflate(R.layout.status_layout, 
                (ViewGroup)activity.findViewById(R.id.status_layout_root));
        TextView statusText  = (TextView)statusLayout.findViewById(R.id.statusText);
        ImageView statusImage = (ImageView)statusLayout.findViewById(R.id.statusImage);
        statusImage.setImageResource(R.drawable.status_on_white);
        statusText.setText(context.getResources().getString(R.string.referral_copied_to_clipboard));
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.BOTTOM|Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(statusLayout);
        toast.show();
    }

    // isPlayVersion checks whether or not the user installed Lantern via
    // the Google Play store
    public static boolean isPlayVersion(Context context) {
       try {
            String installer = context.getPackageManager()
                .getInstallerPackageName(context.getPackageName());

            return installer != null && installer.equals("com.android.vending");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching package information: " + e.getMessage());
        }

        return false;
    }

    // openPlayStore opens Lantern's app details page in the Google Play store.
    // - if we can't open the page in Play itself, resort to opening it in the browser
    public static void openPlayStore(Context context) {
        final String appPackageName = context.getPackageName();
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    public static void hideKeyboard(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager)context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String formatMoney(long amount) {
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();
        return String.format("%s%d", symbol, amount/100);
    }

    public static void showErrorDialog(final FragmentActivity activity, String error) {
        DialogFragment fragment = ErrorDialogFragment.newInstance(R.string.validation_errors, error);
        fragment.show(activity.getSupportFragmentManager(), "error");
    }

    public static boolean isEmailValid(String email) {
        if (email == null || email.equals("")) {
            return false;
        }

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }



    public static void configureEmailInput(final EditText emailInput, final View separator) {

        OnFocusChangeListener focusListener = new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    separator.setBackgroundResource(R.color.blue_color);
                    emailInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.email_active, 0, 0, 0);
                } else {
                    separator.setBackgroundResource(R.color.edittext_color);
                    emailInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.email_inactive, 0, 0, 0);
                }
            }
        };
        emailInput.setOnFocusChangeListener(focusListener);
    }


    public static void showAlertDialog(Activity activity, String title, String msg) {
        Log.d(TAG, "Showing alert dialog...");

        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(msg);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
        }
        );
        alertDialog.show();
    }

    public static Snackbar formatSnackbar(Snackbar snackbar) {
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(Color.BLACK);
        TextView tv = (TextView) snackView.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(14);
        tv.setMaxLines(4);
        return snackbar;
    }

    public static void showPlainSnackbar(final CoordinatorLayout coordinatorLayout, String message) {
        showSnackbar(coordinatorLayout, message, null, 0, null);
    }

    public static void showSnackbar(final CoordinatorLayout coordinatorLayout,
            String message, String action, int actionTextColor, View.OnClickListener onClick) {

        Snackbar snackBar = Snackbar
            .make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        // format snackbar
        snackBar = formatSnackbar(snackBar);
        if (action != null && onClick != null) {
            snackBar.setAction(action, onClick);
            snackBar.setActionTextColor(actionTextColor);
        }

        snackBar.show();
    }

    // isNetworkAvailable checks whether or not we are connected to
    // the Internet; if no connection is available, the toggle
    // switch is inactive
    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager =
            ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null &&
            connectivityManager.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    public static void sendFeedEvent(Context context, String category) {
        Log.d(TAG, "Logging feed event. Category is " + category);

        String analyticsTrackingID = "UA-21815217-14";
        Lantern.trackerFor(context, analyticsTrackingID).send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction("click")
                .build());
    }

    public static final int getColor(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }
}
