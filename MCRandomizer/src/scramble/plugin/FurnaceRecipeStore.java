package scramble.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

public class FurnaceRecipeStore implements RecipeStore{
	public List<Material> craftingitems = new ArrayList<Material>();
	public String finalItem;

	@Override
	public void createRecipe() {
		FurnaceRecipe rec = new FurnaceRecipe(new NamespacedKey(Template.plugin,"furnace"+(Template.recipe_count++)),new ItemStack(Material.getMaterial(finalItem)),new RecipeChoice.MaterialChoice(craftingitems),
				Template.r.nextInt(100)+1f,Template.r.nextInt(81)+20);
		Bukkit.getLogger().info("Added recipe for "+finalItem);
		Bukkit.getLogger().info("Furnace recipe w/");
		Bukkit.getLogger().info("  "+craftingitems.toString());
		Bukkit.addRecipe(rec);
	}

	@Override
	public void setResultItem(String item) {
		this.finalItem=item;
	}
	//6,12,11,102
}
