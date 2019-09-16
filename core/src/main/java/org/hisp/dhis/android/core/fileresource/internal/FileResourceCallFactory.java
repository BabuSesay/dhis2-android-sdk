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

package org.hisp.dhis.android.core.fileresource.internal;

import android.content.Context;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.handlers.internal.HandlerWithTransformer;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.fileresource.FileResource;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import dagger.Reusable;
import okhttp3.ResponseBody;

@Reusable
class FileResourceCallFactory {

    private final FileResourceService fileResourceService;
    private final HandlerWithTransformer<FileResource> handler;
    private final APICallExecutor apiCallExecutor;
    private final Context context;

    private enum Dimension {
        SMALL, MEDIUM
    }

    @Inject
    FileResourceCallFactory(@NonNull FileResourceService fileResourceService,
                            @NonNull HandlerWithTransformer<FileResource> handler,
                            @NonNull APICallExecutor apiCallExecutor,
                            @NonNull Context context) {
        this.fileResourceService = fileResourceService;
        this.handler = handler;
        this.apiCallExecutor = apiCallExecutor;
        this.context = context;
    }

    public Callable<Unit> create(final List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
                                 final List<TrackedEntityDataValue> trackedEntityDataValues) {

        return () -> {
            downloadFileResources(trackedEntityAttributeValues, trackedEntityDataValues);
            downloadFiles(trackedEntityAttributeValues, trackedEntityDataValues);

            return new Unit();
        };
    }

    private void downloadFileResources(final List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
                                       final List<TrackedEntityDataValue> trackedEntityDataValues) throws D2Error {
        List<FileResource> fileResources = new ArrayList<>();

        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            fileResources.add(apiCallExecutor.executeObjectCall(
                    fileResourceService.getFileResource(trackedEntityAttributeValue.value())));
        }

        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
            fileResources.add(apiCallExecutor.executeObjectCall(
                    fileResourceService.getFileResource(trackedEntityDataValue.value())));
        }

        handler.handleMany(fileResources, fileResource -> fileResource.toBuilder()
                .path(FileResourceUtil.getFileResourceDirectory(context).getPath())
                .state(State.SYNCED)
                .build());
    }

    private void downloadFiles(final List<TrackedEntityAttributeValue> trackedEntityAttributeValues,
                               final List<TrackedEntityDataValue> trackedEntityDataValues) throws D2Error {
        for (TrackedEntityAttributeValue trackedEntityAttributeValue : trackedEntityAttributeValues) {
            ResponseBody responseBody = apiCallExecutor.executeObjectCall(
                    fileResourceService.getFileFromTrackedEntityAttribute(
                            trackedEntityAttributeValue.trackedEntityInstance(),
                            trackedEntityAttributeValue.trackedEntityAttribute(),
                            Dimension.MEDIUM.name()));

            FileResourceUtil.saveFileFromResponse(responseBody, trackedEntityAttributeValue.value(), context);
        }

        for (TrackedEntityDataValue trackedEntityDataValue : trackedEntityDataValues) {
            ResponseBody responseBody = apiCallExecutor.executeObjectCall(fileResourceService.getFileFromDataElement(
                    trackedEntityDataValue.event(),
                    trackedEntityDataValue.dataElement(),
                    Dimension.MEDIUM.name()));

            FileResourceUtil.saveFileFromResponse(responseBody, trackedEntityDataValue.value(), context);
        }
    }
}