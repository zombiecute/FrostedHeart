/*
 * Copyright (c) 2022-2024 TeamMoeg
 *
 * This file is part of Frosted Heart.
 *
 * Frosted Heart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * Frosted Heart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Frosted Heart. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.teammoeg.frostedheart.content.research.research.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teammoeg.frostedheart.FHTeamDataManager;
import com.teammoeg.frostedheart.base.team.SpecialDataTypes;
import com.teammoeg.frostedheart.base.team.TeamDataHolder;
import com.teammoeg.frostedheart.content.research.AutoIDItem;
import com.teammoeg.frostedheart.content.research.FHResearch;
import com.teammoeg.frostedheart.content.research.api.ClientResearchDataAPI;
import com.teammoeg.frostedheart.content.research.data.TeamResearchData;
import com.teammoeg.frostedheart.content.research.gui.FHIcons;
import com.teammoeg.frostedheart.content.research.gui.FHIcons.FHIcon;
import com.teammoeg.frostedheart.content.research.gui.FHTextUtil;
import com.teammoeg.frostedheart.content.research.network.FHEffectProgressSyncPacket;
import com.teammoeg.frostedheart.content.research.research.Research;
import com.teammoeg.frostedheart.util.io.SerializeUtil;
import com.teammoeg.frostedheart.util.io.Writeable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// TODO: Auto-generated Javadoc

/**
 * "Effect" of research: how would it become when research is completed ?.
 *
 * @author khjxiaogu
 * file: Effect.java
 * @date 2022/09/02
 */
public abstract class Effect extends AutoIDItem implements Writeable{
	public static class EffectData{
	    String name = "";
	    List<String> tooltip;
	    FHIcon icon;
	    String nonce;
	    boolean hidden;
		public EffectData(String name, List<String> tooltip, FHIcon icon, String nonce, boolean hidden) {
			super();
			this.name = name;
			this.tooltip = tooltip;
			this.icon = icon;
			this.nonce = nonce;
			this.hidden = hidden;
		}
	    
	}
	/*public static final Codec<EffectData> CODEC=RecordCodecBuilder.create(t->
	t.group(SerializeUtil.nullableCodecValue(Codec.STRING).fieldOf("name").forGetter(o->o.name),
		SerializeUtil.nullableCodec(Codec.list(Codec.STRING),ArrayList::new).fieldOf("tooltip").forGetter(o->o.tooltip),
		Codec));*/
    String name = "";

    List<String> tooltip;

    FHIcon icon;

    String nonce;

    boolean hidden;

    public transient Supplier<Research> parent;

    /**
     * Instantiates a new Effect.<br>
     */
    public Effect() {
        this("", new ArrayList<>());
    }

    /**
     * Instantiates a new Effect with a JsonObject object.<br>
     *
     * @param jo the jo<br>
     */
    public Effect(JsonObject jo) {
        if (jo.has("name"))
            name = jo.get("name").getAsString();
        if (jo.has("tooltip"))
            tooltip = SerializeUtil.parseJsonElmList(jo.get("tooltip"), JsonElement::getAsString);
        else
            tooltip = new ArrayList<>();
        if (jo.has("icon"))
            icon = FHIcons.getIcon(jo.get("icon"));
        nonce = jo.get("id").getAsString();
        if (jo.has("hidden"))
            hidden = jo.get("hidden").getAsBoolean();
    }

    /**
     * Instantiates a new Effect with a PacketBuffer object.<br>
     *
     * @param pb the pb<br>
     */
    Effect(PacketBuffer pb) {
        name = pb.readString();
        tooltip = SerializeUtil.readList(pb, PacketBuffer::readString);
        icon = SerializeUtil.readOptional(pb, FHIcons::readIcon).orElse(null);
        nonce = pb.readString();
        hidden = pb.readBoolean();
    }

    /**
     * Instantiates a new Effect.<br>
     *
     * @param name    the name<br>
     * @param tooltip the tooltip<br>
     */
    public Effect(String name, List<String> tooltip) {
        super();
        this.name = name;
        this.tooltip = tooltip;
        this.nonce = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
    }

    /**
     * Instantiates a new Effect.<br>
     *
     * @param name    the name<br>
     * @param tooltip the tooltip<br>
     * @param icon    the icon<br>
     */
    public Effect(String name, List<String> tooltip, FHIcon icon) {
        super();
        this.name = name;
        this.tooltip = tooltip;
        this.icon = icon;
        this.nonce = Long.toHexString(UUID.randomUUID().getMostSignificantBits());
    }

    /**
     * Instantiates a new Effect.<br>
     *
     * @param name    the name<br>
     * @param tooltip the tooltip<br>
     * @param icon    the icon<br>
     */
    public Effect(String name, List<String> tooltip, IItemProvider icon) {
        this(name, tooltip, FHIcons.getIcon(icon));
    }

    /**
     * Instantiates a new Effect.<br>
     *
     * @param name    the name<br>
     * @param tooltip the tooltip<br>
     * @param icon    the icon<br>
     */
    public Effect(String name, List<String> tooltip, ItemStack icon) {
        this(name, tooltip, FHIcons.getIcon(icon));
    }

    /**
     * Delete from the registry and research
     */
    public void delete() {
        deleteSelf();
        if (parent != null) {
            Research r = parent.get();
            if (r != null) {
                r.getEffects().remove(this);
            }
        }
    }

