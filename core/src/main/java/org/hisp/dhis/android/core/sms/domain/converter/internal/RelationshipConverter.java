package org.hisp.dhis.android.core.sms.domain.converter.internal;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.relationship.Relationship;
import org.hisp.dhis.android.core.sms.domain.repository.internal.LocalDbRepository;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;
import org.hisp.dhis.smscompression.models.RelationshipSMSSubmission;
import org.hisp.dhis.smscompression.models.SMSSubmission;

import io.reactivex.Completable;
import io.reactivex.Single;

public class RelationshipConverter extends Converter<Relationship> {
    private final String relationshipUid;

    public RelationshipConverter(LocalDbRepository localDbRepository,
                                 DHISVersionManager dhisVersionManager,
                                 String relationshipUid) {
        super(localDbRepository, dhisVersionManager);
        this.relationshipUid = relationshipUid;
    }

    @Override
    Single<? extends SMSSubmission> convert(@NonNull Relationship relationship, String user, int submissionId) {
        return Single.fromCallable(() -> {
            RelationshipSMSSubmission subm = new RelationshipSMSSubmission();
            subm.setSubmissionID(submissionId);
            subm.setUserID(user);
            subm.setRelationship(relationship.uid());
            subm.setRelationshipType(relationship.relationshipType());
            subm.setFrom(relationship.from().elementUid());
            subm.setTo(relationship.to().elementUid());
            return subm;
        });
    }

    @Override
    public Completable updateSubmissionState(State state) {
        // there is no submission state update for RelationShip
        return Completable.complete();
    }

    @Override
    Single<Relationship> readItemFromDb() {
        return getLocalDbRepository().getRelationship(relationshipUid);
    }
}
