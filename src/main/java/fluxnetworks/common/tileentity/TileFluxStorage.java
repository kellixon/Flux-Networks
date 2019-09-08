package fluxnetworks.common.tileentity;

import fluxnetworks.FluxConfig;
import fluxnetworks.api.network.ITransferHandler;
import fluxnetworks.api.tileentity.IFluxEnergy;
import fluxnetworks.api.tileentity.IFluxStorage;
import fluxnetworks.common.data.FluxNetworkData;
import fluxnetworks.common.connection.SingleTransferHandler;
import fluxnetworks.common.connection.StorageTransfer;
import fluxnetworks.common.core.FluxUtils;
import fluxnetworks.common.core.NBTType;
import fluxnetworks.common.registry.RegistryBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import scala.Int;

public class TileFluxStorage extends TileFluxCore implements IFluxStorage, IFluxEnergy {

    public final SingleTransferHandler handler = new SingleTransferHandler(this, new StorageTransfer(this));

    public static final int C = 1000000;

    public int energyStored;
    public int maxEnergyStorage;
    public int maxTransferRate;

    public ItemStack stack = ItemStack.EMPTY;

    public TileFluxStorage() {
        this(FluxConfig.basicCapacity, FluxConfig.basicTransfer);
        customName = "Basic Storage";
        limit = FluxConfig.basicTransfer;
        stack = new ItemStack(RegistryBlocks.FLUX_STORAGE_1);
    }

    private TileFluxStorage(int maxEnergyStorage, int maxTransferRate) {
        this.maxEnergyStorage = maxEnergyStorage;
        this.maxTransferRate = maxTransferRate;
    }

    public static class Herculean extends TileFluxStorage {

        public Herculean() {
            super(FluxConfig.herculeanCapacity, FluxConfig.herculeanTransfer);
            customName = "Herculean Storage";
            limit = FluxConfig.herculeanTransfer;
            stack = new ItemStack(RegistryBlocks.FLUX_STORAGE_2);
        }
    }

    public static class Gargantuan extends TileFluxStorage {

        public Gargantuan() {
            super(FluxConfig.gargantuanCapacity, FluxConfig.gargantuanTransfer);
            customName = "Gargantuan Storage";
            limit = FluxConfig.gargantuanTransfer;
            stack = new ItemStack(RegistryBlocks.FLUX_STORAGE_3);
        }
    }

    @Override
    public ConnectionType getConnectionType() {
        return ConnectionType.STORAGE;
    }

    @Override
    public ITransferHandler getTransferHandler() {
        return handler;
    }

    @Override
    public NBTTagCompound writeCustomNBT(NBTTagCompound tag, NBTType type) {
        super.writeCustomNBT(tag, type);
        tag.setInteger("energy", energyStored);
        return tag;
    }

    public long addEnergy(long amount, boolean simulate) {
        long energyReceived = Math.min(maxEnergyStorage - energyStored, Math.min(maxTransferRate, amount));
        if (!simulate) {
            energyStored += energyReceived;
            sendPackets();
        }
        return energyReceived;
    }

    public long removeEnergy(long amount, boolean simulate) {
        long energyExtracted = Math.min(energyStored, Math.min(maxTransferRate, amount));
        if (!simulate) {
            energyStored -= energyExtracted;
            sendPackets();
        }
        return energyExtracted;
    }

    @Override
    public long getEnergy() {
        return energyStored;
    }

    @Override
    public long getCurrentLimit() {
        return maxTransferRate;
    }

    @Override
    public int getPriority() {
        return priority - C;
    }

    @Override
    public void readCustomNBT(NBTTagCompound tag, NBTType type) {
        super.readCustomNBT(tag, type);
        energyStored = tag.getInteger("energy");
    }

    public ItemStack writeStorageToDisplayStack(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();

        NBTTagCompound stag = new NBTTagCompound();
        stag.setInteger("energy", energyStored);
        stag.setInteger(FluxNetworkData.NETWORK_ID, networkID);

        tag.setTag(FluxUtils.FLUX_DATA, stag);
        tag.setBoolean(FluxUtils.GUI_COLOR, true);

        stack.setTagCompound(tag);
        return stack;
    }

    @Override
    public ItemStack getDisplayStack() {
        return writeStorageToDisplayStack(stack);
    }
}