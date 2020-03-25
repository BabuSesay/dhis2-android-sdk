/*
 * Copyright (c) 2004-2019, University of Oslo
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

package org.hisp.dhis.android.core.trackedentity.internal;

import org.hisp.dhis.android.core.arch.api.payload.internal.Payload;
import org.hisp.dhis.android.core.arch.helpers.CollectionsHelper;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import dagger.Reusable;
import io.reactivex.Single;

@Reusable
final class TrackedEntityInstancesEndpointCallFactory {

    private final TrackedEntityInstanceService trackedEntityInstanceService;

    @Inject
    TrackedEntityInstancesEndpointCallFactory(
            @NonNull TrackedEntityInstanceService trackedEntityInstanceService) {
        this.trackedEntityInstanceService = trackedEntityInstanceService;
    }

    Single<Payload<TrackedEntityInstance>> getCall(final TeiQuery query) {
        String uidStr = query.uids().isEmpty() ? null :
                CollectionsHelper.joinCollectionWithSeparator(query.uids(), ";");
        String ouStr = query.orgUnits().isEmpty() ? null :
                CollectionsHelper.joinCollectionWithSeparator(query.orgUnits(), ";");

        String programStatus = query.programStatus() == null ? null : query.programStatus().toString();

        return trackedEntityInstanceService.getTrackedEntityInstances(uidStr, ouStr,
                query.ouMode().name(), query.program(), programStatus, query.programStartDate(),
                TrackedEntityInstanceFields.allFields, Boolean.TRUE, query.page(), query.pageSize(),
                query.lastUpdatedStartDate(), true, true);
    }
}