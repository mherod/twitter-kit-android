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

import com.twitter.sdk.android.core.models.ApiError;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.Matchers.*;

@RunWith(RobolectricTestRunner.class)
public class TwitterApiExceptionTest  {

    private static final int API_ERROR_CODE = 239;
    private static final int DEFAULT_ERROR_CODE = 0;
    private static final String API_ERROR_MESSAGE = "Bad guest token";
    private static final String API_ERROR_JSON = "{\"errors\":[{\"message\":\"Bad guest token\"," +
            "\"code\":239}]}\n";
    private static final String API_ERROR_NO_ERROR_CODE = "{\"errors\":[{\"message\":\"Bad " +
            "guest token\"}]}\n";
    private static final String API_ERROR_NO_ERRORS = "{\"errors\": null}\n";
    private static final String API_ERROR_NO_ERROR_MESSAGE = "{\"errors\":[{\"code\":239}]}\n";
    private static final String API_ERROR_NON_JSON = "not a json";

    @Test
    public void testParseErrorCode() {
        final ApiError apiError = TwitterApiException.parseApiError(API_ERROR_JSON);
        Assert.assertThat(apiError.code, is(API_ERROR_CODE));
        Assert.assertThat(apiError.message, is(API_ERROR_MESSAGE));
    }

    @Test
    public void testParseError_nonJSON() {
        Assert.assertThat(TwitterApiException.parseApiError(API_ERROR_NON_JSON), nullValue());
    }

    @Test
    public void testParseError_noErrorCode() {
        final ApiError apiError = TwitterApiException.parseApiError(API_ERROR_NO_ERROR_CODE);
        Assert.assertThat(apiError.code, is(DEFAULT_ERROR_CODE));
        Assert.assertThat(apiError.message, is(API_ERROR_MESSAGE));
    }

    @Test
    public void testParseError_noErrors() {
        final ApiError apiError = TwitterApiException.parseApiError(API_ERROR_NO_ERRORS);
        Assert.assertThat(apiError, nullValue());
    }

    @Test
    public void testParseError_noMessage() {
        final ApiError apiError = TwitterApiException.parseApiError(API_ERROR_NO_ERROR_MESSAGE);
        Assert.assertThat(apiError.code, is(API_ERROR_CODE));
        Assert.assertThat(apiError.message, nullValue());
    }

}