    private void deleteInTree() {
        FHTeamDataManager.INSTANCE.getAllData().forEach(t -> {
        	int iid=FHResearch.effects.getIntId(this);
            if (iid != 0) {
            	TeamResearchData trd=t.getData(SpecialDataTypes.RESEARCH_DATA);
                revoke(trd);

                trd.setGrant(this, false);
            }
        });
    }

    /**
     * Delete from the registry.
     */
    public void deleteSelf() {
        deleteInTree();
        FHResearch.effects.remove(this);
    }

    /**
     * Called when effect is edited.
     */
    public void edit() {
        deleteInTree();
    }

    /**
     * Get brief string describe this effect for show in editor.
     *
     * @return brief<br>
     */
    public abstract String getBrief();

    /**
     * Get default icon.
     * use this when no icon is set.
     *
     * @return default icon<br>
     */
    public abstract FHIcon getDefaultIcon();

    /**
     * Get default name.
     * use this when no name is set.
     *
     * @return default name<br>
     */
    public abstract IFormattableTextComponent getDefaultName();

    /**
     * Get default tooltip.
     * use this when no tooltip is set.
     *
     * @return default tooltip<br>
     */
    public abstract List<ITextComponent> getDefaultTooltip();

    /**
     * Get icon.
     *
     * @return icon<br>
     */
    public final FHIcon getIcon() {
        if (icon == null)
            return getDefaultIcon();
        return icon;
    }

    /**
     * Get name.
     *
     * @return name<br>
     */
    public final IFormattableTextComponent getName() {
        if (name.isEmpty())
            return getDefaultName();
        return (IFormattableTextComponent) FHTextUtil.get(name, "effect", this::getId);
    }

    /**
     * Get nonce.
     *
     * @return nonce<br>
     */
    @Override
    public String getNonce() {
        return nonce;
    }

    /**
     * Get tooltip.
     *
     * @return tooltip<br>
     */
    public final List<ITextComponent> getTooltip() {
        if (tooltip.isEmpty())
            return getDefaultTooltip();
        return FHTextUtil.get(tooltip, "effect", this::getId);
    }

    /**
     * Get type of this effect.
     *
     * @return type<br>
     */
    @Override
    public final String getType() {
        return "effects";
    }

    /**
     * Grant effect to a team.<br>
     * This would be call multiple times, especially when team data loaded from disk if this effect is marked granted.
     *
     * @param team          the team<br>
     * @param triggerPlayer the player trigger the grant, null if this is not triggered by player, typically press "claim" button.<br>
     * @param isload        true if this is run when loaded from disk<br>
     * @return true, if
     */
    public abstract boolean grant(TeamResearchData team, @Nullable PlayerEntity triggerPlayer, boolean isload);

    /**
     * Inits this effect globally.
     * Runs when research is loaded.
     */
    public abstract void init();

    /**
     * Checks if is granted for client.<br>
     *
     * @return if is granted,true.
     */
    @OnlyIn(Dist.CLIENT)
    public boolean isGranted() {
        return ClientResearchDataAPI.getData().isEffectGranted(this);
    }

    /**
     * Checks if is hidden.<br>
     *
     * @return if is hidden,true.
     */
    public boolean isHidden() {
        return hidden;
    }

    @OnlyIn(Dist.CLIENT)
    public void onClick() {
    }

    public void reload() {

    }

    /**
     * This is not necessary to implement as this is just for debugging propose.
     * Called only by research command or admin tools.
     *
     * @param team the team<br>
     */
    public abstract void revoke(TeamResearchData team);

    /**
     * Send effect progress packet for current effect to players in team.
     * Useful for data sync. This would called automatically, Their's no need to call this in effect.
     *
     * @param team the team<br>
     */
    public void sendProgressPacket(TeamDataHolder team) {
        FHEffectProgressSyncPacket packet = new FHEffectProgressSyncPacket(team, this);
        team.sendToOnline(packet);
    }

    /**
     * Serialize.<br>
     *
     * @return returns serialize
     */
    public JsonObject serialize() {
        JsonObject jo = new JsonObject();
        if (!name.isEmpty())
            jo.addProperty("name", name);
        if (!tooltip.isEmpty())
            jo.add("tooltip", SerializeUtil.toJsonStringList(tooltip, e -> e));
        if (icon != null)
            jo.add("icon", FHIcons.save(icon));
        jo.addProperty("id", nonce);
        if (isHidden())
            jo.addProperty("hidden", true);
        return jo;
    }

    /**
     * set granted.
     *
     * @param b value to set granted to.
     */
    @OnlyIn(Dist.CLIENT)
    public void setGranted(boolean b) {
        ClientResearchDataAPI.getData().setGrant(this, b);
    }

    /**
     * set new id, would change registry data.
     *
     * @param id value to set new id to.
     */
    void setNewId(String id) {
        if (!id.equals(this.nonce)) {
            delete();
            this.nonce = id;
            FHResearch.effects.register(this);
            if (parent != null) {
                Research r = parent.get();
                if (r != null) {
                    r.attachEffect(this);
                    r.doIndex();
                }
            }
        }
    }

    /**
     * Write.
     *
     * @param buffer the buffer<br>
     */
    public void write(PacketBuffer buffer) {
        buffer.writeString(name);
        SerializeUtil.writeList2(buffer, tooltip, PacketBuffer::writeString);
        SerializeUtil.writeOptional(buffer, icon, FHIcons::write);
        buffer.writeString(nonce);
        buffer.writeBoolean(isHidden());
    }
}
