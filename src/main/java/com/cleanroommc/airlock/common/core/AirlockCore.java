package com.cleanroommc.airlock.common.core;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("Airlock")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
public class AirlockCore implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return null;
		// return new String[] { "com.cleanroommc.airlock.common.core.AirlockTransformer" };
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data) {

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}