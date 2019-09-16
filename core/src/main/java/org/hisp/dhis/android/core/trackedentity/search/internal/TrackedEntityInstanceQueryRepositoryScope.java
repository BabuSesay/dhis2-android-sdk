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

package org.hisp.dhis.android.core.trackedentity.search.internal;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryMode;
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQuery;

import androidx.annotation.NonNull;

@AutoValue
public abstract class TrackedEntityInstanceQueryRepositoryScope {

    @NonNull
    public abstract RepositoryMode mode();

    @NonNull
    public abstract TrackedEntityInstanceQuery query();

    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_TrackedEntityInstanceQueryRepositoryScope.Builder()
                .mode(RepositoryMode.OFFLINE_ONLY)
                .query(TrackedEntityInstanceQuery.empty());
    }

    public static TrackedEntityInstanceQueryRepositoryScope empty() {
        return builder().build();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder mode(RepositoryMode mode);

        public abstract Builder query(TrackedEntityInstanceQuery query);

        abstract TrackedEntityInstanceQueryRepositoryScope autoBuild();

        // Auxiliary fields to access values
        abstract TrackedEntityInstanceQuery query();

        public TrackedEntityInstanceQueryRepositoryScope build() {
            if (query() != null && query().states() != null) {
                mode(RepositoryMode.OFFLINE_ONLY);
            }

            return autoBuild();
        }
    }
}