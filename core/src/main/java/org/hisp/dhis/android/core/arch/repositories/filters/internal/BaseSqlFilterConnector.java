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

package org.hisp.dhis.android.core.arch.repositories.filters.internal;

import org.hisp.dhis.android.core.arch.repositories.collection.BaseRepository;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.BaseRepositoryFactory;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;

import java.util.Arrays;
import java.util.Collection;

abstract class BaseSqlFilterConnector<R extends BaseRepository, V> extends BaseFilterConnector<R, V> {

    BaseSqlFilterConnector(BaseRepositoryFactory<R> repositoryFactory,
                           RepositoryScope scope,
                           String key) {
        super(repositoryFactory, scope, key);
    }

    public R eq(V value) {
        return newWithWrappedScope("=", value);
    }

    public R neq(V value) {
        return newWithWrappedScope("!=", value);
    }

    public R in(Collection<V> values) {
        return newWithUnwrappedScope("IN", "(" + getCommaSeparatedValues(values) + ")");
    }

    @SafeVarargs
    public final R in(V... values) {
        return in(Arrays.asList(values));
    }

    public R notIn(Collection<V> values) {
        return newWithUnwrappedScope("NOT IN", "(" + getCommaSeparatedValues(values) + ")");
    }

    @SafeVarargs
    public final R notIn(V... values) {
        return notIn(Arrays.asList(values));
    }

    public final R isNull() {
        return newWithUnwrappedScope("", "IS NULL");
    }

    public final R isNotNull() {
        return newWithUnwrappedScope("", "IS NOT NULL");
    }

}
