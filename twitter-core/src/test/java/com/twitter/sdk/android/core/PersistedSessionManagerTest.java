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

package com.twitter.sdk.android.core;

import android.content.SharedPreferences;

import com.twitter.sdk.android.core.internal.persistence.PreferenceStore;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStoreImpl;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStoreStrategy;
import com.twitter.sdk.android.core.internal.persistence.SerializationStrategy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class PersistedSessionManagerTest {

    static final String PREF_KEY_SESSION = "session";

    private static final long TEST_SESSION_ID = 1L;
    private static final String PREF_RANDOM_KEY = "random_key";
    private static final String RESTORED_USER = "restoredUser";

    private PreferenceStore preferenceStore;
    private ConcurrentHashMap<Long, TwitterSession> sessionMap;
    private ConcurrentHashMap<Long, PreferenceStoreStrategy<TwitterSession>> storageMap;
    private PreferenceStoreStrategy<TwitterSession> mockActiveSessionStorage;
    private PersistedSessionManager<TwitterSession> sessionManager;

    @Before
    public void setUp() throws Exception {
        preferenceStore = new PreferenceStoreImpl(RuntimeEnvironment.application, "testSession");
        SerializationStrategy<TwitterSession> mockSerializer = mock(SerializationStrategy.class);
        sessionMap = new ConcurrentHashMap<>();
        storageMap = new ConcurrentHashMap<>();
        mockActiveSessionStorage = mock(PreferenceStoreStrategy.class);
        sessionManager = new PersistedSessionManager<>(preferenceStore,
                mockSerializer, sessionMap, storageMap, mockActiveSessionStorage,
                PREF_KEY_SESSION);
    }

    @After
    public void tearDown() {
        preferenceStore.edit().clear().commit();
    }

    @Test
    public void testIsSessionPreferenceKey_validKey() {
        final String preferenceKey = PREF_KEY_SESSION + "_" + TestFixtures.USER_ID;
        Assert.assertThat(sessionManager.isSessionPreferenceKey(preferenceKey), is(true));
    }

    @Test
    public void testIsSessionPreferenceKey_invalidKey() {
        Assert.assertThat(sessionManager.isSessionPreferenceKey(PREF_RANDOM_KEY), is(false));
    }

    @Test
    public void testRestoreSession_noSavedSession() {
        when(mockActiveSessionStorage.restore()).thenReturn(null);
        sessionManager.restoreAllSessionsIfNecessary();
        Assert.assertThat(sessionManager.getActiveSession(), nullValue());
    }

    @Test
    public void testRestoreSession_savedSession() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);
        sessionManager.restoreAllSessionsIfNecessary();
        Assert.assertThat(sessionManager.getActiveSession(), is(mockSession));
    }

    @Test
    public void testRestoreSession_multipleSavedSessions() {
        // Set up test by creating and serializing some test TwitterSessions.
        final SharedPreferences.Editor editor = preferenceStore.edit();
        final TwitterSession[] sessions = {
                new TwitterSession(new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET),
                        TestFixtures.USER_ID, TestFixtures.SCREEN_NAME),
                new TwitterSession(new TwitterAuthToken(TestFixtures.TOKEN, TestFixtures.SECRET),
                        TestFixtures.USER_ID + 1, TestFixtures.SCREEN_NAME + "1")
        };
        final TwitterSession.Serializer serializer = new TwitterSession.Serializer();
        final PersistedSessionManager<TwitterSession> localSessionManager =
                new PersistedSessionManager<>(preferenceStore,
                        serializer, sessionMap, storageMap, mockActiveSessionStorage,
                        PREF_KEY_SESSION);
        for (TwitterSession session : sessions) {
            final String serializedObject = serializer.serialize(session);
            editor.putString(localSessionManager.getPrefKey(session.getId()), serializedObject);
        }
        preferenceStore.save(editor);

        localSessionManager.restoreAllSessionsIfNecessary();
        assertMapSizes(sessions.length);
        for (TwitterSession session : sessions) {
            Assert.assertThat(localSessionManager.getSession(session.getId()), is(session));
        }
    }

    @Test
    public void testRestoreSession_invalidPreferenceKey() {
        final SharedPreferences.Editor editor = preferenceStore.edit();
        editor.putString(PREF_RANDOM_KEY, "random value");
        preferenceStore.save(editor);

        sessionManager.restoreAllSessionsIfNecessary();
        assertMapSizes(0);
    }

    @Test
    public void testRestoreSession_multipleRestoreCalls() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);

        Assert.assertThat(sessionManager.getActiveSession(), is(mockSession));
        sessionManager.restoreAllSessionsIfNecessary();

        // restore should only be called once.
        verify(mockActiveSessionStorage).restore();
    }

    @Test
    public void testRestoreSession_afterActiveSessionSetExternally() {
        final TwitterSession mockRestoredSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockRestoredSession);

        final TwitterSession mockActiveSession = mock(TwitterSession.class);
        sessionManager.setActiveSession(mockActiveSession);
        sessionManager.restoreAllSessionsIfNecessary();

        Assert.assertThat(sessionManager.getActiveSession(), is(mockActiveSession));
    }

    @Test
    public void testGetActiveSession_restoredSession() {
        final TwitterSession mockRestoredSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockRestoredSession);

        final TwitterSession activeSession = sessionManager.getActiveSession();
        Assert.assertThat(activeSession, is(mockRestoredSession));
        verify(mockActiveSessionStorage).restore();
    }

    @Test
    public void testGetActiveSession_nullSession() {
        Assert.assertThat(sessionManager.getActiveSession(), nullValue());
    }

    @Test
    public void testGetActiveSession_validSession() {
        final TwitterSession session = setupActiveSessionTest();
        Assert.assertThat(sessionManager.getActiveSession(), is(session));
    }

    private TwitterSession setupActiveSessionTest() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockSession.getId()).thenReturn(TEST_SESSION_ID);
        sessionManager.setActiveSession(mockSession);
        return mockSession;
    }

    @Test
    public void testSetActiveSession_nullSession() {
        try {
            sessionManager.setActiveSession(null);
            fail();
        } catch (Exception e) {
            Assert.assertThat(e instanceof IllegalArgumentException, is(true));
        }
    }

    @Test
    public void testSetActiveSession_validSession() {
        final TwitterSession session = setupActiveSessionTest();
        final int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);

        verify(mockActiveSessionStorage).save(session);
        Assert.assertThat(sessionManager.getActiveSession(), is(session));
        Assert.assertThat(sessionManager.getSession(session.getId()), is(session));
    }

    private void assertMapSizes(int count) {
        Assert.assertThat(sessionMap.size(), is(count));
        Assert.assertThat(storageMap.size(), is(count));
    }

    @Test
    public void testSetActiveSession_differentSession() {
        final TwitterSession session = setupActiveSessionTest();
        int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);
        verify(mockActiveSessionStorage).save(session);
        Assert.assertThat(sessionManager.getActiveSession(), is(session));

        final TwitterSession session2 = mock(TwitterSession.class);
        final long differentSessionId = session.getId() + 1;
        when(session2.getId()).thenReturn(differentSessionId);
        sessionManager.setActiveSession(session2);
        numSessionsThisTest++;
        assertMapSizes(numSessionsThisTest);
        verify(mockActiveSessionStorage).save(session2);
        Assert.assertThat(session, not(sameInstance(session2)));
        Assert.assertThat(sessionManager.getActiveSession(), is(session2));
    }

    @Test
    public void testClearActiveSession() {
        setupActiveSessionTest();
        sessionManager.clearActiveSession();
        assertMapSizes(0);
        verify(mockActiveSessionStorage).clear();
        Assert.assertThat(sessionManager.getActiveSession(), nullValue());
    }

    @Test
    public void testClearActiveSession_noActiveSession() {
        try {
            sessionManager.clearActiveSession();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testClearActiveSession_beforeRestoreSession() {
        setupActiveSessionTest();
        sessionManager.clearActiveSession();
        Assert.assertThat(sessionManager.getActiveSession(), nullValue());
    }

    @Test
    public void testGetSession() {
        final TwitterSession session = setupActiveSessionTest();
        Assert.assertThat(sessionManager.getSession(session.getId()), is(session));
    }

    @Test
    public void testGetSession_multipleSessions() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        for (int i = 0; i < count; i++) {
            final TwitterSession session = sessions.get(i);
            Assert.assertThat(sessionManager.getSession(session.getId()), is(session));
        }
    }

    private List<TwitterSession> setupMultipleSessionsTest(int count) {
        final List<TwitterSession> sessions = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            final long id = i;
            final TwitterSession session = mock(TwitterSession.class);
            when(session.getId()).thenReturn(id);
            sessionManager.setSession(id, session);
            sessions.add(session);
        }
        return sessions;
    }

    @Test
    public void testSetSession_nullSession() {
        try {
            sessionManager.setSession(TEST_SESSION_ID, null);
            fail();
        } catch (Exception e) {
            Assert.assertThat(e instanceof IllegalArgumentException, is(true));
        }
    }

    @Test
    public void testSetSession_noActiveSession() {
        final TwitterSession session = mock(TwitterSession.class);
        when(session.getId()).thenReturn(TEST_SESSION_ID);
        sessionManager.setSession(TEST_SESSION_ID, session);
        final int numSessionsThisTest = 1;
        assertMapSizes(numSessionsThisTest);
        // Verify that when setSession is called and there is no active session, the specified
        // session becomes the active session.
        verify(mockActiveSessionStorage).save(session);
        Assert.assertThat(sessionManager.getSession(TEST_SESSION_ID), is(session));
        Assert.assertThat(sessionManager.getActiveSession(), is(session));
    }

    @Test
    public void testSetSession_multipleSessions() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        assertMapSizes(count);

        for (int i = 0; i < count; i++) {
            final TwitterSession session = sessions.get(i);
            Assert.assertThat(sessionManager.getSession(session.getId()), is(session));
        }
        // Verify that the first session is still the active session.
        Assert.assertThat(sessionManager.getActiveSession(), is(sessions.get(0)));
    }

    @Test
    public void testSetSession_updateExistingSession() {
        final TwitterAuthToken authToken = mock(TwitterAuthToken.class);
        final TwitterSession session = new TwitterSession(authToken, TestFixtures.USER_ID,
                TestFixtures.SCREEN_NAME);
        final long sessionId = session.getId();
        sessionManager.setSession(sessionId, session);
        Assert.assertThat(sessionManager.getSession(sessionId), is(session));
        Assert.assertThat(sessionManager.getActiveSession(), is(session));
        assertMapSizes(1);

        final TwitterSession sessionWithDifferentUserName = new TwitterSession(authToken, sessionId,
                "differentUserName");
        sessionManager.setSession(sessionId, sessionWithDifferentUserName);
        Assert.assertThat(sessionManager.getSession(sessionId), is(sessionWithDifferentUserName));
        Assert.assertThat(sessionManager.getActiveSession(), is(sessionWithDifferentUserName));
        assertMapSizes(1);
    }

    @Test
    public void testSetSession_beforeRestoreSession() {
        final TwitterAuthToken authToken = mock(TwitterAuthToken.class);

        final TwitterSession newSession = new TwitterSession(authToken, TestFixtures.USER_ID,
                TestFixtures.SCREEN_NAME);
        final TwitterSession restoredSession =
                new TwitterSession(authToken, TestFixtures.USER_ID, RESTORED_USER);

        setupSessionForRestore(restoredSession);

        sessionManager.setSession(newSession.getId(), newSession);
        sessionManager.restoreAllSessionsIfNecessary();

        // We want to make sure that even if restore sessions is called after setSession.
        // session set in setSession will not be overwritten.
        Assert.assertThat(sessionManager.getSession(newSession.getId()), is(newSession));
    }

    private void setupSessionForRestore(final TwitterSession restoredSession) {
        final SharedPreferences.Editor editor = preferenceStore.edit();
        final TwitterSession.Serializer serializer = new TwitterSession.Serializer();
        final String serializedObject = serializer.serialize(restoredSession);
        editor.putString(sessionManager.getPrefKey(restoredSession.getId()), serializedObject);
        editor.commit();
    }

    @Test
    public void testClearSession() {
        final TwitterSession session = setupActiveSessionTest();
        sessionManager.clearSession(session.getId());
        assertMapSizes(0);
        Assert.assertThat(sessionManager.getActiveSession(), nullValue());
        Assert.assertThat(sessionManager.getSession(session.getId()), nullValue());
    }

    @Test
    public void testClearSession_noSessions() {
        try {
            sessionManager.clearSession(TEST_SESSION_ID);
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testClearSession_multipleSessionsClearFirstSession() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        int numSessionsThisTest = count;
        assertMapSizes(numSessionsThisTest);

        // Clear the first session
        final long firstSessionId = sessions.get(0).getId();
        sessionManager.clearSession(firstSessionId);
        numSessionsThisTest--;
        assertMapSizes(numSessionsThisTest);
        Assert.assertThat(sessionManager.getSession(firstSessionId), nullValue());
        // Make sure the second session is still there
        final long secondSessionId = sessions.get(1).getId();
        Assert.assertThat(sessionManager.getSession(secondSessionId), is(sessions.get(1)));
    }

    @Test
    public void testClearSession_multipleSessionsClearSecondSession() {
        final int count = 2;
        final List<TwitterSession> sessions = setupMultipleSessionsTest(count);
        int numSessionsThisTest = count;
        assertMapSizes(numSessionsThisTest);

        // Clear the second session
        final long secondSessionId = sessions.get(1).getId();
        sessionManager.clearSession(secondSessionId);
        numSessionsThisTest--;
        assertMapSizes(numSessionsThisTest);
        Assert.assertThat(sessionManager.getSession(secondSessionId), nullValue());
        // Make sure the first session is still there
        final long firstSessionId = sessions.get(0).getId();
        Assert.assertThat(sessionManager.getSession(firstSessionId), is(sessions.get(0)));
    }

    @Test
    public void testClearSession_beforeRestoreSession() {
        final TwitterSession restoredSession =
                new TwitterSession(mock(TwitterAuthToken.class), TestFixtures.USER_ID,
                        RESTORED_USER);
        setupSessionForRestore(restoredSession);
        sessionManager.clearSession(TestFixtures.USER_ID);
        sessionManager.restoreAllSessionsIfNecessary();

        Assert.assertThat(sessionManager.getSession(TestFixtures.USER_ID), nullValue());
    }

    @Test
    public void testGetPrefKey() {
        Assert.assertThat(sessionManager.getPrefKey(TEST_SESSION_ID), is(PREF_KEY_SESSION + "_" + TEST_SESSION_ID));
    }

    @Test
    public void testGetSessionMap() {
        try {
            sessionManager.getSessionMap().put(1L, null);
            fail("should be unmodifiable map");
        } catch (UnsupportedOperationException e) {
            // success
        }
    }

    @Test
    public void testGetSessionMap_restoresSessionsIfNecessary() {
        final TwitterSession mockSession = mock(TwitterSession.class);
        when(mockActiveSessionStorage.restore()).thenReturn(mockSession);
        sessionManager.getSessionMap();
        Assert.assertThat(sessionManager.getActiveSession(), is(mockSession));
    }
}
