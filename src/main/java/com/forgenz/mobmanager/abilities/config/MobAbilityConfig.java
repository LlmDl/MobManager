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

package com.forgenz.mobmanager.abilities.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import com.forgenz.mobmanager.abilities.AbilityType;
import com.forgenz.mobmanager.abilities.abilities.Ability;
import com.forgenz.mobmanager.abilities.abilities.AngryAbility;
import com.forgenz.mobmanager.abilities.abilities.BabyAbility;
import com.forgenz.mobmanager.abilities.abilities.ChargedCreeperAbility;
import com.forgenz.mobmanager.abilities.abilities.VillagerAbility;
import com.forgenz.mobmanager.abilities.util.ValueChance;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.common.util.MiscUtil;

public class MobAbilityConfig extends AbstractConfig
{
	public final float equipmentDropChance;
	
	public final float spawnRate;
	public final float babyRate;
	public final double villagerRate;
	public final float angryRate;
	public final float chargedRate;
	
	public final HashMap<AbilityType, ValueChance<Ability>> attributes;
	
	private MobAbilityConfig(ExtendedEntityType mob, String name, ConfigurationSection cfg, boolean abilitySet)
	{
		if (mob == null)
			mob = ExtendedEntityType.UNKNOWN;
		
		attributes = new HashMap<AbilityType, ValueChance<Ability>>();
		
		if (!abilitySet)
		{
			/* ######## SpawnRate ######## */
			float spawnRate = (float) cfg.getDouble("SpawnRate", 1.0F);
			if (spawnRate < 0.0F)
				spawnRate = 1.0F;
			this.spawnRate = spawnRate;
			set(cfg, "SpawnRate", spawnRate);
			
			/* ######## EquipmentDropChance ######## */
			float equipmentDropChance = (float) cfg.getDouble("EquipmentDropChance", 0.15F);
			this.equipmentDropChance = equipmentDropChance < 0 ? 0.0F : equipmentDropChance;
			set(cfg, "EquipmentDropChance", this.equipmentDropChance);
		}
		else
		{
			spawnRate = 1.0F;
			equipmentDropChance = 1.0F;
		}
		
		/* ######## BabyRate ######## */
		if (mob == ExtendedEntityType.UNKNOWN || BabyAbility.isValid(mob))
		{
			float babyRate = (float) cfg.getDouble(AbilityType.BABY.getConfigPath(), 0.0F);
			if (babyRate <= 0.0F)
				babyRate = 0.0F;
			else if (babyRate > 1.0F)
				babyRate = 1.0F;
			this.babyRate = babyRate;
			set(cfg, AbilityType.BABY.getConfigPath(), babyRate);
		}
		else
		{
			babyRate = 0.0F;
		}
		
		/* ######## VillagerRate ######## */
		if (mob == ExtendedEntityType.UNKNOWN || VillagerAbility.isValid(mob.getBukkitEntityType()))
		{
			float villagerRate = (float) cfg.getDouble(AbilityType.VILLAGER.getConfigPath(), 0.0F);
			if (villagerRate <= 0.0F)
				villagerRate = 0.0F;
			this.villagerRate = villagerRate;
			set(cfg, AbilityType.VILLAGER.getConfigPath(), villagerRate);
		}
		else
		{
			villagerRate = 0.0F;
		}
		
		/* ######## AngryRate ######## */
		if (mob == ExtendedEntityType.UNKNOWN || AngryAbility.isValid(mob.getBukkitEntityType()))
		{
			float angryRate = (float) cfg.getDouble(AbilityType.ANGRY.getConfigPath(), 0.0F);
			if (angryRate <= 0.0F)
				angryRate = 0.0F;
			this.angryRate = angryRate;
			set(cfg, AbilityType.ANGRY.getConfigPath(), angryRate);
		}
		else
		{
			angryRate = 0.0F;
		}
		
		/* ######## ChargedRate ######## */
		if (mob == ExtendedEntityType.UNKNOWN || ChargedCreeperAbility.isValid(mob.getBukkitEntityType()))
		{
			float chargedRate = (float) cfg.getDouble(AbilityType.CHARGED.getConfigPath(), 0.0F);
			if (chargedRate <= 0.0F)
				chargedRate = 0.0F;
			this.chargedRate = chargedRate;
			set(cfg, AbilityType.CHARGED.getConfigPath(), chargedRate);
		}
		else
		{
			chargedRate = 0.0F;
		}
		
		/* ######## ValueChance Abilities ######## */
		for (AbilityType ability : AbilityType.values())
		{
			// Ignore abilities which don't work as ValueChance + Stand alone
			if (!ability.isValueChanceAbility())
				continue;
			
			if (abilitySet && ability == AbilityType.ABILITY_SET)
				continue;
			
			ValueChance<Ability> abilityChances = new ValueChance<Ability>();
			// Fetch String list from config
			List<Object> optList = MiscUtil.getList(cfg.getList(ability.getConfigPath()));
			if (optList == null)
				optList = new ArrayList<Object>();
			// Store the ability chances if there is at least one
			if (ability.setup(mob, abilityChances, optList))
				attributes.put(ability, abilityChances);
			
			// Update String list (ability.setup can removes invalid settings)
			set(cfg, ability.getConfigPath(), optList);
		}	
	}
	
	/**
	 * Used for the creation of AbilitySets
	 * @param name The name of the ability set
	 * @param mob The mobType the ability set is aimed at (If exists)
	 * @param cfg The configuration section providing settings for abilities
	 */
	public MobAbilityConfig(String name, ExtendedEntityType mob, ConfigurationSection cfg)
	{
		this(mob, name, cfg, true);
	}
	
	/**
	 * Used for the creation of Mob Abilities
	 * @param mob The mob the abilities will be applied to
	 * @param cfg The configuration section providing settings for abilities
	 */
	public MobAbilityConfig(ExtendedEntityType mob, ConfigurationSection cfg)
	{
		this(mob, mob != null ? mob.toString() : "Unknown", cfg, false);
	}

	public void applyRates(LivingEntity entity)
	{
		BabyAbility.addByChance(entity, this);
		VillagerAbility.addByChance(entity, this);
		AngryAbility.addByChance(entity, this);
		ChargedCreeperAbility.addByChance(entity, this);
	}
}
