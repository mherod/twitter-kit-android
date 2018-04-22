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

import android.os.Environment;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
public class FileStoreImplTest {
    FileStoreImpl fileStore;

    @Before
    public void setUp() throws Exception {
        fileStore = new FileStoreImpl(RuntimeEnvironment.application);
    }

    @Test
    public void testConstructor() {
        try {
            new FileStoreImpl(null);
            fail();
        } catch (IllegalArgumentException ex) {
            Assert.assertThat(ex.getMessage(), is("Context must not be null"));
        }
    }

    @Test
    public void testGetCacheDir() {
        verifyFile(fileStore.getCacheDir());
    }

    @Test
    public void testGetFilesDir() {
        verifyFile(fileStore.getFilesDir());
    }

    @Test
    public void testGetExternalCacheDir() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        verifyFile(fileStore.getExternalCacheDir());
    }

    @Test
    public void testGetExternalCacheDir_withoutExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_REMOVED);
        Assert.assertThat(fileStore.getExternalCacheDir(), nullValue());
    }

    @Test
    public void testGetExternalFilesDir() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
        verifyFile(fileStore.getExternalFilesDir());
    }

    @Test
    public void testGetExternalFilesDir_withoutExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_REMOVED);
        Assert.assertThat(fileStore.getExternalFilesDir(), nullValue());
    }

    @Test
    public void testPrepare() {
        verifyFile(fileStore.prepare(new File(RuntimeEnvironment.application.getFilesDir(),
                "FileStoreImplTest/")));
    }

    @Test
    public void testisExternalStorageAvailable() {
        final String state = Environment.getExternalStorageState();
        Assert.assertThat(fileStore.isExternalStorageAvailable(), is(Environment.MEDIA_MOUNTED.equals(state)));
    }

    private void verifyFile(File file) {
        Assert.assertThat(file, notNullValue());
        Assert.assertThat(file.exists(), is(true));
    }
}
