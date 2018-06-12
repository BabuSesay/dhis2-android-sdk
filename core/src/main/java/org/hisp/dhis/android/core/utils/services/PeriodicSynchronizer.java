/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.core.utils.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.hisp.dhis.android.core.D2;

public class PeriodicSynchronizer extends BroadcastReceiver {
    public static final int FREQUENCY_ONE_MINUTE = 0;
    public static final int FREQUENCY_15_MINTUES = 1;
    public static final int FREQUENCY_ONE_HOUR = 2;
    public static final int FREQUENCY_ONE_DAY = 3;
    public static final int FREQUENCY_DISABLED = 4;
    public static final int DEFAULT_UPDATE_FREQUENCY = FREQUENCY_ONE_DAY;

    public static final String CLASS_TAG = PeriodicSynchronizer.class.getSimpleName();

    private static PeriodicSynchronizer periodicSynchronizer;
    private int currentInterval = getIntervalFromFrequencyIndex(DEFAULT_UPDATE_FREQUENCY);

    public static PeriodicSynchronizer getInstance() {
        if (periodicSynchronizer == null) {
            periodicSynchronizer = new PeriodicSynchronizer();
        }
        return periodicSynchronizer;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(CLASS_TAG, "onReceive.");
        syncIfIsUserLoggedIn();
    }

    private static void syncIfIsUserLoggedIn() {
        try {
            if (D2.d2.isUserLoggedIn().call()) {
                Log.d(CLASS_TAG, "Sync.");
                D2Synchronizer.syncReservedValues();
            } else {
                Log.d(CLASS_TAG, "Not sync.");
            }
        } catch (Exception e) {
            Log.d(CLASS_TAG, e.getMessage());
        }
    }

    public static void activatePeriodicSynchronizer(Context context, int minutes) {
        if (minutes <= 0) {
            return;
        }

        Log.i(CLASS_TAG, "Activate periodic synchronizer each " + minutes + "minutes.");

        Intent intent = new Intent(context, PeriodicSynchronizer.class);
        PendingIntent existingPendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (existingPendingIntent == null) {
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            long intervalMillis = 1000 * 60 * minutes;
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis,
                    pendingIntent);
        }

        syncIfIsUserLoggedIn();
    }

    public static void cancelPeriodicSynchronizer(Context context) {
        Log.i(CLASS_TAG, "Cancel periodic synchronizer.");

        Intent intent = new Intent(context, PeriodicSynchronizer.class);
        PendingIntent existingPendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_NO_CREATE);

        if (existingPendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(existingPendingIntent);
            existingPendingIntent.cancel();
        }
    }

    public static int getInterval() {
        // TODO Get interval from app settings frequency index
        return getIntervalFromFrequencyIndex(DEFAULT_UPDATE_FREQUENCY);
    }

    public static int getIntervalFromFrequencyIndex(int frequencyIndex) {
        int minutes;
        switch (frequencyIndex) {
            case FREQUENCY_ONE_MINUTE:
                minutes = 1;
                break;
            case FREQUENCY_15_MINTUES:
                minutes = 15;
                break;
            case FREQUENCY_ONE_HOUR:
                minutes = 60;
                break;
            case FREQUENCY_ONE_DAY:
                minutes = 60 * 24;
                break;
            case FREQUENCY_DISABLED:
                minutes = 0;
                break;
            default:
                minutes = DEFAULT_UPDATE_FREQUENCY;
        }
        return minutes;
    }

    public static void reActivate(Context context, int interval) {
        if (interval != getInstance().currentInterval) {
            cancelPeriodicSynchronizer(context);
            activatePeriodicSynchronizer(context, interval);
            getInstance().currentInterval = interval;
        }
    }
}