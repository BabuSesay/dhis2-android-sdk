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

package org.hisp.dhis.android.core.trackedentity.internal;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.test.runner.AndroidJUnit4;

import org.hisp.dhis.android.core.BaseRealIntegrationTest;
import org.hisp.dhis.android.core.common.DataColumns;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.IdentifiableColumns;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.data.organisationunit.OrganisationUnitSamples;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitTableInfo;
import org.hisp.dhis.android.core.trackedentity.CreateTrackedEntityUtils;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo.Columns;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeTableInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;
import static org.hisp.dhis.android.core.data.database.CursorAssert.assertThatCursor;

@RunWith(AndroidJUnit4.class)
public class TrackedEntityInstanceStoreShould extends BaseRealIntegrationTest {
    private static final String UID = "test_uid";
    private static final String ORGANISATION_UNIT = "test_organisationUnit";
    private static final String TRACKED_ENTITY = "test_trackedEntity";
    private static final FeatureType GEOMETRY_TYPE = FeatureType.POINT;
    private static final String GEOMETRY_COORDINATES = "[10.02, 9.87]";
    private static final State STATE = State.ERROR;
    private static final String CREATED_AT_CLIENT = "2016-04-28T23:44:28.126";
    private static final String LAST_UPDATED_AT_CLIENT = "2016-04-28T23:44:28.126";

    private final Date date;

    private TrackedEntityInstance trackedEntityInstance;
    private TrackedEntityInstanceStore trackedEntityInstanceStore;

    public TrackedEntityInstanceStoreShould() {
        this.date = new Date();
    }

    private static final String[] PROJECTION = {
            Columns.UID,
            Columns.CREATED,
            Columns.LAST_UPDATED,
            Columns.CREATED_AT_CLIENT,
            Columns.LAST_UPDATED_AT_CLIENT,
            Columns.ORGANISATION_UNIT,
            Columns.TRACKED_ENTITY_TYPE,
            Columns.GEOMETRY_TYPE,
            Columns.GEOMETRY_COORDINATES,
            DataColumns.STATE
    };

    @Before
    @Override
    public void setUp() throws IOException {
        super.setUp();

        trackedEntityInstanceStore = TrackedEntityInstanceStoreImpl.create(databaseAdapter());
        OrganisationUnit organisationUnit = OrganisationUnitSamples.getOrganisationUnit(ORGANISATION_UNIT);
        ContentValues trackedEntityType = CreateTrackedEntityUtils.create(1L, TRACKED_ENTITY);
        databaseAdapter().insert(OrganisationUnitTableInfo.TABLE_INFO.name(), null, organisationUnit.toContentValues());
        databaseAdapter().insert(TrackedEntityTypeTableInfo.TABLE_INFO.name(), null, trackedEntityType);
        trackedEntityInstance = TrackedEntityInstance.builder()
                .uid(UID)
                .created(date)
                .lastUpdated(date)
                .createdAtClient(date)
                .lastUpdatedAtClient(date)
                .organisationUnit(ORGANISATION_UNIT)
                .trackedEntityType(TRACKED_ENTITY)
                .geometry(Geometry.builder().type(GEOMETRY_TYPE).coordinates(GEOMETRY_COORDINATES).build())
                .state(STATE)
                .build();
    }

