/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.client.sdk.models;

import org.hisp.dhis.client.models.enrollment.EnrollmentIntegrationTest;
import org.hisp.dhis.client.sdk.models.constant.ConstantIntegrationTests;
import org.hisp.dhis.client.sdk.models.dataelement.CategoryComboIntegrationTests;
import org.hisp.dhis.client.sdk.models.dataelement.CategoryIntegrationTests;
import org.hisp.dhis.client.sdk.models.dataelement.CategoryOptionComboIntegrationTests;
import org.hisp.dhis.client.sdk.models.dataelement.CategoryOptionIntegrationTests;
import org.hisp.dhis.client.sdk.models.dataelement.DataElementIntegrationTests;
import org.hisp.dhis.client.sdk.models.event.EventIntegrationTest;
import org.hisp.dhis.client.sdk.models.option.OptionIntegrationTests;
import org.hisp.dhis.client.sdk.models.option.OptionSetIntegrationTests;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnitIntegrationTests;
import org.hisp.dhis.client.sdk.models.program.ProgramIndicatorIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleActionIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleVariableIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElementIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramStageIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSectionIntegrationTest;
import org.hisp.dhis.client.sdk.models.program.ProgramTrackedEntityAttributeIntegrationTest;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityAttributeIntegrationTest;
import org.hisp.dhis.client.sdk.models.trackedentity.TrackedEntityIntegrationTest;
import org.hisp.dhis.client.sdk.models.user.UserCredentialIntegrationTest;
import org.hisp.dhis.client.sdk.models.user.UserIntegrationTest;
import org.hisp.dhis.client.sdk.models.user.UserRoleIntegrationTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        ConstantIntegrationTests.class,
        CategoryOptionIntegrationTests.class,
        UserIntegrationTest.class,
        UserCredentialIntegrationTest.class,
        UserRoleIntegrationTest.class,
        CategoryIntegrationTests.class,
        CategoryOptionIntegrationTests.class,
        CategoryComboIntegrationTests.class,
        CategoryOptionComboIntegrationTests.class,
        DataElementIntegrationTests.class,
        EventIntegrationTest.class,
        OptionSetIntegrationTests.class,
        OptionIntegrationTests.class,
        OrganisationUnitIntegrationTests.class,
        TrackedEntityIntegrationTest.class,
        TrackedEntityAttributeIntegrationTest.class,
        ProgramStageDataElementIntegrationTest.class,
        ProgramRuleActionIntegrationTest.class,
        ProgramStageSectionIntegrationTest.class,
        ProgramStageIntegrationTest.class,
        ProgramRuleIntegrationTest.class,
        ProgramRuleVariableIntegrationTest.class,
        ProgramIntegrationTest.class,
        ProgramRuleIntegrationTest.class,
        ProgramIndicatorIntegrationTest.class,
        ProgramTrackedEntityAttributeIntegrationTest.class,
        EnrollmentIntegrationTest.class
})
public class ModelsIntegrationTestsSuite {
}
