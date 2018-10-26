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

package org.hisp.dhis.android.core.datavalue;

import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyCollectionRepositoryImpl;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadWriteCollectionRepository;
import org.hisp.dhis.android.core.common.D2CallException;
import org.hisp.dhis.android.core.common.D2ErrorCode;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;

final class DataValueCollectionRepository extends ReadOnlyCollectionRepositoryImpl<DataValue>
        implements ReadWriteCollectionRepository<DataValue> {

    private final DataValueStore dataValueStore;
    private final DataValueHandler dataValueHandler;

    private DataValueCollectionRepository(DataValueStore dataValueStore,
                                          DataValueHandler dataValueHandler) {
        super(dataValueStore);
        this.dataValueHandler = dataValueHandler;
        this.dataValueStore = dataValueStore;
    }

    static DataValueCollectionRepository create(DatabaseAdapter databaseAdapter) {

        return new DataValueCollectionRepository(DataValueStore.create(databaseAdapter),
                DataValueHandler.create(databaseAdapter));

    }

    @Override
    public void add(DataValue dataValue) throws D2CallException {

        if (dataValueStore.exists(dataValue)) {
            throw D2CallException
                    .builder()
                    .isHttpError(false)
                    .errorCode(D2ErrorCode.CANT_CREATE_EXISTING_OBJECT)
                    .errorDescription("Tried to create already existing DataValue: " + dataValue)
                    .build();
        }

        dataValueHandler.handle(dataValue.toBuilder().state(State.TO_POST).build());
    }

}
