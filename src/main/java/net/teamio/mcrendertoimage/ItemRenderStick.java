package net.teamio.mcrendertoimage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by oliver on 2018-06-20.
 */
public class ItemRenderStick extends Item {
	public ItemRenderStick() {
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		System.out.println("Requesting render for " + pos);

		Main.nextToRender = pos;
		Main.nextToRenderWorld = worldIn;

		return EnumActionResult.SUCCESS;
	}
}
