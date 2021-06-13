package hu.trigary.dragonhatchery.command;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Subcommand responsible for giving {@link BlockData#getAsString()}
 * values to the player, to be used in this plugin's configuration.
 */
public class PrintDataCommand extends SubCommand {
	
	/**
	 * Constructs a new subcommand.
	 * It needs to be registered manually.
	 *
	 * @param plugin the plugin instance
	 */
	public PrintDataCommand(@NotNull DragonHatcheryPlugin plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender,
			@NotNull List<String> args) {
		if (disallowConsole(sender)) {
			return true;
		}
		
		Player player = (Player) sender;
		Block block = player.getTargetBlock(4);
		if (block == null) {
			player.sendMessage(Component.text("You aren't looking at any blocks!",
					NamedTextColor.RED));
		} else {
			String blockName = block.getType().getKey().getKey();
			String data = block.getBlockData().getAsString(true);
			int dataStart = data.indexOf("[");
			data = dataStart == -1 ? "" : data.substring(dataStart);
			plugin.getLogger().info("Data of a '" + blockName
					+ "' (requested by " + player.getName() + "): \""
					+ data + "\"");
			player.sendMessage(Component.text()
					.append(Component.text("The data of the '", NamedTextColor.GREEN))
					.append(Component.text(blockName, NamedTextColor.YELLOW))
					.append(Component.text("' block has been printed to the console.",
							NamedTextColor.GREEN))
			);
		}
		return true;
	}
	
	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
			@NotNull List<String> args) {
		return Collections.emptyList();
	}
	
	@Override
	public @NotNull String getName() {
		return "printdata";
	}
	
	@Override
	public @NotNull String getDescription() {
		return "Get the data of the block you are looking at.";
	}
	
	@Override
	public @NotNull String getUsage() {
		return getName();
	}
}
