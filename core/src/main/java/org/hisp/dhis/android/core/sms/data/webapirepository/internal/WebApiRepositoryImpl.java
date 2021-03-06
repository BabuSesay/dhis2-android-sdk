package org.hisp.dhis.android.core.sms.data.webapirepository.internal;

import org.hisp.dhis.android.core.sms.domain.repository.WebApiRepository;
import org.hisp.dhis.smscompression.models.SMSMetadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.Retrofit;

public class WebApiRepositoryImpl implements WebApiRepository {
    private final ApiService apiService;

    public WebApiRepositoryImpl(Retrofit retrofit) {
        apiService = retrofit.create(ApiService.class);
    }

    @Override
    public Single<SMSMetadata> getMetadataIds(final GetMetadataIdsConfig config) {
        return translateCallToSingle(
                metadataCall(config)
        ).map(response -> {
            SMSMetadata metadata = new SMSMetadata();
            // TODO Server date has not timezone. We cannot use server date because it will be consider as local and
            //  potentially could be move some hours back or forth.
            //metadata.lastSyncDate = response.system().date();
            metadata.lastSyncDate = new Date();
            metadata.categoryOptionCombos = mapIds(response.categoryOptionCombos());
            metadata.dataElements = mapIds(response.dataElements());
            metadata.organisationUnits = mapIds(response.organisationUnits());
            metadata.users = mapIds(response.users());
            metadata.trackedEntityTypes = mapIds(response.trackedEntityTypes());
            metadata.trackedEntityAttributes = mapIds(response.trackedEntityAttributes());
            metadata.programs = mapIds(response.programs());
            return metadata;
        });
    }

    private List<SMSMetadata.ID> mapIds(List<MetadataResponse.MetadataId> ids) {
        if (ids == null) {
            return null;
        }
        ArrayList<SMSMetadata.ID> newList = new ArrayList<>();
        for (MetadataResponse.MetadataId item : ids) {
            newList.add(makeID(item.id()));
        }
        return newList;
    }

    private SMSMetadata.ID makeID(String id) {
        return new SMSMetadata.ID(id);
    }

    private Call<MetadataResponse> metadataCall(final GetMetadataIdsConfig c) {
        return apiService.getMetadataIds(
                val(c.dataElements),
                val(c.categoryOptionCombos),
                val(c.organisationUnits),
                val(c.users),
                val(c.trackedEntityTypes),
                val(c.trackedEntityAttributes),
                val(c.programs)
        );
    }

    private String val(boolean enable) {
        return enable ? ApiService.GET_IDS : null;
    }

    private <T> Single<T> translateCallToSingle(Call<T> call) {
        return Single.fromCallable(call::execute).flatMap(response -> {
            if (response.isSuccessful() && response.body() != null) {
                return Single.just(response.body());
            } else {
                return Single.error(new HttpException(response.code()));
            }
        });
    }
}
