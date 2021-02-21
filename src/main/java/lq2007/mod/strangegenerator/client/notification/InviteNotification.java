package lq2007.mod.strangegenerator.client.notification;

import net.minecraft.util.text.ITextComponent;

public class InviteNotification {

    public final ITextComponent ownerName;
    public final int generatorId;

    public InviteNotification(ITextComponent ownerName, int generatorId) {
        this.ownerName = ownerName;
        this.generatorId = generatorId;
    }
}
