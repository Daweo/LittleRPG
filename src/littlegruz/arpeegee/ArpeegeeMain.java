package littlegruz.arpeegee;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import littlegruz.arpeegee.commands.Begin;
import littlegruz.arpeegee.commands.Join;
import littlegruz.arpeegee.commands.Quests;
import littlegruz.arpeegee.commands.Worlds;
import littlegruz.arpeegee.entities.RPGMagicPlayer;
import littlegruz.arpeegee.entities.RPGMeleePlayer;
import littlegruz.arpeegee.entities.RPGQuest;
import littlegruz.arpeegee.entities.RPGRangedPlayer;
import littlegruz.arpeegee.entities.RPGSubClass;
import littlegruz.arpeegee.gui.LittleGUI;
import littlegruz.arpeegee.listeners.ButtonListener;
import littlegruz.arpeegee.listeners.EnemyDeaths;
import littlegruz.arpeegee.listeners.EntityDamageEntity;
import littlegruz.arpeegee.listeners.PlayerInteract;
import littlegruz.arpeegee.listeners.PlayerJoin;
import littlegruz.arpeegee.listeners.PlayerLevel;
import littlegruz.arpeegee.listeners.PlayerProjectile;
import littlegruz.arpeegee.listeners.PlayerSpeed;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

/* Make armour degrade and then be dropped by mobs
 * Levels and stat increase DONE (but no tested balancing)*/

/* TODO: Feather falling potion effect for ranged.
 * Damaged armour when respawning.
 * Bonus's from wearing a full set of armour.
 * Damage over time attacks.
 * Status ailments.
 * Enchanted melee/ranged weapons.
 * Ability for users to add fetch/travel quests.
 * Parties. Woo PARTAY!*/

public class ArpeegeeMain extends JavaPlugin {
   private LittleGUI gui;
   private File meleePlayerFile;
   private File rangedPlayerFile;
   private File magicPlayerFile;
   private File questStartFile;
   //private File subClassFile;
   private File worldsFile;
   private File textsFile;
   private HashMap<String, RPGMeleePlayer> meleePlayerMap;
   private HashMap<String, RPGRangedPlayer> rangedPlayerMap;
   private HashMap<String, RPGMagicPlayer> magicPlayerMap;
   //private HashMap<String, RPGSubClass> subClassMap;
   private HashMap<Integer, RPGQuest> questMap;
   private HashMap<Location, Integer> questStartMap;
   private HashMap<String, String> berserkMap;
   private HashMap<Entity, String> projMap;
   private HashMap<String, String> worldsMap;
   private HashMap<String, String> textsMap;
   private HashMap<Integer, Integer> expLevelMap;
   private int questNumberToSet;
   private boolean questCanSet;
   private boolean questCanUnset;
   private boolean spoutEnabled;
   
   public final int MAX_LEVEL = 20;
   
