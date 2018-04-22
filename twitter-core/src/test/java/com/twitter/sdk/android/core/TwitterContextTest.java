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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.io.File;

import static org.hamcrest.Matchers.*;

@SuppressWarnings("FieldCanBeLocal")
@RunWith(RobolectricTestRunner.class)
public class TwitterContextTest {
    private static final String ROOT_DIR = ".Fabric";
    private String name;
    private String componentPath;
    private Context context;

    @Before
    public void setUp() throws Exception {
        name = "com.twitter.sdk.android:twitter-core";
        componentPath = ROOT_DIR + "/" + name;
        context = new TwitterContext(RuntimeEnvironment.application, name, componentPath);
    }

    @Test
    public void testGetDatabasePath() {
        final File file = context.getDatabasePath("a");
        assertValidDirStructure(file);
        Assert.assertThat(file.getParentFile().exists(), is(true));
    }

    @Test
    public void testOpenOrCreateDatabase() {
        final SQLiteDatabase db = context.openOrCreateDatabase("b", Context.MODE_PRIVATE, null);
        Assert.assertThat(db, notNullValue());
    }

    @Test
    public void testOpenOrCreateDatabaseWithErrorHandler() {
        final SQLiteDatabase db =
                context.openOrCreateDatabase("b", Context.MODE_PRIVATE, null, null);
        Assert.assertThat(db, notNullValue());
    }

    @Test
    public void testGetFilesDir() {
        final File file = context.getFilesDir();
        assertValidDirStructure(file);
    }

    @Test
    public void testExternalGetFilesDir() {
        final File file = context.getExternalFilesDir(null);
        assertValidDirStructure(file);
    }

    @Test
    public void testGetCacheDir() {
        final File file = context.getCacheDir();
        assertValidDirStructure(file);
    }

    @Test
    public void testGetExternalCacheDir() {
        final File file = context.getExternalCacheDir();
        assertValidDirStructure(file);
    }

    @Test
    public void testGetSharedPreferences() {
        final String testName = "test";
        final String testComponentPath = ROOT_DIR + "/" + testName;
        final Context testContext =
                new TwitterContext(RuntimeEnvironment.application, testName, testComponentPath);

        final SharedPreferences pref =
                context.getSharedPreferences(testName, Context.MODE_PRIVATE);
        final SharedPreferences testPref =
                testContext.getSharedPreferences(testName, Context.MODE_PRIVATE);

        Assert.assertThat(pref, notNullValue());
        Assert.assertThat(testPref, not(is(pref)));
        pref.edit().putBoolean(testName, true).commit();
        Assert.assertThat(testPref.getBoolean(testName, false), is(false));

    }

    private void assertValidDirStructure(File file) {
        Assert.assertThat(file, notNullValue());
        Assert.assertThat(file.getPath(), containsString(name));
        Assert.assertThat(file.getPath(), containsString(ROOT_DIR));
    }
}
