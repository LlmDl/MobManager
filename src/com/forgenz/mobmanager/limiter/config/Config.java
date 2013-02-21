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

package com.forgenz.mobmanager.limiter.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.forgenz.mobmanager.P;
import com.forgenz.mobmanager.common.config.AbstractConfig;
import com.forgenz.mobmanager.common.config.EnumSettingContainer;
import com.forgenz.mobmanager.common.config.TSettingContainer;
import com.forgenz.mobmanager.common.util.ExtendedEntityType;
import com.forgenz.mobmanager.limiter.world.MMWorld;

public class Config extends AbstractConfig
{
	public final static Random rand = new Random();
	
	public final static Pattern layerPattern = Pattern.compile("^\\d+/{1}\\d+$");
	public final static Pattern layerSplitPattern = Pattern.compile("/{1}");
	
	public static boolean disableWarnings;
	public static boolean ignoreCreativePlayers;
	public static boolean useAsyncDespawnScanner;
	
	public static boolean removeTamedAnimals;
	public static boolean countTamedAnimals;
	
	public static boolean enableAnimalDespawning;
	public static double daysTillFarmAnimalCleanup;
	public static int protectedFarmAnimalSaveInterval;
	
	public static short spawnChunkSearchDistance;
	public static short flyingMobAditionalLayerDepth;
	public static int ticksPerRecount;
	public static int ticksPerDespawnScan;
	public static int minTicksLivedForDespawn;
	
	public static TSettingContainer<ExtendedEntityType> ignoredMobs;
	public static TSettingContainer<ExtendedEntityType> disabledMobs;
	
	public static EnumSettingContainer enabledSpawnReasons;
	
	public static Set<String> enabledWorlds = new HashSet<String>();
	public static List<String> layers;
	public static HashSet<Integer> layerBoundaries;
	
	public static HashMap<String, WorldConfig> worldConfigs;
	
	public Config()
	{
		FileConfiguration cfg = getConfig("", LIMITER_CONFIG_NAME);
		
		/* ################ ConfigVersion ################ */
		//String configVersion = cfg.getString("ConfigVersion", null);
		//set(cfg, "ConfigVersion", P.p.getDescription().getVersion());
		/* ################ ActiveWorlds ################ */
		List<String> activeWorlds = cfg.getStringList("EnabledWorlds");
		
		if (activeWorlds == null || activeWorlds.size() == 0)
		{
			activeWorlds = new ArrayList<String>();
			for (World world : P.p.getServer().getWorlds())
			{
				activeWorlds.add(world.getName());
			}
		}
		for (String world : activeWorlds)
			enabledWorlds.add(world);
		set(cfg, "EnabledWorlds", activeWorlds);
		
		/* ################ DisableWarnings ################ */
		disableWarnings = cfg.getBoolean("DisableWarnings", true);
		set(cfg, "DisableWarnings", disableWarnings);
		
		/* ################ UseAsyncDespawnScanner ################ */
		useAsyncDespawnScanner = cfg.getBoolean("UseAsyncDespawnScanner", false);
		set(cfg, "UseAsyncDespawnScanner", useAsyncDespawnScanner);
		
		/* ################ IgnoreCreativePlayers ################ */
		ignoreCreativePlayers = cfg.getBoolean("IgnoreCreativePlayers", false);
		set(cfg, "IgnoreCreativePlayers", ignoreCreativePlayers);
		
		/* ################ TamedAnimals ################ */
		removeTamedAnimals = cfg.getBoolean("RemoveTamedAnimals", false);
		set(cfg, "RemoveTamedAnimals", removeTamedAnimals);
		
		countTamedAnimals = cfg.getBoolean("CountTamedAnimals", true);
		set(cfg, "CountTamedAnimals", countTamedAnimals);
		
		/* ################ Animal Despawning Stuff ################ */
		enableAnimalDespawning = cfg.getBoolean("EnableAnimalDespawning", true);
		set(cfg, "EnableAnimalDespawning", enableAnimalDespawning);
		
		daysTillFarmAnimalCleanup = cfg.getDouble("DaysTillFarmAnimalCleanup", 15.0D);
		set(cfg, "DaysTillFarmAnimalCleanup", daysTillFarmAnimalCleanup);
		
		protectedFarmAnimalSaveInterval = cfg.getInt("ProtectedFarmAnimalSaveInterval", 6000);
		set(cfg, "ProtectedFarmAnimalSaveInterval", protectedFarmAnimalSaveInterval);
		
		/* ################ SpawnChunkSearchDistance ################ */
		spawnChunkSearchDistance = (short) Math.abs(cfg.getInt("SpawnChunkSearchDistance", 5));
		// Validate SpawnChunkSearchDistance
		if (spawnChunkSearchDistance == 0)
			spawnChunkSearchDistance = 1;
		set(cfg, "SpawnChunkSearchDistance", spawnChunkSearchDistance);
		
		/* ################ FlyingMobAditionalLayerDepth ################ */
		flyingMobAditionalLayerDepth = (short) cfg.getInt("FlyingMobAditionalLayerDepth", 2);
		set(cfg, "FlyingMobAditionalLayerDepth", flyingMobAditionalLayerDepth);
		
		/* ################ TicksPerRecount ################ */
		ticksPerRecount = cfg.getInt("TicksPerRecount", 40);
		set(cfg, "TicksPerRecount", ticksPerRecount);
		
		/* ################ TicksPerDespawnScan ################ */
		ticksPerDespawnScan = cfg.getInt("TicksPerDespawnScan", 100);
		set(cfg, "TicksPerDespawnScan", ticksPerDespawnScan);
		
		/* ################ MinTicksLivedForDespawn ################ */
		minTicksLivedForDespawn = cfg.getInt("MinTicksLivedForDespawn", 100);
		set(cfg, "MinTicksLivedForDespawn", minTicksLivedForDespawn);
		
		/* ################ IgnoredMobs ################ */
		ignoredMobs =new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("IgnoredMobs"), "IgnoredMobs");
		ignoredMobs.addDefaults(ExtendedEntityType.get(EntityType.WITHER), ExtendedEntityType.get(EntityType.VILLAGER));
		List<String> ignoredList = ignoredMobs.getList();
		set(cfg, "IgnoredMobs", ignoredList);
		String strList = ignoredMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("IgnoredMobs: " + strList);
		
		
		/* ################ DisabledMobs ################ */
		disabledMobs = new TSettingContainer<ExtendedEntityType>(ExtendedEntityType.values(), cfg.getList("DisabledMobs"), "DisabledMobs");
		List<String> disabledList = disabledMobs.getList();
		set(cfg, "DisabledMobs", disabledList);
		strList = disabledMobs.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("DisabledMobs: " + strList);
		
