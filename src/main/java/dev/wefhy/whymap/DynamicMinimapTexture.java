// Copyright (c) 2025 wefhy

package dev.wefhy.whymap;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;

@Environment(EnvType.CLIENT)
public class DynamicMinimapTexture extends AbstractTexture {

    public final Identifier textureId;
    private NativeImage image;

    public DynamicMinimapTexture(Identifier textureId, NativeImage initialImage) {
        this.textureId = textureId;
        this.image = initialImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(this.getGlId(), this.image.getWidth(), this.image.getHeight());
                this.upload();
            });
        } else {
            TextureUtil.prepareImage(this.getGlId(), this.image.getWidth(), this.image.getHeight());
            this.upload();
        }
    }

    public void updateMinimap(NativeImage newImage) {
        // Close the previous image to prevent memory leaks
        if (this.image != null) {
//            this.image.close();
        }
        this.image = newImage;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(this::upload);
        } else {
            this.upload();
        }
    }

    private void upload() {
        if (this.image != null) {
            this.bindTexture();
            this.image.upload(0, 0, 0, false);
        } else {
            // Handle the case where the image is null (shouldn't happen if used correctly)
            System.err.println("Trying to upload null NativeImage for texture: " + this.textureId);
        }
    }

    @Override
    public void close() {
        if (this.image != null) {
            this.image.close();
        }
        super.close(); // Call the superclass close to clear the GL ID
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        // We don't load from resources, the image is provided directly
        System.err.println("DynamicMinimapTexture.load() should not be called for texture: " + this.textureId);
    }
}