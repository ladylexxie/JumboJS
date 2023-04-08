package ladylexxie.jumbojs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.RegisterRecipeTypesEvent;
import net.minecraft.resources.ResourceLocation;

public class JumboJSPlugin extends KubeJSPlugin{
	@Override
	public void registerRecipeTypes( RegisterRecipeTypesEvent event ) {
		event.register(new ResourceLocation("jumbofurnace:jumbo_smelting"), JumboFurnaceRecipeJS::new);
	}
}
