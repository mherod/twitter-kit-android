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

package com.twitter.sdk.android.tweetcomposer;

import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadows.ShadowMimeTypeMap;

import java.io.File;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class FileUtilsTest {
    ShadowMimeTypeMap mimeTypeMap;

    @Before
    public void setUp() {
        final ShadowMimeTypeMap mimeTypeMap = (ShadowMimeTypeMap) ShadowExtractor
                .extract(MimeTypeMap.getSingleton());
        mimeTypeMap.addExtensionMimeTypMapping("jpg", "image/jpeg");
        mimeTypeMap.addExtensionMimeTypMapping("jpeg", "image/jpeg");
        mimeTypeMap.addExtensionMimeTypMapping("png", "image/png");
        this.mimeTypeMap = mimeTypeMap;
    }

    @Test
    public void testIsMediaDocumentAuthority() {
        final Uri uri = new Uri.Builder()
                .scheme("content")
                .authority("com.android.providers.media.documents")
                .path("image%3A59161")
                .build();
        Assert.assertThat(FileUtils.isMediaDocumentAuthority(uri), is(true));
    }

    @Test
    public void testIsContentScheme() {
        final Uri uri = new Uri.Builder().scheme("content").build();
        Assert.assertThat(FileUtils.isContentScheme(uri), is(true));
    }

    @Test
    public void testIsFileScheme() {
        final Uri uri = new Uri.Builder().scheme("file").build();
        Assert.assertThat(FileUtils.isFileScheme(uri), is(true));
    }

    @Test
    public void testGetMimeType() {
        Assert.assertThat(FileUtils.getMimeType(new File("file.png")), is("image/png"));
        Assert.assertThat(FileUtils.getMimeType(new File("file.jpeg")), is("image/jpeg"));
        Assert.assertThat(FileUtils.getMimeType(new File("file.jpeg")), is("image/jpeg"));
        Assert.assertThat(FileUtils.getMimeType(new File("")), is("application/octet-stream"));
    }

    @Test
    public void testExtensionToMimeType() {
        Assert.assertThat(mimeTypeMap.getMimeTypeFromExtension("png"), is("image/png"));
        Assert.assertThat(mimeTypeMap.getMimeTypeFromExtension("jpg"), is("image/jpeg"));
        Assert.assertThat(mimeTypeMap.getMimeTypeFromExtension("jpeg"), is("image/jpeg"));
        Assert.assertThat(mimeTypeMap.getMimeTypeFromExtension(""), nullValue());
    }

    @Test
    public void testGetExtension() {
        Assert.assertThat(FileUtils.getExtension(""), is(""));
        Assert.assertThat(FileUtils.getExtension("file"), is(""));
        Assert.assertThat(FileUtils.getExtension("file."), is(""));
        Assert.assertThat(FileUtils.getExtension("file.png"), is("png"));
        Assert.assertThat(FileUtils.getExtension("file.jpg"), is("jpg"));
    }
}
