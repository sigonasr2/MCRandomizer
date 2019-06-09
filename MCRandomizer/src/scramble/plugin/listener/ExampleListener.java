package scramble.plugin.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.md_5.bungee.api.ChatColor;
import scramble.plugin.Template;

public class ExampleListener implements Listener {

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		/*
		 * We get the player and make a variable to make it easier to access it when we
		 * need to use it.
		 */
		//Player p = event.getPlayer();
		/*
		 * Here we cancel the event. This means that they can't break the block. In this
		 * case, we send a message to the player saying they don't have the required
		 * permission.
		 */
		/*if (!p.hasPermission("template.breakblocks")) {
			p.sendMessage(Template.CHAT_PREFIX +  ChatColor.WHITE + " > " + ChatColor.RED + "You do not have permission to break blocks!");
			event.setCancelled(true);
		}*/
	}
	
	@EventHandler
	public void onCraftItemEvent(CraftItemEvent ev) {
		/*for (in
				//ev.getViewers().get(0).sendMessagt i=0;i<9;i++) {
			//ev.getViewers().get(0).sendMessage(ev.getInventory().getItem(0).getData().toString());
		}*/
		//ev.getInventory().getSize()
		
		if (Template.enableRecipeModifications) {
			Recipe rec = ev.getRecipe();
			if (rec instanceof ShapelessRecipe) {
				StringBuilder sb = new StringBuilder("0");
				ShapelessRecipe rec2 = (ShapelessRecipe)rec;
				for (int i=0;i<rec2.getIngredientList().size();i++) {
					//ev.getViewers().get(0).sendMessage(rec2.getIngredientList().get(i).getData().toString());
					sb.append(","+getCutItemStackData(rec2.getIngredientList().get(i)));
				}
				sb.append(","+getCutItemStackData(rec2.getResult()));
				SaveData(ev, sb);
			}
			if (rec instanceof ShapedRecipe) {
				ShapedRecipe rec2 = (ShapedRecipe)rec;
				Map<Character,RecipeChoice> map1 = rec2.getChoiceMap();
				Map<Character,ItemStack> map2 = rec2.getIngredientMap();
				StringBuilder sb = new StringBuilder("1");
				//ev.getViewers().get(0).sendMessage(Arrays.toString(rec2.getShape()));
				sb.append(","+Arrays.toString(rec2.getShape()));
				for (Character c : map1.keySet()) {
					//ItemStack it = map2.get(c).;
					sb.append("["+c+","+getCutChoices(map1.get(c))+"]");
					//ev.getViewers().get(0).sendMessage(c+","+map1.get(c));
					//ev.getViewers().get(0).sendMessage(rec2.getIngredientList().get(i).getData().toString());
				}
				sb.append(","+getCutItemStackData(rec2.getResult()));
				SaveData(ev, sb);
			}
		}
	}
	
	@EventHandler
	public void onSmeltItemEvent(FurnaceSmeltEvent ev) {
		if (Template.enableRecipeModifications) {
			Player p = Bukkit.getPlayer("sigonasr2");
			if (p!=null && p.isOnline()) {
				StringBuilder sb = new StringBuilder(getCutItemStackData(ev.getSource()));
				sb.append(","+getCutItemStackData(ev.getResult()));
				Template.smeltingrecipes.add(sb.toString());
				p.sendMessage("Item["+Template.smeltingrecipes.size()+"] "+sb.toString()+" added.");
				File f = new File("furnace_data");
				try{ 
					FileWriter fw = new FileWriter(f,true);
					fw.write(sb.toString()+"\n");
					fw.close();
				} catch (IOException e) {
					
				}
			}
		}
	}
	
	public String getCutItemStackData(ItemStack it) {
		String s=it.toString().replace("ItemStack{", "");
	    return s.substring(0,s.indexOf(' '));
	}
	public String getCutChoices(RecipeChoice c) {
		if (c!=null) {
			String s = c.toString().replace("MaterialChoice{choices=[", "");
			return s.substring(0,s.indexOf(']'));
		} else {
			return "NULL";
		}
	}

	private void SaveData(CraftItemEvent ev, StringBuilder sb) {
		Template.craftingrecipes.add(sb.toString());
		ev.getViewers().get(0).sendMessage("Item["+Template.craftingrecipes.size()+"] " +sb.toString()+" added.");

		File f = new File("recipe_data");
		try {
			FileWriter fw = new FileWriter(f,true);
			fw.write(sb.toString()+"\n");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
