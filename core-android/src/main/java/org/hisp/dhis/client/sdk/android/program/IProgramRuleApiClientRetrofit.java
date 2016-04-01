package org.hisp.dhis.client.sdk.android.program;

import org.hisp.dhis.client.sdk.models.program.ProgramRule;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface IProgramRuleApiClientRetrofit {
    @GET("programRules")
    Call<Map<String, List<ProgramRule>>> getProgramRules(@QueryMap Map<String, String> queryMap,
                                                 @Query("filter") List<String> filters);
}
