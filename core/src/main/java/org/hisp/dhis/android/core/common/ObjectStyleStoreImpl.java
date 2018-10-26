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

import org.hisp.dhis.android.core.arch.db.TableInfo;
import org.hisp.dhis.android.core.arch.db.WhereClauseBuilder;
import org.hisp.dhis.android.core.arch.db.binders.StatementBinder;
import org.hisp.dhis.android.core.arch.db.binders.WhereStatementBinder;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

public final class ObjectStyleStoreImpl extends ObjectWithoutUidStoreImpl<ObjectStyle>
        implements ObjectStyleStore {

    private ObjectStyleStoreImpl(DatabaseAdapter databaseAdapter,
                                 SQLiteStatement insertStatement,
                                 SQLiteStatement updateWhereStatement,
                                 SQLStatementBuilder builder,
                                 StatementBinder<ObjectStyle> binder,
                                 WhereStatementBinder<ObjectStyle> whereBinder,
                                 CursorModelFactory<ObjectStyle> modelFactory) {
        super(databaseAdapter, insertStatement, updateWhereStatement, builder, binder, whereBinder, modelFactory);
    }

    @Override
    public <O extends ObjectWithStyle<?, ?> & ObjectWithUidInterface> ObjectStyle getStyle(O objectWithStyle,
                                                                                           TableInfo tableInfo) {
        String whereClause = new WhereClauseBuilder()
                .appendKeyStringValue(ObjectStyleTableInfo.Columns.OBJECT_TABLE, tableInfo.name())
                .appendKeyStringValue(ObjectStyleTableInfo.Columns.UID, objectWithStyle.uid())
                .build();
        return selectOneWhere(whereClause);
    }

    private static final StatementBinder<ObjectStyle> BINDER = new StatementBinder<ObjectStyle>() {
        @Override
        public void bindToStatement(@NonNull ObjectStyle o, @NonNull SQLiteStatement sqLiteStatement) {
            sqLiteBind(sqLiteStatement, 1, o.uid());
            sqLiteBind(sqLiteStatement, 2, o.objectTable());
            sqLiteBind(sqLiteStatement, 3, o.color());
            sqLiteBind(sqLiteStatement, 4, o.icon());
        }
    };

    private static final WhereStatementBinder<ObjectStyle> WHERE_UPDATE_BINDER
            = new WhereStatementBinder<ObjectStyle>() {
        @Override
        public void bindToUpdateWhereStatement(@NonNull ObjectStyle o, @NonNull SQLiteStatement sqLiteStatement) {
            sqLiteBind(sqLiteStatement, 5, o.uid());
        }
    };

    private static final CursorModelFactory<ObjectStyle> FACTORY = new CursorModelFactory<ObjectStyle>() {
        @Override
        public ObjectStyle fromCursor(Cursor cursor) {
            return ObjectStyle.create(cursor);
        }
    };

    public static ObjectStyleStore create(DatabaseAdapter databaseAdapter) {

        BaseModel.Columns columns = ObjectStyleTableInfo.TABLE_INFO.columns();

        SQLStatementBuilder statementBuilder = new SQLStatementBuilder(
                ObjectStyleTableInfo.TABLE_INFO.name(), columns);

        return new ObjectStyleStoreImpl(databaseAdapter,
                databaseAdapter.compileStatement(statementBuilder.insert()),
                databaseAdapter.compileStatement(statementBuilder.updateWhere()),
                statementBuilder,
                BINDER,
                WHERE_UPDATE_BINDER,
                FACTORY);
    }
}