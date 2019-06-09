package scramble.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
	              
	
	              List<Material> splitter2 = new ArrayList();
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
		int counter=1;
		while (Template.r.nextInt(amt)==0) {
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
  

  public void onEnable()
  {
    craftingrecipes = new ArrayList();
    smeltingrecipes = new ArrayList();
    shufflelist = new ArrayList();
    archivedshufflelist = new ArrayList();
    shufflelist2 = new ArrayList();
    recipeStorage = new ArrayList();
    plugin = (Template)getPlugin(Template.class);
    r = new Random(Bukkit.getWorld("world").getSeed());
    List<Recipe> defaultRecipes = new ArrayList();
    Iterator<Recipe> it = Bukkit.recipeIterator();
    while (it.hasNext()) {
      Recipe r = (Recipe)it.next();
      defaultRecipes.add(r);
      shufflelist2.add(r.getResult());
      Bukkit.getLogger().info("Found a recipe for " + r.getResult());
    }
    Bukkit.clearRecipes();
    HashMap<String, Integer> recipeTypeMap = new HashMap();
    
    while (shufflelist2.size() > 0) {
      Recipe rr = (Recipe)defaultRecipes.get(r.nextInt(defaultRecipes.size()));
	  boolean modified=true;
      try {
        if ((rr instanceof BlastingRecipe)) {
          BlastingRecipe br = (BlastingRecipe)rr;
          BlastingRecipe newbr = new BlastingRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice(), br.getExperience(), br.getCookingTime());
          Bukkit.addRecipe(newbr);
        }
        else if ((rr instanceof CampfireRecipe)) {
          CampfireRecipe br = (CampfireRecipe)rr;
          CampfireRecipe newbr = new CampfireRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice(), br.getExperience(), br.getCookingTime());
          Bukkit.addRecipe(newbr);
        }
        /*else if ((rr instanceof FurnaceRecipe)) {
          FurnaceRecipe br = (FurnaceRecipe)rr;
          FurnaceRecipe newbr = new FurnaceRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice(), br.getExperience(), br.getCookingTime());
          Bukkit.addRecipe(newbr);
        }*/
        else if ((rr instanceof MerchantRecipe)) {
          MerchantRecipe br = (MerchantRecipe)rr;
          MerchantRecipe newbr = new MerchantRecipe((ItemStack)shufflelist2.get(0), br.getUses(), br.getMaxUses(), true, br.getVillagerExperience(), br.getPriceMultiplier());
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
          SmokingRecipe newbr = new SmokingRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice(), br.getExperience(), br.getCookingTime());
          Bukkit.addRecipe(newbr);
        }
        else if ((rr instanceof StonecuttingRecipe)) {
          StonecuttingRecipe br = (StonecuttingRecipe)rr;
          StonecuttingRecipe newbr = new StonecuttingRecipe(br.getKey(), (ItemStack)shufflelist2.get(0), br.getInputChoice());
          Bukkit.addRecipe(newbr);
        } else {
        	modified=false;
        }
      }
      catch (IllegalStateException localIllegalStateException) {}
      
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
    
    PluginManager pm = getServer().getPluginManager();
    
    getCommand("block").setExecutor(new ExampleCommand());
    
    pm.registerEvents(new ExampleListener(), this);
  }
}
