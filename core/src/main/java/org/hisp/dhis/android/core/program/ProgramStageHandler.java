/*
 * Copyright (c) 2017, University of Oslo
 *
 * All rights reserved.
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
package org.hisp.dhis.android.core.program;

import org.hisp.dhis.android.core.arch.handlers.SyncHandlerWithTransformer;
import org.hisp.dhis.android.core.common.CollectionCleaner;
import org.hisp.dhis.android.core.common.CollectionCleanerImpl;
import org.hisp.dhis.android.core.common.HandleAction;
import org.hisp.dhis.android.core.common.IdentifiableHandlerImpl;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectStyleHandler;
import org.hisp.dhis.android.core.common.ObjectStyleModelBuilder;
import org.hisp.dhis.android.core.common.OrphanCleaner;
import org.hisp.dhis.android.core.common.OrphanCleanerImpl;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;
import org.hisp.dhis.android.core.period.FeatureType;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;

import java.util.Collection;

public class ProgramStageHandler extends IdentifiableHandlerImpl<ProgramStage, ProgramStageModel> {
    private final ProgramStageSectionHandler programStageSectionHandler;
    private final ProgramStageDataElementHandler programStageDataElementHandler;
    private final SyncHandlerWithTransformer<ObjectStyle> styleHandler;
    private final OrphanCleaner<ProgramStage, ProgramStageDataElement> programStageDataElementCleaner;
    private final OrphanCleaner<ProgramStage, ProgramStageSection> programStageSectionCleaner;
    private final CollectionCleaner<ProgramStage> collectionCleaner;
    private final DHISVersionManager versionManager;

    ProgramStageHandler(IdentifiableObjectStore<ProgramStageModel> programStageStore,
                        ProgramStageSectionHandler programStageSectionHandler,
                        ProgramStageDataElementHandler programStageDataElementHandler,
                        SyncHandlerWithTransformer<ObjectStyle> styleHandler,
                        OrphanCleaner<ProgramStage, ProgramStageDataElement> programStageDataElementCleaner,
                        OrphanCleaner<ProgramStage, ProgramStageSection> programStageSectionCleaner,
                        CollectionCleaner<ProgramStage> collectionCleaner,
                        DHISVersionManager versionManager) {
        super(programStageStore);
        this.programStageSectionHandler = programStageSectionHandler;
        this.programStageDataElementHandler = programStageDataElementHandler;
        this.styleHandler = styleHandler;
        this.programStageDataElementCleaner = programStageDataElementCleaner;
        this.programStageSectionCleaner = programStageSectionCleaner;
        this.collectionCleaner = collectionCleaner;
        this.versionManager = versionManager;
    }

    @Override
    protected ProgramStage beforeObjectHandled(ProgramStage programStage) {
        ProgramStage adaptedProgramStage;
        ProgramStage.Builder builder = programStage.toBuilder();
        if (versionManager.is2_29()) {
            adaptedProgramStage = programStage.captureCoordinates() ? builder.featureType(FeatureType.POINT).build() :
                    builder.featureType(FeatureType.NONE).build();
        } else {
            if (programStage.featureType() == null) {
                adaptedProgramStage = builder.captureCoordinates(false).featureType(FeatureType.NONE).build();
            } else {
                adaptedProgramStage = builder.captureCoordinates(
                        programStage.featureType() != FeatureType.NONE).build();
            }
        }
        return adaptedProgramStage;
    }

    @Override
    protected void afterObjectHandled(ProgramStage programStage, HandleAction action) {
        programStageDataElementHandler.handleProgramStageDataElements(
                programStage.programStageDataElements());
        programStageSectionHandler.handleProgramStageSection(programStage.uid(),
                programStage.programStageSections());
        styleHandler.handle(programStage.style(),
                new ObjectStyleModelBuilder(programStage.uid(), ProgramStageModel.TABLE));
        if (action == HandleAction.Update) {
            programStageDataElementCleaner.deleteOrphan(programStage, programStage.programStageDataElements());
            programStageSectionCleaner.deleteOrphan(programStage, programStage.programStageSections());
        }
    }

    @Override
    protected void afterCollectionHandled(Collection<ProgramStage> programStages) {
        collectionCleaner.deleteNotPresent(programStages);
    }

    public static ProgramStageHandler create(DatabaseAdapter databaseAdapter, DHISVersionManager versionManager) {
        return new ProgramStageHandler(
                ProgramStageStore.create(databaseAdapter),
                ProgramStageSectionHandler.create(databaseAdapter),
                ProgramStageDataElementHandler.create(databaseAdapter),
                ObjectStyleHandler.create(databaseAdapter),
                new OrphanCleanerImpl<ProgramStage, ProgramStageDataElement>(ProgramStageDataElementModel.TABLE,
                        ProgramStageDataElementModel.Columns.PROGRAM_STAGE, databaseAdapter),
                new OrphanCleanerImpl<ProgramStage, ProgramStageSection>(ProgramStageSectionModel.TABLE,
                        ProgramStageSectionModel.Columns.PROGRAM_STAGE, databaseAdapter),
                new CollectionCleanerImpl<ProgramStage>(ProgramStageModel.TABLE, databaseAdapter),
                versionManager);
    }
}