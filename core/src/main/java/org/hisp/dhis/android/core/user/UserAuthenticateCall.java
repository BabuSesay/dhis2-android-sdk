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

package org.hisp.dhis.android.core.user;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.D2InternalModules;
import org.hisp.dhis.android.core.arch.handlers.SyncHandler;
import org.hisp.dhis.android.core.arch.repositories.object.ReadOnlyObjectRepository;
import org.hisp.dhis.android.core.calls.factories.NoArgumentsCallFactory;
import org.hisp.dhis.android.core.arch.api.executors.APICallExecutor;
import org.hisp.dhis.android.core.arch.api.executors.APICallExecutorImpl;
import org.hisp.dhis.android.core.common.D2CallExecutor;
import org.hisp.dhis.android.core.common.GenericCallData;
import org.hisp.dhis.android.core.common.IdentifiableObjectStore;
import org.hisp.dhis.android.core.common.ObjectWithoutUidStore;
import org.hisp.dhis.android.core.common.SyncCall;
import org.hisp.dhis.android.core.data.database.DatabaseAdapter;
import org.hisp.dhis.android.core.data.database.Transaction;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.maintenance.D2ErrorCode;
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent;
import org.hisp.dhis.android.core.resource.ResourceHandler;
import org.hisp.dhis.android.core.resource.ResourceModel;
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.wipe.WipeModule;
import org.hisp.dhis.android.core.wipe.WipeModuleImpl;

import retrofit2.Call;
import retrofit2.Retrofit;

import static okhttp3.Credentials.basic;
import static org.hisp.dhis.android.core.utils.UserUtils.base64;
import static org.hisp.dhis.android.core.utils.UserUtils.md5;

public final class UserAuthenticateCall extends SyncCall<User> {

    private final DatabaseAdapter databaseAdapter;
    private final Retrofit retrofit;
    private final APICallExecutor apiCallExecutor;

    private final NoArgumentsCallFactory<SystemInfo> systemInfoCallFactory;
    private final DHISVersionManager versionManager;

    // retrofit service
    private final UserService userService;

    private final SyncHandler<User> userHandler;
    private final ResourceHandler resourceHandler;
    private final ObjectWithoutUidStore<AuthenticatedUserModel> authenticatedUserStore;
    private final ReadOnlyObjectRepository<SystemInfo> systemInfoRepository;
    private final IdentifiableObjectStore<User> userStore;
    private final WipeModule wipeModule;

    // username and password of candidate
    private final String username;
    private final String password;
    private final String apiURL;

    UserAuthenticateCall(
            @NonNull DatabaseAdapter databaseAdapter,
            @NonNull Retrofit retrofit,
            @NonNull APICallExecutor apiCallExecutor,
            @NonNull NoArgumentsCallFactory<SystemInfo> systemInfoCallFactory,
            @NonNull DHISVersionManager versionManager,
            @NonNull UserService userService,
            @NonNull SyncHandler<User> userHandler,
            @NonNull ResourceHandler resourceHandler,
            @NonNull ObjectWithoutUidStore<AuthenticatedUserModel> authenticatedUserStore,
            @NonNull ReadOnlyObjectRepository<SystemInfo> systemInfoRepository,
            @NonNull IdentifiableObjectStore<User> userStore,
            @NonNull WipeModule wipeModule,
            @NonNull String username,
            @NonNull String password,
            @NonNull String apiURL) {
        this.databaseAdapter = databaseAdapter;
        this.retrofit = retrofit;
        this.apiCallExecutor = apiCallExecutor;

        this.systemInfoCallFactory = systemInfoCallFactory;
        this.versionManager = versionManager;
        this.userService = userService;

        this.userHandler = userHandler;
        this.resourceHandler = resourceHandler;
        this.authenticatedUserStore = authenticatedUserStore;
        this.systemInfoRepository = systemInfoRepository;
        this.userStore = userStore;
        this.wipeModule = wipeModule;

        this.username = username;
        this.password = password;

        this.apiURL = apiURL;
    }

    @Override
    public User call() throws D2Error {
        setExecuted();
        throwExceptionIfUsernameNull();
        throwExceptionIfPasswordNull();
        throwExceptionIfAlreadyAuthenticated();

        Call<User> authenticateCall =
                userService.authenticate(basic(username, password), UserFields.allFieldsWithoutOrgUnit);

        try {
            User authenticatedUser = apiCallExecutor.executeObjectCallWithErrorCatcher(authenticateCall,
                    new UserAuthenticateCallErrorCatcher());
            return loginOnline(authenticatedUser);
        } catch (D2Error d2Error) {
            if (
                    d2Error.errorCode() == D2ErrorCode.API_RESPONSE_PROCESS_ERROR ||
                    d2Error.errorCode() == D2ErrorCode.SOCKET_TIMEOUT ||
                    d2Error.errorCode() == D2ErrorCode.UNKNOWN_HOST) {
                return loginOffline();
            } else if (d2Error.errorCode() == D2ErrorCode.USER_ACCOUNT_DISABLED) {
                wipeModule.wipeEverything();
                throw d2Error;
            } else {
                throw d2Error;
            }
        }
    }