    @Test
    public void delete_tei_in_data_base_when_delete_organisation_unit_foreign_key() {
        trackedEntityInstanceStore.insert(trackedEntityInstance);

        databaseAdapter().delete(OrganisationUnitTableInfo.TABLE_INFO.name(),
                IdentifiableColumns.UID + "=?",
                new String[]{ ORGANISATION_UNIT });

        Cursor cursor = databaseAdapter().query(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), PROJECTION);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void delete_tracked_entity_instance_in_data_base_when_delete_tracked_entity_foreign_key() {
        trackedEntityInstanceStore.insert(trackedEntityInstance);

        databaseAdapter().delete(TrackedEntityTypeTableInfo.TABLE_INFO.name(),
                TrackedEntityTypeTableInfo.Columns.UID + "=?",
                new String[]{ TRACKED_ENTITY} );

        Cursor cursor = databaseAdapter().query(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), PROJECTION);
        assertThatCursor(cursor).isExhausted();
    }

    @Test
    public void update_set_state_in_data_base_when_update_tracked_entity_instance_stateshouldUpdateTrackedEntityInstanceState() throws Exception {
        ContentValues trackedEntityInstance = new ContentValues();
        trackedEntityInstance.put(Columns.UID, UID);
        trackedEntityInstance.put(Columns.ORGANISATION_UNIT, ORGANISATION_UNIT);
        trackedEntityInstance.put(Columns.TRACKED_ENTITY_TYPE, TRACKED_ENTITY);
        trackedEntityInstance.put(DataColumns.STATE, STATE.name());

        databaseAdapter().insert(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), null, trackedEntityInstance);

        String[] projection = {
                Columns.UID,
                Columns.ORGANISATION_UNIT,
                Columns.TRACKED_ENTITY_TYPE,
                DataColumns.STATE
        };

        Cursor cursor = databaseAdapter().query(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), projection);
        // check that tracked entity instance was successfully inserted
        assertThatCursor(cursor).hasRow(UID, ORGANISATION_UNIT, TRACKED_ENTITY, STATE).isExhausted();
        State updatedState = State.SYNCED;
        trackedEntityInstanceStore.setState(UID, updatedState);

        cursor = databaseAdapter().query(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), projection);

        // check that trackedEntityInstance is updated with updated state
        assertThatCursor(cursor).hasRow(UID, ORGANISATION_UNIT, TRACKED_ENTITY, updatedState).isExhausted();
    }

    @Test
    public void return_list_of_tracked_entity_instance_when_query() throws Exception {
        ContentValues trackedEntityInstanceContentValues = new ContentValues();
        trackedEntityInstanceContentValues.put(Columns.UID, UID);
        trackedEntityInstanceContentValues.put(Columns.ORGANISATION_UNIT, ORGANISATION_UNIT);
        trackedEntityInstanceContentValues.put(Columns.TRACKED_ENTITY_TYPE, TRACKED_ENTITY);
        trackedEntityInstanceContentValues.put(DataColumns.STATE, State.TO_POST.name());
        databaseAdapter().insert(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), null, trackedEntityInstanceContentValues);

        String[] projection = {Columns.UID};
        Cursor cursor = databaseAdapter().query(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), projection);
        // verify that tei was successfully inserted into database
        assertThatCursor(cursor).hasRow(UID).isExhausted();

        List<TrackedEntityInstance> trackedEntityInstances = trackedEntityInstanceStore.queryTrackedEntityInstancesToSync();

        List<String> teiUids = new ArrayList<>();
        for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
            teiUids.add(trackedEntityInstance.uid());
        }
        assertThat(teiUids.contains(UID)).isTrue();

        TrackedEntityInstance trackedEntityInstance = trackedEntityInstanceStore.selectByUid(UID);
        assertThat(trackedEntityInstance.uid()).isEqualTo(UID);
        assertThat(trackedEntityInstance.organisationUnit()).isEqualTo(ORGANISATION_UNIT);
        assertThat(trackedEntityInstance.trackedEntityType()).isEqualTo(TRACKED_ENTITY);

    }

    @Test(expected = RuntimeException.class)
    public void throw_sqlite_constraint_exception_when_insert_tracked_entity_instance_with_invalid_org_unit_foreign_key() {
        trackedEntityInstanceStore.insert(trackedEntityInstance.toBuilder().organisationUnit("wrong").build());
    }

    @Test(expected = RuntimeException.class)
    public void throw_sqlite_constraint_exception_when_insert_tracked_entity_instance_with_invalid_tracked_entity_foreign_key() {
        trackedEntityInstanceStore.insert(trackedEntityInstance.toBuilder().trackedEntityType("wrong").build());
    }

    @Test(expected = IllegalStateException.class)
    public void throw_sqlite_constraint_exception_when_insert_null_uid() {
        trackedEntityInstanceStore.insert(trackedEntityInstance.toBuilder().uid(null).build());
    }

    //Removed NOT NULL constraint in 2.30 to allow TEI with just a UID
    //@Test(expected = SQLiteConstraintException.class)
    public void throw_sqlite_constraint_exception_when_insert_null_organisation_unit() {
        trackedEntityInstanceStore.insert(trackedEntityInstance.toBuilder().organisationUnit(null).build());

    }

    //Removed NOT NULL constraint in 2.30 to allow TEI with just a UID
    //@Test(expected = SQLiteConstraintException.class)
    public void throw_sqlite_constraint_exception_when_insert_null_tracked_entity() {
        trackedEntityInstanceStore.insert(trackedEntityInstance.toBuilder().trackedEntityType(null).build());

    }
}