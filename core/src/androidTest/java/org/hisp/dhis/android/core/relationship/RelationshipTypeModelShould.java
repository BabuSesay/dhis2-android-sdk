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

import android.content.ContentValues;
import android.database.MatrixCursor;

import androidx.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.common.Access;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.relationship.RelationshipTypeTableInfo.Columns;
import org.hisp.dhis.android.core.utils.Utils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static com.google.common.truth.Truth.assertThat;
import static org.hisp.dhis.android.core.AndroidTestUtils.toBoolean;

@RunWith(AndroidJUnit4.class)
public class RelationshipTypeModelShould {
    //BaseIdentifiableModel attributes:
    private static final long ID = 11L;
    private static final String UID = "test_uid";
    private static final String CODE = "test_code";
    private static final String NAME = "test_name";
    private static final String DISPLAY_NAME = "test_display_name";
    //RelationshipTypeModel attributes:
    private static final String TO_FROM_NAME = "cat of";
    private static final String FROM_TO_NAME = "owner of";
    private static final Integer BIDIRECTIONAL = 1;
    private static final Integer ACCESS_DATA_WRITE = 1;
    private final Date date = new Date();
    private final String dateString = BaseIdentifiableObject.DATE_FORMAT.format(date);

    private RelationshipType expectedType = RelationshipType.builder()
            .id(ID)
            .uid(UID)
            .code(CODE)
            .name(NAME)
            .displayName(DISPLAY_NAME)
            .created(date)
            .lastUpdated(date)
            .toFromName(TO_FROM_NAME)
            .fromToName(FROM_TO_NAME)
            .bidirectional(toBoolean(BIDIRECTIONAL))
            .access(Access.createForDataWrite(toBoolean(ACCESS_DATA_WRITE)))
            .build();

    @Test
    public void create_model_when_created_from_database_cursor() {
        String[] columnsWithId = Utils.appendInNewArray(new RelationshipTypeTableInfo.Columns().all(),
                RelationshipTypeTableInfo.Columns.ID);
        MatrixCursor cursor = new MatrixCursor(columnsWithId);
        cursor.addRow(new Object[]{UID, CODE, NAME, DISPLAY_NAME, dateString, dateString,
                FROM_TO_NAME, TO_FROM_NAME, BIDIRECTIONAL, ACCESS_DATA_WRITE, ID});

        cursor.moveToFirst();
        RelationshipType typeFromDb = RelationshipType.create(cursor);
        cursor.close();

        assertThat(typeFromDb).isEqualTo(expectedType);
    }

    @Test
    public void create_content_values_when_created_from_builder() {
        ContentValues contentValues = expectedType.toContentValues();

        assertThat(contentValues.getAsLong(Columns.ID)).isEqualTo(ID);
        assertThat(contentValues.getAsString(Columns.UID)).isEqualTo(UID);
        assertThat(contentValues.getAsString(Columns.NAME)).isEqualTo(NAME);
        assertThat(contentValues.getAsString(Columns.DISPLAY_NAME)).isEqualTo(DISPLAY_NAME);
        assertThat(contentValues.getAsString(Columns.CREATED)).isEqualTo(dateString);
        assertThat(contentValues.getAsString(Columns.LAST_UPDATED)).isEqualTo(dateString);
        assertThat(contentValues.getAsString(Columns.FROM_TO_NAME)).isEqualTo(FROM_TO_NAME);
        assertThat(contentValues.getAsString(Columns.TO_FROM_NAME)).isEqualTo(TO_FROM_NAME);
        assertThat(contentValues.getAsBoolean(Columns.BIDIRECTIONAL)).isEqualTo(toBoolean(BIDIRECTIONAL));
        assertThat(contentValues.getAsBoolean(Columns.ACCESS_DATA_WRITE)).isEqualTo(toBoolean(ACCESS_DATA_WRITE));
    }
}

