package com.com.chaos.Menu;

import com.com.chaos.Blocks.AtmosphereLiquifierBlockEntity;
import com.com.chaos.Blocks.ModBlocks;
import com.com.chaos.Items.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class AtmosphereLiquifierMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 4;
    private static final int MACHINE_SLOT_COUNT = AtmosphereLiquifierBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;

    private static final int OXYGEN_INDEX = 0;
    private static final int NITROGEN_INDEX = 1;
    private static final int ARGON_INDEX = 2;
    private static final int CAPACITY_INDEX = 3;

    private final ContainerLevelAccess access;
    private final Container container;
    private final ContainerData data;

    public AtmosphereLiquifierMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                ContainerLevelAccess.NULL,
                new SimpleContainerData(DATA_COUNT)
        );
    }

    public AtmosphereLiquifierMenu(int containerId, Inventory playerInventory, AtmosphereLiquifierBlockEntity blockEntity) {
        this(
                containerId,
                playerInventory,
                blockEntity,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                blockEntity.getContainerData()
        );
    }

    private AtmosphereLiquifierMenu(int containerId, Inventory playerInventory, Container container, ContainerLevelAccess access, ContainerData data) {
        super(ModMenus.ATMOSPHERE_LIQUIFIER_MENU.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.access = access;
        this.container = container;
        this.data = data;

        container.startOpen(playerInventory.player);
        addMachineSlots(container);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(data);
    }

    private void addMachineSlots(Container container) {
        addSlot(new InputCapsuleSlot(container, AtmosphereLiquifierBlockEntity.OXYGEN_INPUT_SLOT, 15, 15));
        addSlot(new InputCapsuleSlot(container, AtmosphereLiquifierBlockEntity.NITROGEN_INPUT_SLOT, 15, 35));
        addSlot(new InputCapsuleSlot(container, AtmosphereLiquifierBlockEntity.ARGON_INPUT_SLOT, 15, 55));

        addSlot(new OutputSlot(container, AtmosphereLiquifierBlockEntity.OXYGEN_OUTPUT_SLOT, 136, 15));
        addSlot(new OutputSlot(container, AtmosphereLiquifierBlockEntity.NITROGEN_OUTPUT_SLOT, 136, 35));
        addSlot(new OutputSlot(container, AtmosphereLiquifierBlockEntity.ARGON_OUTPUT_SLOT, 136, 55));
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int slot = 0; slot < 9; slot++) {
            addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 142));
        }
    }

    public int getOxygenStored() {
        return data.get(OXYGEN_INDEX);
    }

    public int getNitrogenStored() {
        return data.get(NITROGEN_INDEX);
    }

    public int getArgonStored() {
        return data.get(ARGON_INDEX);
    }

    public int getCapacity() {
        return Math.max(1, data.get(CAPACITY_INDEX));
    }

    public int getOxygenBarWidth(int pixels) {
        return getOxygenStored() * pixels / getCapacity();
    }

    public int getNitrogenBarWidth(int pixels) {
        return getNitrogenStored() * pixels / getCapacity();
    }

    public int getArgonBarWidth(int pixels) {
        return getArgonStored() * pixels / getCapacity();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ATMOSPHERE_LIQUIFIER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack originalStack = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        originalStack = stackInSlot.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (stackInSlot.is(ModItems.EMPTY_CAPSULE.get())) {
            if (!moveItemStackTo(stackInSlot, AtmosphereLiquifierBlockEntity.OXYGEN_INPUT_SLOT,
                    AtmosphereLiquifierBlockEntity.ARGON_INPUT_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_INVENTORY_END) {
            if (!moveItemStackTo(stackInSlot, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < PLAYER_HOTBAR_END) {
            if (!moveItemStackTo(stackInSlot, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (stackInSlot.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        slot.onTake(player, stackInSlot);
        return originalStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }

    private static class InputCapsuleSlot extends Slot {
        private InputCapsuleSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.is(ModItems.EMPTY_CAPSULE.get());
        }
    }

    private static class OutputSlot extends Slot {
        private OutputSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
