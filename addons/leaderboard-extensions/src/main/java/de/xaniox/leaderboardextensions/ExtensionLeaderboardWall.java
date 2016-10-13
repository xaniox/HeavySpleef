/*
 * This file is part of addons.
 * Copyright (c) 2014-2016 Matthias Werning
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
package de.xaniox.leaderboardextensions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import de.xaniox.heavyspleef.commands.base.Command;
import de.xaniox.heavyspleef.commands.base.CommandContext;
import de.xaniox.heavyspleef.commands.base.CommandException;
import de.xaniox.heavyspleef.commands.base.PlayerOnly;
import de.xaniox.heavyspleef.core.HeavySpleef;
import de.xaniox.heavyspleef.core.Permissions;
import de.xaniox.heavyspleef.core.PlayerPostActionHandler;
import de.xaniox.heavyspleef.core.config.ConfigType;
import de.xaniox.heavyspleef.core.config.DatabaseConfig;
import de.xaniox.heavyspleef.core.config.SignLayoutConfiguration;
import de.xaniox.heavyspleef.core.event.GameEndEvent;
import de.xaniox.heavyspleef.core.event.Subscribe;
import de.xaniox.heavyspleef.core.event.Subscribe.Priority;
import de.xaniox.heavyspleef.core.extension.Extension;
import de.xaniox.heavyspleef.core.extension.ExtensionLobbyWall.SignRow;
import de.xaniox.heavyspleef.core.extension.ExtensionLobbyWall.SignRow.SignLooper;
import de.xaniox.heavyspleef.core.extension.ExtensionManager;
import de.xaniox.heavyspleef.core.extension.GameExtension;
import de.xaniox.heavyspleef.core.i18n.I18N;
import de.xaniox.heavyspleef.core.i18n.I18NManager;
import de.xaniox.heavyspleef.core.i18n.Messages;
import de.xaniox.heavyspleef.core.player.SpleefPlayer;
import de.xaniox.heavyspleef.core.script.Variable;
import de.xaniox.heavyspleef.core.stats.Statistic;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.dom4j.Element;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;

@Extension(name = "leaderboard-wall", hasCommands = true)
public class ExtensionLeaderboardWall extends GameExtension {

    private static final String METADATA_ID = "leaderboard_wall";

	@Command(name = "addleaderboardwall", minArgs = 1, usage = "/spleef addleaderboardwall <name>",
			descref = LEMessages.ADDLEADERBOARDWALL, i18nref = LeaderboardAddOn.I18N_REFERENCE, 
			permission = LEPermissions.PERMISSION_ADD_LEADERBOARD_WALL)
	public static void onAddLeaderboardWallCommand(CommandContext context, HeavySpleef heavySpleef, LeaderboardAddOn addon) throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		DatabaseConfig config = heavySpleef.getConfiguration(ConfigType.DATABASE_CONFIG);
		if (!config.isStatisticsModuleEnabled()) {
			sender.sendMessage(addon.getI18n().getString(Messages.Command.NEED_STATISTICS_ENABLED));
			return;
		}
		
		String name = context.getString(0);
		ExtensionManager manager = addon.getGlobalExtensionManager();
		for (ExtensionLeaderboardWall wall : manager.getExtensionsByType(ExtensionLeaderboardWall.class, false)) {
			if (!wall.getName().equalsIgnoreCase(name)) {
				continue;
			}
			
			throw new CommandException(addon.getI18n().getString(LEMessages.WALL_ALREADY_EXISTS));
		}
		
		ExtensionLeaderboardWall wall = new ExtensionLeaderboardWall(name);
		wall.setLayoutConfig(addon.getWallConfig());
		manager.addExtension(wall);
		
		sender.sendMessage(addon.getI18n().getVarString(LEMessages.WALL_ADDED)
				.setVariable("name", name)
				.toString());
		addon.saveExtensions();
	}
	
	@Command(name = "removeleaderboardwall", minArgs = 1, usage = "/spleef removeleaderboardwall <name>",
			descref = LEMessages.REMOVELEADERBOARDWALL, i18nref = LeaderboardAddOn.I18N_REFERENCE,
			permission = LEPermissions.PERMISSION_REMOVE_LEADERBOARD_WALL)
	public static void onRemoveLeaderboardWallCommand(CommandContext context, HeavySpleef heavySpleef, LeaderboardAddOn addon)
			throws CommandException {
		CommandSender sender = context.getSender();
		if (sender instanceof Player) {
			sender = heavySpleef.getSpleefPlayer(sender);
		}
		
		String name = context.getString(0);
		ExtensionManager manager = addon.getGlobalExtensionManager();
		ExtensionLeaderboardWall removed = null;
		
		for (ExtensionLeaderboardWall wall : manager.getExtensionsByType(ExtensionLeaderboardWall.class, false)) {
			if (!wall.getName().equalsIgnoreCase(name)) {
				continue;
			}
			
			removed = wall;
			wall.clearWall();
			manager.removeExtension(wall);
			break;
		}
		
		sender.sendMessage(addon.getI18n().getVarString(removed != null ? LEMessages.WALL_REMOVED : LEMessages.WALL_NOT_FOUND)
				.setVariable("name", removed != null ? removed.getName() : name)
				.toString());
		addon.saveExtensions();
	}
	
	@Command(name = "addleaderboardrow", minArgs = 1, usage = "/spleef addleaderboardrow <name>",
			descref = LEMessages.ADDLEADERBOARDROW, i18nref = LeaderboardAddOn.I18N_REFERENCE,
			permission = LEPermissions.PERMISSION_ADD_LEADERBOARD_ROW)
	@PlayerOnly
	public static void onAddLeaderboardRowCommand(CommandContext context, HeavySpleef heavySpleef, final LeaderboardAddOn addon)
			throws CommandException {
		processRowCommand(context, heavySpleef, addon, true);
	}
	
	@Command(name = "removeleaderboardrow", minArgs = 1, usage = "/spleef removeleaderboardrow <name>",
			descref = LEMessages.REMOVELEADERBOARDROW, i18nref = LeaderboardAddOn.I18N_REFERENCE,
			permission = LEPermissions.PERMISSION_REMOVE_LEADERBOARD_ROW)
	@PlayerOnly
	public static void onRemoveLeaderboardRow(CommandContext context, HeavySpleef heavySpleef, final LeaderboardAddOn addon) throws CommandException {
		processRowCommand(context, heavySpleef, addon, false);
	}
	
	private static void processRowCommand(CommandContext context, HeavySpleef heavySpleef, final LeaderboardAddOn addon, final boolean add)
			throws CommandException {
		final SpleefPlayer player = heavySpleef.getSpleefPlayer(context.getSender());
		
		String name = context.getString(0);
		ExtensionManager manager = addon.getGlobalExtensionManager();
		ExtensionLeaderboardWall found = null;
		
		for (ExtensionLeaderboardWall wall : manager.getExtensionsByType(ExtensionLeaderboardWall.class, false)) {
			if (!wall.getName().equalsIgnoreCase(name)) {
				continue;
			}
			
			found = wall;
		}
		
		if (found == null) {
			throw new CommandException(addon.getI18n().getVarString(LEMessages.WALL_NOT_FOUND)
					.setVariable(name, "name")
					.toString());
		}
		
		heavySpleef.getPostActionHandler().addPostAction(player, PlayerInteractEvent.class, new PlayerPostActionHandler.PostActionCallback<PlayerInteractEvent>() {

			@Override
			public void onPostAction(PlayerInteractEvent event, SpleefPlayer player, Object cookie) {
				ExtensionLeaderboardWall wall = (ExtensionLeaderboardWall) cookie;
				Block block = event.getClickedBlock();
				
				event.setCancelled(true);
				if (block == null || block.getType() != Material.WALL_SIGN) {
					addon.getI18n().getString(Messages.Command.BLOCK_NOT_A_SIGN);
					return;
				}
				
				Sign sign = (Sign) block.getState();
				int removed = 0;
				
				if (add) {
					SignRow row = SignRow.generateRow(sign);
                    row.enableSignMetadata();
					wall.addRow(row);
				} else {
					for (Iterator<SignRow> iterator = wall.getRows().iterator(); iterator.hasNext();) {
						SignRow candidate = iterator.next();
                        org.bukkit.util.Vector start = candidate.getStart();
						org.bukkit.util.Vector end = candidate.getEnd();
						
						Vector startVec = BukkitUtil.toVector(start);
						Vector endVec = BukkitUtil.toVector(end);
						Vector blockVec = BukkitUtil.toVector(block);
						
						Region region = new CuboidRegion(startVec, endVec);
						
						if (!region.contains(blockVec)) {
							continue;
						}
						
						candidate.clearAll();
						iterator.remove();
						removed++;
					}
				}

				wall.update();
				player.sendMessage(addon.getI18n().getVarString(add ? LEMessages.ROW_ADDED : LEMessages.ROWS_REMOVED)
						.setVariable("removed", String.valueOf(removed))
						.toString());
				addon.saveExtensions();
			}
			
		}, found);
		
		player.sendMessage(addon.getI18n().getString(LEMessages.CLICK_ON_SIGN));
	}
	
	private String name;
	private SignLayoutConfiguration layoutConfig;
	private List<SignRow> rows;
    private I18N i18n = I18NManager.getGlobal();
	
	protected ExtensionLeaderboardWall() {
		this.rows = Lists.newArrayList();
	}
	
	public ExtensionLeaderboardWall(String name) {
		this();
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setLayoutConfig(SignLayoutConfiguration layoutConfig) {
		this.layoutConfig = layoutConfig;
	}
	
	public List<SignRow> getRows() {
		return rows;
	}
	
	public void addRow(SignRow row) {
		if (rows.contains(row)) {
			throw new IllegalArgumentException("SignRow already added");
		}
		
		rows.add(row);
	}
	
	public void removeRow(SignRow row) {
		rows.remove(row);
	}
	
	public void removeRow(int index) {
		rows.remove(index);
	}
	
	@Subscribe(priority = Priority.MONITOR)
	public void onGameEnd(GameEndEvent event) {
		update();
	}

	@SuppressWarnings("unchecked")
	@EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        SpleefPlayer player = heavySpleef.getSpleefPlayer(event.getPlayer());
        if (!player.hasPermission(Permissions.PERMISSION_STATS_OTHER)) {
            return;
        }

        Block block = event.getClickedBlock();
        boolean isWallBlock = false;
        SignRow clickedRow = null;

        for (SignRow row : rows) {
            org.bukkit.util.Vector sv = row.getStart();
            org.bukkit.util.Vector ev = row.getEnd();

            Block start = row.getWorld().getBlockAt(sv.getBlockX(), sv.getBlockY(), sv.getBlockZ());
            Block end = row.getWorld().getBlockAt(ev.getBlockX(), ev.getBlockY(), ev.getBlockZ());
            Block current = start;

            do {
                if (current.equals(block)) {
                    clickedRow = row;
                    isWallBlock = true;
                    break;
                }

                current = current.getRelative(row.getDirection().getBlockFace3D());
            } while (!current.equals(end));

            if (isWallBlock) {
                break;
            }
        }

        if (!isWallBlock) {
            return;
        }

        org.bukkit.util.Vector blockVec = block.getLocation().toVector();
        Map<String, Object> metadata = clickedRow.getMetadata(blockVec);
        if (metadata == null) {
            return;
        }

        Entry<String, Statistic> statisticEntry = (Entry<String, Statistic>) metadata.get(METADATA_ID);
        if (statisticEntry == null) {
            player.sendMessage(i18n.getString(Messages.Command.ERROR_ON_STATISTIC_LOAD));
            return;
        }

        Statistic.FullStatisticPrinter printer = new Statistic.FullStatisticPrinter(heavySpleef.getDatabaseHandler(),
                player.getBukkitPlayer(), statisticEntry.getKey(), heavySpleef.getLogger());
        printer.print();
    }
	
	public void update() {
		int lengthSum = 0;
		for (SignRow row : rows) {
			lengthSum += row.getLength();
		}
		
		heavySpleef.getDatabaseHandler().getTopStatistics(0, lengthSum, new FutureCallback<Map<String,Statistic>>() {
			
			@Override
			public void onSuccess(Map<String, Statistic> result) {
				update(result);
			}
			
			@Override
			public void onFailure(Throwable t) {
				heavySpleef.getLogger().log(Level.WARNING, "Cannot retrieve top statistics for leaderboard wall", t);
			}
		});
	}
	
	public void update(Map<String, Statistic> statistics) {
		Iterator<Entry<String, Statistic>> iterator = statistics.entrySet().iterator();
		int index = 0;
		
		for (SignRow row : rows) {
			WallSignLooper looper = new WallSignLooper(iterator, index);
			row.loopSigns(looper);
			
			index = looper.getIndex();
		}
	}
	
	public void clearWall() {
		for (SignRow row : rows) {
			row.clearAll();
		}
	}
	
	@Override
	public void marshal(Element element) {
		element.addElement("name").setText(name);
		for (SignRow row : rows) {
			Element rowElement = element.addElement("row");
			row.marshal(rowElement);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void unmarshal(Element element) {
		name = element.elementText("name");
		
		List<Element> rowElements = element.elements("row");
		for (Element rowElement : rowElements) {
			SignRow row = new SignRow();
			row.unmarshal(rowElement);
			rows.add(row);
		}
	}
	
	private class WallSignLooper implements SignLooper {

		private Iterator<Entry<String, Statistic>> iterator;
		private int index;
		
		public WallSignLooper(Iterator<Entry<String, Statistic>> iterator, int index) {
			this.iterator = iterator;
			this.index = index;
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public LoopReturn loop(int rowIndex, SignRow.WrappedMetadataSign sign) {
            Sign bukkitSign = sign.getBukkitSign();
			if (!iterator.hasNext()) {
				return LoopReturn.RETURN;
			}
			
			Entry<String, Statistic> entry = iterator.next();
			Set<Variable> variables = Sets.newHashSet();
			entry.getValue().supply(variables, null);
			variables.add(new Variable("player", entry.getKey()));
			variables.add(new Variable("rank", index + 1));
			layoutConfig.getLayout().inflate(bukkitSign, variables);
            bukkitSign.update();

            sign.setMetadata(METADATA_ID, entry);
			
			++index;
			return LoopReturn.DEFAULT;
		}
		
	}

}