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

package org.hisp.dhis.android.core.user.internal;

import org.hisp.dhis.android.core.arch.api.executors.internal.APICallErrorCatcher;
import org.hisp.dhis.android.core.arch.api.executors.internal.APICallExecutor;
import org.hisp.dhis.android.core.arch.api.fields.internal.Fields;
import org.hisp.dhis.android.core.arch.db.access.Transaction;
import org.hisp.dhis.android.core.arch.db.stores.internal.IdentifiableObjectStore;
import org.hisp.dhis.android.core.arch.db.stores.internal.ObjectWithoutUidStore;
import org.hisp.dhis.android.core.arch.handlers.internal.Handler;
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyWithDownloadObjectRepository;
import org.hisp.dhis.android.core.arch.storage.internal.Credentials;
import org.hisp.dhis.android.core.arch.storage.internal.CredentialsSecureStore;
import org.hisp.dhis.android.core.common.BaseCallShould;
import org.hisp.dhis.android.core.configuration.ConfigurationManager;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.resource.internal.ResourceHandler;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.user.AuthenticatedUser;
import org.hisp.dhis.android.core.user.User;
import org.hisp.dhis.android.core.wipe.internal.WipeModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static okhttp3.Credentials.basic;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hisp.dhis.android.core.arch.helpers.UserHelper.md5;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@RunWith(JUnit4.class)
public class UserAuthenticateCallUnitShould extends BaseCallShould {

    @Mock
    private UserService userService;

    @Mock
    private APICallExecutor apiCallExecutor;

    @Mock
    private Handler<User> userHandler;

    @Mock
    private ResourceHandler resourceHandler;

    @Mock
    private ObjectWithoutUidStore<AuthenticatedUser> authenticatedUserStore;

    @Mock
    private CredentialsSecureStore credentialsSecureStore;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private retrofit2.Call<User> authenticateAPICall;

    @Captor
    private ArgumentCaptor<String> credentialsCaptor;

    @Captor
    private ArgumentCaptor<Fields<User>> filterCaptor;

    @Mock
    private User user;

    @Mock
    private User anotherUser;

    @Mock
    private User loggedUser;

    @Mock
    private SystemInfo systemInfoFromAPI;

    @Mock
    private SystemInfo systemInfoFromDb;

    @Mock
    private AuthenticatedUser authenticatedUser;

    @Mock
    private Credentials credentials;

    @Mock
    private IdentifiableObjectStore<User> userStore;

    @Mock
    private ReadOnlyWithDownloadObjectRepository<SystemInfo> systemInfoRepository;

    @Mock
    private WipeModule wipeModule;

    @Mock
    private ConfigurationManager configurationManager;

    // call we are testing
    private Single<User> logInSingle;

    private static final String USERNAME = "test_username";
    private static final String UID = "test_uid";
    private static final String PASSWORD = "test_password";

    private static final String baseEndpoint = "https://dhis-instance.org";
    private static final String serverUrl = baseEndpoint;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();

        when(user.uid()).thenReturn(UID);
        when(loggedUser.uid()).thenReturn(UID);
        when(systemInfoFromAPI.serverDate()).thenReturn(serverDate);

        when(anotherUser.uid()).thenReturn("anotherUserUid");

        when(credentials.username()).thenReturn(USERNAME);
        when(credentials.password()).thenReturn(PASSWORD);

        when(authenticatedUser.user()).thenReturn(UID);
        when(authenticatedUser.hash()).thenReturn(md5(USERNAME, PASSWORD));

        when(systemInfoFromAPI.contextPath()).thenReturn(baseEndpoint);
        when(systemInfoFromDb.contextPath()).thenReturn(baseEndpoint);

        when(userService.authenticate(any(String.class), any(Fields.class))).thenReturn(authenticateAPICall);

        when(systemInfoRepository.download()).thenReturn(Completable.complete());
        whenAPICall().thenReturn(user);

