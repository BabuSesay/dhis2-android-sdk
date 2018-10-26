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
package org.hisp.dhis.android.core.systeminfo;

import org.hisp.dhis.android.core.calls.Call;
import org.hisp.dhis.android.core.calls.factories.NoArgumentsCallFactory;
import org.hisp.dhis.android.core.wipe.WipeableModule;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;

import retrofit2.Retrofit;

public final class SystemInfoInternalModule implements WipeableModule {

    private final DatabaseAdapter databaseAdapter;
    private final Retrofit retrofit;
    public final SystemInfoModule publicModule;

    private SystemInfoInternalModule(DatabaseAdapter databaseAdapter, Retrofit retrofit,
                                     SystemInfoModule publicModule) {
        this.databaseAdapter = databaseAdapter;
        this.retrofit = retrofit;
        this.publicModule = publicModule;
    }

    public final NoArgumentsCallFactory<SystemInfo> callFactory = new NoArgumentsCallFactory<SystemInfo>() {
        @Override
        public Call<SystemInfo> create() {
            return SystemInfoCall.create(databaseAdapter, retrofit, publicModule.versionManager);
        }
    };

    @Override
    public void wipeMetadata() {
        SystemInfoStore.create(databaseAdapter).delete();
    }

    @Override
    public void wipeData() {
        // Without data to wipe
    }

    public static SystemInfoInternalModule create(DatabaseAdapter databaseAdapter, Retrofit retrofit) {
        return new SystemInfoInternalModule(
                databaseAdapter,
                retrofit,
                SystemInfoModule.create(databaseAdapter)
        );
    }
}
