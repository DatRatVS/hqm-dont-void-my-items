package datrat.hqmdontvoidmyitems.qds;

import hardcorequesting.quests.ItemPrecision;
import hardcorequesting.quests.Quest;
import hardcorequesting.quests.QuestDataTask;
import hardcorequesting.quests.QuestDataTaskItems;
import hardcorequesting.quests.QuestTask;
import hardcorequesting.quests.QuestTaskItems;
import hardcorequesting.quests.QuestTaskItemsConsumeQDS;
import hardcorequesting.tileentity.TileEntityBarrel;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public final class QdsAcceptanceHooks {
    private static final int SYNC_TIME = 20;

    private QdsAcceptanceHooks() {
    }

    public static boolean canAcceptItem(TileEntityBarrel barrel, ItemStack stack) {
        ActiveQdsTask activeTask = resolveActiveTask(barrel);
        return activeTask != null && getAcceptedItemAmount(activeTask, stack) > 0;
    }

    public static boolean acceptItem(TileEntityBarrel barrel, ItemStack stack) {
        ActiveQdsTask activeTask = resolveActiveTask(barrel);
        int accepted = getAcceptedItemAmount(activeTask, stack);
        if (accepted <= 0) {
            return false;
        }

        ItemStack acceptedStack = stack.copy();
        acceptedStack.stackSize = accepted;
        ItemStack[] acceptedStacks = new ItemStack[] {acceptedStack};
        boolean progressed = activeTask.task.increaseItems(acceptedStacks, activeTask.data, activeTask.playerName);
        if (!progressed) {
            return false;
        }

        int remaining = acceptedStacks[0] == null ? 0 : acceptedStacks[0].stackSize;
        int consumed = accepted - Math.max(0, remaining);
        if (consumed <= 0) {
            return false;
        }

        stack.stackSize = Math.max(0, stack.stackSize - consumed);
        scheduleSync(barrel);
        return true;
    }

    public static boolean canAcceptFluid(TileEntityBarrel barrel, Fluid fluid) {
        ActiveQdsTask activeTask = resolveActiveTask(barrel);
        return activeTask != null && getAcceptedFluidAmount(activeTask, fluid, Integer.MAX_VALUE) > 0;
    }

    public static int acceptFluid(TileEntityBarrel barrel, FluidStack resource, boolean doFill) {
        ActiveQdsTask activeTask = resolveActiveTask(barrel);
        if (activeTask == null || resource == null || resource.amount <= 0) {
            return 0;
        }

        int accepted = getAcceptedFluidAmount(activeTask, resource.getFluid(), resource.amount);
        if (accepted <= 0) {
            return 0;
        }

        if (!doFill) {
            return accepted;
        }

        FluidStack acceptedFluid = resource.copy();
        acceptedFluid.amount = accepted;
        boolean progressed = activeTask.task.increaseFluid(acceptedFluid, activeTask.data, activeTask.playerName);
        if (!progressed) {
            return 0;
        }

        int consumed = accepted - Math.max(0, acceptedFluid.amount);
        if (consumed <= 0) {
            return 0;
        }

        scheduleSync(barrel);
        return consumed;
    }

    private static ActiveQdsTask resolveActiveTask(TileEntityBarrel barrel) {
        if (!(barrel instanceof TileEntityBarrelAccess)) {
            return null;
        }

        TileEntityBarrelAccess access = (TileEntityBarrelAccess) barrel;
        String playerName = access.hqmdontvoidmyitems$getPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            return null;
        }

        QuestTask currentTask = barrel.getCurrentTask();
        if (!(currentTask instanceof QuestTaskItemsConsumeQDS)) {
            return null;
        }

        QuestTaskItemsConsumeQDS task = (QuestTaskItemsConsumeQDS) currentTask;
        Quest parent = task.getParent();
        if (parent == null || !parent.isAvailable(playerName)) {
            return null;
        }

        QuestDataTask data = task.getData(playerName);
        if (!(data instanceof QuestDataTaskItems) || data.completed) {
            return null;
        }

        QuestTaskItems.ItemRequirement[] requirements = task.getItems();
        QuestDataTaskItems itemData = (QuestDataTaskItems) data;
        if (requirements == null || itemData.progress == null || itemData.progress.length < requirements.length) {
            return null;
        }

        return new ActiveQdsTask(playerName, task, itemData, requirements);
    }

    private static int getAcceptedItemAmount(ActiveQdsTask activeTask, ItemStack stack) {
        if (activeTask == null || stack == null || stack.stackSize <= 0) {
            return 0;
        }

        int accepted = 0;
        for (int i = 0; i < activeTask.requirements.length && accepted < stack.stackSize; i++) {
            QuestTaskItems.ItemRequirement requirement = activeTask.requirements[i];
            if (!isUnmetItemRequirement(requirement, activeTask.data.progress[i])) {
                continue;
            }

            ItemPrecision precision = requirement.getPrecision();
            if (precision != null && precision.areItemsSame(stack, requirement.getItem())) {
                accepted += Math.min(stack.stackSize - accepted, getRemainingRequirement(requirement, activeTask.data.progress[i]));
            }
        }

        return accepted;
    }

    private static int getAcceptedFluidAmount(ActiveQdsTask activeTask, Fluid fluid, int maxAmount) {
        if (activeTask == null || fluid == null || maxAmount <= 0) {
            return 0;
        }

        for (int i = 0; i < activeTask.requirements.length; i++) {
            QuestTaskItems.ItemRequirement requirement = activeTask.requirements[i];
            if (!isUnmetFluidRequirement(requirement, activeTask.data.progress[i])) {
                continue;
            }

            if (requirement.fluid.getID() == fluid.getID()) {
                return Math.min(maxAmount, getRemainingRequirement(requirement, activeTask.data.progress[i]));
            }
        }

        return 0;
    }

    private static boolean isUnmetItemRequirement(QuestTaskItems.ItemRequirement requirement, int progress) {
        return requirement != null
            && requirement.hasItem
            && requirement.getItem() != null
            && getRemainingRequirement(requirement, progress) > 0;
    }

    private static boolean isUnmetFluidRequirement(QuestTaskItems.ItemRequirement requirement, int progress) {
        return requirement != null
            && !requirement.hasItem
            && requirement.fluid != null
            && getRemainingRequirement(requirement, progress) > 0;
    }

    private static int getRemainingRequirement(QuestTaskItems.ItemRequirement requirement, int progress) {
        return requirement.required - progress;
    }

    private static void scheduleSync(TileEntityBarrel barrel) {
        TileEntityBarrelAccess access = (TileEntityBarrelAccess) barrel;
        if (access.hqmdontvoidmyitems$getModifiedSyncTimer() <= 0) {
            access.hqmdontvoidmyitems$setModifiedSyncTimer(SYNC_TIME);
        }
    }

    private static final class ActiveQdsTask {
        private final String playerName;
        private final QuestTaskItemsConsumeQDS task;
        private final QuestDataTaskItems data;
        private final QuestTaskItems.ItemRequirement[] requirements;

        private ActiveQdsTask(String playerName, QuestTaskItemsConsumeQDS task, QuestDataTaskItems data, QuestTaskItems.ItemRequirement[] requirements) {
            this.playerName = playerName;
            this.task = task;
            this.data = data;
            this.requirements = requirements;
        }
    }
}
