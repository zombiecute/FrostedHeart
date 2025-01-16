package com.teammoeg.chorda.util.io.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.network.FriendlyByteBuf;

public class PacketBufferSerializer<T> {

	private List<Function<FriendlyByteBuf, T>> fromPacket = new ArrayList<>();
	private Map<Class<? extends T>,Integer> types=new HashMap<>();
	public PacketBufferSerializer() {
		super();
	}

	public T read(FriendlyByteBuf pb) {
	    int id = pb.readByte();
	    if (id < 0 || id >= fromPacket.size())
	        throw new IllegalArgumentException("Packet Error");
	    return fromPacket.get(id).apply(pb);
	}
	public void register(Class<? extends T> cls,Function<FriendlyByteBuf, T> from) {
		int id=fromPacket.size();
		fromPacket.add(from);
		types.put(cls, id);
	}
	public T readOrDefault(FriendlyByteBuf pb, T def) {
	    int id = pb.readByte();
	    if (id < 0 || id >= fromPacket.size())
	        return def;
	    return fromPacket.get(id).apply(pb);
	}

	protected void writeId(FriendlyByteBuf pb, T obj) {
		Integer dat=types.get(obj.getClass());
		if(dat==null)dat=0;
	    pb.writeByte(dat);
	}

}