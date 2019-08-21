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

package org.hisp.dhis.android.core.trackedentity.search;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.arch.call.queries.internal.BaseQuery;
import org.hisp.dhis.android.core.arch.dateformat.internal.SafeDateFormat;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@AutoValue
public abstract class TrackedEntityInstanceQuery extends BaseQuery {

    private static final SafeDateFormat QUERY_FORMAT = new SafeDateFormat("yyyy-MM-dd");

    @NonNull
    public abstract List<String> orgUnits();

    @Nullable
    public abstract OrganisationUnitMode orgUnitMode();

    @Nullable
    public abstract String program();

    @Nullable
    public abstract QueryFilter query();

    @NonNull
    public abstract List<QueryItem> attribute();

    @NonNull
    public abstract List<QueryItem> filter();

    @Nullable
    public abstract Date programStartDate();

    @Nullable
    public abstract Date programEndDate();

    @Nullable
    public abstract String trackedEntityType();

    public String formattedProgramStartDate() {
        return programStartDate() == null ? null : QUERY_FORMAT.format(programStartDate());
    }

    public String formattedProgramEndDate() {
        return programEndDate() == null ? null : QUERY_FORMAT.format(programEndDate());
    }

    public static Builder builder() {
        return new AutoValue_TrackedEntityInstanceQuery.Builder()
                .attribute(Collections.<QueryItem>emptyList())
                .filter(Collections.emptyList())
                .orgUnits(Collections.emptyList());
    }

    public abstract Builder toBuilder();

    public static TrackedEntityInstanceQuery empty() {
        return builder().page(1).pageSize(DEFAULT_PAGE_SIZE).paging(true).build();
    }

    @AutoValue.Builder
    public abstract static class Builder extends BaseQuery.Builder<Builder> {
        public abstract Builder orgUnits(List<String> orgUnits);

        public abstract Builder orgUnitMode(OrganisationUnitMode orgUnitMode);

        public abstract Builder program(String program);

        public abstract Builder query(QueryFilter query);

        public abstract Builder attribute(List<QueryItem> attribute);

        public abstract Builder filter(List<QueryItem> filter);

        public abstract Builder programStartDate(Date programStartDate);

        public abstract Builder programEndDate(Date programEndDate);

        public abstract Builder trackedEntityType(String trackedEntityType);

        public abstract TrackedEntityInstanceQuery build();
    }

    public static class QueryParams {
        public static final String QUERY_SCOPE = "queryScope";
        public static final String FILTER = "filter";
        public static final String ATTRIBUTE = "attribute";
        public static final String PROGRAM = "program";
        public static final String PROGRAM_STATUS = "programStatus";
        public static final String PROGRAM_START_DATE = "programStartDate";
        public static final String PROGRAM_END_DATE = "programEndDate";
        public static final String ORG_UNITS = "ou";
        public static final String ORG_UNIT_MODE = "ouMode";
        public static final String TRACKED_ENTITY_TYPE = "trackedEntityType";
    }
}