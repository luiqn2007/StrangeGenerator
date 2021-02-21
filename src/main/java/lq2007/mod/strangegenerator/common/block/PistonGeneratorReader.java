package lq2007.mod.strangegenerator.common.block;

import lq2007.mod.strangegenerator.common.tile.TilePistonGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class PistonGeneratorReader extends Block {

    public PistonGeneratorReader() {
        super(Properties.create(Material.IRON));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TilePistonGenerator();
    }
}
