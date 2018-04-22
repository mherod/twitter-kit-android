/*
 * Copyright (C) 2015 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.twitter.sdk.android.core.internal;

import android.text.format.DateUtils;

import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.SessionManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class SessionMonitorTest {

    private static final long TEST_TIME_1200_UTC = 1422014401245L;
    private static final long TEST_TIME_2359_UTC = 1422057541245L;
    private static final long TEST_TIME_0001_UTC = 1422057661245L;

    private SessionManager<Session> mockSessionManager;
    private SystemCurrentTimeProvider mockSystemCurrentTimeProvider;
    private ExecutorService mockExecutorService;
    private SessionMonitor.MonitorState mockMonitorState;

    private SessionMonitor<Session> sessionMonitor;
    private SessionMonitor.MonitorState monitorState;
    private Map<Long, Session> sessionMap;
    private SessionVerifier mockSessionVerifier;

    @Before
    public void setUp() throws Exception {

        mockSessionManager = mock(SessionManager.class);
        mockSystemCurrentTimeProvider = mock(SystemCurrentTimeProvider.class);
        mockExecutorService = mock(ExecutorService.class);
        mockMonitorState = mock(SessionMonitor.MonitorState.class);
        mockSessionVerifier = mock(SessionVerifier.class);
        sessionMonitor = new SessionMonitor<>(mockSessionManager, mockSystemCurrentTimeProvider,
                mockExecutorService, mockMonitorState, mockSessionVerifier);
        monitorState = new SessionMonitor.MonitorState();
        final Session testSession = mock(Session.class);
        when(testSession.getId()).thenReturn(1L);
        sessionMap = new HashMap<>();
        sessionMap.put(1L, testSession);

        when(mockSessionManager.getSessionMap()).thenReturn(sessionMap);
        when(mockSessionManager.getActiveSession()).thenReturn(testSession);
    }

    @Test
    public void testMonitorActivityLifecycle_registersActivityLifecycleCallbacks() {
        final ActivityLifecycleManager mockActivityLifecycleManager =
                mock(ActivityLifecycleManager.class);
        sessionMonitor = new SessionMonitor<>(mockSessionManager, mockExecutorService,
                mockSessionVerifier);

        sessionMonitor.monitorActivityLifecycle(mockActivityLifecycleManager);

        verify(mockActivityLifecycleManager)
                .registerCallbacks(any(ActivityLifecycleManager.Callbacks.class));
    }

    @Test
    public void testTriggerVerificationIfNecessary_respectsFalseStartVerification() {
        when(mockMonitorState.beginVerification(anyLong())).thenReturn(false);
        sessionMonitor.triggerVerificationIfNecessary();
        verifyZeroInteractions(mockExecutorService);
    }

    @Test
    public void testTriggerVerificationIfNecessary_doesNotTriggerIfNoActiveSession() {
        when(mockMonitorState.beginVerification(anyLong())).thenReturn(true);
        when(mockSessionManager.getActiveSession()).thenReturn(null);

        sessionMonitor.triggerVerificationIfNecessary();

        verifyZeroInteractions(mockExecutorService);
    }

    @Test
    public void testTriggerVerificationIfNecessary_submitsRunnable() {
        when(mockMonitorState.beginVerification(anyLong())).thenReturn(true);
        sessionMonitor.triggerVerificationIfNecessary();
        verify(mockExecutorService).submit(any(Runnable.class));
    }

    @Test
    public void testVerifyAll_verifiesAllSessions() {
        sessionMap.put(2L, mock(Session.class));
        sessionMonitor.verifyAll();
        verify(mockSessionVerifier, times(2)).verifySession(any(Session.class));
    }

    @Test
    public void testVerifyAll_shouldNotImmediatelyReverify() {
        when(mockSystemCurrentTimeProvider.getCurrentTimeMillis()).thenReturn(TEST_TIME_1200_UTC);
        sessionMonitor.verifyAll();
        Assert.assertThat(sessionMonitor.monitorState.beginVerification(TEST_TIME_1200_UTC + 1), is(false));
    }

    @Test
    public void testVerifySession_callsAccountService() {
        when(mockMonitorState.beginVerification(anyLong())).thenReturn(Boolean.TRUE);
        sessionMonitor.triggerVerificationIfNecessary();
        final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable
                .class);
        verify(mockExecutorService).submit(runnableArgumentCaptor.capture());
        final Runnable tasks = runnableArgumentCaptor.getValue();
        tasks.run();
        verify(mockSessionVerifier).verifySession(any(Session.class));
    }

    @Test
    public void testMonitorStateStartVerification_duringVerification() {
        final long startTime = TEST_TIME_1200_UTC;
        final long now =  startTime + 9 * DateUtils.HOUR_IN_MILLIS;

        monitorState.lastVerification = startTime;
        monitorState.verifying = true;
        Assert.assertThat(monitorState.beginVerification(now), is(false));
    }

    @Test
    public void testMonitorStateStartVerification_beforeTimeThreshold() {
        final long startTime = TEST_TIME_1200_UTC;
        final long now =  startTime + DateUtils.HOUR_IN_MILLIS;

        monitorState.lastVerification = startTime;
        monitorState.verifying = false;
        Assert.assertThat(monitorState.beginVerification(now), is(false));
    }

    @Test
    public void testMonitorStateStartVerification_dayChangedButBeforeThreshold() {
        final long startTime = TEST_TIME_2359_UTC;
        final long now =  TEST_TIME_0001_UTC;

        monitorState.lastVerification = startTime;
        monitorState.verifying = false;
        Assert.assertThat(monitorState.beginVerification(now), is(true));
    }

    @Test
    public void testMonitorStateStartVerification_pastTimeThreshold() {
        final long startTime = TEST_TIME_1200_UTC;
        final long now = startTime + (8 * DateUtils.HOUR_IN_MILLIS);

        monitorState.lastVerification = startTime;
        monitorState.verifying = false;
        Assert.assertThat(monitorState.beginVerification(now), is(true));
    }

    @Test
    public void testMonitorStateStartVerification_newState() {
        Assert.assertThat(monitorState.beginVerification(System.currentTimeMillis()), is(true));
    }

    @Test
    public void testMonitorStateStartVerification_marksVerificationInProgress() {
        Assert.assertThat(monitorState.verifying, is(false));
        Assert.assertThat(monitorState.beginVerification(System.currentTimeMillis()), is(true));
        Assert.assertThat(monitorState.verifying, is(true));
    }

    @Test
    public void testMonitorStateFinishVerification_marksVerificationDone() {
        monitorState.verifying = true;
        monitorState.lastVerification = TEST_TIME_1200_UTC;
        monitorState.endVerification(TEST_TIME_2359_UTC);
        Assert.assertThat(monitorState.verifying, is(false));
        Assert.assertThat(monitorState.lastVerification, is(TEST_TIME_2359_UTC));
    }
}
