package io.github.npc_strider.oldbeacon.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.boss.BossBar.Color;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

import java.util.LinkedList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.npc_strider.oldbeacon.OldBeacon;

@Mixin(BeaconBlockEntityRenderer.class)
public class BeaconMixin {
	@Shadow static final Identifier BEAM_TEXTURE =  new Identifier("textures/entity/beacon_beam.png"); //"textures/misc/beam.png" //Not going to bother trying to reimplement the old beacon beam - have to modify code for it to accept alpha channel?
	@Shadow private static void render(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, float f, long l, int i, int j, float[] fs) {};

	private static final boolean IDLE_ANIM = OldBeacon.IDLE_ANIM;
	private static final float d = 0.1F; // Really not sure what coefficient to use - decompiled source has 0.05F but visually it looks too small. Original of 0.4F in endcrystal is too much.
	private static final Identifier NETHER_STAR_TEXTURE = new Identifier("textures/misc/beacon.png");
	private static final RenderLayer NETHER_STAR = RenderLayer.getEntityCutoutNoCull(NETHER_STAR_TEXTURE); // Originally 'END_CRYSTAL'
	private static final float SINE_45_DEGREES = (float)Math.sin(0.7853981633974483D);
	private ModelPart core = new ModelPart(64, 32, 32, 0).addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
	private ModelPart frame = new ModelPart(64, 32, 0, 0).addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
	private ModelPart bottom = new ModelPart(64, 32, 0, 16).addCuboid(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F);

