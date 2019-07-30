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

package org.hisp.dhis.android.core.indicator.internal;

import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.data.database.IdentifiableObjectStoreAbstractIntegrationShould;
import org.hisp.dhis.android.core.data.indicator.IndicatorSamples;
import org.hisp.dhis.android.core.indicator.Indicator;
import org.hisp.dhis.android.core.indicator.IndicatorTableInfo;
import org.hisp.dhis.android.core.utils.integration.mock.DatabaseAdapterFactory;
import org.hisp.dhis.android.core.utils.runner.D2JunitRunner;
import org.junit.runner.RunWith;

@RunWith(D2JunitRunner.class)
public class IndicatorStoreIntegrationShould extends IdentifiableObjectStoreAbstractIntegrationShould<Indicator> {

    public IndicatorStoreIntegrationShould() {
        super(IndicatorStore.create(DatabaseAdapterFactory.get()), IndicatorTableInfo.TABLE_INFO, DatabaseAdapterFactory.get());
    }

    @Override
    protected Indicator buildObject() {
        return IndicatorSamples.getIndicator();
    }

    @Override
    protected Indicator buildObjectToUpdate() {
        return IndicatorSamples.getIndicator().toBuilder()
                .indicatorType(ObjectWithUid.create("new_indicator_type_uid"))
                .build();
    }
}