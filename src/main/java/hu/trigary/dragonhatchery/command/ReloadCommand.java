package hu.trigary.dragonhatchery.command;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * Subcommand responsible for executing {@link DragonHatcheryPlugin#reload()}.
 */
public class ReloadCommand extends SubCommand {
	
	/**
	 * Constructs a new subcommand.
	 * It needs to be registered manually.
	 *
	 * @param plugin the plugin instance
	 */
	public ReloadCommand(@NotNull DragonHatcheryPlugin plugin) {
		super(plugin);
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender,
			@NotNull List<String> args) {
		plugin.reload();
		sender.sendMessage(Component.text("The configuration has been reloaded."
				+ " Check the console for errors.", NamedTextColor.GREEN));
		return true;
	}
	
	@Override
	public @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
			@NotNull List<String> args) {
		return Collections.emptyList();
	}
	
	@Override
	public @NotNull String getName() {
		return "reload";
	}
	
	@Override
	public @NotNull String getDescription() {
		return "Reload the plugin's configuration.";
	}
	
	@Override
	public @NotNull String getUsage() {
		return getName();
	}
}
