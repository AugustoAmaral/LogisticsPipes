package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.network.NewGuiHandler;
import logisticspipes.network.abstractguis.ModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.ModuleInHandGuiProvider;
import logisticspipes.network.guis.pipe.ChassisGuiProvider;
import logisticspipes.pipes.PipeLogisticsChassis;
import logisticspipes.pipes.PipeLogisticsChassis.ChassiTargetInformation;
import logisticspipes.proxy.computers.objects.CCSinkResponder;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import network.rs485.logisticspipes.module.Gui;
import network.rs485.logisticspipes.module.PipeServiceProviderUtilKt;

public class ChassisModule extends LogisticsModule implements Gui {

	private final LogisticsModule[] modules;
	private final PipeLogisticsChassis parentChassis;

	public ChassisModule(int moduleCount, PipeLogisticsChassis parentChassis) {
		modules = new LogisticsModule[moduleCount];
		this.parentChassis = parentChassis;
		registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	public void installModule(int slot, LogisticsModule module) {
		modules[slot] = module;
	}

	public void removeModule(int slot) {
		modules[slot] = null;
	}

	public LogisticsModule getModule(int slot) {
		return modules[slot];
	}

	public boolean hasModule(int slot) {
		return (modules[slot] != null);
	}

	public LogisticsModule[] getModules() {
		return modules;
	}

	@Override
	public SinkReply sinksItem(@Nonnull ItemStack stack, ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit, boolean forcePassive) {
		SinkReply bestresult = null;
		for (LogisticsModule module : modules) {
			if (module != null) {
				if (!forcePassive || module.recievePassive()) {
					SinkReply result = module.sinksItem(stack, item, bestPriority, bestCustomPriority, allowDefault, includeInTransit, forcePassive);
					if (result != null && result.maxNumberOfItems >= 0) {
						bestresult = result;
						bestPriority = result.fixedPriority.ordinal();
						bestCustomPriority = result.customPriority;
					}
				}
			}
		}

		if (bestresult == null) {
			return null;
		}
		//Always deny items when we can't put the item anywhere
		final ISlotUpgradeManager upgradeManager = parentChassis.getUpgradeManager(ModulePositionType.SLOT, ((ChassiTargetInformation) bestresult.addInfo).getModuleSlot());
		IInventoryUtil invUtil = PipeServiceProviderUtilKt.availableSneakyInventories(parentChassis, upgradeManager).stream().findFirst().orElse(null);
		if (invUtil == null) {
			return null;
		}
		int roomForItem;
		if (includeInTransit) {
			int onRoute = parentChassis.countOnRoute(item);
			final ItemStack copy = stack.copy();
			copy.setCount(onRoute + item.getMaxStackSize());
			roomForItem = invUtil.roomForItem(copy);
			roomForItem -= onRoute;
		} else {
			roomForItem = invUtil.roomForItem(stack);
		}
		if (roomForItem < 1) {
			return null;
		}

		if (bestresult.maxNumberOfItems == 0) {
			return new SinkReply(bestresult, roomForItem);
		}
		return new SinkReply(bestresult, Math.min(bestresult.maxNumberOfItems, roomForItem));
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] != null) {
				if (nbttagcompound.hasKey("slot" + i)) {
					modules[i].readFromNBT(nbttagcompound.getCompoundTag("slot" + i));
				}
			}
		}
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		for (int i = 0; i < modules.length; i++) {
			if (modules[i] != null) {
				NBTTagCompound slot = new NBTTagCompound();
				modules[i].writeToNBT(slot);
				nbttagcompound.setTag("slot" + i, slot);
			}
		}
	}

	@Override
	public void tick() {
		for (LogisticsModule module : modules) {
			if (module == null) {
				continue;
			}
			module.tick();
		}
	}

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		for (LogisticsModule module : modules) {
			if (module != null && module.recievePassive()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<CCSinkResponder> queueCCSinkEvent(ItemIdentifierStack item) {
		List<CCSinkResponder> list = new ArrayList<>();
		for (LogisticsModule module : modules) {
			if (module != null) {
				list.addAll(module.queueCCSinkEvent(item));
			}
		}
		return list;
	}

	@Nonnull
	@Override
	public ModuleCoordinatesGuiProvider getPipeGuiProvider() {
		return NewGuiHandler.getGui(ChassisGuiProvider.class).setFlag(parentChassis.getUpgradeManager().hasUpgradeModuleUpgrade());
	}

	@Nonnull
	@Override
	public ModuleInHandGuiProvider getInHandGuiProvider() {
		throw new UnsupportedOperationException("Chassis GUI can never be opened in hand");
	}
}
