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

package org.hisp.dhis.android.core.relationship;

import android.database.Cursor;
import net.sqlcipher.database.SQLiteStatement;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.arch.db.binders.StatementBinder;
import org.hisp.dhis.android.core.arch.db.binders.WhereStatementBinder;
import org.hisp.dhis.android.core.common.BaseModel;
import org.hisp.dhis.android.core.common.ObjectWithoutUidStoreImpl;
import org.hisp.dhis.android.core.common.SQLStatementBuilder;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.hisp.dhis.android.core.utils.StoreUtils.sqLiteBind;

final class RelationshipItemStore extends ObjectWithoutUidStoreImpl<RelationshipItemModel>
        implements RelationshipItemStoreInterface {

    private RelationshipItemStore(DatabaseAdapter databaseAdapter,
                                  SQLiteStatement insertStatement,
                                  SQLiteStatement updateWhereStatement,
                                  SQLStatementBuilder builder,
                                  StatementBinder<RelationshipItemModel> binder,
                                  WhereStatementBinder<RelationshipItemModel> whereBinder) {
        super(databaseAdapter, insertStatement, updateWhereStatement, builder, binder, whereBinder);
    }

    @Override
    public List<String> getRelationshipsFromAndToTEI(@NonNull String fromTEI, @NonNull String toTEI) {
        String query = "SELECT " + RelationshipItemModel.Columns.RELATIONSHIP + ", " +
                        "MAX(CASE WHEN " + RelationshipItemModel.Columns.RELATIONSHIP_ITEM_TYPE + " = 'FROM' " +
                            "THEN " + RelationshipItemModel.Columns.TRACKED_ENTITY_INSTANCE + " END) AS fromTEI, " +
                        "MAX(CASE WHEN " + RelationshipItemModel.Columns.RELATIONSHIP_ITEM_TYPE + " = 'TO' " +
                            "THEN " + RelationshipItemModel.Columns.TRACKED_ENTITY_INSTANCE + " END) AS toTEI " +
                        "FROM " + RelationshipItemModel.TABLE +
                        " GROUP BY " + RelationshipItemModel.Columns.RELATIONSHIP;

        Cursor cursor = this.databaseAdapter.query(query);

        List<String> relationships = new ArrayList<>();
        try {
            if(cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    String relationshipInDB = cursor.getString(0);
                    String fromTEIInDb = cursor.getString(1);
                    String toTEIInDB = cursor.getString(2);

                    if (fromTEI.equals(fromTEIInDb) && toTEI.equals(toTEIInDB)) {
                        relationships.add(relationshipInDB);
                    }
                }
                while(cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }

        return relationships;
    }

    private static final StatementBinder<RelationshipItemModel> BINDER = new StatementBinder<RelationshipItemModel>() {
        @Override
        public void bindToStatement(@NonNull RelationshipItemModel o, @NonNull SQLiteStatement sqLiteStatement) {
            sqLiteBind(sqLiteStatement, 1, o.relationship());
            sqLiteBind(sqLiteStatement, 2, o.relationshipItemType());
            sqLiteBind(sqLiteStatement, 3, o.trackedEntityInstance());
            sqLiteBind(sqLiteStatement, 4, o.enrollment());
            sqLiteBind(sqLiteStatement, 5, o.event());
        }
    };


    private static final WhereStatementBinder<RelationshipItemModel> WHERE_UPDATE_BINDER
            = new WhereStatementBinder<RelationshipItemModel>() {
        @Override
        public void bindToUpdateWhereStatement(@NonNull RelationshipItemModel o,
                                               @NonNull SQLiteStatement sqLiteStatement) {
            sqLiteBind(sqLiteStatement, 6, o.relationship());
            sqLiteBind(sqLiteStatement, 7, o.relationshipItemType());
        }
    };

    public static RelationshipItemStoreInterface create(DatabaseAdapter databaseAdapter) {
        BaseModel.Columns columns = new RelationshipItemModel.Columns();

        SQLStatementBuilder statementBuilder = new SQLStatementBuilder(
                RelationshipItemModel.TABLE, columns);

        return new RelationshipItemStore(
                databaseAdapter,
                databaseAdapter.compileStatement(statementBuilder.insert()),
                databaseAdapter.compileStatement(statementBuilder.updateWhere()),
                statementBuilder,
                BINDER,
                WHERE_UPDATE_BINDER
        );
    }
}
