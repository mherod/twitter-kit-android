@file:JvmName("UserUtils")

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

package com.twitter.sdk.android.core.internal

import com.twitter.sdk.android.core.models.User

object UserUtils {

    fun getProfileImageUrlHttps(user: User?, size: AvatarSize?): String? {
        if (user?.profileImageUrlHttps != null) {
            val url = user.profileImageUrlHttps
            if (size == null || url == null) {
                return url
            }

            return when (size) {
                AvatarSize.NORMAL, AvatarSize.BIGGER, AvatarSize.MINI, AvatarSize.ORIGINAL, AvatarSize.REASONABLY_SMALL -> url.replace(AvatarSize.NORMAL.suffix, size.suffix)
            }
        } else {
            return null
        }
    }

    /**
     * @return the given screenName, prepended with an "@"
     */
    @JvmStatic
    fun formatScreenName(screenName: CharSequence?): CharSequence = when {
        screenName.isNullOrBlank() -> ""
        screenName?.startsWith('@') == true -> screenName
        else -> "@$screenName"
    }
}
