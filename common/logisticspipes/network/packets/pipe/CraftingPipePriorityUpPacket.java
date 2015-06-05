package logisticspipes.network.packets.pipe;

import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.ModuleCoordinatesPacket;

import net.minecraft.entity.player.EntityPlayer;

public class CraftingPipePriorityUpPacket extends ModuleCoordinatesPacket {

	public CraftingPipePriorityUpPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new CraftingPipePriorityUpPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		ModuleCrafter module = this.getLogisticsModule(player, ModuleCrafter.class);
		if (module == null) {
			return;
		}
		module.priorityUp(player);
	}
}
