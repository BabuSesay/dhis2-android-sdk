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

import org.hisp.dhis.android.core.arch.handlers.IdentifiableSyncHandlerImpl;
import org.hisp.dhis.android.core.arch.handlers.SyncHandlerWithTransformer;
import org.hisp.dhis.android.core.common.CollectionCleaner;
import org.hisp.dhis.android.core.common.CollectionCleanerImpl;
import org.hisp.dhis.android.core.common.GenericHandler;
import org.hisp.dhis.android.core.common.HandleAction;
import org.hisp.dhis.android.core.common.IdentifiableHandlerImpl;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectStyleHandler;
import org.hisp.dhis.android.core.common.ObjectStyleModelBuilder;
import org.hisp.dhis.android.core.common.ParentOrphanCleaner;
import org.hisp.dhis.android.core.arch.db.implementations.DatabaseAdapter;

import java.util.Collection;

public class ProgramHandler extends IdentifiableHandlerImpl<Program, ProgramModel> {

    private final ProgramRuleVariableHandler programRuleVariableHandler;
    private final GenericHandler<ProgramIndicator, ProgramIndicatorModel> programIndicatorHandler;
    private final IdentifiableSyncHandlerImpl<ProgramRule> programRuleHandler;
    private final GenericHandler<ProgramTrackedEntityAttribute, ProgramTrackedEntityAttributeModel>
            programTrackedEntityAttributeHandler;
    private final GenericHandler<ProgramSection, ProgramSectionModel> programSectionHandler;
    private final SyncHandlerWithTransformer<ObjectStyle> styleHandler;
    private final ParentOrphanCleaner<Program> orphanCleaner;
    private final CollectionCleaner<Program> collectionCleaner;

    ProgramHandler(IdentifiableObjectStore<ProgramModel> programStore,
                   ProgramRuleVariableHandler programRuleVariableHandler,
                   GenericHandler<ProgramIndicator, ProgramIndicatorModel> programIndicatorHandler,
                   IdentifiableSyncHandlerImpl<ProgramRule> programRuleHandler,
                   GenericHandler<ProgramTrackedEntityAttribute, ProgramTrackedEntityAttributeModel>
                           programTrackedEntityAttributeHandler,
                   GenericHandler<ProgramSection, ProgramSectionModel> programSectionHandler,
                   SyncHandlerWithTransformer<ObjectStyle> styleHandler,
                   ParentOrphanCleaner<Program> orphanCleaner,
                   CollectionCleaner<Program> collectionCleaner) {
        super(programStore);
        this.programRuleVariableHandler = programRuleVariableHandler;
        this.programIndicatorHandler = programIndicatorHandler;
        this.programRuleHandler = programRuleHandler;
        this.programTrackedEntityAttributeHandler = programTrackedEntityAttributeHandler;
        this.programSectionHandler = programSectionHandler;
        this.styleHandler = styleHandler;
        this.orphanCleaner = orphanCleaner;
        this.collectionCleaner = collectionCleaner;
    }

    public static ProgramHandler create(DatabaseAdapter databaseAdapter) {
        return new ProgramHandler(
                ProgramStore.create(databaseAdapter),
                ProgramRuleVariableHandler.create(databaseAdapter),
                ProgramIndicatorHandler.create(databaseAdapter),
                ProgramRuleHandler.create(databaseAdapter),
                ProgramTrackedEntityAttributeHandler.create(databaseAdapter),
                ProgramSectionHandler.create(databaseAdapter),
                ObjectStyleHandler.create(databaseAdapter),
                ProgramOrphanCleaner.create(databaseAdapter),
                new CollectionCleanerImpl<Program>(ProgramModel.TABLE, databaseAdapter)
        );
    }

    @Override
    protected void afterObjectHandled(Program program, HandleAction action) {
        programTrackedEntityAttributeHandler.handleMany(program.programTrackedEntityAttributes(),
                new ProgramTrackedEntityAttributeModelBuilder());
        programIndicatorHandler.handleMany(program.programIndicators(), new ProgramIndicatorModelBuilder());
        programRuleHandler.handleMany(program.programRules());
        programRuleVariableHandler.handleProgramRuleVariables(program.programRuleVariables());
        programSectionHandler.handleMany(program.programSections(), new ProgramSectionModelBuilder());
        styleHandler.handle(program.style(), new ObjectStyleModelBuilder(program.uid(), ProgramModel.TABLE));

        if (action == HandleAction.Update) {
            orphanCleaner.deleteOrphan(program);
        }
    }

    @Override
    protected void afterCollectionHandled(Collection<Program> programs) {
        collectionCleaner.deleteNotPresent(programs);
    }
}