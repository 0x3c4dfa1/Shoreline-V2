package net.shoreline.client.impl.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;

import java.util.Optional;

public class ClientPipelines
{
    public static final RenderPipeline QUADS = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/shoreline_quads")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false).build());

    public static final RenderPipeline DEBUG_LINES = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation("pipeline/shoreline_quads")
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false).build());

    public static final RenderPipeline ITEMS = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
                    .withLocation("pipeline/shoreline_item_cutout")
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .build());

    public static final RenderPipeline ITEMS_TRANSLUCENT = RenderPipelines
            .register(RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
                    .withLocation("pipeline/shoreline_item_translucent")
                    .withDepthStencilState(new DepthStencilState(CompareOp.EQUAL, true))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                    .build());

    public static final RenderPipeline GLINT = RenderPipelines
            .register(RenderPipeline.builder()
                    .withLocation("pipeline/shoreline_glint")
                    .withVertexShader("core/glint")
                    .withFragmentShader("core/glint")
                    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                    .withUniform("Fog", UniformType.UNIFORM_BUFFER)
                    .withUniform("Globals", UniformType.UNIFORM_BUFFER)
                    .withSampler("Sampler0")
                    .withCull(false)
                    .withColorTargetState(new ColorTargetState(BlendFunction.GLINT))
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, true))
                    .build());
}
