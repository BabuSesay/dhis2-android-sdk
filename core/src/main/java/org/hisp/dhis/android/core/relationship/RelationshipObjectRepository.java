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

package org.hisp.dhis.android.core.relationship;

import android.util.Log;

import org.hisp.dhis.android.core.arch.db.stores.internal.StoreWithState;
import org.hisp.dhis.android.core.arch.repositories.children.internal.ChildrenAppender;
import org.hisp.dhis.android.core.arch.repositories.object.ReadWriteObjectRepository;
import org.hisp.dhis.android.core.arch.repositories.object.internal.ReadOnlyOneObjectRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent;
import org.hisp.dhis.android.core.relationship.internal.RelationshipItemElementStoreSelector;
import org.hisp.dhis.android.core.relationship.internal.RelationshipStore;

import java.util.Map;

import io.reactivex.Completable;

final class RelationshipObjectRepository
        extends ReadOnlyOneObjectRepositoryImpl<Relationship, RelationshipObjectRepository>
        implements ReadWriteObjectRepository<Relationship> {

    private final RelationshipStore relationshipStore;
    private final RelationshipItemElementStoreSelector storeSelector;
    private final String uid;

    RelationshipObjectRepository(final RelationshipStore store,
                                 final String uid,
                                 final Map<String, ChildrenAppender<Relationship>> childrenAppenders,
                                 final RepositoryScope scope,
                                 final RelationshipItemElementStoreSelector storeSelector) {
        super(store, childrenAppenders, scope,
                s -> new RelationshipObjectRepository(store, uid, childrenAppenders, s, storeSelector));
        this.relationshipStore = store;
        this.storeSelector = storeSelector;
        this.uid = uid;
    }

    @Override
    public Completable delete() {
        return Completable.fromAction(this::blockingDelete);
    }

    @Override
    public void blockingDelete() throws D2Error {
        Relationship relationship = blockingGet();
        if (relationship == null) {
            throw D2Error
                    .builder()
                    .errorComponent(D2ErrorComponent.SDK)
                    .errorCode(D2ErrorCode.CANT_DELETE_NON_EXISTING_OBJECT)
                    .errorDescription("Tried to delete non existing relationship")
                    .build();
        } else {
            RelationshipItem fromItem = relationship.from();
            StoreWithState elementStore = storeSelector.getElementStore(fromItem);
            relationshipStore.delete(uid);
            elementStore.setState(fromItem.elementUid(), State.TO_UPDATE);
        }
    }

    @Override
    public Completable deleteIfExist() {
        return Completable.fromAction(this::blockingDeleteIfExist);
    }

    @Override
    public void blockingDeleteIfExist() {
        try {
            blockingDelete();
        } catch (D2Error d2Error) {
            Log.v(RelationshipObjectRepository.class.getCanonicalName(), d2Error.errorDescription());

        }
    }
}