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

package com.twitter.sdk.android.core

import com.google.gson.annotations.SerializedName

/**
 * Base class for session associated with [com.twitter.sdk.android.core.AuthToken].
 */
open class Session<out T : AuthToken>(
        @SerializedName("auth_token") val authToken: T?,
        @SerializedName("id") val id: Long
)
