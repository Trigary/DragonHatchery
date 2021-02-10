package hu.trigary.dragonhatchery.command;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a subcommand, so a command that has its own logic,
 * but lies under a {@link Command} and cannot be accessed directly.
 */
public abstract class SubCommand {
	protected final String logPrefix = getClass().getSimpleName() + ": ";
	protected final DragonHatcheryPlugin plugin;
	
	/**
	 * Constructs a new subcommand.
	 * It needs to be registered manually.
	 *
	 * @param plugin the plugin instance
	 */
	protected SubCommand(@NotNull DragonHatcheryPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Handles this subcommand being executed.
	 *
	 * @param sender who executed the command
	 * @param args the arguments provided when executing the subcommand;
	 * this does not include the subcommand itself; it may be empty
	 * @return false if the {@link #getUsage()} should be sent, false otherwise
	 * @see CommandExecutor#onCommand(CommandSender, Command, String, String[])
	 */
	public abstract boolean onCommand(@NotNull CommandSender sender,
			@NotNull List<String> args);
	
	/**
	 * Handles this subcommand being tab completed.
	 *
	 * @param sender who executed the command
	 * @param args the arguments provided when tab completing the subcommand;
	 * this does not include the subcommand itself; it may be empty
	 * @return the tab completions to suggest to the sender
	 * @see TabCompleter#onTabComplete(CommandSender, Command, String, String[])
	 */
	public abstract @NotNull List<String> onTabComplete(@NotNull CommandSender sender,
			@NotNull List<String> args);
	
	/**
	 * Gets the name of this subcommand.
	 * This name is also the text being used to refer
	 * to this subcommand when attempting to execute it.
	 * It shouldn't contain color codes.
	 *
	 * @return the name of this subcommand
	 */
	@Contract(pure = true)
	public abstract @NotNull String getName();
	
	/**
	 * Gets the description of this subcommand.
	 * This is used eg. when listing subcommands.
	 * It shouldn't contain color codes and it should be short.
	 *
	 * @return the description of this subcommand
	 */
	@Contract(pure = true)
	public abstract @NotNull String getDescription();
	
	/**
	 * Gets the explanation of how to use this subcommand:
	 * the required and optional arguments in human readable text form.
	 * The returned value should usually start with {@link #getName()};
	 * the main/base command's name is usually prepended to this method's output.
	 * Example return value: {@code getName() + "<required> [optional]"}
	 *
	 * @return the explanation on how to use this subcommand
	 */
	@Contract(pure = true)
	public abstract @NotNull String getUsage();
	
	
	/**
	 * Utility method that has multiple functions that informs non-player
	 * {@link CommandSender} instances that they can't use this subcommand.
	 * The return value also signifies whether the sender is a player or not,
	 * for seamless integration with early return if statements.
	 *
	 * @param sender the sender to disallow if it isn't a {@link Player}
	 * @return false if the sender is a {@link Player}, true otherwise
	 */
	protected boolean disallowConsole(@NotNull CommandSender sender) {
		if (sender instanceof Player) {
			return false;
		} else {
			sender.sendMessage(new ComponentBuilder("Only players can use this command!")
					.color(ChatColor.RED).create());
			return true;
		}
	}
}
