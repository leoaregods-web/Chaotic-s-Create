package com.com.chaos;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ===================== Processor Upgrades =====================
    public static final ModConfigSpec.IntValue BASIC_UPGRADE_SPEED;
    public static final ModConfigSpec.IntValue ADVANCED_UPGRADE_SPEED;
    public static final ModConfigSpec.IntValue ELITE_UPGRADE_SPEED;

    // ===================== Abyssal Ore Crusher =====================
    public static final ModConfigSpec.IntValue CRUSHER_FE_CAPACITY;
    public static final ModConfigSpec.IntValue CRUSHER_FE_PER_TICK;
    public static final ModConfigSpec.IntValue CRUSHER_PROCESS_TIME;
    public static final ModConfigSpec.DoubleValue CRUSHER_BONUS_CHANCE;

    // ===================== Processor Inscriber =====================
    public static final ModConfigSpec.IntValue INSCRIBER_FE_CAPACITY;
    public static final ModConfigSpec.IntValue INSCRIBER_MAX_RECEIVE;

    // ===================== Astra-Abyss Generator =====================
    public static final ModConfigSpec.IntValue GENERATOR_FE_CAPACITY;
    public static final ModConfigSpec.IntValue GENERATOR_BASE_OUTPUT;
    public static final ModConfigSpec.IntValue GENERATOR_MAX_TRANSFER;

    // ===================== Atmosphere Liquifier =====================
    public static final ModConfigSpec.IntValue LIQUIFIER_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue LIQUIFIER_COLLECTION_INTERVAL;
    public static final ModConfigSpec.IntValue LIQUIFIER_COLLECTION_AMOUNT;
    public static final ModConfigSpec.IntValue LIQUIFIER_CAPSULE_FILL_COST;
    public static final ModConfigSpec.IntValue LIQUIFIER_NITROGEN_WEIGHT;
    public static final ModConfigSpec.IntValue LIQUIFIER_OXYGEN_WEIGHT;
    public static final ModConfigSpec.IntValue LIQUIFIER_ARGON_WEIGHT;

    // ===================== Capsule Effects =====================
    public static final ModConfigSpec.BooleanValue CAPSULES_BREAK_BLOCKS;
    public static final ModConfigSpec.DoubleValue OXYGEN_EXPLOSION_POWER;
    public static final ModConfigSpec.DoubleValue OXYGEN_SMALL_EXPLOSION_POWER;
    public static final ModConfigSpec.DoubleValue NITROGEN_EXPLOSION_POWER;
    public static final ModConfigSpec.DoubleValue NITROGEN_SMALL_EXPLOSION_POWER;
    public static final ModConfigSpec.DoubleValue ARGON_SMALL_EXPLOSION_POWER;

    static {
        BUILDER.push("Processor Upgrades");
        BASIC_UPGRADE_SPEED = BUILDER
                .comment("Speed bonus granted by a Basic Processor Upgrade")
                .defineInRange("basicUpgradeSpeed", 2, 1, 64);
        ADVANCED_UPGRADE_SPEED = BUILDER
                .comment("Speed bonus granted by an Advanced Processor Upgrade")
                .defineInRange("advancedUpgradeSpeed", 4, 1, 64);
        ELITE_UPGRADE_SPEED = BUILDER
                .comment("Speed bonus granted by an Elite Processor Upgrade")
                .defineInRange("eliteUpgradeSpeed", 6, 1, 64);
        BUILDER.pop();

        BUILDER.push("Abyssal Ore Crusher");
        CRUSHER_FE_CAPACITY = BUILDER
                .comment("Internal FE buffer of the Abyssal Ore Crusher")
                .defineInRange("crusherFeCapacity", 2000, 100, Integer.MAX_VALUE);
        CRUSHER_FE_PER_TICK = BUILDER
                .comment("FE consumed per tick while crushing")
                .defineInRange("crusherFePerTick", 20, 1, Integer.MAX_VALUE);
        CRUSHER_PROCESS_TIME = BUILDER
                .comment("Ticks of progress needed to finish crushing (before speed upgrades)")
                .defineInRange("crusherProcessTime", 120, 1, Integer.MAX_VALUE);
        CRUSHER_BONUS_CHANCE = BUILDER
                .comment("Chance (0.0-1.0) of an extra bonus nugget drop per crush")
                .defineInRange("crusherBonusChance", 0.35, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.push("Processor Inscriber");
        INSCRIBER_FE_CAPACITY = BUILDER
                .comment("Internal FE buffer of the Processor Inscriber")
                .defineInRange("inscriberFeCapacity", 1000, 100, Integer.MAX_VALUE);
        INSCRIBER_MAX_RECEIVE = BUILDER
                .comment("Max FE the Processor Inscriber can receive per tick")
                .defineInRange("inscriberMaxReceive", 10, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Astra-Abyss Generator");
        GENERATOR_FE_CAPACITY = BUILDER
                .comment("Internal FE buffer of the Astra-Abyss Generator")
                .defineInRange("generatorFeCapacity", 100000, 1000, Integer.MAX_VALUE);
        GENERATOR_BASE_OUTPUT = BUILDER
                .comment("Base FE generated per tick (before speed upgrades) when both liquid sources are present")
                .defineInRange("generatorBaseOutput", 1, 1, Integer.MAX_VALUE);
        GENERATOR_MAX_TRANSFER = BUILDER
                .comment("Max FE the generator can push to a single neighbor per tick")
                .defineInRange("generatorMaxTransfer", 1000, 1, Integer.MAX_VALUE);
        BUILDER.pop();

        BUILDER.push("Atmosphere Liquifier");
        LIQUIFIER_TANK_CAPACITY = BUILDER
                .comment("Capacity in mB of each of the three internal fluid tanks")
                .defineInRange("liquifierTankCapacity", 4000, 100, Integer.MAX_VALUE);
        LIQUIFIER_COLLECTION_INTERVAL = BUILDER
                .comment("Ticks of progress needed per atmospheric collection roll (before speed upgrades)")
                .defineInRange("liquifierCollectionInterval", 100, 1, Integer.MAX_VALUE);
        LIQUIFIER_COLLECTION_AMOUNT = BUILDER
                .comment("mB of fluid collected per successful roll")
                .defineInRange("liquifierCollectionAmount", 25, 1, Integer.MAX_VALUE);
        LIQUIFIER_CAPSULE_FILL_COST = BUILDER
                .comment("mB consumed to fill one empty capsule")
                .defineInRange("liquifierCapsuleFillCost", 250, 1, Integer.MAX_VALUE);
        LIQUIFIER_NITROGEN_WEIGHT = BUILDER
                .comment("Relative weight of rolling Nitrogen (out of nitrogen+oxygen+argon total)")
                .defineInRange("liquifierNitrogenWeight", 780, 0, 1000);
        LIQUIFIER_OXYGEN_WEIGHT = BUILDER
                .comment("Relative weight of rolling Oxygen")
                .defineInRange("liquifierOxygenWeight", 210, 0, 1000);
        LIQUIFIER_ARGON_WEIGHT = BUILDER
                .comment("Relative weight of rolling Argon")
                .defineInRange("liquifierArgonWeight", 10, 0, 1000);
        BUILDER.pop();

        BUILDER.push("Capsule Effects");
        CAPSULES_BREAK_BLOCKS = BUILDER
                .comment("Whether Oxygen capsule explosions are allowed to break terrain/blocks")
                .define("capsulesBreakBlocks", true);
        OXYGEN_EXPLOSION_POWER = BUILDER
                .comment("Explosion power of a regular/large Oxygen Capsule")
                .defineInRange("oxygenExplosionPower", 4.0, 0.0, 128.0);
        OXYGEN_SMALL_EXPLOSION_POWER = BUILDER
                .comment("Explosion power of a Small Oxygen Capsule")
                .defineInRange("oxygenSmallExplosionPower", 8.0, 0.0, 128.0);
        NITROGEN_EXPLOSION_POWER = BUILDER
                .comment("Explosion power of a regular/large Nitrogen Capsule")
                .defineInRange("nitrogenExplosionPower", 3.5, 0.0, 128.0);
        NITROGEN_SMALL_EXPLOSION_POWER = BUILDER
                .comment("Explosion power of a Small Nitrogen Capsule")
                .defineInRange("nitrogenSmallExplosionPower", 7.0, 0.0, 128.0);
        ARGON_SMALL_EXPLOSION_POWER = BUILDER
                .comment("Explosion power of a Small Argon Capsule")
                .defineInRange("argonSmallExplosionPower", 0.5, 0.0, 128.0);
        BUILDER.pop();
    }

    static final ModConfigSpec SPEC = BUILDER.build();
}