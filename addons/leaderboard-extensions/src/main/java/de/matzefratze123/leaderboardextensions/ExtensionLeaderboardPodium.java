/*
 * This file is part of addons.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * This file is part of HeavySpleef.
 * Copyright (c) 2014-2015 matzefratze123
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.matzefratze123.leaderboardextensions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.dom4j.Element;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;

import de.matzefratze123.heavyspleef.commands.base.Command;
import de.matzefratze123.heavyspleef.commands.base.CommandContext;
import de.matzefratze123.heavyspleef.commands.base.CommandException;
import de.matzefratze123.heavyspleef.commands.base.PlayerOnly;
import de.matzefratze123.heavyspleef.commands.base.TabComplete;
import de.matzefratze123.heavyspleef.core.HeavySpleef;
import de.matzefratze123.heavyspleef.core.config.ConfigType;
import de.matzefratze123.heavyspleef.core.config.DatabaseConfig;
import de.matzefratze123.heavyspleef.core.config.SignLayoutConfiguration;
import de.matzefratze123.heavyspleef.core.event.GameEndEvent;
import de.matzefratze123.heavyspleef.core.event.Subscribe;
import de.matzefratze123.heavyspleef.core.event.Subscribe.Priority;
import de.matzefratze123.heavyspleef.core.extension.Extension;
import de.matzefratze123.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.BlockFace2D;
import de.matzefratze123.heavyspleef.core.extension.ExtensionManager;
import de.matzefratze123.heavyspleef.core.extension.GameExtension;
import de.matzefratze123.heavyspleef.core.game.Game;
import de.matzefratze123.heavyspleef.core.i18n.Messages;
import de.matzefratze123.heavyspleef.core.layout.SignLayout;
import de.matzefratze123.heavyspleef.core.player.SpleefPlayer;
import de.matzefratze123.heavyspleef.core.script.Variable;
import de.matzefratze123.heavyspleef.core.stats.Statistic;
import de.matzefratze123.heavyspleef.core.stats.StatisticRecorder;

@Extension(name = "winner-podium", hasCommands = true)
public class ExtensionLeaderboardPodium extends GameExtension {
	
	@Command(name = "addpodium", descref = LEMessages.ADDPODIUM, 
			i18nref = LeaderboardAddOn.I18N_REFERENCE, minArgs = 1,
			permission = LEPermissions.PERMISSION_ADD_PODIUM,
			usage = "/spleef addpodium <name> [small|large]")
	@PlayerOnly
	public static void onAddPodiumCommand(CommandContext context, HeavySpleef heavySpleef, LeaderboardAddOn addon) throws CommandException {
		SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		DatabaseConfig config = heavySpleef.getConfiguration(ConfigType.DATABASE_CONFIG);
		if (!config.isStatisticsModuleEnabled()) {
			player.sendMessage(addon.getI18n().getString(Messages.Command.NEED_STATISTICS_ENABLED));
			return;
		}
		
		Location playerLocation = player.getBukkitPlayer().getLocation();
		BlockFace2D direction = BlockFace2D.byYaw(playerLocation.getYaw());
		PodiumSize size = PodiumSize.SMALL;
		
		String name = context.getString(0);
		String sizeString = context.getStringSafely(1);
		
		if (sizeString != null) {
			size = PodiumSize.byName(sizeString);
			
			if (size == null) {
				size = PodiumSize.SMALL;
			}
		}
		
		ExtensionManager manager = addon.getGlobalExtensionManager();
		for (ExtensionLeaderboardPodium otherPodium : manager.getExtensionsByType(ExtensionLeaderboardPodium.class, false)) {
			if (!otherPodium.getName().equalsIgnoreCase(name)) {
				continue;
			}
			
			throw new CommandException(addon.getI18n().getVarString(LEMessages.PODIUM_ALREADY_EXISTS)
					.setVariable("name", name)
					.toString());
		}
		
		
		ExtensionLeaderboardPodium podium = new ExtensionLeaderboardPodium(name, playerLocation, direction, size);
		podium.setLayoutConfig(addon.getPodiumConfig());
		
		manager.addExtension(podium);
		podium.update(true);
		
		player.sendMessage(addon.getI18n().getVarString(LEMessages.PODIUM_ADDED)
				.setVariable("name", name)
				.toString());
		
		addon.saveExtensions();
	}
	
	@Command(name = "removepodium", descref = LEMessages.REMOVEPODIUM, i18nref = LeaderboardAddOn.I18N_REFERENCE,
			minArgs = 1, permission = LEPermissions.PERMISSION_REMOVE_PODIUM,
			usage = "/spleef removepodium <name>")
	public static void onRemovePodiumCommand(CommandContext context, HeavySpleef heavySpleef, LeaderboardAddOn addon) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String name = context.getString(0);
		ExtensionManager manager = addon.getGlobalExtensionManager();
		
		ExtensionLeaderboardPodium removed = null;
		for (ExtensionLeaderboardPodium podium : manager.getExtensionsByType(ExtensionLeaderboardPodium.class, false)) {
			if (!podium.getName().equalsIgnoreCase(name)) {
				continue;
			}
			
			//A ConcurrentModificationException is prevented because ExtensionManager#getExtensionsByType(Class<Extension>)
			//returns a copied set and not the actual set of registered extensions
			manager.removeExtension(podium);
			podium.update(null, false, true);
			removed = podium;
			break;
		}
		
		sender.sendMessage(addon.getI18n().getVarString(removed != null ? LEMessages.PODIUM_REMOVED : LEMessages.PODIUM_NOT_FOUND)
				.setVariable("name", removed != null ? removed.getName() : name)
				.toString());
		
		addon.saveExtensions();
	}
	
	@TabComplete("removepodium")
	public static void onRemovePodiumTabComplete(CommandContext context, List<String> list, HeavySpleef heavySpleef, LeaderboardAddOn addon) {
		if (context.argsLength() == 1) {
			ExtensionManager manager = addon.getGlobalExtensionManager();
			for (ExtensionLeaderboardPodium podium : manager.getExtensionsByType(ExtensionLeaderboardPodium.class, false)) {
				list.add(podium.getName());
			}
		}
	}
	
	private static final byte SKULL_ON_FLOOR = 1;
	
	private String name;
	private Location baseLocation;
	private BlockFace2D direction;
	private PodiumSize size;
	private SignLayoutConfiguration layoutConfig;
	
	public ExtensionLeaderboardPodium(String name, Location baseLocation, BlockFace2D direction, PodiumSize size) {
		this.name = name;
		this.baseLocation = baseLocation;
		this.direction = direction;
		this.size = size;
	}
	
	protected ExtensionLeaderboardPodium() {}
	
	public String getName() {
		return name;
	}
	
	public void setLayoutConfig(SignLayoutConfiguration layoutConfig) {
		this.layoutConfig = layoutConfig;
	}

	@Subscribe(priority = Priority.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		Game game = event.getGame();
		StatisticRecorder recorder = game.getStatisticRecorder();
		
		if (!recorder.isEnableRating()) {
			return;
		}
		
		update(false);
	}
	
	public void update(final boolean forceBlocks) {
		getHeavySpleef().getDatabaseHandler().getTopStatistics(0, size.getStatisticAmount(), new FutureCallback<Map<String,Statistic>>() {
			
			@Override
			public void onSuccess(Map<String, Statistic> result) {
				update(result, forceBlocks, false);
			}
			
			@Override
			public void onFailure(Throwable t) {
				getHeavySpleef().getLogger().log(Level.WARNING, "Cannot retrieve top statistics for podium", t);
			}
		});
	}
	
	@SuppressWarnings("deprecation")
	public void update(Map<String, Statistic> statistics, boolean forceBlocks, boolean delete) {
		BlockFace2D rightDir = direction.right();
		BlockFace2D leftDir = direction.left();
		
		BlockFace rightFace = rightDir.getBlockFace3D();
		BlockFace leftFace = leftDir.getBlockFace3D();
		
		Block baseBlock = baseLocation.getBlock();
		if (delete) {
			baseBlock.setType(Material.AIR);
		}
		
		SignLayout layout = layoutConfig.getLayout();
		
		Iterator<Entry<String, Statistic>> iterator = statistics != null ? statistics.entrySet().iterator() : null;
		for (int i = 0; i < size.getStatisticAmount(); i++) {
			Entry<String, Statistic> entry = iterator != null && iterator.hasNext() ? iterator.next() : null;
			
			Block position = null;
			Material type = null;
			
			switch (i) {
			case 0:
				//Top
				position = baseBlock.getRelative(BlockFace.UP);
				type = Material.DIAMOND_BLOCK;
				break;
			case 1:
				//First left
				position = baseBlock.getRelative(leftFace);
				type = Material.GOLD_BLOCK;
				break;
			case 2:
				//First right
				position = baseBlock.getRelative(rightFace);
				type = Material.IRON_BLOCK;
				break;
			case 3:
				//Second left
				position = baseBlock.getRelative(leftFace, 2);
				type = Material.DOUBLE_STEP;
				break;
			case 4:
				//Second right
				position = baseBlock.getRelative(rightFace, 2);
				type = Material.DOUBLE_STEP;
				break;
			}
			
			if (position == null) {
				continue;
			}
			
			Block signBlock = position.getRelative(direction.getBlockFace3D());
			Block skullBlock = position.getRelative(BlockFace.UP);
			
			if (delete) {
				signBlock.setType(Material.AIR);
				skullBlock.setType(Material.AIR);
				position.setType(Material.AIR);
				continue;
			}
			
			if (baseBlock.getType() == Material.AIR || forceBlocks) {
				baseBlock.setType(Material.DOUBLE_STEP);
			}
			
			if (position.getType() == Material.AIR || forceBlocks) {
				position.setType(type);
			}
			
			if (entry == null) {
				continue;
			}
			
			/* For legacy reasons and compatibility */
			signBlock.setTypeId(Material.WALL_SIGN.getId(), false);
			skullBlock.setTypeId(Material.SKULL.getId(), false);
			
			Skull skull = (Skull) skullBlock.getState();
			skull.setRotation(direction.getBlockFace3D());
			skull.setSkullType(SkullType.PLAYER);
			skull.setOwner(entry.getKey());
			skull.setRawData(SKULL_ON_FLOOR);
			skull.update(true, false);
			
			Sign sign = (Sign) signBlock.getState();
			
			Set<Variable> variables = Sets.newHashSet();
			entry.getValue().supply(variables, null);
			variables.add(new Variable("player", entry.getKey()));
			variables.add(new Variable("rank", i + 1));
			
			layout.inflate(sign, variables);
			org.bukkit.material.Sign data = new org.bukkit.material.Sign(Material.WALL_SIGN);
			data.setFacingDirection(direction.getBlockFace3D());
			sign.setData(data);
			sign.update();
		}
	}
	
	@Override
	public void marshal(Element element) {
		element.addElement("name").addText(name);
		
		Element locElement = element.addElement("base-location");
		locElement.addElement("world").setText(String.valueOf(baseLocation.getWorld().getName()));
		locElement.addElement("x").setText(String.valueOf(baseLocation.getBlockX()));
		locElement.addElement("y").setText(String.valueOf(baseLocation.getBlockY()));
		locElement.addElement("z").setText(String.valueOf(baseLocation.getBlockZ()));
		
		element.addElement("direction").addText(direction.name());
		element.addElement("size").addText(size.name());
	}

	@Override
	public void unmarshal(Element element) {
		name = element.elementText("name");
		
		Element locElement = element.element("base-location");
		
		World world = Bukkit.getWorld(locElement.elementText("world"));
		int x = Integer.parseInt(locElement.elementText("x"));
		int y = Integer.parseInt(locElement.elementText("y"));
		int z = Integer.parseInt(locElement.elementText("z"));
		
		baseLocation = new Location(world, x, y, z);
		direction = BlockFace2D.valueOf(element.elementText("direction"));
		size = PodiumSize.valueOf(element.elementText("size"));
	}

	public enum PodiumSize {
		
		SMALL(3),
		LARGE(5);
		
		private int statisticAmount;
		
		private PodiumSize(int statisticAmount) {
			this.statisticAmount = statisticAmount;
		}
		
		public int getStatisticAmount() {
			return statisticAmount;
		}
		
		public static PodiumSize byName(String name) {
			for (PodiumSize size : values()) {
				if (!size.name().equalsIgnoreCase(name)) {
					continue;
				}
				
				return size;
			}
			
			return null;
		}
		
	}
	
}