    private User loginOnline(User authenticatedUser) throws D2Error {
        if (wasLoggedAndUserIsNew(authenticatedUser) || wasLoggedAndServerIsNew()) {
            wipeModule.wipeEverything();
        }

        Transaction transaction = databaseAdapter.beginNewTransaction();
        try {
            AuthenticatedUserModel authenticatedUserModel = buildAuthenticatedUserModel(authenticatedUser.uid());
            authenticatedUserStore.updateOrInsertWhere(authenticatedUserModel);
            SystemInfo systemInfo = new D2CallExecutor().executeD2Call(systemInfoCallFactory.create());
            handleUser(authenticatedUser, GenericCallData.create(databaseAdapter, retrofit, systemInfo.serverDate(),
                    versionManager));
            transaction.setSuccessful();
            return authenticatedUser;
        } finally {
            transaction.end();
        }
    }

    private User loginOffline() throws D2Error {
        if (wasLoggedAndServerIsNew()) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.DIFFERENT_SERVER_OFFLINE)
                    .errorDescription("Cannot switch servers offline.")
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }

        AuthenticatedUserModel existingUser = authenticatedUserStore.selectFirst();

        if (existingUser == null) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.NO_AUTHENTICATED_USER_OFFLINE)
                    .errorDescription("No user has been previously authenticated. Cannot login offline.")
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }

        if (!md5(username, password).equals(existingUser.hash())) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.DIFFERENT_AUTHENTICATED_USER_OFFLINE)
                    .errorDescription("Credentials do not match authenticated user. Cannot switch users offline.")
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }

        Transaction transaction = databaseAdapter.beginNewTransaction();
        try {
            AuthenticatedUserModel authenticatedUserModel = buildAuthenticatedUserModel(existingUser.user());
            authenticatedUserStore.updateOrInsertWhere(authenticatedUserModel);
            transaction.setSuccessful();
        } finally {
            transaction.end();
        }

        return userStore.selectByUid(existingUser.user());
    }

    private void throwExceptionIfUsernameNull() throws D2Error {
        if (username == null) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.LOGIN_USERNAME_NULL)
                    .errorDescription("Username is null")
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }
    }

    private void throwExceptionIfPasswordNull() throws D2Error {
        if (password == null) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.LOGIN_PASSWORD_NULL)
                    .errorDescription("Password is null")
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }
    }

    private void throwExceptionIfAlreadyAuthenticated() throws D2Error {
        AuthenticatedUserModel authenticatedUser = authenticatedUserStore.selectFirst();
        if (authenticatedUser != null && authenticatedUser.credentials() != null) {
            throw D2Error.builder()
                    .errorCode(D2ErrorCode.ALREADY_AUTHENTICATED)
                    .errorDescription("A user is already authenticated: " + authenticatedUser.user())
                    .errorComponent(D2ErrorComponent.SDK)
                    .build();
        }
    }

    private boolean wasLoggedAndUserIsNew(User newUser) {
        User lastUser = userStore.selectFirst();
        return lastUser != null && !lastUser.uid().equals(newUser.uid());
    }

    private boolean wasLoggedAndServerIsNew() {
        SystemInfo lastSystemInfo = systemInfoRepository.get();
        return lastSystemInfo != null && !(lastSystemInfo.contextPath() + "/api/").equals(apiURL);
    }

    private void handleUser(User user, GenericCallData genericCallData) {
        userHandler.handle(user);
        resourceHandler.handleResource(ResourceModel.Type.USER, genericCallData.serverDate());
        resourceHandler.handleResource(ResourceModel.Type.USER_CREDENTIALS, genericCallData.serverDate());
        resourceHandler.handleResource(ResourceModel.Type.AUTHENTICATED_USER, genericCallData.serverDate());
    }

    private AuthenticatedUserModel buildAuthenticatedUserModel(String uid) {
        return AuthenticatedUserModel.builder()
                .user(uid)
                .credentials(base64(username, password))
                .hash(md5(username, password))
                .build();
    }

    public static UserAuthenticateCall create(
            @NonNull DatabaseAdapter databaseAdapter,
            @NonNull Retrofit retrofit,
            @NonNull D2InternalModules internalModules,
            @NonNull String username,
            @NonNull String password) {
        return new UserAuthenticateCall(
                databaseAdapter,
                retrofit,
                APICallExecutorImpl.create(databaseAdapter),
                internalModules.systemInfo.callFactory,
                internalModules.systemInfo.publicModule.versionManager,
                retrofit.create(UserService.class),
                UserHandler.create(databaseAdapter),
                ResourceHandler.create(databaseAdapter),
                AuthenticatedUserStore.create(databaseAdapter),
                internalModules.systemInfo.publicModule.systemInfo,
                UserStore.create(databaseAdapter),
                WipeModuleImpl.create(databaseAdapter, internalModules),
                username,
                password,
                retrofit.baseUrl().toString()
        );
    }
}