package com.twitter.sdk.android.core.internal

// see https://dev.twitter.com/overview/general/user-profile-images-and-banners
// see also: https://confluence.twitter.biz/display/PLATFORM/Image+Types+and+Sizes
enum class AvatarSize(val suffix: String) {
    NORMAL("_normal"),
    BIGGER("_bigger"),
    MINI("_mini"),
    ORIGINAL("_original"),
    REASONABLY_SMALL("_reasonably_small")
}
