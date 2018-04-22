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

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Represents an authorization token and its secret.
 */
@Parcelize
open class TwitterAuthToken @JvmOverloads constructor(
        @field:SerializedName("token") var token: String?,
        @field:SerializedName("secret") var secret: String?,
        override val createdAt: Long = System.currentTimeMillis()
) : AuthToken(createdAt), Parcelable {

    override val isExpired: Boolean
        get() = false // Twitter does not expire OAuth1a tokens

    override fun toString(): String = "token=$token,secret=$secret"
}
