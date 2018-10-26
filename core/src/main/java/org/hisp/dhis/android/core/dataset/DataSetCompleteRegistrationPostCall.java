/*
 * Copyright (c) 2004-2018, University of Oslo
 * All rights reserved.
 *
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

package org.hisp.dhis.android.core.dataset;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.common.APICallExecutor;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.SyncCall;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;
import org.hisp.dhis.android.core.imports.ImportSummary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import retrofit2.Retrofit;

public final class DataSetCompleteRegistrationPostCall extends SyncCall<ImportSummary> {

    private final DataSetCompleteRegistrationService dataSetCompleteRegistrationService;
    private final DataSetCompleteRegistrationStore dataSetCompleteRegistrationStore;

    @Override
    public ImportSummary call() throws Exception {

        setExecuted();

        List<DataSetCompleteRegistration> toPostDataSetCompleteRegistrations = new ArrayList<>();

        appendPostableDataValues(toPostDataSetCompleteRegistrations);
        appendUpdatableDataValues(toPostDataSetCompleteRegistrations);

        if (toPostDataSetCompleteRegistrations.isEmpty()) {
            return ImportSummary.EMPTY;
        }

        DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload
                = new DataSetCompleteRegistrationPayload(toPostDataSetCompleteRegistrations);

        ImportSummary importSummary = new APICallExecutor().executeObjectCall(
                dataSetCompleteRegistrationService.postDataSetCompleteRegistrations(
                        dataSetCompleteRegistrationPayload));

        handleImportSummary(dataSetCompleteRegistrationPayload, importSummary);

        return importSummary;
    }

    private void appendPostableDataValues(Collection<DataSetCompleteRegistration> dataSetCompleteRegistrations) {
        dataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getDataSetCompleteRegistrationsWithState(State.TO_POST));
    }

    private void appendUpdatableDataValues(Collection<DataSetCompleteRegistration> dataSetCompleteRegistrations) {
        dataSetCompleteRegistrations.addAll(
                dataSetCompleteRegistrationStore.getDataSetCompleteRegistrationsWithState(State.TO_UPDATE));
    }

    private void handleImportSummary(DataSetCompleteRegistrationPayload dataSetCompleteRegistrationPayload,
                                     ImportSummary importSummary) {

        DataSetCompleteRegistrationImportHandler dataSetCompleteRegistrationImportHandler =
                new DataSetCompleteRegistrationImportHandler(dataSetCompleteRegistrationStore);

        dataSetCompleteRegistrationImportHandler.handleImportSummary(
                dataSetCompleteRegistrationPayload, importSummary);
    }

    private DataSetCompleteRegistrationPostCall(
            @NonNull DataSetCompleteRegistrationService dataSetCompleteRegistrationService,
            @NonNull DataSetCompleteRegistrationStore dataSetCompleteRegistrationStore) {

        this.dataSetCompleteRegistrationService = dataSetCompleteRegistrationService;
        this.dataSetCompleteRegistrationStore = dataSetCompleteRegistrationStore;
    }

    public static DataSetCompleteRegistrationPostCall create(@NonNull DatabaseAdapter databaseAdapter,
                                           @NonNull Retrofit retrofit) {

        return new DataSetCompleteRegistrationPostCall(retrofit.create(DataSetCompleteRegistrationService.class),
                DataSetCompleteRegistrationStore.create(databaseAdapter));
    }

}