		/* ################ EnabledSpawnReasons ################ */
		enabledSpawnReasons = new EnumSettingContainer(SpawnReason.class, cfg.getList("EnabledSpawnReasons", null), "The Spawn Reason '%s' is invalid");
		enabledSpawnReasons.addDefaults(SpawnReason.DEFAULT,
				SpawnReason.NATURAL,
				SpawnReason.SPAWNER,
				SpawnReason.CHUNK_GEN,
				SpawnReason.VILLAGE_DEFENSE,
				SpawnReason.VILLAGE_INVASION,
				SpawnReason.BUILD_IRONGOLEM,
				SpawnReason.BUILD_SNOWMAN,
				SpawnReason.BREEDING,
				SpawnReason.EGG);
		List<String> srList = enabledSpawnReasons.getList();
		set(cfg, "EnabledSpawnReasons", srList);
		strList = enabledSpawnReasons.toString();
		if (strList.length() != 0)
			P.p.getLogger().info("EnabledSpawnReasons: " + strList);
		
		
		/* ################ Layers ################ */
		layers = new ArrayList<String>();
		layerBoundaries = new HashSet<Integer>();
		for (String layer : cfg.getStringList("Layers"))
		{
			if (!layerPattern.matcher(layer).matches())
			{
				P.p.getLogger().info("The layer '" + layer + "' is invalid");
				continue;
			}
			
			// Splits the range string for the layer
			final String[] range = Config.layerSplitPattern.split(layer);

			// Converts range into integers
			int miny = Integer.valueOf(range[0]);
			int maxy = Integer.valueOf(range[1]);

			// Makes sure miny is actually the lower value
			if (maxy < miny)
			{
				miny = miny ^ maxy;
				maxy = miny ^ maxy;
				miny = miny ^ maxy;
			}
			
			layer = miny + "/" + maxy;
			// Add the boundaries of the layer
			layerBoundaries.add(miny);
			layerBoundaries.add(maxy);
			layers.add(layer);
		}
		
		if (layers.size() == 0)
		{
			for (int i = 0; i <= 240; i += 8)
			{
				layers.add(i + "/" + (i + 16));
			}
		}
		
		set(cfg, "Layers", layers);
		P.p.getLogger().info(layers.size() + " layers found");
		
		// Copy the header to the file
		copyHeader(cfg, "Limiter_ConfigHeader.txt", P.p.getDescription().getName() + " Limiter Global Config " + P.p.getDescription().getVersion() + "\n");
		saveConfig("", LIMITER_CONFIG_NAME, cfg);
	}
	
	public int setupWorlds()
	{
		int numWorlds = 0;
		worldConfigs = new HashMap<String, WorldConfig>();
		
		for (String worldName : enabledWorlds)
		{
			World world = P.p.getServer().getWorld(worldName);
			
			if (world == null)
				continue;
			
			WorldConfig wc = new WorldConfig(world);
			
			worldConfigs.put(world.getName(), wc);
			P.worlds.put(world.getName(), new MMWorld(world, wc));
			
			++numWorlds;
		}
		
		return numWorlds;
	}
}