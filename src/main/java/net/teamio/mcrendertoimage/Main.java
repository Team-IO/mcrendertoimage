package net.teamio.mcrendertoimage;

import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by oliver on 2018-06-20.
 */
@Mod(modid = Constants.MOD_ID)
public class Main {
	@Mod.Instance(Constants.MOD_ID)
	public static Main instance;

	public static ItemRenderStick itemRenderStick;

	public static BlockPos nextToRender;
	public static IBlockAccess nextToRenderWorld;


	private static void registerItem(Item item, String name) {
		item.setUnlocalizedName(Constants.MOD_ID + "." + name);
		//item.setCreativeTab(creativeTab);
		item.setRegistryName(name);
		GameRegistry.register(item);
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		registerItem(itemRenderStick = new ItemRenderStick(), Constants.ITEM_RENDER_STICK);

		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onRender(RenderGameOverlayEvent event) {
		if(nextToRender != null) {
			System.out.println("Rendering " + nextToRender);
			RenderTools.RenderAt(nextToRenderWorld, nextToRender);
			nextToRender = null;
		}
	}
}
