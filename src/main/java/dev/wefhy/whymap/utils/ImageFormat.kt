// Copyright (c) 2023 wefhy

package dev.wefhy.whymap.utils

import io.ktor.http.*

enum class ImageFormat(val contentType: ContentType) {
    PNG(ContentType.Image.PNG) {
        override fun matchesExtension(extension: String): Boolean {
            return extension.equals("png", true)
        }
    },
    JPEG(ContentType.Image.JPEG) {
        override fun matchesExtension(extension: String): Boolean {
            return extension.equals("jpg", true) || extension.equals("jpeg", true)
        }
    };
    abstract fun matchesExtension(extension: String): Boolean
}