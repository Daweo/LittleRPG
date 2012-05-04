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
      // New weapon for the melee class
      if(plugin.getMeleePlayerMap().get(event.getPlayer().getName()) != null){
         RPGMeleePlayer rpgPlaya = plugin.getMeleePlayerMap().get(event.getPlayer().getName());
         levelUp(rpgPlaya, event);
         
         if(rpgPlaya.getLevel() >= 5)
            event.getPlayer().getInventory().setItem(1, new ItemStack(Material.DIAMOND_SWORD,1));
      }
      // New weapon for the ranged class
      else if(plugin.getRangedPlayerMap().get(event.getPlayer().getName()) != null){
         RPGRangedPlayer rpgPlaya = plugin.getRangedPlayerMap().get(event.getPlayer().getName());
         levelUp(rpgPlaya, event);
         
         if(rpgPlaya.getLevel() >= 7)
            event.getPlayer().getInventory().setItem(1, new ItemStack(Material.EGG,1));
      }
      // New weapons for the magic class
      else if(plugin.getMagicPlayerMap().get(event.getPlayer().getName()) != null){
         RPGMagicPlayer rpgPlaya = plugin.getMagicPlayerMap().get(event.getPlayer().getName());
         levelUp(rpgPlaya, event);
         
         // Create the base dye type first
         ItemStack is = new ItemStack(351,1);
         // Heal
         if(rpgPlaya.getLevel() >= 3){
            is.setDurability((short)15);
            event.getPlayer().getInventory().setItem(1, is);
         }
         // Fireball
         if(rpgPlaya.getLevel() >= 5){
            is.setDurability((short)1);
            event.getPlayer().getInventory().setItem(2, is);
         }
         // Teleport
         if(rpgPlaya.getLevel() >= 8){
            is.setDurability((short)13);
            event.getPlayer().getInventory().setItem(3, is);
         }
         // Sheep summon
         if(rpgPlaya.getLevel() >= 10){
            is.setType(Material.WHEAT);
            event.getPlayer().getInventory().setItem(4, is);
         }
         // Advanced heal
         if(rpgPlaya.getLevel() >= 11){
            is.setType(Material.BONE);
            event.getPlayer().getInventory().setItem(5, is);
         }
         // Advanced lightning
         if(rpgPlaya.getLevel() >= 13){
            is.setType(Material.BLAZE_ROD);
            event.getPlayer().getInventory().setItem(6, is);
         }
      }
   }
   
   private void levelUp(RPGPlayer rpgPlaya, PlayerLevelChangeEvent event){

      if(event.getNewLevel() > 15){
         rpgPlaya.setLevel(15);
         event.getPlayer().setLevel(15);
      }
      else
         rpgPlaya.setLevel(event.getNewLevel());
   }
}