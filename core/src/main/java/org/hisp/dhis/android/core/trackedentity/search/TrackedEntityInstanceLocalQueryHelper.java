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

import org.hisp.dhis.android.core.arch.db.WhereClauseBuilder;
import org.hisp.dhis.android.core.data.api.OuMode;
import org.hisp.dhis.android.core.enrollment.EnrollmentFields;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;

import static org.hisp.dhis.android.core.common.BaseIdentifiableObjectModel.Columns.UID;
import static org.hisp.dhis.android.core.enrollment.EnrollmentFields.ENROLLMENT_DATE;
import static org.hisp.dhis.android.core.enrollment.EnrollmentFields.PROGRAM;
import static org.hisp.dhis.android.core.enrollment.EnrollmentStatus.ACTIVE;
import static org.hisp.dhis.android.core.enrollment.EnrollmentStatus.COMPLETED;
import static org.hisp.dhis.android.core.organisationunit.OrganisationUnitFields.PARENT;
import static org.hisp.dhis.android.core.organisationunit.OrganisationUnitFields.PATH;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueFields.VALUE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_ATTRIBUTE;
import static org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_INSTANCE;

abstract class TrackedEntityInstanceLocalQueryHelper {

    private static String TEI_ALIAS = "tei";
    private static String ENROLLMENT_ALIAS = "en";
    private static String ORGUNIT_ALIAS = "ou";
    private static String TEAV_ALIAS = "teav";

    static String getSqlQuery(TrackedEntityInstanceQuery query) {

        String queryStr = "SELECT " + TEI_ALIAS + ".uid FROM " +
                TrackedEntityInstanceTableInfo.TABLE_INFO.name() + " " + TEI_ALIAS;

        WhereClauseBuilder where = new WhereClauseBuilder();

        if (hasProgram(query)) {
            queryStr += String.format(" JOIN %s %s ON %s = %s",
                    EnrollmentTableInfo.TABLE_INFO.name(), ENROLLMENT_ALIAS,
                    dot(TEI_ALIAS, UID),
                    dot(ENROLLMENT_ALIAS, "trackedentityinstance"));

            appendProgramWhere(where, query);
        }

        if (hasOrgunits(query)) {
            queryStr += String.format(" JOIN %s %s ON %s = %s",
                    OrganisationUnitTableInfo.TABLE_INFO.name(), ORGUNIT_ALIAS,
                    dot(TEI_ALIAS, "organisationUnit"),
                    dot(ORGUNIT_ALIAS, UID));

            appendOrgunitWhere(where, query);
        }

        appendQueryWhere(where, query);
        appendFiltersWhere(where, query);

        if (!where.isEmpty()) {
            queryStr += " WHERE " + where.build();
        }

        // TODO Paging

        if (hasProgram(query)) {
            String enrollStatus = dot(ENROLLMENT_ALIAS, EnrollmentFields.STATUS);
            queryStr += " ORDER BY CASE WHEN " + enrollStatus + " = '" + ACTIVE + "' THEN 1 " +
                    "WHEN " + enrollStatus + " = '" + COMPLETED + "' THEN 2 ELSE 3 END ASC, " +
                    TEI_ALIAS + ".lastUpdated DESC ";
        } else {
            queryStr += " ORDER BY " + TEI_ALIAS + ".lastUpdated DESC";
        }


        return queryStr;
    }

    private static boolean hasProgram(TrackedEntityInstanceQuery query) {
        return query.program() != null;
    }

    private static void appendProgramWhere(WhereClauseBuilder where, TrackedEntityInstanceQuery query) {
        if (query.program() != null) {
            where.appendKeyStringValue(dot(ENROLLMENT_ALIAS, PROGRAM), query.program());
        }
        if (query.programStartDate() != null) {
            where.appendKeyGreaterOrEqStringValue(dot(ENROLLMENT_ALIAS, ENROLLMENT_DATE),
                    query.formattedProgramStartDate());
        }
        if (query.programEndDate() != null) {
            where.appendKeyLessThanOrEqStringValue(dot(ENROLLMENT_ALIAS, ENROLLMENT_DATE),
                    query.formattedProgramEndDate());
        }
    }

