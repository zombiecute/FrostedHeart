package com.teammoeg.frostedheart.research.effects;

import com.google.gson.JsonObject;
import com.teammoeg.frostedheart.research.SerializerRegistry;

import net.minecraft.network.PacketBuffer;

public class Effects {
    private static SerializerRegistry<Effect> registry=new SerializerRegistry<>();

    static {
    	registry.register(EffectBuilding.class,"multiblock", EffectBuilding::new, EffectBuilding::new);
    	registry.register(EffectCrafting.class,"recipe", EffectCrafting::new, EffectCrafting::new);
    	registry.register(EffectItemReward.class,"item", EffectItemReward::new, EffectItemReward::new);
    	registry.register(EffectStats.class,"stats", EffectStats::new, EffectStats::new);
    	registry.register(EffectUse.class,"use", EffectUse::new, EffectUse::new);
    	registry.register(EffectShowCategory.class,"category", EffectShowCategory::new, EffectShowCategory::new);
    }

    private Effects() {
    }
    public static void writeId(Effect e,PacketBuffer pb) {
    	registry.writeId(pb, e);
    }
    public static Effect deserialize(JsonObject jo) {
        return registry.read(jo);
    }
    
    public static Effect deserialize(PacketBuffer data) {
        return registry.read(data);
    }
}
