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
package org.hisp.dhis.android.core.program.internal;

import org.hisp.dhis.android.core.arch.call.factories.internal.ListCallFactory;
import org.hisp.dhis.android.core.arch.call.factories.internal.UidsCallFactory;
import org.hisp.dhis.android.core.common.BaseCallShould;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.option.OptionSet;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ProgramModuleDownloaderShould extends BaseCallShould {
    @Mock
    private Program program;

    @Mock
    private TrackedEntityType trackedEntityType;

    @Mock
    private TrackedEntityAttribute trackedEntityAttribute;

    @Mock
    private Callable<List<Program>> programEndpointCall;

    @Mock
    private Callable<List<ProgramStage>> programStageEndpointCall;

    @Mock
    private Callable<List<ProgramRule>> programRuleEndpointCall;

    @Mock
    private Callable<List<TrackedEntityType>> trackedEntityTypeCall;

    @Mock
    private Callable<List<TrackedEntityAttribute>> trackedEntityAttributeCall;

    @Mock
    private Callable<List<RelationshipType>> relationshipTypeCall;

    @Mock
    private Callable<List<OptionSet>> optionSetCall;

    @Mock
    private Callable<List<Option>> optionCall;

    @Mock
    private Callable<List<OptionGroup>> optionGroupCall;

    @Mock
    private UidsCallFactory<Program> programCallFactory;

    @Mock
    private UidsCallFactory<ProgramStage> programStageCallFactory;

    @Mock
    private UidsCallFactory<ProgramRule> programRuleCallFactory;

    @Mock
    private UidsCallFactory<TrackedEntityType> trackedEntityCallFactory;

    @Mock
    private UidsCallFactory<TrackedEntityAttribute> trackedEntityAttributeCallFactory;

    @Mock
    private ListCallFactory<RelationshipType> relationshipTypeCallFactory;

    @Mock
    private UidsCallFactory<OptionSet> optionSetCallFactory;

    @Mock
    private UidsCallFactory<Option> optionCallFactory;

    @Mock
    private UidsCallFactory<OptionGroup> optionGroupCallFactory;

    @Mock
    private DHISVersionManager versionManager;

    // object to test
    private Callable<List<Program>> programDownloadCall;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();

        errorResponse = Response.error(
                HttpsURLConnection.HTTP_CLIENT_TIMEOUT,
                ResponseBody.create(MediaType.parse("application/json"), "{}"));

        // Call factories
        when(programCallFactory.create(anySet()))
                .thenReturn(programEndpointCall);
        when(programStageCallFactory.create(any(Set.class)))
                .thenReturn(programStageEndpointCall);
        when(programRuleCallFactory.create(any(Set.class)))
                .thenReturn(programRuleEndpointCall);
        when(trackedEntityCallFactory.create(any(Set.class)))
                .thenReturn(trackedEntityTypeCall);
        when(trackedEntityAttributeCallFactory.create(any(Set.class)))
                .thenReturn(trackedEntityAttributeCall);
        when(relationshipTypeCallFactory.create())
                .thenReturn(relationshipTypeCall);
        when(optionSetCallFactory.create(any(Set.class)))
                .thenReturn(optionSetCall);
        when(optionCallFactory.create(any(Set.class)))
                .thenReturn(optionCall);
        when(optionGroupCallFactory.create(any(Set.class)))
                .thenReturn(optionGroupCall);

        // Calls
        when(programEndpointCall.call()).thenReturn(Collections.singletonList(program));
        when(trackedEntityTypeCall.call()).thenReturn(Collections.singletonList(trackedEntityType));
        when(trackedEntityAttributeCall.call()).thenReturn(Collections.singletonList(trackedEntityAttribute));
        when(relationshipTypeCall.call()).thenReturn(Collections.emptyList());
        when(optionSetCall.call()).thenReturn(Collections.emptyList());
        when(optionCall.call()).thenReturn(Collections.emptyList());
        when(optionGroupCall.call()).thenReturn(Collections.emptyList());
        when(programStageEndpointCall.call()).thenReturn(Collections.emptyList());
        when(programRuleEndpointCall.call()).thenReturn(Collections.emptyList());

        when(versionManager.is2_29()).thenReturn(Boolean.FALSE);

        // Metadata call
        programDownloadCall = new ProgramModuleDownloader(
                programCallFactory,
                programStageCallFactory,
                programRuleCallFactory,
                trackedEntityCallFactory,
                trackedEntityAttributeCallFactory,
                relationshipTypeCallFactory,
                optionSetCallFactory,
                optionCallFactory,
                optionGroupCallFactory,
                versionManager).downloadMetadata(anySet());
    }

    @Test
    public void succeed_when_endpoint_calls_succeed() throws Exception {
        programDownloadCall.call();
    }

    @Test
    public void return_programs() throws Exception {
        List<Program> programs = programDownloadCall.call();
        assertTrue(!programs.isEmpty());
        assertThat(programs.get(0)).isEqualTo(program);
    }

    @Test(expected = Exception.class)
    public void fail_when_program_call_fail() throws Exception {
        whenEndpointCallFails(programEndpointCall);
        programDownloadCall.call();
    }

    @Test(expected = Exception.class)
    public void fail_when_program_stage_call_fail() throws Exception {
        whenEndpointCallFails(programStageEndpointCall);
        programDownloadCall.call();
    }

    @Test(expected = Exception.class)
    public void fail_when_program_rule_call_fail() throws Exception {
        whenEndpointCallFails(programRuleEndpointCall);
        programDownloadCall.call();
    }

    @Test(expected = Exception.class)
    public void fail_when_tracked_entity_types_call_fail() throws Exception {
        whenEndpointCallFails(trackedEntityTypeCall);
        programDownloadCall.call();
    }

    @Test(expected = Exception.class)
    public void fail_when_tracked_entity_attributes_call_fail() throws Exception {
        whenEndpointCallFails(trackedEntityAttributeCall);
        programDownloadCall.call();
    }

    @Test(expected = Exception.class)
    public void fail_when_relationship_type_call_fail() throws Exception {
        whenEndpointCallFails(relationshipTypeCall);
        programDownloadCall.call();
    }
}
