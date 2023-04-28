package ladylexxie.jumbojs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientStack;
import dev.latvian.mods.kubejs.platform.forge.ingredient.IngredientStackImpl;
import dev.latvian.mods.kubejs.recipe.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class JumboFurnaceRecipeJS extends RecipeJS {
	public List<Ingredient> inputs;
	public ItemStack result;
	public float xp;

	@Override
	public JsonElement serializeIngredientStack( IngredientStack in ) {
		if( in.getCount() == 1 ) return in.getIngredient().toJson();
		JsonObject o = (JsonObject) in.getIngredient().toJson();
		o.addProperty("count", in.getCount());
		return o;
	}

	public List<Ingredient> parseIngredientsJumbo( Object o ) {
		List<Ingredient> inputs = new ArrayList<>();
		//		if(jsonArray.isJsonArray()){
		//			jsonArray.forEach(jsonElement -> {
		//				if(jsonElement.isJsonObject()){
		//					IngredientStack ingredientStack = new IngredientStackImpl(
		//							IngredientJS.ofJson(jsonElement.getAsJsonObject().get("ingredient")),
		//							jsonElement.getAsJsonObject()jsonElement.getAsJsonObject().has("count") ? jsonElement.getAsJsonObject().get("count").getAsInt() : 1
		//					);
		//					inputs.add(ingredientStack);
		//				}
		//			});
		//		}

		if(o instanceof JsonArray jsonArray){
			jsonArray.forEach(json -> {
				inputs.add(parseItemInput(json));
				System.out.println("===TEST=== " + json);
			});
		}

		return inputs;
	}

	@Override
	public void create( RecipeArguments args ) {
		result = parseItemOutput(args.get(0));
		inputs = parseIngredientsJumbo(args.get(1));
		if( args.size() >= 3 ) xp(args.getFloat(2, 0f));
	}

	public JumboFurnaceRecipeJS xp( float xp ) {
		json.addProperty("experience", Math.max(0f, xp));
		save();
		return this;
	}

	@Override
	public void deserialize() {
		result = parseItemOutput(json.get("result"));
		inputs = parseIngredientsJumbo(json.get("ingredients").getAsJsonArray());
		//		inputs = new ArrayList<>();
		//		JsonArray array = json.get("ingredients").getAsJsonArray();
		//		for(JsonElement element : array) {
		//			Ingredient ingredientJS = IngredientJS.ofJson(element);
		//			inputs.add(ingredientJS);
		//		}
	}

	@Override
	public void serialize() {
		if( serializeInputs ) {
			JsonArray array = new JsonArray();
			for( Ingredient ingredient : inputs ) {
				array.add(serializeIngredientStack((IngredientStackImpl) ingredient));
			}
			json.add("ingredients", array);
		}
		if( serializeOutputs ) {
			json.add("result", itemToJson(result));
		}
	}

	@Override
	public boolean hasInput( IngredientMatch match ) {
		for( Ingredient ingredient : inputs ) {
			if( match.contains(ingredient) ) return true;
		}
		return false;
	}

	@Override
	public boolean replaceInput( IngredientMatch match, Ingredient with, ItemInputTransformer transformer ) {
		boolean changed = false;

		for( int i = 0; i < inputs.size(); i++ ) {
			Ingredient ingredient = inputs.get(i);
			if( match.contains(ingredient) ) {
				inputs.set(i, transformer.transform(this, match, ingredient, with));
				changed = true;
			}
		}

		return changed;
	}

	@Override
	public boolean hasOutput( IngredientMatch match ) {
		return match.contains(result);
	}

	@Override
	public boolean replaceOutput( IngredientMatch match, ItemStack with, ItemOutputTransformer transformer ) {
		if( hasOutput(match) ) {
			result = transformer.transform(this, match, result, with);
			return true;
		}
		return false;
	}
}
