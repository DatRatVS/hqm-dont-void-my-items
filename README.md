<pre align="center">
   ___       __  ___       __    __      _                       ___
  / _ \___ _/ /_/ _ \___ _/ /_  / /_____(_)__ ___   _______  ___/ (_)__  ___ _
 / // / _ `/ __/ , _/ _ `/ __/ / __/ __/ / -_|_-<  / __/ _ \/ _  / / _ \/ _ `/
/____/\_,_/\__/_/|_|\_,_/\__/  \__/_/ /_/\__/___/  \__/\___/\_,_/_/_//_/\_, /
                                                                       /___/
</pre>

<p align="center">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-555?style=for-the-badge">
  <img alt="1.7.10" src="https://img.shields.io/badge/1.7.10-555?style=for-the-badge">
</p>

<p align="center">
  <a href="#Features">Features</a> ·
  <a href="#Coremod-Approach">Coremod Approach</a> ·
  <a href="#QDS-Input-Protection">QDS Input Protection</a> ·
  <a href="#Limits">Limits</a> ·
  <a href="#Build-Instructions">Build Instructions</a>
</p>

# Don't Void My Items

A Minecraft mod that prevents Hardcore Questing Mode Quest Delivery Systems from accepting item or fluid input unless that input would advance the bound active consume task.

## Features

- **Item Protection**: Rejects items that do not match an unmet QDS item requirement.
- **Fluid Protection**: Rejects fluids that do not match an unmet QDS fluid requirement.
- **No Overfill Voiding**: Accepts only the amount still needed by the task.
- **Unbound QDS Safety**: Rejects item and fluid input when the QDS is not bound to a player task.
- **Completed Task Safety**: Rejects input after the bound task is already complete.
- **Cooldown Safety**: Respects HQM repeatable quest availability, including quests on cooldown.
- **HQM Integration**: Targets Hardcore Questing Mode `4.4.4` Quest Delivery Systems.

## Coremod Approach

This mod uses a small coremod patch because HQM's QDS accepts and consumes input inside the tile entity itself.

- **QDS Tile Patch**: Uses UniMixins to patch `hardcorequesting.tileentity.TileEntityBarrel` directly.
- **Late Mixin Loading**: Registers the QDS mixin as a late UniMixins mixin so HQM classes are visible before the target is prepared.
- **Inventory Gatekeeping**: Overrides QDS item insertion validation and insertion handling.
- **Fluid Gatekeeping**: Overrides QDS fluid fill validation and fill handling.
- **Scoped Behavior**: The patch is limited to HQM's Quest Delivery System; unrelated HQM blocks should keep their normal behavior.
- **Compatibility Target**: Built and tested against Minecraft `1.7.10`, Forge `10.13.4.1614`, UniMixins `0.3.0`, and HQM `4.4.4`.

## QDS Input Protection

The QDS only accepts input when it can resolve an active bound consume task:

- The QDS has a stored player name.
- The QDS resolves a current task.
- The current task is `QuestTaskItemsConsume`, including QDS consume tasks.
- The parent quest is available for that player.
- The task data is `QuestDataTaskItems`.
- The task is not already completed.

Items are checked against unmet item requirements using HQM's own `ItemPrecision.areItemsSame` rules. Fluids are checked against unmet fluid requirements by fluid id.

Valid progress is still applied through HQM's original task methods:

- Items use `QuestTaskItems.increaseItems`.
- Fluids use `QuestTaskItemsConsume.increaseFluid`.

After successful progress, the QDS sync timer is scheduled just like HQM does internally.

## Limits

- Automation that respects `IInventory.isItemValidForSlot` or `IFluidHandler.fill` should leave invalid input behind.
- Automation that deletes source items before calling QDS insertion cannot be fully protected by the QDS itself.
- The mod does not reimplement HQM repeat timing; it follows HQM's quest availability and task progress state.

## Build Instructions

Required local dependency jars go in `deps/`:

- `deps/HQM-The Journey-4.4.4.jar`
- `deps/+unimixins-all-1.7.10-0.3.0.jar`

Example placeholder files are included in `deps/` to document the expected jar names.

To build the mod:

```bash
JAVA_HOME=/usr/lib/jvm/java-8-openjdk ./gradlew --no-daemon clean build
```

The built JAR will be located in `build/libs/`.
