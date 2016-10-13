/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.client.sdk.ui.bindings.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;

import org.hisp.dhis.client.sdk.ui.activities.AbsHomeActivity;
import org.hisp.dhis.client.sdk.ui.bindings.App;
import org.hisp.dhis.client.sdk.ui.bindings.R;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenter;
import org.hisp.dhis.client.sdk.ui.fragments.WrapperFragment;

public abstract class DefaultHomeActivity extends AbsHomeActivity implements HomeView {
    private HomePresenter homePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // injecting dependencies
        homePresenter = App.from(getApplication()).getUserComponent().homePresenter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        homePresenter.attachView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        homePresenter.detachView();
    }

    @Override
    public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);
        homePresenter.calculateLastSyncedPeriod();
    }

    @Override
    public void showLastSyncedMessage(String message) {
        setSynchronizedMessage(message);
    }

    @NonNull
    @Override
    protected Fragment getProfileFragment() {
        return WrapperFragment.newInstance(DefaultProfileFragment.class,
                getString(R.string.drawer_item_profile));
    }

    @NonNull
    @Override
    protected Fragment getSettingsFragment() {
        return WrapperFragment.newInstance(DefaultSettingsFragment.class,
                getString(R.string.drawer_item_settings));
    }

    @Override
    public void setUsername(CharSequence username) {
        getUsernameTextView().setText(username);
    }

    @Override
    public void setUserInfo(CharSequence userInfo) {
        getUserInfoTextView().setText(userInfo);
    }

    @Override
    public void setUserInitials(CharSequence userLetters) {
        getUsernameLetterTextView().setText(userLetters);
    }

    @Override
    protected Fragment getInformationFragment() {
        return WrapperFragment.newInstance(DefaultInformationFragment.class,
                getString(R.string.drawer_item_information), new Bundle());
    }
}
