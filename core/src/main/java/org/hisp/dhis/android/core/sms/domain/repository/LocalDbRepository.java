package org.hisp.dhis.android.core.sms.domain.repository;

import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.common.State;

import io.reactivex.Completable;
import io.reactivex.Single;

public interface LocalDbRepository {

    Single<String> getDefaultCategoryOptionCombo();

    Single<String> getUserName();

    Single<String> getGatewayNumber();

    Completable setGatewayNumber(String number);

    Single<String> getConfirmationSenderNumber();

    Completable setConfirmationSenderNumber(String number);

    Completable updateSubmissionState(BaseDataModel event, State sentViaSms);
}
