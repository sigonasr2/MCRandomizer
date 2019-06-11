package scramble.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;

public class ShapelessRecipeStore implements RecipeStore{
	public List<String> craftingitems = new ArrayList<String>();
	public String finalItem;

	@Override
	public void createRecipe() {
		ShapelessRecipe sr = new ShapelessRecipe(new NamespacedKey(Template.plugin,"shapelessrecipe_"+(Template.recipe_count++)),new ItemStack(Material.getMaterial(finalItem),Template.randomizeAmount()));
		for (int i=0;i<craftingitems.size();i++) {
			sr.addIngredient(Material.getMaterial(craftingitems.get(i)));
			Bukkit.getLogger().info("  Added ingredient "+craftingitems.get(i));
		}
		Bukkit.getLogger().info("Added recipe for "+finalItem);
		Bukkit.addRecipe(sr);
	}

	@Override
	public void setResultItem(String item) {
		this.finalItem=item;
	}
	
}
