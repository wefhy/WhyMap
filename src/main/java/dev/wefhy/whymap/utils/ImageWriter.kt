// Copyright (c) 2022 wefhy

package dev.wefhy.whymap.utils

import java.awt.image.BufferedImage
import java.io.OutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.plugins.jpeg.JPEGImageWriteParam


object ImageWriter {

    val jpegWriter
        get() = ImageIO.getImageWritersByFormatName("jpg").next()
    val pngWriter
        get() = ImageIO.getImageWritersByFormatName("png").next()


    val jpegParams = JPEGImageWriteParam(null).apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = 1f // 0.95 is good for texture view; 1f for regular zoom
    }

    val pngParam: ImageWriteParam = pngWriter.defaultWriteParam.apply {
        compressionMode = ImageWriteParam.MODE_EXPLICIT
        compressionQuality = 0.2f
//            tilingMode = ImageWriteParam.MODE_EXPLICIT this would be epic for TIFF
//            setTiling(16, 16, 0, 0)
    }

//    val pngParams = PNGImageWriteParam(null).apply {
//        compressionMode = ImageWriteParam.MODE_EXPLICIT
//        compressionQuality = 1f // 0.95 is good for texture view; 1f for regular zoom
//    }


    fun OutputStream.encodeJPEG(bitmap: BufferedImage) {
//        val writer = jpegWriter.next()
        val writer = jpegWriter
        writer.reset()
        writer.output = ImageIO.createImageOutputStream(this)
        writer.write(null, IIOImage(bitmap, null, null), jpegParams)
        writer.dispose()
    }

    fun OutputStream.encodePNG(bitmap: BufferedImage) {
//        val writer = pngWriter.next()
        val writer = pngWriter
        writer.reset()
        writer.output = ImageIO.createImageOutputStream(this)
        writer.write(null, IIOImage(bitmap, null, null), pngParam)
        writer.dispose()
    }

    enum class ImageType {
        PNG,
        JPEG
    }


}