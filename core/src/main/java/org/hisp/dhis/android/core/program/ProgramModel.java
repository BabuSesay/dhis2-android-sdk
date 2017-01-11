package org.hisp.dhis.android.core.program;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnAdapter;
import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.BaseNameableObjectModel;
import org.hisp.dhis.android.core.data.database.DbProgramTypeColumnAdapter;

@AutoValue
public abstract class ProgramModel extends BaseNameableObjectModel {

    public interface Columns extends BaseNameableObjectModel.Columns {
        String VERSION = "version";
        String ONLY_ENROLL_ONCE = "onlyEnrollOnce";
        String ENROLLMENT_DATE_LABEL = "enrollmentDateLabel";
        String DISPLAY_INCIDENT_DATE = "displayIncidentDate";
        String INCIDENT_DATE_LABEL = "incidentDateLabel";
        String REGISTRATION = "registration";
        String SELECT_ENROLLMENT_DATES_IN_FUTURE = "selectEnrollmentDatesInFuture";
        String DATA_ENTRY_METHOD = "dataEntryMethod";
        String IGNORE_OVERDUE_EVENTS = "ignoreOverdueEvents";
        String RELATIONSHIP_FROM_A = "relationshipFromA";
        String SELECT_INCIDENT_DATES_IN_FUTURE = "selectIncidentDatesInFuture";
        String CAPTURE_COORDINATES = "captureCoordinates";
        String USE_FIRST_STAGE_DURING_REGISTRATION = "useFirstStageDuringRegistration";
        String DISPLAY_FRONT_PAGE_LIST = "displayFrontPageList";
        String PROGRAM_TYPE = "programType";
        String RELATIONSHIP_TYPE = "relationshipType";
        String RELATIONSHIP_TEXT = "relationshipText";
        String RELATED_PROGRAM = "relatedProgram";
        String TRACKED_ENTITY = "trackedEntity";
    }

    public static ProgramModel create(Cursor cursor) {
        return AutoValue_ProgramModel.createFromCursor(cursor);
    }

    public static Builder builder() {
        return new $$AutoValue_ProgramModel.Builder();
    }

    @NonNull
    public abstract ContentValues toContentValues();

    @Nullable
    @ColumnName(Columns.VERSION)
    public abstract Integer version();

    @Nullable
    @ColumnName(Columns.ONLY_ENROLL_ONCE)
    public abstract Boolean onlyEnrollOnce();

    @Nullable
    @ColumnName(Columns.ENROLLMENT_DATE_LABEL)
    public abstract String enrollmentDateLabel();

    @Nullable
    @ColumnName(Columns.DISPLAY_INCIDENT_DATE)
    public abstract Boolean displayIncidentDate();

    @Nullable
    @ColumnName(Columns.INCIDENT_DATE_LABEL)
    public abstract String incidentDateLabel();

    @Nullable
    @ColumnName(Columns.REGISTRATION)
    public abstract Boolean registration();

    @Nullable
    @ColumnName(Columns.SELECT_ENROLLMENT_DATES_IN_FUTURE)
    public abstract Boolean selectEnrollmentDatesInFuture();

    @Nullable
    @ColumnName(Columns.DATA_ENTRY_METHOD)
    public abstract Boolean dataEntryMethod();

    @Nullable
    @ColumnName(Columns.IGNORE_OVERDUE_EVENTS)
    public abstract Boolean ignoreOverdueEvents();

    @Nullable
    @ColumnName(Columns.RELATIONSHIP_FROM_A)
    public abstract Boolean relationshipFromA();

    @Nullable
    @ColumnName(Columns.SELECT_INCIDENT_DATES_IN_FUTURE)
    public abstract Boolean selectIncidentDatesInFuture();

    @Nullable
    @ColumnName(Columns.CAPTURE_COORDINATES)
    public abstract Boolean captureCoordinates();

    @Nullable
    @ColumnName(Columns.USE_FIRST_STAGE_DURING_REGISTRATION)
    public abstract Boolean useFirstStageDuringRegistration();

    @Nullable
    @ColumnName(Columns.DISPLAY_FRONT_PAGE_LIST)
    public abstract Boolean displayFrontPageList();

    @Nullable
    @ColumnName(Columns.PROGRAM_TYPE)
    @ColumnAdapter(DbProgramTypeColumnAdapter.class)
    public abstract ProgramType programType();

    @Nullable
    @ColumnName(Columns.RELATIONSHIP_TYPE)
    public abstract String relationshipType();

    @Nullable
    @ColumnName(Columns.RELATIONSHIP_TEXT)
    public abstract String relationshipText();

    @Nullable
    @ColumnName(Columns.RELATED_PROGRAM)
    public abstract String relatedProgram();

    @Nullable
    @ColumnName(Columns.TRACKED_ENTITY)
    public abstract String trackedEntity();

    //TODO: Add these to the model/sql/... later:
//    @Nullable
//    @ColumnName(Columns.CATEGORY_COMBO)
//    public abstract String categoryCombo();
//

    @AutoValue.Builder
    public static abstract class Builder extends BaseNameableObjectModel.Builder<Builder> {

        public abstract Builder version(@Nullable Integer version);

        public abstract Builder onlyEnrollOnce(@Nullable Boolean onlyEnrollOnce);

        public abstract Builder enrollmentDateLabel(@Nullable String enrollmentDateLabel);

        public abstract Builder displayIncidentDate(@Nullable Boolean displayIncidentDate);

        public abstract Builder incidentDateLabel(@Nullable String incidentDateLabel);

        public abstract Builder registration(@Nullable Boolean registration);

        public abstract Builder selectEnrollmentDatesInFuture(@Nullable Boolean selectEnrollmentDatesInFuture);

        public abstract Builder dataEntryMethod(@Nullable Boolean dataEntryMethod);

        public abstract Builder ignoreOverdueEvents(@Nullable Boolean ignoreOverdueEvents);

        public abstract Builder relationshipFromA(@Nullable Boolean relationshipFromA);

        public abstract Builder selectIncidentDatesInFuture(@Nullable Boolean selectIncidentDatesInFuture);

        public abstract Builder captureCoordinates(@Nullable Boolean captureCoordinates);

        public abstract Builder useFirstStageDuringRegistration(@Nullable Boolean useFirstStageDuringRegistration);

        public abstract Builder displayFrontPageList(@Nullable Boolean displayInFrontPageList);

        public abstract Builder programType(@Nullable ProgramType programType);

        public abstract Builder relationshipType(@Nullable String relationshipType);

        public abstract Builder relationshipText(@Nullable String relationshipText);

        public abstract Builder relatedProgram(@Nullable String relatedProgram);

        public abstract Builder trackedEntity(@Nullable String trackedEntity);
//
//        public abstract Builder categoryCombo(@Nullable String categoryCombo);

        public abstract ProgramModel build();
    }
}