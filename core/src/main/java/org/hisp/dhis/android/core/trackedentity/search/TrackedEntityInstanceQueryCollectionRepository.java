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

import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppenderExecutor;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenSelection;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyCollectionRepository;
import org.hisp.dhis.android.core.arch.repositories.collection.internal.BaseRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.FilterConnectorFactory;
import org.hisp.dhis.android.core.arch.repositories.filters.internal.QueryItemFilterConnector;
import org.hisp.dhis.android.core.arch.repositories.object.ReadOnlyObjectRepository;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.arch.repositories.scope.internal.RepositoryMode;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceFields;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceStore;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import dagger.Reusable;
import io.reactivex.Single;

import static org.hisp.dhis.android.core.trackedentity.search.TrackedEntityInstanceQuery.QueryParams;

@Reusable
public final class TrackedEntityInstanceQueryCollectionRepository
        implements ReadOnlyCollectionRepository<TrackedEntityInstance> {

    private final RepositoryScope scope;

    private final TrackedEntityInstanceQueryFilterConnectorFactory connectorFactory;

    private final TrackedEntityInstanceStore store;
    private final TrackedEntityInstanceQueryCallFactory onlineCallFactory;
    private final Map<String, ChildrenAppender<TrackedEntityInstance>> childrenAppenders;

    @Inject
    public TrackedEntityInstanceQueryCollectionRepository(
            final RepositoryScope scope,
            final TrackedEntityInstanceStore store,
            final Map<String, ChildrenAppender<TrackedEntityInstance>> childrenAppenders,
            final TrackedEntityInstanceQueryCallFactory onlineCallFactory,
            final TrackedEntityInstanceQueryFilterConnectorFactory connectorFactory) {
        this.scope = scope;
        this.store = store;
        this.childrenAppenders = childrenAppenders;
        this.onlineCallFactory = onlineCallFactory;
        this.connectorFactory = connectorFactory;
    }

    /**
     * Only TrackedEntityInstances coming from the server are shown in the list.
     * <br><b>Important:</b> Internet connection is required to use this mode.
     *
     * @return
     */
    public TrackedEntityInstanceQueryCollectionRepository onlineOnly() {
        return cf.baseString(QueryParams.QUERY_SCOPE).eq(RepositoryMode.ONLINE_ONLY.name());
    }

    /**
     * Only TrackedEntityInstances coming from local database are shown in the list.
     *
     * @return
     */
    public TrackedEntityInstanceQueryCollectionRepository offlineOnly() {
        return cf.baseString(QueryParams.QUERY_SCOPE).eq(RepositoryMode.OFFLINE_ONLY.name());
    }

    /**
     * TrackedEntityInstances coming from the server are shown in first place. Once there are no more results online,
     * it continues with TrackedEntityInstances in local database.
     * <br><b>Important:</b> Internet connection is required to use this mode.
     *
     * @return
     */
    public TrackedEntityInstanceQueryCollectionRepository onlineFirst() {
        return cf.baseString(QueryParams.QUERY_SCOPE).eq(RepositoryMode.ONLINE_FIRST.name());
    }

    /**
     * TrackedEntityInstances coming from local database are shown in first place. Once there are no more results, it
     * continues with TrackedEntityInstances coming from the server. This method may speed up the initial load.
     * <br><b>Important:</b> Internet connection is required to use this mode.
     *
     * @return
     */
    public TrackedEntityInstanceQueryCollectionRepository offlineFirst() {
        return new TrackedEntityInstanceQueryCollectionRepository(store, onlineCallFactory, childrenAppenders,
                scope.toBuilder().mode(RepositoryMode.OFFLINE_FIRST).build());
    }

    public QueryItemFilterConnector<TrackedEntityInstanceQueryCollectionRepository> query() {
        return cf.query();
    }

    @Override
    public LiveData<PagedList<TrackedEntityInstance>> getPaged(int pageSize) {
        DataSource.Factory<TrackedEntityInstance, TrackedEntityInstance> factory =
                new DataSource.Factory<TrackedEntityInstance, TrackedEntityInstance>() {
                    @Override
                    public DataSource<TrackedEntityInstance, TrackedEntityInstance> create() {
                        return getDataSource();
                    }
                };

        return new LivePagedListBuilder<>(factory, pageSize).build();
    }

    public DataSource<TrackedEntityInstance, TrackedEntityInstance> getDataSource() {
        return new TrackedEntityInstanceQueryDataSource(store, onlineCallFactory, scope, childrenAppenders);
    }

    @Override
    public List<TrackedEntityInstance> blockingGet() {
        if (scope.mode().equals(RepositoryMode.OFFLINE_ONLY) || scope.mode().equals(RepositoryMode.OFFLINE_FIRST)) {
            String sqlQuery = TrackedEntityInstanceLocalQueryHelper.getSqlQuery(scope.query(), Collections.emptyList(),
                    -1);
            List<TrackedEntityInstance> instances = store.selectRawQuery(sqlQuery);
            return ChildrenAppenderExecutor.appendInObjectCollection(instances, childrenAppenders,
                    new ChildrenSelection(Collections.singleton(
                            TrackedEntityInstanceFields.TRACKED_ENTITY_ATTRIBUTE_VALUES), false));
        } else {
            try {
                TrackedEntityInstanceQuery noPagingQuery = scope.query().toBuilder().paging(false).build();
                return onlineCallFactory.getCall(noPagingQuery).call();
            } catch (D2Error e) {
                return Collections.emptyList();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }
    }

    @Override
    public Single<List<TrackedEntityInstance>> get() {
        return Single.fromCallable(this::blockingGet);
    }

    @Override
    public Single<Integer> count() {
        return Single.fromCallable(this::blockingCount);
    }

    @Override
    public int blockingCount() {
        return blockingGet().size();
    }

    @Override
    public Single<Boolean> isEmpty() {
        return Single.fromCallable(this::blockingIsEmpty);
    }

    @Override
    public boolean blockingIsEmpty() {
        return blockingCount() == 0;
    }

    @Override
    public ReadOnlyObjectRepository<TrackedEntityInstance> one() {
        return new ReadOnlyObjectRepository<TrackedEntityInstance>() {

            @Override
            public Single<TrackedEntityInstance> get() {
                return Single.fromCallable(this::blockingGet);
            }

            @Override
            public TrackedEntityInstance blockingGet() {
                List<TrackedEntityInstance> list = TrackedEntityInstanceQueryCollectionRepository.this.blockingGet();
                return list.isEmpty() ? null : list.get(0);
            }

            @Override
            public Single<Boolean> exists() {
                return Single.fromCallable(this::blockingExists);
            }

            @Override
            public boolean blockingExists() {
                return blockingGet() != null;
            }
        };
    }
}