        when(userStore.selectFirst()).thenReturn(loggedUser);
        when(systemInfoRepository.blockingGet()).thenReturn(systemInfoFromDb);

        when(databaseAdapter.beginNewTransaction()).then((Answer<Transaction>) invocation -> {
            transaction.begin();
            return transaction;
        });

        when(d2Error.errorCode()).thenReturn(D2ErrorCode.SOCKET_TIMEOUT);

        logInSingle = instantiateCall(USERNAME, PASSWORD, serverUrl);
    }

    private Single<User> instantiateCall(String username, String password, String serverUrl) {
        return new UserAuthenticateCallFactory(databaseAdapter, apiCallExecutor,
                userService, credentialsSecureStore, userHandler, resourceHandler, authenticatedUserStore,
                systemInfoRepository, userStore, wipeModule, configurationManager).logIn(username, password, serverUrl);
    }

    private OngoingStubbing<User> whenAPICall() throws D2Error {
        return when(apiCallExecutor.executeObjectCallWithErrorCatcher(same(authenticateAPICall), any(APICallErrorCatcher.class)));
    }

    @Test
    public void throw_d2_call_exception_for_null_username() {
        TestObserver<User> testObserver = instantiateCall(null, PASSWORD, serverUrl).test();
        assertD2Error(testObserver);
    }

    @Test
    public void throw_d2_call_exception_for_null_password() {
        TestObserver<User> testObserver = instantiateCall(USERNAME, null, serverUrl).test();
        assertD2Error(testObserver);
    }

    private void assertD2Error(TestObserver<User> testObserver) {
        testObserver.awaitTerminalEvent();
        assertThat(testObserver.errors().get(0)).isInstanceOf(D2Error.class);
        testObserver.dispose();
    }

    @Test
    public void invoke_server_with_correct_parameters_after_call() {
        when(userService.authenticate(
                credentialsCaptor.capture(), filterCaptor.capture())
        ).thenReturn(authenticateAPICall);

        logInSingle.blockingGet();

        assertThat(basic(USERNAME, PASSWORD))
                .isEqualTo(credentialsCaptor.getValue());
    }

    @Test
    public void not_invoke_stores_on_exception_on_call() throws D2Error {
        whenAPICall().thenThrow(d2Error);

        TestObserver<User> testObserver = logInSingle.test();
        testObserver.awaitTerminalEvent();

        assertThat(testObserver.errorCount()).isEqualTo(1);
        testObserver.dispose();

        verifyNoTransactionStarted();

        // stores must not be invoked
        verify(authenticatedUserStore, never()).updateOrInsertWhere(any(AuthenticatedUser.class));
        verifyNoMoreInteractions(userHandler);
    }

    @Test
    public void not_invoke_stores_on_exception_on_request_fail() throws Exception {
        whenAPICall().thenThrow(d2Error);
        when(d2Error.errorCode()).thenReturn(D2ErrorCode.UNEXPECTED);

        TestObserver<User> testObserver = logInSingle.test();
        testObserver.awaitTerminalEvent();

        assertThat(testObserver.errorCount()).isEqualTo(1);

        testObserver.dispose();

        verifyNoTransactionStarted();

        // stores must not be invoked
        verify(credentialsSecureStore).getCredentials();
        verify(authenticatedUserStore).selectFirst();
        verifyNoMoreInteractions(userHandler);
    }

    @Test
    public void succeed_when_no_previous_user_or_system_info() {
        logInSingle.blockingGet();
        verifySuccess();
    }

    @Test
    public void not_wipe_db_when_no_previous_user_or_system_info() throws Exception {
        logInSingle.blockingGet();

        verify(wipeModule, never()).wipeEverything();
        verifySuccess();
    }

    @Test
    public void not_wipe_db_when_previously_same_user() throws Exception {
        when(userStore.selectFirst()).thenReturn(user);

        logInSingle.blockingGet();

        verify(wipeModule, never()).wipeEverything();
        verifySuccess();
    }

    @Test
    public void wipe_db_when_previously_another_user() throws Exception {
        when(userStore.selectFirst()).thenReturn(anotherUser);

        logInSingle.blockingGet();

        verify(wipeModule).wipeEverything();
        verifySuccess();
    }

    @Test
    public void wipe_db_when_previously_equal_user_but_different_server() throws Exception {
        when(systemInfoFromDb.contextPath()).thenReturn("https://another-instance.org/");

        logInSingle.blockingGet();

        verify(wipeModule).wipeEverything();
        verifySuccess();
    }

    @Test
    public void wipe_db_when_previously_different_user() throws Exception {
        when(loggedUser.uid()).thenReturn("previous_user");

        logInSingle.blockingGet();

        verify(wipeModule).wipeEverything();
        verifySuccess();
    }

    @Test
    public void throw_d2_call_exception_state_exception_if_user_already_signed_in() {
        when(credentialsSecureStore.getCredentials()).thenReturn(credentials);
        when(authenticatedUserStore.selectFirst()).thenReturn(authenticatedUser);
        TestObserver<User> testObserver = logInSingle.test();
        assertD2Error(testObserver);
    }

    // Offline support

    @Test
    public void continue_if_user_has_logged_out() {
        when(credentialsSecureStore.getCredentials()).thenReturn(null);
        when(authenticatedUserStore.selectFirst()).thenReturn(authenticatedUser);
        logInSingle.blockingGet();
        verifySuccess();
    }

    @Test
    public void user_login_offline_if_previously_logged() throws Exception {
        whenAPICall().thenThrow(d2Error);

        when(credentialsSecureStore.getCredentials()).thenReturn(null);
        when(authenticatedUserStore.selectFirst()).thenReturn(authenticatedUser);

        logInSingle.test().awaitTerminalEvent();
        verifySuccessOffline();
    }

    @Test
    public void throw_d2_exception_if_no_previous_authenticated_user_offline() throws Exception {
        whenAPICall().thenThrow(d2Error);

        when(authenticatedUserStore.selectFirst()).thenReturn(null);

        TestObserver<User> testObserver = logInSingle.test();
        testObserver.awaitTerminalEvent();

        D2Error d2Error = (D2Error) testObserver.errors().get(0);
        assertThat(d2Error.errorCode()).isEqualTo(D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE);

        testObserver.dispose();
    }

    @Test
    public void throw_d2_exception_if_different_authenticated_user_offline() throws Exception {
        whenAPICall().thenThrow(d2Error);

        when(credentialsSecureStore.getCredentials()).thenReturn(null);
        when(authenticatedUser.hash()).thenReturn("different_hash");
        when(authenticatedUserStore.selectFirst()).thenReturn(authenticatedUser);


        TestObserver<User> testObserver = logInSingle.test();
        testObserver.awaitTerminalEvent();

        D2Error d2Error = (D2Error) testObserver.errors().get(0);
        assertThat(d2Error.errorCode()).isEqualTo(D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE);

        testObserver.dispose();
    }

    private void verifySuccess() {
        AuthenticatedUser authenticatedUserModel =
                AuthenticatedUser.builder()
                .user(UID)
                .hash(md5(USERNAME, PASSWORD))
                .build();
        verifyTransactionComplete();
        verify(authenticatedUserStore).updateOrInsertWhere(authenticatedUserModel);
        verify(userHandler).handle(eq(user));
    }

    private void verifySuccessOffline() {
        AuthenticatedUser authenticatedUserModel =
                AuthenticatedUser.builder()
                        .user(UID)
                        .hash(md5(USERNAME, PASSWORD))
                        .build();
        verifyTransactionComplete();
        verify(authenticatedUserStore).updateOrInsertWhere(authenticatedUserModel);
    }
}
