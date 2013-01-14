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

package com.forgenz.mobmanager.listeners.commands;

import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.forgenz.mobmanager.P;

public class MMCommandReload extends MMCommand
{

	MMCommandReload()
	{
		super(Pattern.compile("reload", Pattern.CASE_INSENSITIVE), Pattern.compile("^.*$"),
				0, 0);
	}

	@Override
	public void run(CommandSender sender, String maincmd, String[] args)
	{
		if (sender instanceof Player && !sender.hasPermission("mobmanager.reload"))
		{
			sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use /mm reload");
			return;
		}
		
		P p = P.p;
		p.getServer().getPluginManager().disablePlugin(p);
		p.reloadConfig();
		p.getServer().getPluginManager().enablePlugin(p);
		
		sender.sendMessage(ChatColor.GRAY + "~Reload Complete");
	}

	@Override
	public String getUsage()
	{
		return "%s/%s %s%s";
	}

	@Override
	public String getDescription()
	{
		return "Reloads MobManager configuration";
	}

	@Override
	public String getAliases()
	{
		return "reload";
	}

}