	@Inject(
		at = @At("HEAD"),
		method = "BeaconBlockEntityRenderer()V"
	)
	public BeaconBlockEntityRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) { // For some reason you have to ignore the error during debugging? Everything seems to work.
		super(blockEntityRenderDispatcher);
		this.frame = new ModelPart(64, 32, 0, 0).addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		this.core = new ModelPart(64, 32, 32, 0).addCuboid(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F);
		this.bottom = new ModelPart(64, 32, 0, 16).addCuboid(-6.0F, 0.0F, -6.0F, 12.0F, 4.0F, 12.0F);
	};
	
	//Why am I not using injects ?
	//	I'm using overrides for the render for several reasons
	//	1. Not many mods will modify the vanilla beacon renderer - if they did then it's probably because they want to change its graphics.
	//		If they are changing its graphics, then why bother using this mod? The graphics would conflict if the mixins worked together.
	//	2. This modification seems so complex that using injection isn't worth it anymore. 
	//		I was considering using it but once I started making changes to the beacon beam renderer pretty much everything within render is changed, so might as well override it.
	public void render(BeaconBlockEntity beaconBlockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		//Code here is for the nether star
		VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(NETHER_STAR);

		float scale = 1.0F; // Original in EndCrystalEntityRenderer: 2.0F => matrixStack.scale(2.0F, 2.0F, 2.0F);
		float[] ccolor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};	//=> Core color (the star)
		float[] fcolor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};	//=> Frame color (the white box that rotates around the star)
		if (beaconBlockEntity.getBeamSegments().size() == 0 & IDLE_ANIM == true) {	//This is if the beacon is INACTIVE.
			scale = 0.7F;	//Make the star smaller when inactive. Also tint the star core and frame so it's cyan.
			fcolor = new float[]{0.65F, 1.0F, 1.0F, 0.05F}; //Alpha doesn't work??
			ccolor = new float[]{0.1F, 0.5F, 1.0F, 0.05F}; //Alpha doesn't work??
		};

		matrixStack.push();
		long tick = beaconBlockEntity.getWorld().getTime(); // Unfortunately the block has no time (endcrystals have endCrystalAge), so all block have the same animations
		float h = OldBeacon_getYOffset(tick, tickDelta) - (1.75F - d - 0.075F);//(h)eight offset of crystal //tickDelta ensures smooth animation (Not capped to 20fps/ 1 frame per tick.)
		float phase = (tick + tickDelta) * 3.0F; //phase or angle - results in rotational motion
		matrixStack.push();
		matrixStack.scale(scale, scale, scale);
		matrixStack.translate(0.5D/scale, -0.2D/scale, 0.5D/scale);	//Translate it to the center of the block. Original Y was -0.5D
		int k = OverlayTexture.DEFAULT_UV;
		// if (BeaconBlockEntity.getShowBottom()) { [...] }; 	//Originally code here for the bedrock 'base' of end crystals. Note that on the original beacon, this bedrock base did render, but I'm not including it because it looks bad.

		// Below here is graphical stuff, presumably results in the arcane motion of the end crystal. Don't mess with it - idk how tf quaternions rotations even work!
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(phase));
		matrixStack.translate(0.0D, (double)(1.5F + h / 2.0F)/scale, 0.0D);		//Translate up/down according to h - the oscillating height.
		matrixStack.multiply(new Quaternion(new Vector3f(SINE_45_DEGREES, 0.0F, SINE_45_DEGREES), 60.0F, true));
		this.frame.render(matrixStack, vertexConsumer, i, k, fcolor[0], fcolor[1], fcolor[2], fcolor[3]);	//Render the frame, with the tint
		float l = 0.875F; //Not sure why this number - don't want to hear about it.
		matrixStack.scale(l, l, l);
		matrixStack.multiply(new Quaternion(new Vector3f(SINE_45_DEGREES, 0.0F, SINE_45_DEGREES), 60.0F, true));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(phase));
		this.frame.render(matrixStack, vertexConsumer, i, k, fcolor[0], fcolor[1], fcolor[2], fcolor[3]);	//Render the frame, with the tint
		matrixStack.scale(l, l, l);
		matrixStack.multiply(new Quaternion(new Vector3f(SINE_45_DEGREES, 0.0F, SINE_45_DEGREES), 60.0F, true));
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(phase));
		this.core.render(matrixStack, vertexConsumer, i, k, ccolor[0], ccolor[1], ccolor[2], ccolor[3]);	//Render the frame, with the tint
		matrixStack.pop();
		matrixStack.pop();
		//	Originally code here for the ender dragon beam which goes from crystal to dragon.
		// BlockPos blockPos = BeaconBlockEntity.getBeamTarget(); [...]
		
		//Code below here is for the beacon beam.
		float tr = 0.5F + 0.075F; //Translate beam so it's inside of the star.
		matrixStack.translate(0.0D, tr, 0.0D); // Make sure beam starts inside of star, not below it.
		matrixStack.scale(1.0F, (1.0F-tr), 1.0F);
		int x = 0;
		List<BeaconBlockEntity.BeamSegment> list = new LinkedList<>(beaconBlockEntity.getBeamSegments()); //Need a linkedlist because I'm adding a new beam segment
		if (list.size() > 0 ) {
			list.add(0, new BeaconBlockEntity.BeamSegment(new float[]{1.0F, 1.0F, 1.0F})); 
				// Adding a new beam segment which is only inside of the beacon block to account for the transformations. Cannot simply subtract a float height and shift (j is an int) so we need to be a bit clever.
				// The next beam become a 'buffer beam' which is also special as it's truncated (m == 2)
		}

		for(int m = 0; m < list.size(); ++m) {
			BeaconBlockEntity.BeamSegment beamSegment = (BeaconBlockEntity.BeamSegment)list.get(m);
			int j_ = beamSegment.getHeight();
			if (m == 1){ //lots of hacky stuff to ensure glass blocks tint the beam at the start, not in the middle of the glass (if we only transformed it up by 0.5F)
				matrixStack.scale(1.0F, 1.0F/(1.0F-tr), 1.0F); //Scale up the next beam segment so it's 1 block tall again
				matrixStack.translate(0.0D, -tr, 0.0D); //Re-translate the beam so it's aligned to the grid.
				j_--; // This covers the case where the tint block is immediately above the beacon (=> height of 0)
			} else if (m == 2){
				matrixStack.translate(0.0D, -1.0F, 0.0D); // Need to cover the truncated height of the previous beam segment (j_--), otherwise we get a 1 block gap between the starting segment and the first tinted segment.
			}
			render(matrixStack, vertexConsumerProvider, tickDelta, tick, x, m == list.size() - 1 ? 1024 : j_, beamSegment.getColor());
			x += beamSegment.getHeight();
		}
	}

	private static float OldBeacon_getYOffset(long tick, float tickDelta) {
		float f = tick + tickDelta;
		float g = MathHelper.sin(f * 0.2F) / 2.0F + 0.5F;
		g = (g * g + g) * d;
		return g; // Center of the beacon. Again, not sure of the official constant (-1.4F) used so I'm using my own. (1.75F is the real center, but it clips due to its oscillation). Moved the constant to the result.
	}
	
}