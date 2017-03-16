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
package org.hisp.dhis.android.core.trackedentity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.common.Call;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.data.api.Fields;
import org.hisp.dhis.android.core.data.api.Filter;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.resource.ResourceStore;
import org.hisp.dhis.android.core.utils.HeaderUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import retrofit2.Response;

public class TrackedEntityCall implements Call<Response<Payload<TrackedEntity>>> {

    private final TrackedEntityService service;
    private final SQLiteDatabase database;
    private final TrackedEntityStore store;
    private final ResourceStore resourceStore;
    private final Set<String> uidSet;
    private Boolean isExecuted = false;

    public TrackedEntityCall(@Nullable Set<String> uidSet,
                             @NonNull SQLiteDatabase database,
                             @NonNull TrackedEntityStore store,
                             @NonNull ResourceStore resourceStore,
                             @NonNull TrackedEntityService service
    ) {
        this.uidSet = uidSet;
        this.database = database;
        this.store = store;
        this.resourceStore = resourceStore;
        this.service = service;
    }

    @Override
    public boolean isExecuted() {
        synchronized (this) {
            return isExecuted;
        }
    }

    @Override
    public Response<Payload<TrackedEntity>> call() throws Exception {
        synchronized (this) {
            if (isExecuted) {
                throw new IllegalStateException("AlreadyExecuted");
            }
            isExecuted = true;
        }
        //TODO: uid will be null if we want all uids. Still make the call ! + Test for it !

        Response<Payload<TrackedEntity>> response = null;
        database.beginTransaction();
        try {
            Filter<TrackedEntity, String> idFilter = null;
            if (uidSet != null) {
                idFilter = TrackedEntity.uid.in(uidSet);
            }
            Filter<TrackedEntity, String> lastUpdatedFilter = TrackedEntity.lastUpdated.gt(
                    getLastUpdated(OrganisationUnit.class.getSimpleName()));

            response = getTrackedEntities(idFilter, lastUpdatedFilter);

            if (response != null && response.isSuccessful()) {
                for (TrackedEntity trackedEntity : response.body().items()) {
                    persistTrackedEntities(trackedEntity);
                }
                updateInResourceStore(response.headers().getDate(HeaderUtils.DATE),
                        OrganisationUnit.class.getSimpleName());
                database.setTransactionSuccessful();
            }
        } finally {
            database.endTransaction();
        }
        return response;
    }

    private Response<Payload<TrackedEntity>> getTrackedEntities(Filter<TrackedEntity, String> idFilter,
                                                                Filter<TrackedEntity, String> lastUpdatedFilter
    ) throws IOException {

        Fields<TrackedEntity> fields = Fields.<TrackedEntity>builder().fields(
                TrackedEntity.uid, TrackedEntity.code, TrackedEntity.name,
                TrackedEntity.displayName, TrackedEntity.created, TrackedEntity.lastUpdated,
                TrackedEntity.shortName, TrackedEntity.displayShortName,
                TrackedEntity.description, TrackedEntity.displayDescription,
                TrackedEntity.deleted
        ).build();

        retrofit2.Call<Payload<TrackedEntity>> call = service.trackedEntities(fields, idFilter,
                lastUpdatedFilter, false);
        return call.execute();
    }

    private void persistTrackedEntities(TrackedEntity trackedEntity) {
        if (trackedEntity.deleted()) {
            store.delete(trackedEntity.uid());
        } else {
            int updatedRow = store.update(
                    trackedEntity.uid(),
                    trackedEntity.code(),
                    trackedEntity.name(),
                    trackedEntity.displayName(),
                    trackedEntity.created(),
                    trackedEntity.lastUpdated(),
                    trackedEntity.shortName(),
                    trackedEntity.displayShortName(),
                    trackedEntity.description(),
                    trackedEntity.displayDescription(),
                    trackedEntity.uid()
            );
            if (updatedRow <= 0) {
                store.insert(
                        trackedEntity.uid(),
                        trackedEntity.code(),
                        trackedEntity.name(),
                        trackedEntity.displayName(),
                        trackedEntity.created(),
                        trackedEntity.lastUpdated(),
                        trackedEntity.shortName(),
                        trackedEntity.displayShortName(),
                        trackedEntity.description(),
                        trackedEntity.displayDescription()
                );
            }
        }
    }

    //TODO: use these from the stores when implemented:
    private void updateInResourceStore(Date serverDate, String className) {
        int rowId = resourceStore.update(className, serverDate,
                OrganisationUnit.class.getSimpleName());
        if (rowId <= 0) {
            resourceStore.insert(OrganisationUnit.class.getSimpleName(), serverDate);
        }
    }

    private String getLastUpdated(String className) {
        String lastUpdated = null;
        Cursor cursor = database.query(
                ResourceModel.TABLE,
                new String[]{ResourceModel.Columns.LAST_SYNCED},
                ResourceModel.Columns.RESOURCE_TYPE + "=?",
                new String[]{className},
                null, null, null
        );
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                lastUpdated = cursor.getString(cursor.getColumnIndex(ResourceModel.Columns.LAST_SYNCED));
            }
            cursor.close();
        }
        return lastUpdated;
    }
}