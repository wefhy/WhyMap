// Copyright (c) 2025 wefhy

package dev.wefhy.whymap;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class DynamicMinimapTexture extends AbstractTexture {

    public final Identifier textureId;
    private final AtomicReference<NativeImage> frontBuffer = new AtomicReference<>();
    private NativeImage backBuffer;
    private final Object uploadLock = new Object();
    
    public DynamicMinimapTexture(Identifier textureId, NativeImage initialImage) {
        this.textureId = textureId;
        this.backBuffer = initialImage;
        this.frontBuffer.set(new NativeImage(initialImage.getFormat(), initialImage.getWidth(), initialImage.getHeight(), false));
        final NativeImage initImage = this.backBuffer;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getGlId(), initImage.getWidth(), initImage.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getGlId(), initImage.getWidth(), initImage.getHeight());
            this.upload();
        }
    }

    public void updateMinimap(NativeImage newImage) {
        synchronized (uploadLock) {
            try {
                // Copy to back buffer then swap
                if (backBuffer.getWidth() != newImage.getWidth() ||
                    backBuffer.getHeight() != newImage.getHeight() ||
                    backBuffer.getFormat() != newImage.getFormat()) {
                    
                    backBuffer.close();
                    backBuffer = new NativeImage(newImage.getFormat(), newImage.getWidth(), newImage.getHeight(), false);
                }
                backBuffer.copyFrom(newImage);
                NativeImage oldFront = frontBuffer.getAndSet(backBuffer);
                backBuffer = oldFront; // Reuse old front buffer as new back buffer
            } finally {
                newImage.close();
            }
        }
        RenderSystem.recordRenderCall(this::upload);
    }

    private void upload() {
        synchronized (uploadLock) {
            NativeImage currentFront = frontBuffer.get();
            if (currentFront != null) {
                try {
                    this.bindTexture();
                    currentFront.upload(0, 0, 0, false);
                } catch (Exception e) {
                    System.err.println("Texture upload failed for " + textureId + ": " + e.getMessage());
                }
            } else {
                System.err.println("Attempted to upload null texture for " + textureId);
            }
        }
    }

    @Override
    public void close() {
        synchronized (uploadLock) {
            NativeImage front = frontBuffer.get();
            if (front != null) {
                front.close();
            }
            if (backBuffer != null) {
                backBuffer.close();
            }
        }
        super.close();
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        // We don't load from resources, the image is provided directly
        System.err.println("DynamicMinimapTexture.load() should not be called for texture: " + this.textureId);
    }
}