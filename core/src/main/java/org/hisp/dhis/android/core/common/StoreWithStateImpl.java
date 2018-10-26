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

package org.hisp.dhis.android.core.common;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel.Columns;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

public class StoreWithStateImpl implements StoreWithState {

    private final String selectStateQuery;
    private final String existsQuery;
    private final SQLiteStatement setStateStatement;
    protected final DatabaseAdapter databaseAdapter;

    public StoreWithStateImpl(DatabaseAdapter databaseAdapter, String tableName) {
        this.databaseAdapter = databaseAdapter;

        String setStateUpdate = "UPDATE " + tableName + " SET " +
                Columns.STATE + " =?" +
                " WHERE " +
                Columns.UID + " =?;";
        this.setStateStatement = databaseAdapter.compileStatement(setStateUpdate);


        this.selectStateQuery = "SELECT state FROM " + tableName + " WHERE " + Columns.UID + " =?;";
        this.existsQuery = "SELECT 1 FROM " + tableName + " WHERE " + Columns.UID + " =?;";

    }

    @Override
    public int setState(@NonNull String uid, @NonNull State state) {
        sqLiteBind(setStateStatement, 1, state);

        // bind the where argument
        sqLiteBind(setStateStatement, 2, uid);

        int updatedRow = databaseAdapter.executeUpdateDelete(TrackedEntityInstanceModel.TABLE, setStateStatement);
        setStateStatement.clearBindings();

        return updatedRow;
    }

    @Override
    public State getState(@NonNull String uid) {
        Cursor cursor = databaseAdapter.query(selectStateQuery, uid);
        State state = null;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            state = cursor.getString(0) == null ? null :
                    State.valueOf(State.class, cursor.getString(0));
        }
        return state;
    }

    @Override
    public Boolean exists(@NonNull String uid) {
        Cursor cursor = databaseAdapter.query(existsQuery, uid);
        return cursor.getCount() > 0;
    }
}
