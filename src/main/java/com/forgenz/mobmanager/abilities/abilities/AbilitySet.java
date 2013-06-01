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

package com.forgenz.mobmanager.abilities.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.MMComponent;
import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.config.MobAbilityConfig;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.integration.MobManagerProtector;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;

public class AbilitySet extends Ability
{
	/** Stores all the ability sets */
	private final static HashMap<String, AbilitySet> abilitySets = new HashMap<String, AbilitySet>();
	public static void resetAbilitySets()
	{
		abilitySets.clear();
		// Add default none ability
		abilitySets.put("none", new AbilitySet(null, null, false, true));
	}
	
	protected final ExtendedEntityType type;
	private final MobAbilityConfig setCfg;
	private final boolean protectFromDespawner;
	private final boolean applyNormalAbilities;
	
	public static AbilitySet getAbilitySet(String name)
	{
		return abilitySets.get(name.toLowerCase());
	}
	
	public static String[] getAbilitySetNames()
	{
		return abilitySets.keySet().toArray(new String[abilitySets.size()]);
	}
	
	public static int getNumAbilitySets()
	{
		return abilitySets.size();
	}
	
	private AbilitySet(MobAbilityConfig setCfg, ExtendedEntityType type, boolean protectFromDespawner, boolean allowNormalAbilities)
	{
		this.setCfg = setCfg;
		this.type = type;
		this.protectFromDespawner = protectFromDespawner;
		this.applyNormalAbilities = allowNormalAbilities;
	}

	@Override
	public void addAbility(LivingEntity entity)
	{		
		if (setCfg != null)
		{
			// Make sure we prevent the mob from being despawned
			if (protectFromDespawner)
			{
				MobManagerProtector.getInstance().addProtectedEntity(entity);
			}
			
			// Add each ability to the entity
			for (ValueChance<Ability> abilityChance : setCfg.attributes.values())
			{
				Ability ability = abilityChance.getBonus();
				
				if (ability != null)
					ability.addAbility(entity);
			}
		}
	}

	@Override
	public AbilityType getAbilityType()
	{
		return AbilityType.ABILITY_SET;
	}
	
	public int getNumAbilities()
	{
		return setCfg == null ? 0 : setCfg.attributes.size();
	}
	
	public boolean applyNormalAbilities()
	{
		return applyNormalAbilities;
	}
	
	public ExtendedEntityType getAbilitySetsEntityType()
	{
		return type;
	}
	
	public static void createAbilitySet(ConfigurationSection cfg)
	{
		// If there are no options return
		if (cfg == null)
			return;
		
		// Fetch the Sets name from the map
		String name = cfg.getName();
		
		// If no name is given return
		if (name == null)
		{
			MMComponent.getAbilities().warning("Must provide a name for every AbilitySet");
			return;
		}
		
		// If the name already exists return
		if (abilitySets.containsKey(name.toLowerCase()))
		{
			MMComponent.getAbilities().warning("AbilitySet with name " + name + " already exists");
			return;
		}
		
		// Fetch the default mob type if it exists
		ExtendedEntityType entityType = null;
		if (cfg.contains("MobType"))
		{
			String key = cfg.getString("MobType");
			if (key != null)
				entityType = ExtendedEntityType.valueOf(key);
			
			if (entityType == null)
			{
				MMComponent.getAbilities().warning("Invalid EntityType " + key + " in AbilitySets");
			}
		}
		
		boolean protectFromDespawner = cfg.getBoolean("ProtectFromDespawner", false);
		boolean applyNormalAbilities = cfg.getBoolean("ApplyNormalAbilities", false);
		
		ConfigurationSection abilities = cfg.getConfigurationSection("Abilities");
		if (abilities == null)
			abilities = cfg.createSection("Abilities");
		
		MobAbilityConfig setCfg = new MobAbilityConfig(name, entityType, abilities);
		
		// Create the ability set
		abilitySets.put(name.toLowerCase(), new AbilitySet(setCfg, entityType, protectFromDespawner, applyNormalAbilities));
	} 

	public static void setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
	{
		Iterator<Object> it = optList.iterator();
		
		while (it.hasNext())
		{
			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
			
			if (optMap == null)
				continue;
			
			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
			
			if (chance <= 0)
				continue;
			
			String setName = MiscUtil.getMapValue(optMap, "SETNAME", mob.toString(), String.class);
			
			if (setName == null)
				continue;
			
			AbilitySet set = getAbilitySet(setName);
			
			if (set == null)
			{
				MMComponent.getAbilities().warning("Missing SetName " + setName + " for " + mob);
				continue;
			}
			
			abilityChances.addChance(chance, set);
		}
	}

}