   public void onEnable(){
      BufferedReader br;
      String input;
      StringTokenizer st;
      
      // Create the directory if needed
      new File(getDataFolder().toString()).mkdir();
      meleePlayerFile = new File(getDataFolder().toString() + "/meleePlayer.txt");
      rangedPlayerFile = new File(getDataFolder().toString() + "/rangedPlayer.txt");
      magicPlayerFile = new File(getDataFolder().toString() + "/magicPlayer.txt");
      questStartFile = new File(getDataFolder().toString() + "/questStarters.txt");
      //subClassFile = new File(getDataFolder().toString() + "/subclasses.txt");
      worldsFile = new File(getDataFolder().toString() + "/worlds.txt");
      textsFile = new File(getDataFolder().toString() + "/texts.txt");
      
      spoutEnabled = getServer().getPluginManager().isPluginEnabled("Spout");

      /*subClassMap = new HashMap<String, RPGSubClass>();
      // Load up the sub-classes from file
      try{
         br = new BufferedReader(new FileReader(subClassFile));
         
         // Load sub-class file data into the sub-class HashMap
         while((input = br.readLine()) != null){
            String name;
            st = new StringTokenizer(input, " ");
            name = st.nextToken();
            subClassMap.put(name, new RPGSubClass(name,
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken())));
         }
         br.close();

      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG sub-class file found. One will be created for you");
         subClassMap.put("Wizard", new RPGSubClass("Wizard", 0, 0, 0, 1, 2));
         subClassMap.put("Warrior", new RPGSubClass("Warrior", 0, 1, 1, 0, 0));
         subClassMap.put("Archer", new RPGSubClass("Archer", 1, 0, 0, 1, 0));
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG sub-class file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG sub-class file");
      }*/

      meleePlayerMap = new HashMap<String, RPGMeleePlayer>();
      // Load up the melee players from file
      try{
         br = new BufferedReader(new FileReader(meleePlayerFile));
         
         // Load player file data into the player HashMap
         while((input = br.readLine()) != null){
            String name;
            RPGSubClass rpgSubClass;
            int level, rage;
            
            st = new StringTokenizer(input, " ");
            name = st.nextToken();
            rpgSubClass = new RPGSubClass(st.nextToken(),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()));
            
            /*if(subClassMap.get(rpgSubClass.getName()) == null)
               getLogger().warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");*/

            //TODO (Remove eventually) This stuff is here to make upgrading past v1.1 un-noticable for users
            level = Integer.parseInt(st.nextToken());
            rage = Integer.parseInt(st.nextToken());
            if(st.hasMoreTokens())
               meleePlayerMap.put(name, new RPGMeleePlayer(name, rpgSubClass, level, rage, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken())));
            else
               meleePlayerMap.put(name, new RPGMeleePlayer(name, rpgSubClass, level, rage, "none", "none", -1));
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG melee player file found. One will be created for you");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG melee player file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG melee player file");
      }

      rangedPlayerMap = new HashMap<String, RPGRangedPlayer>();
      // Load up the ranged players from file
      try{
         br = new BufferedReader(new FileReader(rangedPlayerFile));
         
         // Load ranged player file data into the ranged player HashMap
         while((input = br.readLine()) != null){
            String name;
            RPGSubClass rpgSubClass;
            int level;
            
            st = new StringTokenizer(input, " ");
            name = st.nextToken();
            rpgSubClass = new RPGSubClass(st.nextToken(),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()));
            
            /*if(subClassMap.get(rpgSubClass.getName()) == null)
               getLogger().warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");*/

            // This stuff is here to make upgrading un-noticable for users
            level = Integer.parseInt(st.nextToken());
            if(st.hasMoreTokens())
               rangedPlayerMap.put(name, new RPGRangedPlayer(name, rpgSubClass, level, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken())));
            else
               rangedPlayerMap.put(name, new RPGRangedPlayer(name, rpgSubClass, level, "none", "none", -1));
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG ranged player file found. One will be created for you");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG ranged player file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG ranged player file");
      }

      magicPlayerMap = new HashMap<String, RPGMagicPlayer>();
      // Load up the magic players from file
      try{
         br = new BufferedReader(new FileReader(magicPlayerFile));
         
         // Load magic player file data into the magic player HashMap
         while((input = br.readLine()) != null){
            String name;
            RPGSubClass rpgSubClass;
            int level;
            
            st = new StringTokenizer(input, " ");
            name = st.nextToken();
            rpgSubClass = new RPGSubClass(st.nextToken(),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()),
                  Double.parseDouble(st.nextToken()));
            
            /*if(subClassMap.get(rpgSubClass.getName()) == null)
               getLogger().warning("Player " + name + " has an unfound sub-class name. Please fix this before they login.");*/
            
            // This stuff is here to make upgrading un-noticable for users
            level = Integer.parseInt(st.nextToken());
            if(st.hasMoreTokens())
               magicPlayerMap.put(name, new RPGMagicPlayer(name, rpgSubClass, level, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken())));
            else
               magicPlayerMap.put(name, new RPGMagicPlayer(name, rpgSubClass, level, "none", "none", -1));
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG magic player file found. One will be created for you");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG magic player file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG magic player file");
      }
      
      worldsMap = new HashMap<String, String>();
      // Load up the worlds from file
      try{
         br = new BufferedReader(new FileReader(worldsFile));
         
         // Load world file data into the world HashMap
         while((input = br.readLine()) != null){
            worldsMap.put(input, input);
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG world file found. One will be created for you");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG world file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG world file");
      }
      
      textsMap = new HashMap<String, String>();
      // Load up the worlds from file
      try{
         String type, msg;
         br = new BufferedReader(new FileReader(textsFile));
         
         // Load world file data into the world HashMap
         while((input = br.readLine()) != null){
            st = new StringTokenizer(input, " ");
            type = st.nextToken();
            msg = st.nextToken();
            
            while(st.hasMoreTokens())
               msg += " " + st.nextToken();
            
            textsMap.put(type, msg);
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG text file found. One will be created for you");
         
         if(!spoutEnabled){
         textsMap.put("intro", ChatColor.RED + " Welcome to a LittleRPG world. Please choose a class to begin. "
               + ChatColor.YELLOW + "Melee class: type /ichoosemelee                                "
               + ChatColor.GREEN + "Ranged class: type /ichooseranged                              "
               + ChatColor.BLUE + "Magic class: type /ichoosemagic                                ");
         }
         else
            textsMap.put("intro", ChatColor.RED + "Welcome to a LittleRPG world. You want to be the very best, like no one ever was.");
         
         textsMap.put("return", ChatColor.RED + "Welcome back, brave adventurer.");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG text file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG text file");
      }
      
      questStartMap = new HashMap<Location, Integer>();
      // Load up the quest starting points from file
      try{
         Location loc;
         br = new BufferedReader(new FileReader(questStartFile));
         
         // Load quest start file data into the world HashMap
         while((input = br.readLine()) != null){
            st = new StringTokenizer(input, " ");
            loc = new Location(getServer().getWorld(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
            
            questStartMap.put(loc, Integer.parseInt(st.nextToken()));
         }
         br.close();
         
      }catch(FileNotFoundException e){
         getLogger().info("No original LittleRPG quest start file found. One will be created for you");
      }catch(IOException e){
         getLogger().info("Error reading LittleRPG quest start file");
      }catch(Exception e){
         getLogger().info("Incorrectly formatted LittleRPG quest start file");
      }

      // Load up the exp limits
      expLevelMap = new HashMap<Integer, Integer>();
      expLevelMap.put(1, 45);
      expLevelMap.put(2, 100);
      expLevelMap.put(3, 160);
      expLevelMap.put(4, 230);
      expLevelMap.put(5, 310);
      expLevelMap.put(6, 400);
      expLevelMap.put(7, 500);
      expLevelMap.put(8, 600);
      expLevelMap.put(9, 710);
      expLevelMap.put(10, 830);
      expLevelMap.put(11, 960);
      expLevelMap.put(12, 1100);
      expLevelMap.put(13, 1260);
      expLevelMap.put(14, 1440);
      expLevelMap.put(15, 1640);
      expLevelMap.put(16, 1860);
      expLevelMap.put(17, 2100);
      expLevelMap.put(18, 2380);
      expLevelMap.put(19, 2720);
      expLevelMap.put(20, 3140);
      
      //Get data from config.yml
      questMap = new HashMap<Integer, RPGQuest>();
      loadQuests();

      //Set up listeners
      getServer().getPluginManager().registerEvents(new EnemyDeaths(this), this);
      getServer().getPluginManager().registerEvents(new EntityDamageEntity(this), this);
      getServer().getPluginManager().registerEvents(new PlayerInteract(this), this);
      getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
      getServer().getPluginManager().registerEvents(new PlayerLevel(this), this);
      getServer().getPluginManager().registerEvents(new PlayerProjectile(this), this);
      getServer().getPluginManager().registerEvents(new PlayerRespawn(this), this);
      getServer().getPluginManager().registerEvents(new PlayerSpeed(this), this);
      if(spoutEnabled)
         getServer().getPluginManager().registerEvents(new ButtonListener(this), this);

      //getCommand("displaysubclass").setExecutor(new Display(this));
      getCommand("ichoosemelee").setExecutor(new Begin(this));
      getCommand("ichooseranged").setExecutor(new Begin(this));
      getCommand("ichoosemagic").setExecutor(new Begin(this));
      getCommand("addrpgworld").setExecutor(new Worlds(this));
      getCommand("removerpgworld").setExecutor(new Worlds(this));
      getCommand("setquest").setExecutor(new Quests(this));
      getCommand("unsetquest").setExecutor(new Quests(this));
      getCommand("displayquests").setExecutor(new Quests(this));
      getCommand("setrpgintro").setExecutor(new Join(this));
      getCommand("setrpgreturn").setExecutor(new Join(this));

      berserkMap = new HashMap<String, String>();
      projMap = new HashMap<Entity, String>();
      
      questNumberToSet = -1;
      questCanSet = false;
      questCanUnset = false;
      
      if(spoutEnabled)
         gui = new LittleGUI(this);

      getLogger().info(this.toString() + " enabled");
   }
   
   public void onDisable(){

      // Save ALL the data!
      BufferedWriter bw;
      try{
         bw = new BufferedWriter(new FileWriter(meleePlayerFile));
         
         // Save all melee players to file
         Iterator<Map.Entry<String, RPGMeleePlayer>> it = meleePlayerMap.entrySet().iterator();
         while(it.hasNext()){
            Entry<String, RPGMeleePlayer> player = it.next();
            bw.write(player.getKey() + " "
                  + player.getValue().getSubClassObject().getName() + " "
                  + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                  + Integer.toString(player.getValue().getLevel()) + " "
                  + Integer.toString(player.getValue().getRage()) + " "
                  + player.getValue().getIncomplete() + " "
                  + player.getValue().getComplete() + " "
                  + Integer.toString(player.getValue().getParty()) + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG melee players");
      }
      
      try{
         bw = new BufferedWriter(new FileWriter(rangedPlayerFile));
         
         // Save all ranged players to file
         Iterator<Map.Entry<String, RPGRangedPlayer>> it = rangedPlayerMap.entrySet().iterator();
         while(it.hasNext()){
            Entry<String, RPGRangedPlayer> player = it.next();
            bw.write(player.getKey() + " "
                  + player.getValue().getSubClassObject().getName() + " "
                  + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                  + Integer.toString(player.getValue().getLevel()) + " "
                  + player.getValue().getIncomplete() + " "
                  + player.getValue().getComplete() + " "
                  + Integer.toString(player.getValue().getParty()) + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG ranged players");
      }
      
      try{
         bw = new BufferedWriter(new FileWriter(magicPlayerFile));
         
         // Save all magic players to file
         Iterator<Map.Entry<String, RPGMagicPlayer>> it = magicPlayerMap.entrySet().iterator();
         while(it.hasNext()){
            Entry<String, RPGMagicPlayer> player = it.next();
            bw.write(player.getKey() + " "
                  + player.getValue().getSubClassObject().getName() + " "
                  + Double.toString(player.getValue().getSubClassObject().getArch()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlade()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getBlock()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getEgg()) + " "
                  + Double.toString(player.getValue().getSubClassObject().getSpell()) + " "
                  + Integer.toString(player.getValue().getLevel()) + " "
                  + player.getValue().getIncomplete() + " "
                  + player.getValue().getComplete() + " "
                  + Integer.toString(player.getValue().getParty()) + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG magic players");
      }
/*
      try{
         bw = new BufferedWriter(new FileWriter(subClassFile));
         
         // Save classes to file
         Iterator<Map.Entry<String, RPGSubClass>> it = subClassMap.entrySet().iterator();
         while(it.hasNext()){
            Entry<String, RPGSubClass> subClassIter = it.next();
            bw.write(subClassIter.getKey() + " "
                  + Double.toString(subClassIter.getValue().getArch()) + " "
                  + Double.toString(subClassIter.getValue().getBlade()) + " "
                  + Double.toString(subClassIter.getValue().getBlock()) + " "
                  + Double.toString(subClassIter.getValue().getEgg()) + " "
                  + Double.toString(subClassIter.getValue().getSpell()) + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG sub-classes");
      }*/
      
      try{
         bw = new BufferedWriter(new FileWriter(worldsFile));
         Iterator<Map.Entry<String, String>> it = worldsMap.entrySet().iterator();
         
         // Save all world names to file
         while(it.hasNext()){
            Entry<String, String> world = it.next();
            bw.write(world.getValue() + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG worlds");
      }
      
      try{
         bw = new BufferedWriter(new FileWriter(textsFile));
         Iterator<Map.Entry<String, String>> it = textsMap.entrySet().iterator();
         
         // Save all world names to file
         while(it.hasNext()){
            Entry<String, String> text = it.next();
            bw.write(text.getKey() + " " + text.getValue() + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG texts file");
      }
      
      try{
         bw = new BufferedWriter(new FileWriter(questStartFile));
         Iterator<Map.Entry<Location, Integer>> it = questStartMap.entrySet().iterator();
         
         // Save all quest starting points to file
         while(it.hasNext()){
            Entry<Location, Integer> quest = it.next();
            bw.write(quest.getKey().getWorld().getName() + " "
                  + Integer.toString(quest.getKey().getBlockX()) + " "
                  + Integer.toString(quest.getKey().getBlockY()) + " "
                  + Integer.toString(quest.getKey().getBlockZ()) + " "
                  + Integer.toString(quest.getValue()) + "\n");
         }
         bw.close();
      }catch(IOException e){
         getLogger().info("Error saving LittleRPG quest starting points");
      }
      
      getLogger().info(this.toString() + " disabled");
   }

   public HashMap<String, RPGMeleePlayer> getMeleePlayerMap() {
      return meleePlayerMap;
   }

   public HashMap<String, RPGRangedPlayer> getRangedPlayerMap() {
      return rangedPlayerMap;
   }

   public HashMap<String, RPGMagicPlayer> getMagicPlayerMap() {
      return magicPlayerMap;
   }
   
   /*public HashMap<String, RPGSubClass> getSubClassMap() {
      return subClassMap;
   }*/
   
   public HashMap<Integer, RPGQuest> getQuestMap() {
      return questMap;
   }
   
   public HashMap<String, String> getBerserkMap() {
      return berserkMap;
   }
   
   public HashMap<Entity, String> getProjMap() {
      return projMap;
   }

   public HashMap<String, String> getWorldsMap(){
      return worldsMap;
   }

   public HashMap<Location, Integer> getQuestStartMap(){
      return questStartMap;
   }

   public int getQuestNumberToSet(){
      return questNumberToSet;
   }

   public void setQuestNumberToSet(int questNumberToSet){
      this.questNumberToSet = questNumberToSet;
   }

   public boolean isQuestCanSet(){
      return questCanSet;
   }

   public void setQuestCanSet(boolean questCanSet){
      this.questCanSet = questCanSet;
   }

   public boolean isQuestCanUnset(){
      return questCanUnset;
   }

   public void setQuestCanUnset(boolean questCanUnset){
      this.questCanUnset = questCanUnset;
   }
   
   public boolean isSpoutEnabled(){
      return spoutEnabled;
   }

   public HashMap<String, String> getTextsMap(){
      return textsMap;
   }

   public LittleGUI getGUI(){
      return gui;
   }

   /* Returns true if the RNG smiles upon the user*/
   public boolean probabilityRoll(int percent){
      Random rand = new Random();
      
      if(rand.nextInt(100) <= percent)
         return true;
      else
         return false;
   }
   
   /* Checks if the given entity is an enemy*/
   public boolean isEnemy(Entity ent){
      if(ent instanceof Animals)
         return true;
      else if(ent instanceof Squid)
         return true;
      else if(ent instanceof Monster)
         return true;
      else if(ent instanceof EnderDragon)
         return true;
      return false;
   }

   /* Sets a task to turn off a melee ability to attack*/
   public void giveCooldown(final RPGMeleePlayer playa, int delay){
      getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
         public void run() {
            meleePlayerMap.get(playa.getName()).setAttackReadiness(true);
         }
     }, (delay * 20) - playa.getLevel());
   }

   /* Sets a task to turn off a magical ability's cooldown*/
   public void giveCooldown(final Player playa, final String type, String classType, double delay){
      if(classType.compareTo("magic") == 0){
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
               RPGMagicPlayer rpgPlaya = magicPlayerMap.get(playa.getName());
               ItemStack is = new ItemStack(351,1);
               
               if(type.compareTo("heal") == 0){
                  rpgPlaya.setHealReadiness(true);
                  is.setDurability((short)15);
                  playa.getInventory().setItem(1, is);
               }
               else if(type.compareTo("advHeal") == 0){
                  rpgPlaya.setAdvHealReadiness(true);
                  is.setType(Material.BONE);
                  playa.getInventory().setItem(5, is);
               }
               else if(type.compareTo("light") == 0){
                  rpgPlaya.setLightningReadiness(true);
                  is.setDurability((short)11);
                  playa.getInventory().setItem(0, is);
               }
               else if(type.compareTo("advLight") == 0){
                  rpgPlaya.setAdvLightningReadiness(true);
                  is.setType(Material.BLAZE_ROD);
                  playa.getInventory().setItem(6, is);
               }
               else if(type.compareTo("fire") == 0){
                  rpgPlaya.setFireballReadiness(true);
                  is.setDurability((short)1);
                  playa.getInventory().setItem(2, is);
               }
               else if(type.compareTo("tele") == 0){
                  rpgPlaya.setTeleportReadiness(true);
                  is.setDurability((short)13);
                  playa.getInventory().setItem(3, is);
               }
               else if(type.compareTo("baaa") == 0){
                  rpgPlaya.setSheepReadiness(true);
                  is.setType(Material.WHEAT);
                  playa.getInventory().setItem(4, is);
               }
            }
        }, (long) (delay * 20));
      }
      /* Sets a task to turn off a ranged ability's cooldown*/
      else if(classType.compareTo("ranged") == 0){
         getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
            public void run() {
               RPGRangedPlayer rpgPlaya = rangedPlayerMap.get(playa.getName());
               
               if(type.compareTo("egg") == 0){
                  rpgPlaya.setEggReadiness(true);
                  playa.getInventory().setItem(2, new ItemStack(Material.EGG,1));
               }
               else if(type.compareTo("fire") == 0){
                  rpgPlaya.setFireBowReadiness(true);
                  playa.getInventory().setItem(1, new ItemStack(Material.BOW,1));
               }
            }
        }, (long) (delay * 20)); // Multiplied by 20 to turn the delay time into seconds
      }
   }

   /* Example quests from config.yml
    * No prereq quest, must be at least level 5 and player must have at
    * least 5 dirt blocks. Finishing condition is 25 dirt blocks with a
    * reward of a sand block and 20xp.
    * Quest 2 has a finishing condition of 1 coal and 1 stick, reward is 30xp.
    * quest0:
    * - Level:5
    * - Items:3|5
    * - Text:Greetings traveller, I need more dirt to make my first home.
    * - Text:Could you please fetch me 25 dirt blocks?
    * - FC:3|25
    * - Pass:Thank you, I can now make my lovely 3x4x3 dirt home.
    * - Fail:You do not have enough dirt.
    * - Reward:12|1:XP|20
    * quest1:
    * - PQ:0
    * - Text:I now must decorate it, fetch me 1 piece of coal and 1 stick.
    * - FC:263|1:280|1
    * - Pass:Decorations! Yay!
    * - Fail:That is not right.
    * - Reward:XP|30*/
   
   // Loads all the quests from config.yml
   public void loadQuests(){
      int i, quest, level, preReqQuest;
      String type, item, finishers, reward;
      ArrayList<String> text;
      List<String> questData;
      StringTokenizer st;
      
      // Get data and create each quest
      for(quest = 0; getConfig().isList("quest" + quest); quest++){
         questData = getConfig().getStringList("quest" + quest);
         text = new ArrayList<String>();
         level = -1;
         preReqQuest = -1;
         item = "";
         finishers = "";
         reward = "";
         
         for(i = 0; i < questData.size(); i++){
            st = new StringTokenizer(questData.get(i), ":");
            type = st.nextToken();
            
            if(type.compareToIgnoreCase("pq") == 0){
               preReqQuest = Integer.parseInt(st.nextToken());
            }
            else if(type.compareToIgnoreCase("level") == 0){
               level = Integer.parseInt(st.nextToken());
            }
            else if(type.compareToIgnoreCase("items") == 0){
               item = st.nextToken();
               while(st.hasMoreTokens()){
                  item += "|" + st.nextToken();
               }
            }
            else if(type.compareToIgnoreCase("text") == 0
                  || type.compareToIgnoreCase("pass") == 0
                  || type.compareToIgnoreCase("fail") == 0){
               text.add(type.toLowerCase() + "|" + st.nextToken());
            }
            else if(type.compareToIgnoreCase("fc") == 0){
               finishers = st.nextToken();
               while(st.hasMoreTokens()){
                  finishers += "|" + st.nextToken();
               }
            }
            else if(type.compareToIgnoreCase("reward") == 0){
               reward = st.nextToken();
               while(st.hasMoreTokens()){
                  reward += "|" + st.nextToken();
               }
            }
         }
         
         // Create the new quest and store it in the quest hashmap
         questMap.put(quest, new RPGQuest(quest, preReqQuest, level, item, text, finishers, reward));
      }
   }

   /* Gets the percentage of the exp gained */
   public void giveExp(Player playa, int exp){
      float amount;
      
      amount = (float) exp / expLevelMap.get(playa.getLevel());
      if(playa.getExp() + amount > 1){
         amount = (amount + playa.getExp()) - 1;
         playa.setLevel(playa.getLevel() + 1);
         playa.setExp(amount);
      }
      else
         playa.setExp(playa.getExp() + amount);
   }
   
   /* Sets how effective damage is against certain creatures */
   public void ohTheDamage(Event event, Entity entity, int dmg){
      if(event instanceof EntityDamageByEntityEvent){
         if(entity instanceof Animals || entity instanceof Squid)
            ((EntityDamageByEntityEvent) event).setDamage(dmg * 2);
         else
            ((EntityDamageByEntityEvent) event).setDamage(dmg);
      }
      /* If null then it is from the PlayerInteract event */
      else if(event == null){
         if(entity instanceof Animals || entity instanceof Squid)
            ((LivingEntity)entity).damage(dmg * 2);
         else
            ((LivingEntity)entity).damage(dmg);
      }
   }
}
