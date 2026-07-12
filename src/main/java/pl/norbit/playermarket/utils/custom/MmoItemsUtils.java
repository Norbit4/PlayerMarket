package pl.norbit.playermarket.utils.custom;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;

public class MmoItemsUtils {
    private MmoItemsUtils() {}

    protected static ItemStack getItem(String id){
        MMOItem mmoitem = getMmoItem(id);

        if(mmoitem == null){
            return null;
        }

        return mmoitem.newBuilder().build();
    }

    protected static MMOItem getMmoItem(String id){
        String[] split = id.split(":");

        if (split.length < 2){
            return null;
        }
        Type type = MMOItems.plugin.getTypes().get(split[0]);

        if(type == null){
            return null;
        }

        return MMOItems.plugin.getMMOItem(type, split[1]);
    }

    public static boolean isEqual(ItemStack stack1, String id) {
        MMOItem mmoitem = getMmoItem(id);

        if(mmoitem == null){
            return false;
        }

        ItemStack build = mmoitem.newBuilder().build();

        if(build == null){
            return false;
        }

        return build.isSimilar(stack1);
    }
}
