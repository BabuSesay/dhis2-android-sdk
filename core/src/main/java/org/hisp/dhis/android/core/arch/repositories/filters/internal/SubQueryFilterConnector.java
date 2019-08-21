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

import org.hisp.dhis.android.core.arch.db.querybuilders.internal.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.repositories.collection.BaseRepository;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.BaseRepositoryFactory;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;

import java.util.List;

public final class SubQueryFilterConnector<R extends BaseRepository>
        extends BaseSqlFilterConnector<R, String> {

    SubQueryFilterConnector(BaseRepositoryFactory<R> repositoryFactory,
                            RepositoryScope scope,
                            String key) {
        super(repositoryFactory, scope, key);
    }

    String wrapValue(String value) {
        return value;
    }

    public R inLinkTable(String linkTable, String linkParent, String linkChild, List<String> children) {
        WhereClauseBuilder clauseBuilder = new WhereClauseBuilder().appendInKeyStringValues(linkChild, children);

        return newWithWrappedScope("IN", "(" + String.format(
                "SELECT DISTINCT %s FROM %s WHERE %s", linkParent, linkTable, clauseBuilder.build()) + ")");
    }

    @SuppressWarnings({"PMD.AvoidInstantiatingObjectsInLoops"})
    public R withThoseChildrenExactly(String linkTable, String linkParent, String linkChild, List<String> children) {
        RepositoryScope repositoryScope = null;

        for (String child : children) {
            String clause = new WhereClauseBuilder().appendKeyStringValue(linkChild, child).build();
            String value = "(" + String.format("SELECT %s FROM %s WHERE %s ", linkParent, linkTable, clause) + ")";

            repositoryScope = repositoryScope == null ? updatedUnwrappedScope("IN", value) :
                    updatePassedScope("IN", value, repositoryScope);
        }

        return newWithPassedScope("IN", "(" + String.format(
                "SELECT %s FROM %s WHERE 1 GROUP BY %s HAVING COUNT(*) = %s ",
                linkParent, linkTable, linkParent, children.size()) + ")", repositoryScope);
    }
}