/*
 * Copyright 2013 Michael McKnight. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and contributors and should not be interpreted as representing official policies,
 * either expressed or implied, of anybody else.
 */

package com.forgenz.mobmanager.abilities.listeners;

import java.util.List;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.abilities.Ability;
import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
import com.forgenz.mobmanager.abilities.abilities.AngryAbility;
import com.forgenz.mobmanager.abilities.abilities.BabyAbility;
import com.forgenz.mobmanager.abilities.abilities.ChargedCreeperAbility;
import com.forgenz.mobmanager.abilities.abilities.DamageAbility;
import com.forgenz.mobmanager.abilities.abilities.DeathSpawnAbility;
import com.forgenz.mobmanager.abilities.abilities.DropsAbility;
import com.forgenz.mobmanager.abilities.config.AbilityConfig;
import com.forgenz.mobmanager.abilities.config.MobAbilityConfig;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.limiter.config.LimiterConfig;

public class AbilitiesMobListener implements Listener
{
	/**
	 * Handles random chance rates
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void rates(CreatureSpawnEvent event)
	{
		if (P.p().shouldIgnoreNextSpawn() || P.p().shouldAbilitiesIgnoreNextSpawn())
			return;
		
		if (!P.p().getPluginIntegration().canApplyAbilities(event.getEntity()))
			return;
		
		MobAbilityConfig ma = AbilityConfig.i().getMobConfig(event.getLocation().getWorld().getName(), ExtendedEntityType.get(event.getEntity()), event.getSpawnReason());
		
		if (ma == null)
			return;
		
		if (ma.spawnRate < 1.0)
		{
			if (ma.spawnRate == 0.0)
			{
				event.setCancelled(true);
				return;
			}
			// If the random number is higher than the spawn chance we disallow the spawn
			if (LimiterConfig.rand.nextFloat() >= ma.spawnRate)
			{
				event.setCancelled(true);
				return;
			}
		}
		
		BabyAbility.addByChance(event.getEntity(), ma);
		
		AngryAbility.addByChance(event.getEntity(), ma);
		ChargedCreeperAbility.addByChance(event.getEntity(), ma);
	}
	
	/**
	 * Adds abilities to the entity
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event)
	{
		if (P.p().shouldIgnoreNextSpawn() || P.p().shouldAbilitiesIgnoreNextSpawn())
			return;
		
		addAbilities(event.getEntity(), event.getSpawnReason());
	}
	
	/**
	 * Adds a set of random abilities to the entity (If the given spawnReason permits)
	 * @param entity The entity to have abilities added to
	 * @param spawnReason The reason why the entity spawned (null to ignore)
	 */
	public static void addAbilities(LivingEntity entity, SpawnReason spawnReason)
	{
		// Check if any plugins are protecting this entity
		if (!P.p().getPluginIntegration().canApplyAbilities(entity))
			return;
		
		// Fetch the mob config for this entity
		MobAbilityConfig ma = AbilityConfig.i().getMobConfig(entity.getWorld().getName(), ExtendedEntityType.get(entity), spawnReason);
		
		// If there is not config for the entity there is nothing more to do
		if (ma == null)
			return;
		
		// Fetch Ability Sets
		ValueChance<Ability> abilityChance = ma.attributes.get(AbilityType.ABILITY_SET);
		if (abilityChance != null)
		{
			// Fetch an ability
			AbilitySet ability = (AbilitySet) abilityChance.getBonus();
			// If there is no ability or the 'none' ability set is given
			// we allow other abilities to be applied
			if (ability != null && ability.getNumAbilities() > 0)
			{
				// Add the ability and return to prevent other abilities being applied
				ability.addAbility(entity);
				return;
			}
		}
		
		// Cycle through each type of ability and apply them 
		// Note: Make sure we do not apply abilitySets (hence i < types.length - 1)
		AbilityType[] types = AbilityType.values();
		for (int i = 0; i < types.length - 1; ++i)
		{
			if (!types[i].isValueChanceAbility())
				continue;
			
			abilityChance = ma.attributes.get(types[i]);
			
			if (abilityChance == null)
				continue;
			
			Ability ability = abilityChance.getBonus();
			
			if (ability == null)
				continue;
			
			ability.addAbility(entity);
		}
	}
	
	/**
	 * Handles the DeathSpawn/Drops Abilities
	 */
	@EventHandler
	public void onEntityDeath(EntityDeathEvent event)
	{
		// Handle DeathSpawn abilities
		DeathSpawnAbility deathSpawnAbility = DeathSpawnAbility.getDeathSpawnAbility(event.getEntity());
		
		if (deathSpawnAbility != null)
		{
			deathSpawnAbility.addAbility(event.getEntity());
		}
		
		// Handle DropsAbility
		DropsAbility dropsAbility = DropsAbility.getAbility(event.getEntity());
		
		if (dropsAbility != null)
		{
			if (dropsAbility.replaceDrops())
			{
				event.getDrops().clear();
			}
			
			List<ItemStack> items = dropsAbility.getItemList();
			
			for (ItemStack item : items)
			{
				event.getDrops().add(item);
			}
		}
	}
	
	/**
	 * Multiplies damage for entities
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void setDamageMulti(EntityDamageByEntityEvent event)
	{
		// We are only interested in these three damage types
		if (event.getCause() != DamageCause.ENTITY_ATTACK && event.getCause() != DamageCause.PROJECTILE && event.getCause() != DamageCause.ENTITY_EXPLOSION)
			return;
		
		// Get the entity which dealt the damage
		LivingEntity damager = null;
		
		if (event.getDamager() instanceof Projectile)
		{
			Projectile entity = (Projectile) event.getDamager();
			
			damager = entity.getShooter();
		}
		else if (event.getDamager() instanceof LivingEntity)
		{			
			damager = (LivingEntity) event.getDamager();
		}
		
		if (damager == null)
			return;
		
		// Fetch the multiplier for damage caused by the mob
		float multi = DamageAbility.getMetaValue(damager);
		
		// If the multiplier is 1.0F we don't do anything
		if (multi != 1.0F)
		{
			if (!P.p().getPluginIntegration().canApplyAbilities(damager))
				return;
			
			// Calculate the new damage
			int newDamage = (int) (event.getDamage() * multi);
			
			// Set the new damage
			event.setDamage(newDamage);
		}
	}
}
