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

import com.twitter.sdk.android.core.internal.persistence.PreferenceStore;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStoreImpl;
import com.twitter.sdk.android.core.internal.persistence.PreferenceStoreStrategy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class PreferenceStoreStrategyTest {
    private PreferenceStoreStrategy<TwitterSession> preferenceStrategy;

    @Before
    public void setUp() throws Exception {
        PreferenceStore preferenceStore = new PreferenceStoreImpl(RuntimeEnvironment.application, "testSession");
        preferenceStrategy = new PreferenceStoreStrategy<>(preferenceStore,
                new TwitterSession.Serializer(), "testSession");
    }

    @After
    public void tearDown() {
        preferenceStrategy.clear();
    }

    @Test
    public void testRestore_emptyStore() {
        Assert.assertThat(preferenceStrategy.restore(), nullValue());
    }

    @Test
    public void testSaveAndRestore_nullSession() {
        preferenceStrategy.save(null);
        final TwitterSession restoredSession = preferenceStrategy.restore();
        Assert.assertThat(restoredSession, nullValue());
    }

    @Test
    public void testSaveAndRestore_session() {
        final TwitterSession session = new TwitterSession(new TwitterAuthToken
                (TestFixtures.TOKEN, TestFixtures.SECRET), TwitterSession.UNKNOWN_USER_ID,
                TwitterSession.UNKNOWN_USER_NAME);
        preferenceStrategy.save(session);
        final TwitterSession restoredSession = preferenceStrategy.restore();
        Assert.assertThat(restoredSession, is(session));
    }
}
