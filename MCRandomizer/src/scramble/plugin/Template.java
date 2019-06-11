package scramble.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.StonecuttingRecipe;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import scramble.plugin.command.ExampleCommand;
import scramble.plugin.listener.ExampleListener;




public class Template
  extends JavaPlugin
{
  public static final String CHAT_PREFIX = ChatColor.AQUA + "Scrambled Recipes";
  
  static Template plugin;
  
  public static List<String> craftingrecipes;
  
  public static List<String> smeltingrecipes;
  
  public static List<RecipeStore> recipeStorage;
  public static List<String> shufflelist;
  public static List<String> archivedshufflelist;
  public static List<ItemStack> shufflelist2;
  public static HashMap<EntityType,EntityType> breedingTable = new HashMap<EntityType,EntityType>();
  public static boolean enableRecipeModifications = false;
  public static HashMap<EntityType,List<ItemStack>> monsterDropTable = new HashMap<EntityType,List<ItemStack>>();
  public static HashMap<String,FurnaceRecipeStore> furnaceRecipeTables = new HashMap<String,FurnaceRecipeStore>();
  public static ItemStack randomMelonItem;
  public static int recipe_count=0;
  public static boolean finished=false;
  public static int villager_count=0;
  
  PluginDescriptionFile pdfFile;
  
  public static Random r;
  public static int randomChance=10; //Chance of a random drop.
  
  public static Template getPlugin()
  {
    return plugin;
  }
  
  public static void ReadRecipeData() {
    if (!enableRecipeModifications) {
      File f = new File("recipe_data");
      ReadIntoList(f, craftingrecipes);
      File f2 = new File("furnace_data");
      ReadIntoList(f2, smeltingrecipes);
      plugin.getLogger().info("Loaded " + craftingrecipes.size() + " crafting entries, " + smeltingrecipes.size() + " furnace entries.");
      String shape;
      for (String s : smeltingrecipes) {
    	  String[] split = s.split(",");
    	  //FurnaceRecipe rec = new FurnaceRecipe(new NamespacedKey(plugin,"furnace"),new ItemStack(Material.getMaterial(name)));
    	  if (furnaceRecipeTables.containsKey(split[1])) {
    		  FurnaceRecipeStore frs = furnaceRecipeTables.get(split[1]);
    		  frs.craftingitems.add(Material.getMaterial(split[0]));
    		  furnaceRecipeTables.put(split[1], frs);
    	  } else {
        	  FurnaceRecipeStore frs = new FurnaceRecipeStore();
    		  furnaceRecipeTables.put(split[1],frs);
    		  frs.craftingitems.add(Material.getMaterial(split[0]));
        	  recipeStorage.add(frs);
        	  shufflelist.add(split[1]);
    	  }
      }
      for (String s : craftingrecipes)
      {
        String temp = s.substring(0, s.indexOf(","));
        s = s.substring(s.indexOf(",") + 1);
        switch (temp) {
	        case "0":
	          {
	            String[] split = s.replace(" ", "").split(",");
	            ShapelessRecipeStore srs = new ShapelessRecipeStore();
	            for (int i = 0; i < split.length - 1; i++) {
	              srs.craftingitems.add(split[i]);
	            }
	            recipeStorage.add(srs);
	            shufflelist.add(split[(split.length - 1)]);
	          }break;
	        case "1":{
	            shape = s.substring(s.indexOf('[') + 1, s.indexOf(']'));
	            String[] shape_split = shape.replace(" ", "").split(",");
	            s = s.substring(s.indexOf(']') + 1);
	            String itemsList = s.substring(s.indexOf('[') + 1, s.lastIndexOf(']'));
	            
	            String[] itemsSplit = itemsList.replace(" ", "").split("\\]\\[");
	            
	            ShapedRecipeStore srs = new ShapedRecipeStore(shape_split);
	            for (String s2 : itemsSplit) {
	              String[] splitter = s2.split(",");
	              
	
	              List<Material> splitter2 = new ArrayList<Material>();
	              for (int i = 1; i < splitter.length; i++) {
	                Material m = Material.getMaterial(splitter[i]);
	                
	                if (m != null) {
	                  splitter2.add(m);
	                }
	                else {
	                  splitter2.add(Material.AIR);
	                }
	              }
	              
	              srs.ingredientsMap.put(Character.valueOf(splitter[0].charAt(0)), new RecipeChoice.MaterialChoice(splitter2));
	            }
	            recipeStorage.add(srs);
	            s = s.substring(s.lastIndexOf(',') + 1);
	            shufflelist.add(s);
	          }
	          break; 
          }
      }
      plugin.getLogger().info("There are " + recipeStorage.size() + " recipes and " + shufflelist.size() + " shuffle items.");
      if (recipeStorage.size() != shufflelist.size()) {
        plugin.getLogger().severe("Recipe storage and Shuffle list sizes DO NOT MATCH. Exiting here...");
        Bukkit.shutdown();
      }
      
      plugin.getLogger().info("Creating recipes...");
      archivedshufflelist.addAll(shufflelist);
      while (shufflelist.size()>0)
      {
        Integer numb = Integer.valueOf(r.nextInt(recipeStorage.size()));
        RecipeStore rs = (RecipeStore)recipeStorage.get(numb.intValue());
        rs.setResultItem((String)shufflelist.get(0));
        if ((rs instanceof ShapedRecipeStore)) {
          ShapedRecipeStore rss = (ShapedRecipeStore)rs;
          Bukkit.getLogger().info(Arrays.toString(rss.shape));
          for (Character c : rss.ingredientsMap.keySet()) {
            Bukkit.getLogger().info("  " + c + ": " + rss.ingredientsMap.get(c));
          }
        }
        rs.createRecipe();
        shufflelist.remove(0);
        recipeStorage.remove(rs);
      }
      plugin.getLogger().info("Shuffling monster drops...");
      shufflelist.addAll(archivedshufflelist);
      EntityType[] monsterTypeList = new EntityType[]{
    		  EntityType.BLAZE,
    		  EntityType.CAVE_SPIDER,
    		  EntityType.CREEPER,
    		  EntityType.DROWNED,
    		  EntityType.ELDER_GUARDIAN,
    		  EntityType.ENDERMAN,
    		  EntityType.ENDERMITE,
    		  EntityType.EVOKER,
    		  EntityType.GUARDIAN,
    		  EntityType.HUSK,
    		  EntityType.ILLUSIONER,
    		  EntityType.PIG_ZOMBIE,
    		  EntityType.PILLAGER,
    		  EntityType.RAVAGER,
    		  EntityType.SILVERFISH,
    		  EntityType.SKELETON,
    		  EntityType.SPIDER,
    		  EntityType.STRAY,
    		  EntityType.VEX,
    		  EntityType.VINDICATOR,
    		  EntityType.WITCH,
    		  EntityType.WITHER,
    		  EntityType.ZOMBIE,
    		  EntityType.ZOMBIE_VILLAGER,
      };
      while (shufflelist.size()>0) {
    	  EntityType pick = monsterTypeList[r.nextInt(monsterTypeList.length)];
    	  ItemStack it = new ItemStack(Material.getMaterial(shufflelist.get(0)));
    	  if (monsterDropTable.containsKey(pick)) {
    		  List<ItemStack> addTable = monsterDropTable.get(pick);
    		  addTable.add(it);
    		  monsterDropTable.put(pick, addTable);
    	  } else {
    		  List<ItemStack> addTable = new ArrayList<ItemStack>();
    		  addTable.add(it);
    		  monsterDropTable.put(pick, addTable);
    	  }
    	  shufflelist.remove(0);
      }
      plugin.getLogger().info("Done! All recipes shuffled!");
    }
  }

	public static int randomizeAmount() {
		return randomizeAmount(4);
	}
	public static int randomizeAmount(int amt) {
		return randomizeAmount(Template.r,amt);
	}
	public static int randomizeAmount(Random r, int amt) {
		int counter=1;
		while (r.nextInt(amt)==0) {
			counter++;
		}
		return counter;
	}
  
  private static void ReadIntoList(File f, List<String> list)
  {
    try {
      FileReader fr = new FileReader(f);
      BufferedReader br = new BufferedReader(fr);
      String s = br.readLine();
      while (s != null) {
        list.add(s);
        s = br.readLine();
      }
      br.close();
      fr.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void AddInDefaultRecipes() {
	  ShapedRecipe sr = new ShapedRecipe(new ItemStack(Material.CRAFTING_TABLE));
	  sr.shape(new String[]{"aa","aa"});
	  sr.setIngredient('a', new RecipeChoice.MaterialChoice(new Material[]{
		Material.ACACIA_PLANKS,  
		Material.BIRCH_PLANKS,
		Material.DARK_OAK_PLANKS,
		Material.JUNGLE_PLANKS,
		Material.OAK_PLANKS,
		Material.SPRUCE_PLANKS,
	  }));
	  Bukkit.addRecipe(sr);
  }
  
  public void setBreedingTable() {
	  List<EntityType> canBreed = new ArrayList<EntityType>();
	  canBreed.addAll(Arrays.asList(new EntityType[]{
			  EntityType.HORSE,
			  EntityType.VILLAGER,
			  EntityType.CAT,
			  EntityType.CHICKEN,
			  EntityType.COW,
			  EntityType.DONKEY,
			  EntityType.FOX,
			  EntityType.LLAMA,
			  EntityType.MULE,
			  EntityType.MUSHROOM_COW,
			  EntityType.OCELOT,
			  EntityType.PANDA,
			  EntityType.PARROT,
			  EntityType.PIG,
			  EntityType.POLAR_BEAR,
			  EntityType.RABBIT,
			  EntityType.SHEEP,
			  EntityType.SKELETON_HORSE,
			  EntityType.TURTLE,
			  EntityType.VILLAGER,
			  EntityType.WANDERING_TRADER,
			  EntityType.WOLF,
			  EntityType.ZOMBIE_HORSE
	  }));
	  while (canBreed.size()>1) {
		  breedingTable.put(canBreed.remove(0), canBreed.remove(r.nextInt(canBreed.size())));
	  }
	  if (canBreed.size()>0) {
		  breedingTable.put(canBreed.get(0), canBreed.get(0));
	  }
  }

  boolean IsAlphabeticallyAfter(String name, String name2, String compare2, String compare22) {
		for (int i=0;i<name.length();i++) {
			if (i<name.length() && i<name2.length()) {
				if (name.charAt(i)<name2.charAt(i)) {
					return false;
				} else 
				if (name.charAt(i)>name2.charAt(i)) {
					return true;
				}
			} else {
				break;
			}
		}
		if (name.length()>name2.length()) {
			return true;
		} else 
		if (name.length()<name2.length()) {
			return false;
		} else {
			name=compare2;
			name2=compare22;
			for (int i=0;i<name.length();i++) {
				if (i<name.length() && i<name2.length()) {
					if (name.charAt(i)<name2.charAt(i)) {
						return false;
					} else 
					if (name.charAt(i)>name2.charAt(i)) {
						return true;
					}
				}
			}
			return false;
		}
	}

  public void onEnable()
  {
	//  Bukkit.getLogger().setLevel(Level.WARNING);
	Bukkit.resetRecipes();
    craftingrecipes = new ArrayList<String>();
    smeltingrecipes = new ArrayList<String>();
    shufflelist = new ArrayList<String>();
    archivedshufflelist = new ArrayList<String>();
    shufflelist2 = new ArrayList<ItemStack>();
    recipeStorage = new ArrayList<RecipeStore>();
    plugin = (Template)getPlugin(Template.class);
    r = new Random(Bukkit.getWorld("world").getSeed());
    Bukkit.getLogger().info("Random seed is "+Bukkit.getWorld("world").getSeed());
    LoadVillagerCount();
    List<Recipe> defaultRecipes = new ArrayList<Recipe>();
    Iterator<Recipe> it = Bukkit.recipeIterator();
    while (it.hasNext()) {
      Recipe r = it.next();
      boolean added=false;
      /*if (r instanceof BlastingRecipe ||
    		  r instanceof CampfireRecipe ||
    		  r instanceof SmokingRecipe ||
    		  r instanceof StonecuttingRecipe) {*/
	      for (int i=0;i<shufflelist2.size();i++) {
	    	  if (!IsAlphabeticallyAfter(r.getResult().getType().name(),shufflelist2.get(i).getType().name(),r.getClass().getName(),defaultRecipes.get(i).getClass().getName())) {
	    		  //Bukkit.getLogger().info(r.getResult().getType().name()+" is not alphabetically after "+shufflelist2.get(i).getType().name()+". Inserting at position "+i);
	    		  shufflelist2.add(i,r.getResult());
	    	      defaultRecipes.add(i,r);
	    		  added=true;
	    		  break;
	    	  }
	      }
	      if (!added) {
	    	  if (shufflelist2.size()>0) {
	    		  //Bukkit.getLogger().info(r.getResult().getType().name()+" is alphabetically after "+shufflelist2.get(shufflelist2.size()-1).getType().name()+". Inserting at the end.");
	    	  }
	    	  shufflelist2.add(r.getResult());
		      defaultRecipes.add(r);
	      }
	      //Bukkit.getLogger().info("Found a recipe for " + r.getResult());
	      //THIS LIST IS NOT SORTED. REQUIRES MANUAL SORTING.
      //}
    }
    for (int i=0;i<shufflelist2.size();i++) {
    	Bukkit.getLogger().info("Found a recipe for "+shufflelist2.get(i)+"|"+defaultRecipes.get(i).getResult());
    }
    Bukkit.clearRecipes();
    HashMap<String, Integer> recipeTypeMap = new HashMap<String, Integer>();
    
    int blastingrecipe = 0;
    int campfirerecipe = 0;
    int smokingrecipe = 0;
    int stonecuttingrecipe = 0;
    
    for (int i=0;i<defaultRecipes.size();i++) {
    	Bukkit.getLogger().info(i+": "+defaultRecipes.get(i).getResult() + " - "+defaultRecipes.get(i).getClass());
    }
    
    while (shufflelist2.size() > 0) {
      int recipe_id = r.nextInt(defaultRecipes.size());
      Recipe rr = defaultRecipes.get(recipe_id);
      Bukkit.getLogger().info("Adding recipe for "+shufflelist2.get(0)+" at recipe "+recipe_id);
	  boolean modified=true;
      //int amt = Bukkit.getRecipesFor(shufflelist2.get(0)).size();
	    if ((rr instanceof BlastingRecipe)) {
	      BlastingRecipe br = (BlastingRecipe)rr;
	      BlastingRecipe newbr = new BlastingRecipe(new NamespacedKey(plugin,"blasting_recipe"+shufflelist2.size()), shufflelist2.get(0), br.getInput().getType(), br.getExperience(), br.getCookingTime());
	      Bukkit.addRecipe(newbr);
	        Bukkit.getLogger().info(" BLASTING");
	      blastingrecipe++;
	    }
	    else if ((rr instanceof CampfireRecipe)) {
	      CampfireRecipe br = (CampfireRecipe)rr;
	      CampfireRecipe newbr = new CampfireRecipe(new NamespacedKey(plugin,"campfire_recipe"+shufflelist2.size()), shufflelist2.get(0), br.getInput().getType(), br.getExperience(), br.getCookingTime());
	      Bukkit.addRecipe(newbr);
	        Bukkit.getLogger().info(" CAMPFIRE");
	      campfirerecipe++;
	    }
	    /*else if ((rr instanceof FurnaceRecipe)) {
	      FurnaceRecipe br = (FurnaceRecipe)rr;
	      FurnaceRecipe newbr = new FurnaceRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice(), br.getExperience(), br.getCookingTime());
	      Bukkit.addRecipe(newbr);
	    }*/
	    else if ((rr instanceof MerchantRecipe)) {
	      MerchantRecipe br = (MerchantRecipe)rr;
	      MerchantRecipe newbr = new MerchantRecipe(shufflelist2.get(0), br.getUses(), br.getMaxUses(), true, br.getVillagerExperience(), br.getPriceMultiplier());
	      Bukkit.addRecipe(newbr);
	    }
	    /*else if ((rr instanceof ShapedRecipe)) {
	      ShapedRecipe br = (ShapedRecipe)rr;
	      ShapedRecipe newbr = new ShapedRecipe((ItemStack)shufflelist2.get(0));
	      newbr.shape(br.getShape());
	      
	      for (Character c : br.getChoiceMap().keySet())
	      {
	        newbr.setIngredient(c.charValue(), (RecipeChoice)br.getChoiceMap().get(c));
	      }
	      Bukkit.addRecipe(newbr);
	    }
	    else if ((rr instanceof ShapelessRecipe)) {
	      ShapelessRecipe br = (ShapelessRecipe)rr;
	      ShapelessRecipe newbr = new ShapelessRecipe((ItemStack)shufflelist2.get(0));
	      for (ItemStack i : br.getIngredientList()) {
	        newbr.addIngredient(i.getType());
	      }
	      Bukkit.addRecipe(newbr);
	    }*/
	    else if ((rr instanceof SmokingRecipe)) {
	      SmokingRecipe br = (SmokingRecipe)rr;
	      SmokingRecipe newbr = new SmokingRecipe(new NamespacedKey(plugin,"smoking_recipe"+shufflelist2.size()), shufflelist2.get(0), br.getInput().getType(), br.getExperience(), br.getCookingTime());
	      Bukkit.addRecipe(newbr);
	        Bukkit.getLogger().info(" SMOKING");
	      smokingrecipe++;
	    }
	    else if ((rr instanceof StonecuttingRecipe)) {
	      StonecuttingRecipe br = (StonecuttingRecipe)rr;
	      StonecuttingRecipe newbr = new StonecuttingRecipe(new NamespacedKey(plugin,"stonecutting_recipe"+shufflelist2.size()), shufflelist2.get(0), br.getInput().getType());
	    	Bukkit.addRecipe(newbr);
	        Bukkit.getLogger().info(" STONECUTTING");
		      stonecuttingrecipe++;
	    } else {
	        Bukkit.getLogger().info(" UNMODIFIED ("+rr.getClass().getName()+")");
	    	modified=false;
	    }
      
      if (modified) {
    	  recipeTypeMap.put(rr.getClass().getName(), Integer.valueOf(recipeTypeMap.containsKey(rr.getClass().getName()) ? ((Integer)recipeTypeMap.get(rr.getClass().getName())).intValue() + 1 : 1));
      }
      shufflelist2.remove(0);
    }
    ReadRecipeData();
    AddInDefaultRecipes();
    setBreedingTable();

    randomMelonItem = new ItemStack(Material.getMaterial(Template.archivedshufflelist.get(Template.r.nextInt(Template.archivedshufflelist.size()))));


    for (String s : recipeTypeMap.keySet()) {
      Bukkit.getLogger().info(" Randomized " + recipeTypeMap.get(s) + " " + s + " recipes.");
    }
    Bukkit.getLogger().info("");
    Bukkit.getLogger().info("Blasting: "+blastingrecipe);
    Bukkit.getLogger().info("Campefire: "+campfirerecipe);
    Bukkit.getLogger().info("Smoking: "+smokingrecipe);
    Bukkit.getLogger().info("Stonecutting: "+stonecuttingrecipe);
    
    PluginManager pm = getServer().getPluginManager();
    
    getCommand("block").setExecutor(new ExampleCommand());
    
    pm.registerEvents(new ExampleListener(), this);
    
    finished=true;
  }

	public static void LoadVillagerCount() {
		File f = new File("villager_count");
		if (f.exists()) {
			try {
				FileReader fr = new FileReader(f);
				BufferedReader br = new BufferedReader(fr);
				villager_count = Integer.parseInt(br.readLine());
				Bukkit.getLogger().info(villager_count+" Villagers loaded.");
				br.close();
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public static void IncreaseAndSaveVillagerCount() {
		File f = new File("villager_count");
		try {
			FileWriter fw = new FileWriter(f);
			fw.write((++villager_count)+"");
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
