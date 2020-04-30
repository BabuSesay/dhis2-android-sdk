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
import org.hisp.dhis.android.core.arch.call.factories.internal.UidsCall;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.modules.internal.MetadataModuleByUidDownloader;
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

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import dagger.Reusable;
import io.reactivex.Single;

@Reusable
public class ProgramModuleDownloader implements MetadataModuleByUidDownloader<List<Program>> {

    private final UidsCall<Program> programCall;
    private final UidsCall<ProgramStage> programStageCall;
    private final UidsCall<ProgramRule> programRuleCall;
    private final UidsCall<TrackedEntityType> trackedEntityTypeCall;
    private final UidsCall<TrackedEntityAttribute> trackedEntityAttributeCall;
    private final ListCallFactory<RelationshipType> relationshipTypeCallFactory;
    private final UidsCall<OptionSet> optionSetCall;
    private final UidsCall<Option> optionCall;
    private final UidsCall<OptionGroup> optionGroupCallFactory;
    private final DHISVersionManager versionManager;

    @Inject
    ProgramModuleDownloader(UidsCall<Program> programCall,
                            UidsCall<ProgramStage> programStageCall,
                            UidsCall<ProgramRule> programRuleCall,
                            UidsCall<TrackedEntityType> trackedEntityTypeCall,
                            UidsCall<TrackedEntityAttribute> trackedEntityAttributeCall,
                            ListCallFactory<RelationshipType> relationshipTypeCallFactory,
                            UidsCall<OptionSet> optionSetCall,
                            UidsCall<Option> optionCall,
                            UidsCall<OptionGroup> optionGroupCallFactory,
                            DHISVersionManager versionManager) {
        this.programCall = programCall;
        this.programStageCall = programStageCall;
        this.programRuleCall = programRuleCall;
        this.trackedEntityTypeCall = trackedEntityTypeCall;
        this.trackedEntityAttributeCall = trackedEntityAttributeCall;
        this.relationshipTypeCallFactory = relationshipTypeCallFactory;
        this.optionSetCall = optionSetCall;
        this.optionCall = optionCall;
        this.optionGroupCallFactory = optionGroupCallFactory;
        this.versionManager = versionManager;
    }

    @Override
    public Single<List<Program>> downloadMetadata(Set<String> orgUnitProgramUids) {
        return Single.fromCallable(() -> {
            List<Program> programs = programCall.download(orgUnitProgramUids).blockingGet();

            Set<String> programUids = UidsHelper.getUids(programs);
            List<ProgramStage> programStages = programStageCall.download(programUids).blockingGet();

            programRuleCall.download(programUids).blockingGet();

            Set<String> trackedEntityUids = ProgramParentUidsHelper.getAssignedTrackedEntityUids(programs);

            List<TrackedEntityType> trackedEntityTypes = trackedEntityTypeCall.download(trackedEntityUids)
                    .blockingGet();

            Set<String> attributeUids = ProgramParentUidsHelper.getAssignedTrackedEntityAttributeUids(programs,
                    trackedEntityTypes);

            List<TrackedEntityAttribute> attributes = trackedEntityAttributeCall.download(attributeUids).blockingGet();

            relationshipTypeCallFactory.create().call();

            Set<String> optionSetUids = ProgramParentUidsHelper.getAssignedOptionSetUids(attributes, programStages);
            optionSetCall.download(optionSetUids).blockingGet();

            optionCall.download(optionSetUids).blockingGet();

            if (!versionManager.is2_29()) {
                optionGroupCallFactory.download(optionSetUids).blockingGet();
            }

            return programs;
        });
    }
}