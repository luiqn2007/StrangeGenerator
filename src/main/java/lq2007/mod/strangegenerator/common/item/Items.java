package lq2007.mod.strangegenerator.common.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static lq2007.mod.strangegenerator.StrangeGenerator.ID;

public class Items {

    public static final DeferredRegister<Item> REGISTER = DeferredRegister.create(ForgeRegistries.ITEMS, ID);

    public static ItemGroup GROUP_GENERATOR = new ItemGroup(ID) {

        private ItemStack icon = ItemStack.EMPTY;

        @Override
        public ItemStack createIcon() {
            if (icon == ItemStack.EMPTY) {
                icon = new ItemStack(ITEM_DEBUG.get());
            }
            return icon;
        }
    };

    public static RegistryObject<Item> ITEM_DEBUG = REGISTER.register("debug", ItemDebug::new);
}
