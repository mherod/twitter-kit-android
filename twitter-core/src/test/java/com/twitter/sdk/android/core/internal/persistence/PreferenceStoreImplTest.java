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

package com.twitter.sdk.android.core.internal.persistence;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class PreferenceStoreImplTest {
    PreferenceStoreImpl preferenceStore;

    @Before
    public void setUp() {
        preferenceStore = new PreferenceStoreImpl(RuntimeEnvironment.application, "Stub");
    }

    @Test
    @SuppressLint("CommitPrefEdits")
    public void testGet() {
        Assert.assertThat(preferenceStore.get(), notNullValue());
        Assert.assertThat(preferenceStore.get() instanceof SharedPreferences, is(true));
    }

    @Test
    @SuppressLint("CommitPrefEdits")
    public void testEdit() {
        Assert.assertThat(preferenceStore.edit(), notNullValue());
        Assert.assertThat(preferenceStore.edit() instanceof SharedPreferences.Editor, is(true));
    }

    @Test
    @SuppressLint("CommitPrefEdits")
    public void testSave() {
        final String key = "Test Key";
        final String value = "Test Value";
        final SharedPreferences.Editor editor = preferenceStore.edit();
        editor.putString(key, value);
        Assert.assertThat(preferenceStore.save(editor), is(true));

        final String result = preferenceStore.get().getString(key, null);

        Assert.assertThat(result, notNullValue());
        Assert.assertThat(result, is(value));
    }

    @Test
    @SuppressLint("CommitPrefEdits")
    public void testNamespace() {
        final String key = "Test namespace key";
        final String value = "Test namespace value";

        final PreferenceStoreImpl secondPrefStore =
                new PreferenceStoreImpl(RuntimeEnvironment.application, "PersistenceTest");

        Assert.assertThat(secondPrefStore.get(), not(sameInstance(preferenceStore.get())));

        preferenceStore.save(preferenceStore.edit().putString(key, value));

        Assert.assertThat(secondPrefStore.get().getString(key, null), nullValue());

    }
}
