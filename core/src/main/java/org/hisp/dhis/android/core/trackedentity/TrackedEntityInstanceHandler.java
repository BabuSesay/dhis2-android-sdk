package org.hisp.dhis.android.core.trackedentity;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.D2InternalModules;
import org.hisp.dhis.android.core.common.DataOrphanCleanerImpl;
import org.hisp.dhis.android.core.common.OrphanCleaner;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentHandler;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.relationship.Relationship229Compatible;
import org.hisp.dhis.android.core.relationship.RelationshipDHISVersionManager;
import org.hisp.dhis.android.core.relationship.RelationshipHandler;

import java.util.Collection;
import java.util.List;

import static org.hisp.dhis.android.core.utils.Utils.isDeleted;

@SuppressWarnings({
        "PMD.CyclomaticComplexity",
        "PMD.StdCyclomaticComplexity",
        "PMD.ModifiedCyclomaticComplexity",
        "PMD.NPathComplexity"
})
public class TrackedEntityInstanceHandler {
    private final RelationshipDHISVersionManager relationshipVersionManager;
    private final RelationshipHandler relationshipHandler;
    private final TrackedEntityInstanceStore trackedEntityInstanceStore;
    private final TrackedEntityAttributeValueHandler trackedEntityAttributeValueHandler;
    private final EnrollmentHandler enrollmentHandler;
    private final OrphanCleaner<TrackedEntityInstance, Enrollment> enrollmentOrphanCleaner;

    public TrackedEntityInstanceHandler(
            @NonNull RelationshipDHISVersionManager relationshipVersionManager,
            @NonNull RelationshipHandler relationshipHandler,
            @NonNull TrackedEntityInstanceStore trackedEntityInstanceStore,
            @NonNull TrackedEntityAttributeValueHandler trackedEntityAttributeValueHandler,
            @NonNull EnrollmentHandler enrollmentHandler,
            @NonNull OrphanCleaner<TrackedEntityInstance, Enrollment> enrollmentOrphanCleaner) {
        this.relationshipVersionManager = relationshipVersionManager;
        this.relationshipHandler = relationshipHandler;
        this.trackedEntityInstanceStore = trackedEntityInstanceStore;
        this.trackedEntityAttributeValueHandler = trackedEntityAttributeValueHandler;
        this.enrollmentHandler = enrollmentHandler;
        this.enrollmentOrphanCleaner = enrollmentOrphanCleaner;
    }

    public void handle(@NonNull TrackedEntityInstance trackedEntityInstance, boolean asRelationship) {
        if (trackedEntityInstance == null) {
            return;
        }

        if (isDeleted(trackedEntityInstance)) {
            trackedEntityInstanceStore.delete(trackedEntityInstance.uid());
        } else {

            if (asRelationship) {
                State currentState = trackedEntityInstanceStore.getState(trackedEntityInstance.uid());

                if (currentState == State.RELATIONSHIP) {
                    updateOrInsert(trackedEntityInstance, State.RELATIONSHIP);
                } else if (currentState == null) {
                    insert(trackedEntityInstance, State.RELATIONSHIP);
                }

            } else {
                updateOrInsert(trackedEntityInstance, State.SYNCED);
            }

            trackedEntityAttributeValueHandler.handle(
                    trackedEntityInstance.uid(),
                    trackedEntityInstance.trackedEntityAttributeValues());

            List<Enrollment> enrollments = trackedEntityInstance.enrollments();
            if (enrollments != null) {
                enrollmentHandler.handle(enrollments);
            }

            List<Relationship229Compatible> relationships = trackedEntityInstance.relationships();
            if (relationships != null) {
                for (Relationship229Compatible relationship229 : trackedEntityInstance.relationships()) {

                    Relationship relationship = relationshipVersionManager.from229Compatible(relationship229);
                    TrackedEntityInstance relativeTEI = relationshipVersionManager.getRelativeTei(relationship229,
                            trackedEntityInstance.uid());

                    if (relativeTEI != null) {
                        this.handle(relativeTEI, true);
                        relationshipHandler.handle(relationship);
                    }
                }
            }
        }
        enrollmentOrphanCleaner.deleteOrphan(trackedEntityInstance, trackedEntityInstance.enrollments());
    }

    private void updateOrInsert(@NonNull TrackedEntityInstance trackedEntityInstance, State state) {
        int affectedRows = trackedEntityInstanceStore.update(
                trackedEntityInstance.uid(), trackedEntityInstance.created(),
                trackedEntityInstance.lastUpdated(), trackedEntityInstance.createdAtClient(),
                trackedEntityInstance.lastUpdatedAtClient(), trackedEntityInstance.organisationUnit(),
                trackedEntityInstance.trackedEntityType(), trackedEntityInstance.coordinates(),
                trackedEntityInstance.featureType(), state, trackedEntityInstance.uid());
        if (affectedRows <= 0) {
            insert(trackedEntityInstance, state);
        }
    }

    private void insert(@NonNull TrackedEntityInstance trackedEntityInstance, State state) {
        trackedEntityInstanceStore.insert(
                trackedEntityInstance.uid(), trackedEntityInstance.created(),
                trackedEntityInstance.lastUpdated(), trackedEntityInstance.createdAtClient(),
                trackedEntityInstance.lastUpdatedAtClient(), trackedEntityInstance.organisationUnit(),
                trackedEntityInstance.trackedEntityType(), trackedEntityInstance.coordinates(),
                trackedEntityInstance.featureType(), state);
    }

    public void handleMany(@NonNull Collection<TrackedEntityInstance> trackedEntityInstances, boolean asRelationship) {
        for (TrackedEntityInstance trackedEntityInstance : trackedEntityInstances) {
            handle(trackedEntityInstance, asRelationship);
        }
    }

    public static TrackedEntityInstanceHandler create(DatabaseAdapter databaseAdapter,
                                                      D2InternalModules internalModules) {
        return new TrackedEntityInstanceHandler(
                new RelationshipDHISVersionManager(internalModules.systemInfo.publicModule.versionManager),
                internalModules.relationshipModule.relationshipHandler,
                new TrackedEntityInstanceStoreImpl(databaseAdapter),
                TrackedEntityAttributeValueHandler.create(databaseAdapter),
                EnrollmentHandler.create(databaseAdapter),
                new DataOrphanCleanerImpl<TrackedEntityInstance, Enrollment>(EnrollmentModel.TABLE,
                        EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE, EnrollmentModel.Columns.STATE, databaseAdapter)
        );
    }
}
