package org.hisp.dhis.android.core.option;

import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.Payload;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.data.api.FilterConverterFactory;
import org.hisp.dhis.android.core.data.database.AbsStoreTestCase;
import org.hisp.dhis.android.core.data.database.DbOpenHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.google.common.truth.Truth.assertThat;
import static org.hisp.dhis.android.core.data.database.CursorAssert.assertThatCursor;

@RunWith(AndroidJUnit4.class)
public class OptionSetCallIntegrationTest extends AbsStoreTestCase {
    private static final String[] OPTION_SET_PROJECTION = {
            OptionSetModel.Columns.ID,
            OptionSetModel.Columns.UID,
            OptionSetModel.Columns.CODE,
            OptionSetModel.Columns.NAME,
            OptionSetModel.Columns.DISPLAY_NAME,
            OptionSetModel.Columns.CREATED,
            OptionSetModel.Columns.LAST_UPDATED,
            OptionSetModel.Columns.VERSION,
            OptionSetModel.Columns.VALUE_TYPE
    };

    private static final String[] OPTION_PROJECTION = {
            OptionModel.Columns.ID,
            OptionModel.Columns.UID,
            OptionModel.Columns.CODE,
            OptionModel.Columns.NAME,
            OptionModel.Columns.DISPLAY_NAME,
            OptionModel.Columns.CREATED,
            OptionModel.Columns.LAST_UPDATED,
            OptionModel.Columns.OPTION_SET
    };

    private MockWebServer mockWebServer;
    private OptionSetCall optionSetCall;

    @Override
    @Before
    public void setUp() throws IOException {
        super.setUp();

        mockWebServer = new MockWebServer();
        mockWebServer.start();

        MockResponse mockResponse = new MockResponse();
        mockResponse.setBody("{\n" +
                "\n" +
                "    \"pager\": {\n" +
                "        \"page\": 1,\n" +
                "        \"pageCount\": 1,\n" +
                "        \"total\": 1,\n" +
                "        \"pageSize\": 50\n" +
                "    },\n" +
                "    \"optionSets\": [\n" +
                "        {\n" +
                "            \"lastUpdated\": \"2012-09-20T16:05:17.555\",\n" +
                "            \"created\": \"2012-09-20T16:05:17.555\",\n" +
                "            \"name\": \"MNCH PMTCT code\",\n" +
                "            \"id\": \"POc7DkGU3QU\",\n" +
                "            \"displayName\": \"MNCH PMTCT code\",\n" +
                "            \"valueType\": \"TEXT\",\n" +
                "            \"version\": 1,\n" +
                "            \"options\": [\n" +
                "                {\n" +
                "                    \"code\": \"C\",\n" +
                "                    \"created\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"lastUpdated\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"name\": \"C\",\n" +
                "                    \"id\": \"s2gIL3CEyKL\",\n" +
                "                    \"displayName\": \"C\",\n" +
                "                    \"externalAccess\": false,\n" +
                "                    \"sortOrder\": 1,\n" +
                "                    \"optionSet\": {\n" +
                "                        \"id\": \"POc7DkGU3QU\"\n" +
                "                    },\n" +
                "                    \"userGroupAccesses\": [ ],\n" +
                "                    \"attributeValues\": [ ],\n" +
                "                    \"translations\": [ ],\n" +
                "                    \"userAccesses\": [ ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"code\": \"TR\",\n" +
                "                    \"created\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"lastUpdated\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"name\": \"TR\",\n" +
                "                    \"id\": \"poM80hUlVi9\",\n" +
                "                    \"displayName\": \"TR\",\n" +
                "                    \"externalAccess\": false,\n" +
                "                    \"sortOrder\": 2,\n" +
                "                    \"optionSet\": {\n" +
                "                        \"id\": \"POc7DkGU3QU\"\n" +
                "                    },\n" +
                "                    \"userGroupAccesses\": [ ],\n" +
                "                    \"attributeValues\": [ ],\n" +
                "                    \"translations\": [ ],\n" +
                "                    \"userAccesses\": [ ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"code\": \"TRR\",\n" +
                "                    \"created\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"lastUpdated\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"name\": \"TRR\",\n" +
                "                    \"id\": \"hcvPpAy3kb2\",\n" +
                "                    \"displayName\": \"TRR\",\n" +
                "                    \"externalAccess\": false,\n" +
                "                    \"sortOrder\": 3,\n" +
                "                    \"optionSet\": {\n" +
                "                        \"id\": \"POc7DkGU3QU\"\n" +
                "                    },\n" +
                "                    \"userGroupAccesses\": [ ],\n" +
                "                    \"attributeValues\": [ ],\n" +
                "                    \"translations\": [ ],\n" +
                "                    \"userAccesses\": [ ]\n" +
                "                },\n" +
                "                {\n" +
                "                    \"code\": \"TRRDm\",\n" +
                "                    \"created\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"lastUpdated\": \"2014-08-18T12:39:16.000\",\n" +
                "                    \"name\": \"TRRDm\",\n" +
                "                    \"id\": \"u4wsy7OPQIg\",\n" +
                "                    \"displayName\": \"TRRDm\",\n" +
                "                    \"externalAccess\": false,\n" +
                "                    \"sortOrder\": 4,\n" +
                "                    \"optionSet\": {\n" +
                "                        \"id\": \"POc7DkGU3QU\"\n" +
                "                    },\n" +
                "                    \"userGroupAccesses\": [ ],\n" +
                "                    \"attributeValues\": [ ],\n" +
                "                    \"translations\": [ ],\n" +
                "                    \"userAccesses\": [ ]\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ]\n" +
                "\n" +
                "}");

        mockWebServer.enqueue(mockResponse);

        // ToDo: consider moving this out
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(BaseIdentifiableObject.DATE_FORMAT);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mockWebServer.url("/"))
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .addConverterFactory(FilterConverterFactory.create())
                .build();

