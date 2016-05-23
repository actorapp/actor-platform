/*
 * libjingle
 * Copyright 2013, Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package im.actor.sdk.controllers.calls;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Singleton helper: install a default unhandled exception handler which shows
 * an informative dialog and kills the app.  Useful for apps whose
 * error-handling consists of throwing RuntimeExceptions.
 * NOTE: almost always more useful to
 * Thread.setDefaultUncaughtExceptionHandler() rather than
 * Thread.setUncaughtExceptionHandler(), to apply to background threads as well.
 */
public class UnhandledExceptionHandler
        implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "AppRTCDemoActivity";
    private final Activity activity;

    public UnhandledExceptionHandler(final Activity activity) {
        this.activity = activity;
    }

    public void uncaughtException(Thread unusedThread, final Throwable e) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String title = "Fatal error: " + getTopLevelCauseMessage(e);
                String msg = getRecursiveStackTrace(e);
                TextView errorView = new TextView(activity);
                errorView.setText(msg);
                errorView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                ScrollView scrollingContainer = new ScrollView(activity);
                scrollingContainer.addView(errorView);
                Log.e(TAG, title + "\n\n" + msg);
                DialogInterface.OnClickListener listener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog, int which) {
                                dialog.dismiss();
                                System.exit(1);
                            }
                        };
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity);
                builder
                        .setTitle(title)
                        .setView(scrollingContainer)
                        .setPositiveButton("Exit", listener).show();
            }
        });
    }

    // Returns the Message attached to the original Cause of |t|.
    private static String getTopLevelCauseMessage(Throwable t) {
        Throwable topLevelCause = t;
        while (topLevelCause.getCause() != null) {
            topLevelCause = topLevelCause.getCause();
        }
        return topLevelCause.getMessage();
    }

    // Returns a human-readable String of the stacktrace in |t|, recursively
    // through all Causes that led to |t|.
    private static String getRecursiveStackTrace(Throwable t) {
        StringWriter writer = new StringWriter();
        t.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
