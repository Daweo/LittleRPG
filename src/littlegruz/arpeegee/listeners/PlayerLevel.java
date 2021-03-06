package littlegruz.arpeegee.listeners;

import littlegruz.arpeegee.ArpeegeeMain;
import littlegruz.arpeegee.entities.RPGMagicPlayer;
import littlegruz.arpeegee.entities.RPGMeleePlayer;
import littlegruz.arpeegee.entities.RPGPlayer;
import littlegruz.arpeegee.entities.RPGRangedPlayer;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerLevel implements Listener{
   private ArpeegeeMain plugin;
   
   public PlayerLevel(ArpeegeeMain instance){
      plugin = instance;
   }

   // Give new weapons if the necessary level is reached
   @EventHandler
   public void onPlayerLevel(PlayerLevelChangeEvent event){
      if(plugin.getWorldsMap().containsKey(event.getPlayer().getWorld().getName())){
         if(event.getNewLevel() > event.getOldLevel())
            event.getPlayer().sendMessage("Level up!");
         
         // New weapon for the melee class
         if(plugin.getMeleePlayerMap().get(event.getPlayer().getName()) != null){
            RPGMeleePlayer rpgPlaya = plugin.getMeleePlayerMap().get(event.getPlayer().getName());
   
            // Increase player stats
            levelUp(rpgPlaya, event);
            rpgPlaya.getSubClassObject().setBlade((rpgPlaya.getLevel() - 1) * 0.2 + 1.0);
            rpgPlaya.getSubClassObject().setBlock((rpgPlaya.getLevel() - 1) * 0.35 + 1.0);
            
            if(rpgPlaya.getLevel() >= 4)
               event.getPlayer().getInventory().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            if(rpgPlaya.getLevel() >= 8)
               event.getPlayer().getInventory().setItem(1, new ItemStack(Material.DIAMOND_SWORD,1));
            if(rpgPlaya.getLevel() >= 10)
               event.getPlayer().getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
            if(rpgPlaya.getLevel() >= 14)
               event.getPlayer().getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            if(rpgPlaya.getLevel() >= 17)
               event.getPlayer().getInventory().setHelmet(new ItemStack(Material.IRON_HELMET));
         }
         // New weapon for the ranged class
         else if(plugin.getRangedPlayerMap().get(event.getPlayer().getName()) != null){
            RPGRangedPlayer rpgPlaya = plugin.getRangedPlayerMap().get(event.getPlayer().getName());
            
            // Increase player stats
            levelUp(rpgPlaya, event);
            rpgPlaya.getSubClassObject().setArch((rpgPlaya.getLevel() - 1) * 0.3 + 1.0);
            rpgPlaya.getSubClassObject().setEgg((rpgPlaya.getLevel() - 1) * 0.4 + 1.0);
   
            if(rpgPlaya.getLevel() >= 3)
               event.getPlayer().getInventory().setChestplate(new ItemStack(Material.LEATHER_CHESTPLATE));
            if(rpgPlaya.getLevel() >= 6)
               event.getPlayer().getInventory().setBoots(new ItemStack(Material.LEATHER_BOOTS));
            if(rpgPlaya.getLevel() >= 7)
               event.getPlayer().getInventory().setItem(1, new ItemStack(Material.BOW,1));
            if(rpgPlaya.getLevel() >= 9)
               event.getPlayer().getInventory().setLeggings(new ItemStack(Material.LEATHER_LEGGINGS));
            if(rpgPlaya.getLevel() >= 10)
               event.getPlayer().getInventory().setItem(2, new ItemStack(Material.EGG,1));
            if(rpgPlaya.getLevel() >= 13)
               event.getPlayer().getInventory().setHelmet(new ItemStack(Material.LEATHER_HELMET));
         }
         // New weapons for the magic class
         else if(plugin.getMagicPlayerMap().get(event.getPlayer().getName()) != null){
            RPGMagicPlayer rpgPlaya = plugin.getMagicPlayerMap().get(event.getPlayer().getName());
            
            // Increase player stats
            levelUp(rpgPlaya, event);
            rpgPlaya.getSubClassObject().setSpell((rpgPlaya.getLevel() - 1) * 0.3 + 1.0);
            
            // Create the base dye type first
            ItemStack is = new ItemStack(351,1);
            // Heal
            if(rpgPlaya.getLevel() >= 3){
               is.setDurability((short)15);
               event.getPlayer().getInventory().setItem(1, is);
            }
            // Robe
            if(rpgPlaya.getLevel() >= 5)
               event.getPlayer().getInventory().setChestplate(new ItemStack(Material.GOLD_CHESTPLATE));
            // Fireball
            if(rpgPlaya.getLevel() >= 8){
               is.setDurability((short)1);
               event.getPlayer().getInventory().setItem(2, is);
            }
            // Teleport
            if(rpgPlaya.getLevel() >= 10){
               is.setDurability((short)13);
               event.getPlayer().getInventory().setItem(3, is);
            }
            // Wizard hat
            if(rpgPlaya.getLevel() >= 11)
               event.getPlayer().getInventory().setHelmet(new ItemStack(Material.GOLD_HELMET));
            // Sheep summon
            if(rpgPlaya.getLevel() >= 13){
               is.setType(Material.WHEAT);
               event.getPlayer().getInventory().setItem(4, is);
            }
            // Advanced heal
            if(rpgPlaya.getLevel() >= 15){
               is.setType(Material.BONE);
               event.getPlayer().getInventory().setItem(5, is);
            }
            // Advanced lightning
            if(rpgPlaya.getLevel() >= 18){
               is.setType(Material.BLAZE_ROD);
               event.getPlayer().getInventory().setItem(6, is);
            }
         }
      }
   }
   
   private void levelUp(RPGPlayer rpgPlaya, PlayerLevelChangeEvent event){

      if(event.getNewLevel() > plugin.MAX_LEVEL){
         rpgPlaya.setLevel(plugin.MAX_LEVEL);
         event.getPlayer().setLevel(plugin.MAX_LEVEL);
      }
      else
         rpgPlaya.setLevel(event.getNewLevel());
   }
}
