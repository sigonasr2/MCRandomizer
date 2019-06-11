package scramble.plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class ShapedRecipeStore implements RecipeStore{
	public String finalItem;
	public String[] shape;
	public HashMap<Character,MaterialChoice> ingredientsMap = new HashMap<Character,MaterialChoice>(); 
	
	public ShapedRecipeStore(String[] shape) {
		this.shape=shape;
	}

	@Override
	public void createRecipe() {
		ShapedRecipe sr = new ShapedRecipe(new NamespacedKey(Template.plugin,"shapedrecipe_"+(Template.recipe_count++)),new ItemStack(Material.getMaterial(finalItem),Template.randomizeAmount()));
		sr.shape(shape);
		for (Character c : ingredientsMap.keySet()) {
			MaterialChoice m = ingredientsMap.get(c);
			if (m==null) {
				sr.setIngredient(c, Material.AIR);
			} else {
				sr.setIngredient(c, m);
			}
		}
		Bukkit.getLogger().info("Added recipe for "+finalItem);
		Bukkit.addRecipe(sr);
	}

	@Override
	public void setResultItem(String item) {
		this.finalItem=item;
	}
	
}