    private static boolean hasOrgunits(TrackedEntityInstanceQuery query) {
        return !query.orgUnits().isEmpty() &&
                !OuMode.ALL.equals(query.orgUnitMode()) &&
                !OuMode.ACCESSIBLE.equals(query.orgUnitMode());
    }

    private static void appendOrgunitWhere(WhereClauseBuilder where, TrackedEntityInstanceQuery query) {
        OuMode ouMode = query.orgUnitMode() == null ? OuMode.SELECTED : query.orgUnitMode();

        WhereClauseBuilder inner = new WhereClauseBuilder();
        switch (ouMode) {
            case SELECTED:
                for (String orgunit : query.orgUnits()) {
                    inner.appendOrKeyStringValue(dot(ORGUNIT_ALIAS, UID), orgunit);
                }

                break;
            case DESCENDANTS:
                for (String orgunit : query.orgUnits()) {
                    inner.appendOrKeyLikeStringValue(dot(ORGUNIT_ALIAS, PATH), "%" + orgunit + "%");
                }
                break;
            case CHILDREN:
                for (String orgunit : query.orgUnits()) {
                    inner.appendOrKeyStringValue(dot(ORGUNIT_ALIAS, PARENT), orgunit);
                    // TODO Include orgunit?
                    inner.appendOrKeyStringValue(dot(ORGUNIT_ALIAS, UID), orgunit);
                }
                break;
        }

        if (!inner.isEmpty()) {
            where.appendComplexQuery(inner.build());
        }
    }

    private static void appendQueryWhere(WhereClauseBuilder where, TrackedEntityInstanceQuery query) {
        if (query.query() != null) {
            for (String filterStr : query.query().getSqlFilters()) {
                String sub = String.format("SELECT 1 FROM %s %s WHERE %s = %s AND %s %s '%s'",
                        TrackedEntityAttributeValueTableInfo.TABLE_INFO.name(), TEAV_ALIAS,
                        dot(TEAV_ALIAS, TRACKED_ENTITY_INSTANCE), dot(TEI_ALIAS, UID),
                        dot(TEAV_ALIAS, VALUE), query.query().operator().getSqlOperator(), filterStr);
                where.appendExistsSubQuery(sub);
            }
        }
    }

    private static void appendFiltersWhere(WhereClauseBuilder where, TrackedEntityInstanceQuery query) {
        // TODO Filter by program attributes in case program is provided

        for (QueryItem item : query.filter()) {
            if (!item.filters().isEmpty()) {
                appendFilterWhere(where, item);
            }
        }
        for (QueryItem item : query.attribute()) {
            if (!item.filters().isEmpty()) {
                appendFilterWhere(where, item);
            }
        }
    }

    private static void appendFilterWhere(WhereClauseBuilder where, QueryItem item) {
        for (QueryFilter filter : item.filters()) {
            String sub = String.format("SELECT 1 FROM %s %s WHERE %s = %s AND %s = '%s' AND %s %s '%s'",
                    TrackedEntityAttributeValueTableInfo.TABLE_INFO.name(), TEAV_ALIAS,
                    dot(TEAV_ALIAS, TRACKED_ENTITY_INSTANCE), dot(TEI_ALIAS, UID),
                    dot(TEAV_ALIAS, TRACKED_ENTITY_ATTRIBUTE), item.item(),
                    dot(TEAV_ALIAS, VALUE), filter.operator().getSqlOperator(), filter.getSqlFilter());
            where.appendExistsSubQuery(sub);
        }
    }

    private static String dot(String item1, String item2) {
        return item1 + "." + item2;
    }
}