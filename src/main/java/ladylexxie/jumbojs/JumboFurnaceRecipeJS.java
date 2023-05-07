package ladylexxie.jumbojs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;
import dev.latvian.mods.kubejs.item.ingredient.IngredientStack;
import dev.latvian.mods.kubejs.platform.forge.ingredient.IngredientStackImpl;
import dev.latvian.mods.kubejs.recipe.*;
import dev.latvian.mods.kubejs.util.MapJS;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class JumboFurnaceRecipeJS extends RecipeJS {
//	public static final Logger LOG = LogManager.getLogger();
	public List<IngredientStackImpl> inputs;
	public ItemStack result;
	public float xp;

	@Override
	public JsonElement serializeIngredientStack( IngredientStack in ) {
		JsonElement o = in.getIngredient().toJson();
		if( o.isJsonObject() ) {
			JsonObject obj = o.getAsJsonObject();
			while(obj.has("type")
					&& obj.get("type").isJsonPrimitive()
					&& obj.getAsJsonPrimitive("type").isString()
					&& obj.getAsJsonPrimitive("type").getAsString().equalsIgnoreCase("kubejs:stack")){
//				LOG.info("Unwrapped double ingredient stack: {}", obj);
				obj = obj.getAsJsonObject("ingredient");
			}
			obj.addProperty("type", "forge:nbt");
			obj.addProperty("count", in.getCount());
			return obj;
		}
		return o;
	}

	public List<IngredientStackImpl> parseIngredientsJumbo( Object o ) {
		if(o == null) return new ArrayList<>();

		List<IngredientStackImpl> inputs = new ArrayList<>();
		if (o instanceof IngredientStackImpl ingredientStack){
			inputs.add(ingredientStack);
		} else if(o instanceof Ingredient ingredient){
			inputs.add(new IngredientStackImpl(ingredient,1));
		} else if (o instanceof Iterable<?> iterable){
			iterable.forEach(object -> inputs.addAll(parseIngredientsJumbo(object)));
		} else {
			Ingredient ingredient = IngredientJS.of(o);
			if(ingredient == null) return inputs;
			if (ingredient instanceof IngredientStack stack) {
				inputs.add((IngredientStackImpl) stack);
			}else {
				inputs.add(new IngredientStackImpl(ingredient, 1));
			}
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
		inputs = parseIngredientsJumbo(json.get("ingredients"));
		if(json.has("experience"))
			xp = json.get("experience").getAsFloat();
	}

	@Override
	public void serialize() {
		if( serializeInputs ) {
			JsonArray array = new JsonArray();
			for( IngredientStackImpl ingredient : inputs ) {
				final JsonElement serialize = serializeIngredientStack(ingredient);
//				LOG.info("Serialized JumboFurnaceRecipeJS ingredient from {}x{} to {}", ingredient.getIngredient(), ingredient.getCount(), serialize);
				array.add(serialize);
			}
			json.add("ingredients", array);
		}
		if( serializeOutputs ) {
			json.add("result", itemToJson(result));
		}

//		LOG.info("Serialized JumboFurnaceRecipeJS JSON: {}", json);
	}

	@Override
	public boolean hasInput( IngredientMatch match ) {
		for( IngredientStack ingredient : inputs ) {
			if( match.contains(ingredient.getIngredient()) ) return true;
		}
		return false;
	}

	@Override
	public boolean replaceInput( IngredientMatch match, Ingredient with, ItemInputTransformer transformer ) {
		boolean changed = false;

		for( int i = 0; i < inputs.size(); i++ ) {
			IngredientStackImpl ingredient = inputs.get(i);
			if( match.contains(ingredient.getIngredient()) ) {
				final Ingredient transformed = transformer.transform(this, match, ingredient, with);
				if( with instanceof IngredientStackImpl withStack) {
					inputs.set(i, new IngredientStackImpl(transformed, withStack.getCount()));
				} else {
					inputs.set(i, new IngredientStackImpl(transformed, 1));
				}
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
