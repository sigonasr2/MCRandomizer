package scramble.plugin.listener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.metadata.FixedMetadataValue;

import net.md_5.bungee.api.ChatColor;
import scramble.plugin.Template;

public class ExampleListener implements Listener {
	
	@EventHandler
	public void AcquireTrades(VillagerAcquireTradeEvent ev) {
		//Bukkit.getLogger().info(ev.getEntity()+" getting "+ev.getRecipe());
		MerchantRecipe prev = ev.getRecipe();
		MerchantRecipe newRecipe = new MerchantRecipe(new ItemStack(Material.getMaterial(Template.archivedshufflelist.get(Template.r.nextInt(Template.archivedshufflelist.size()))),Template.randomizeAmount(2)*Template.randomizeAmount(2)),
				prev.getUses(),prev.getMaxUses(),true,prev.getVillagerExperience(),prev.getPriceMultiplier());
		List<ItemStack> ingredients = new ArrayList<ItemStack>();
		ingredients.add(new ItemStack(Material.getMaterial(Template.archivedshufflelist.get(Template.r.nextInt(Template.archivedshufflelist.size()))),Template.randomizeAmount()));
		while (Template.r.nextInt(10)==0) {
			ingredients.add(new ItemStack(Material.getMaterial(Template.archivedshufflelist.get(Template.r.nextInt(Template.archivedshufflelist.size()))),Template.randomizeAmount()));
		}
		newRecipe.setIngredients(ingredients);
		ev.setRecipe(newRecipe);
	}
	
	@EventHandler
	public void onEntityBreed(EntityBreedEvent ev) {
		//Bukkit.getLogger().info("Breeder is "+ev.getBreeder());
		if (ev.getBreeder()!=null &&
				ev.getBreeder() instanceof Player) {
			ev.getEntity().remove();
			if (ev.getFather()!=null &&
					!ev.getFather().hasMetadata("hasBred") || Math.abs(ev.getFather().getWorld().getFullTime()-ev.getFather().getMetadata("hasBred").get(0).asLong())>1) {
				SpawnRandomizedEntity(ev.getFather());
			} else {
				if (!ev.getMother().hasMetadata("hasBred") || Math.abs(ev.getMother().getWorld().getFullTime()-ev.getMother().getMetadata("hasBred").get(0).asLong())>1) {
					SpawnRandomizedEntity(ev.getMother());
				}
			}
			if (ev.getFather()!=null) {
				ev.getFather().setMetadata("hasBred", new FixedMetadataValue(Template.getPlugin(),ev.getFather().getWorld().getFullTime()));
				Bukkit.getLogger().info("Set father hasBred to "+ev.getFather().getMetadata("hasBred").get(0).asLong());
			}
			if (ev.getMother()!=null) {
				ev.getMother().setMetadata("hasBred", new FixedMetadataValue(Template.getPlugin(),ev.getMother().getWorld().getFullTime()));
				Bukkit.getLogger().info("Set mother hasBred to "+ev.getFather().getMetadata("hasBred").get(0).asLong());
			}
		}
	}
	
	private void SpawnRandomizedEntity(LivingEntity ent) {
		Entity baby = ent.getWorld().spawnEntity(ent.getLocation(), Template.breedingTable.get(ent.getType()));
		if (baby instanceof Ageable) {
			Ageable baby_ent = (Ageable)baby;
			baby_ent.setBaby();
		}
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent ev) {
		if (Template.monsterDropTable.containsKey(ev.getEntityType())) {
			if (Template.r.nextInt(100)<Template.randomChance) {
				List<ItemStack> itemList = Template.monsterDropTable.get(ev.getEntityType());
				ev.getDrops().add(itemList.get(Template.r.nextInt(itemList.size())));
			}
		}
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		//Player p = event.getPlayer();
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
