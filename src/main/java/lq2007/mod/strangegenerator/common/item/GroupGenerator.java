package lq2007.mod.strangegenerator.common.item;

import lq2007.mod.strangegenerator.StrangeGenerator;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class GroupGenerator extends ItemGroup {

    public static GroupGenerator INSTANCE = new GroupGenerator();

    private ItemStack icon = ItemStack.EMPTY;

    public GroupGenerator() {
        super(StrangeGenerator.ID);
    }

    @Override
    public ItemStack createIcon() {
        if (icon == ItemStack.EMPTY) {
            icon = new ItemStack(StrangeGenerator.ITEMS.get(ItemDebug.class));
        }
        return icon;
    }
}
