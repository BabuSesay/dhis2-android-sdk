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

package org.hisp.dhis.android.core.configuration.internal;

import org.hisp.dhis.android.core.arch.storage.internal.ObjectSecureStore;
import org.hisp.dhis.android.core.arch.storage.internal.SecureStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import okhttp3.HttpUrl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class ConfigurationSecureStoreShould {

    @Mock
    private SecureStore store;

    private ObjectSecureStore<Configuration> configurationSecureStore;

    private final static String KEY = "server_url";

    private final String SERVER_URL = "http://testserver.org/";

    private final Configuration configuration = Configuration.forServerUrl(HttpUrl.parse(SERVER_URL));

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        configurationSecureStore = new ConfigurationSecureStoreImpl(store);
    }

    @Test
    public void return_correct_values_when_configuration_manager_is_configured_with_saved_store() {
        when(store.getData(KEY)).thenReturn(SERVER_URL);

        Configuration dbConfiguration = configurationSecureStore.get();

        verify(store).getData(KEY);
        assertThat(dbConfiguration).isEqualTo(configuration);
    }

    @Test
    public void thrown_illegal_argument_exception_after_configure_with_null_argument() {
        try {
            configurationSecureStore.set(null);

            fail("IllegalArgumentException was not thrown");
        } catch (IllegalArgumentException illegalArgumentException) {
            // swallow exception
        }
    }

    @Test
    public void return_null_if_configuration_is_not_persisted() {
        Configuration configuration = configurationSecureStore.get();

        verify(store).getData(KEY);
        assertThat(configuration).isNull();
    }

    @Test
    public void invoke_remove_data_on_secure_store_when_remove_method_is_called() {
        configurationSecureStore.remove();
        verify(store).removeData(KEY);
    }

    @Test
    public void invoke_set_data_on_secure_store_when_configuring() {
        configurationSecureStore.set(configuration);
        verify(store).setData(KEY, SERVER_URL);
    }
}