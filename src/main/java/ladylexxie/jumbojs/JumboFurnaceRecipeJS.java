package ladylexxie.jumbojs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
	public void create( RecipeArguments args ) {
		result = parseItemOutput(args.get(0));
		inputs = parseItemInputList(args.get(1));
		if( args.size() >= 3 ) xp(args.getFloat(2, 0f));
//		for( Ingredient ingredient : inputs ) {
//			int count = ((IngredientStackImpl) ingredient).getCount();
//			System.out.println("===TEST=== " + count);
//		}
	}

	public JumboFurnaceRecipeJS xp( float xp ) {
		json.addProperty("experience", Math.max(0f, xp));
		save();
		return this;
	}

	@Override
	public void deserialize() {
		result = parseItemOutput(json.get("result"));
//		inputs = parseItemInputList(json.get("ingredients"));
		List<Ingredient> ingredients = new ArrayList<>();
		JsonArray array = json.get("ingredients").getAsJsonArray();
		for(JsonElement element : array) {
			IngredientStackImpl ingredientStack = (IngredientStackImpl) parseItemInput(element);
			if(element.getAsJsonObject().has("count")) ingredientStack.kjs$withCount(element.getAsJsonObject().get("count").getAsInt());
			ingredients.add(ingredientStack);
		}
		inputs = ingredients;
	}

	@Override
	public void serialize() {
		if( serializeInputs ) {
			JsonArray array = new JsonArray();
			for( Ingredient ingredient : inputs ) {
				int count = ((IngredientStackImpl) ingredient).getCount();
				JsonObject jsonObject = ingredient.toJson().getAsJsonObject();
				jsonObject.addProperty("count", count);
				array.add(jsonObject);
			}
			System.out.println("===TEST=== " + array);
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
