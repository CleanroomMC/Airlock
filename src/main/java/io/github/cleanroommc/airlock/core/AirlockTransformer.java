package io.github.cleanroommc.airlock.core;

import net.minecraft.launchwrapper.IClassTransformer;

public class AirlockTransformer implements IClassTransformer {

	@Override
	public byte[] transform(String name, String transformedName, byte[] basicClass) {
		return new byte[0];
	}

}