        OptionSetService optionSetService = retrofit.create(OptionSetService.class);
        OptionSetStore optionSetStore = new OptionSetStoreImpl(database());
        OptionStore optionStore = new OptionStoreImpl(database());

        optionSetCall = new OptionSetCall(optionSetService, database(), optionSetStore, optionStore);

    }


    @Test
    public void call_shouldPersistOptionSetWithOptionsInDatabase() throws Exception {
        optionSetCall.call();

        Cursor optionSetCursor = database().query(DbOpenHelper.Tables.OPTION_SET,
                OPTION_SET_PROJECTION, null, null, null, null, null);
        Cursor optionCursor = database().query(DbOpenHelper.Tables.OPTION,
                OPTION_PROJECTION, null, null, null, null, null);

        assertThatCursor(optionSetCursor)
                .hasRow(
                        1L, // id
                        "POc7DkGU3QU", // uid
                        null, // code
                        "MNCH PMTCT code", // name
                        "MNCH PMTCT code", // displayName
                        "2012-09-20T16:05:17.555", // created
                        "2012-09-20T16:05:17.555", // lastUpdated
                        1, // version
                        "TEXT" // valueType
                ).isExhausted();

        assertThatCursor(optionCursor)
                .hasRow(
                        1L, // id
                        "s2gIL3CEyKL", // uid
                        "C", // code
                        "C", // name
                        "C", // displayName
                        "2014-08-18T12:39:16.000", // created
                        "2014-08-18T12:39:16.000", // lastUpdated
                        "POc7DkGU3QU"  // optionSet
                );

        assertThatCursor(optionCursor)
                .hasRow(
                        2L, // id
                        "poM80hUlVi9", // uid
                        "TR", // code
                        "TR", // name
                        "TR", // displayName
                        "2014-08-18T12:39:16.000", // created
                        "2014-08-18T12:39:16.000", // lastUpdated
                        "POc7DkGU3QU"  // optionSet
                );


        assertThatCursor(optionCursor)
                .hasRow(
                        3L, // id
                        "hcvPpAy3kb2", // uid
                        "TRR", // code
                        "TRR", // name
                        "TRR", // displayName
                        "2014-08-18T12:39:16.000", // created
                        "2014-08-18T12:39:16.000", // lastUpdated
                        "POc7DkGU3QU"  // optionSet
                );

        assertThatCursor(optionCursor)
                .hasRow(
                        4L, // id
                        "u4wsy7OPQIg", // uid
                        "TRRDm", // code
                        "TRRDm", // name
                        "TRRDm", // displayName
                        "2014-08-18T12:39:16.000", // created
                        "2014-08-18T12:39:16.000", // lastUpdated
                        "POc7DkGU3QU"  // optionSet
                )
                .isExhausted();

    }

    @Test
    public void call_shouldReturnToOptionSetModel() throws Exception {
        Response<Payload<OptionSet>> response = optionSetCall.call();

        List<OptionSet> optionSetList = response.body().items();

        assertThat(optionSetList.size()).isEqualTo(1);

        OptionSet optionSet = optionSetList.get(0);

        assertThat(optionSet.uid()).isEqualTo("POc7DkGU3QU");
        assertThat(optionSet.code()).isNull();
        assertThat(optionSet.name()).isEqualTo("MNCH PMTCT code");
        assertThat(optionSet.displayName()).isEqualTo("MNCH PMTCT code");
        assertThat(optionSet.created()).isEqualTo(
                BaseIdentifiableObject.DATE_FORMAT.parse("2012-09-20T16:05:17.555"));
        assertThat(optionSet.lastUpdated()).isEqualTo(
                BaseIdentifiableObject.DATE_FORMAT.parse("2012-09-20T16:05:17.555"));
        assertThat(optionSet.version()).isEqualTo(1);
        assertThat(optionSet.valueType()).isEqualTo(ValueType.TEXT);

        assertThat(optionSet.options().size()).isEqualTo(4);


    }


    @After
    @Override
    public void tearDown() throws IOException {
        super.tearDown();

        mockWebServer.shutdown();
    }
}