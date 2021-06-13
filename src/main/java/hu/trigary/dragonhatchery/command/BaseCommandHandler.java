package hu.trigary.dragonhatchery.command;

import hu.trigary.dragonhatchery.DragonHatcheryPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Handler of the commands this plugin provides, including the subcommands.
 */
public class BaseCommandHandler implements TabExecutor {
	private final String logPrefix = getClass().getSimpleName() + ": ";
	private final Map<String, SubCommand> subCommands = new LinkedHashMap<>();
	private final DragonHatcheryPlugin plugin;
	
	/**
	 * Constructs a new instance.
	 * Subcommands are registered internally.
	 *
	 * @param plugin the plugin instance
	 */
	public BaseCommandHandler(@NotNull DragonHatcheryPlugin plugin) {
		this.plugin = plugin;
		
		registerSubCommand(new ReloadCommand(plugin));
		registerSubCommand(new PrintDataCommand(plugin));
	}
	
	@Override
	public boolean onCommand(@NotNull CommandSender sender,
			@NotNull Command command, @NotNull String label, @NotNull String[] args) {
		SubCommand subCommand = getSubCommand(args);
		if (subCommand == null) {
			onWrongSubCommand(sender);
		} else {
			List<String> subArgs = Arrays.asList(args).subList(1, args.length);
			if (!subCommand.onCommand(sender, subArgs)) {
				sender.sendMessage(Component.text("Usage: /" + label
						+ " " + subCommand.getUsage(), NamedTextColor.RED));
			}
		}
		return true;
	}
	
	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
			@NotNull Command command, @NotNull String alias,
			@NotNull String @NotNull [] args) {
		if (args.length == 0 || args[0].isEmpty()) {
			return new ArrayList<>(subCommands.keySet());
		}
		
		SubCommand subCommand = getSubCommand(args);
		if (subCommand != null) {
			List<String> subArgs = Arrays.asList(args).subList(1, args.length);
			return subCommand.onTabComplete(sender, subArgs);
		}
		
		String partial = args[0].toLowerCase();
		return subCommands.keySet().stream()
				.filter(s -> s.startsWith(partial))
				.collect(Collectors.toList());
	}
	
	/**
	 * Sends the specified {@link CommandSender} an informative message
	 * that should help them when they should be invoking a subcommand, but aren't.
	 *
	 * @param sender who should receive the help message
	 */
	private void onWrongSubCommand(@NotNull CommandSender sender) {
		TextComponent.Builder builder = Component.text()
				.append(Component.text("Please specify one of the following subcommands:",
						NamedTextColor.YELLOW));
		for (SubCommand subCommand : subCommands.values()) {
			builder.append(Component.newline())
					.append(Component.text(" - ", NamedTextColor.GRAY))
					.append(Component.text(subCommand.getName(), NamedTextColor.GOLD))
					.append(Component.text(": ", NamedTextColor.GRAY))
					.append(Component.text(subCommand.getDescription(),
							NamedTextColor.WHITE));
		}
		sender.sendMessage(builder);
	}
	
	/**
	 * Registers the specified subcommand in this instance.
	 *
	 * @param subCommand the subcommand to register
	 */
	private void registerSubCommand(@NotNull SubCommand subCommand) {
		subCommands.put(subCommand.getName().toLowerCase(), subCommand);
		plugin.getLogger().log(Level.FINE,
				() -> logPrefix + "Registered subcommand: " + subCommand.getName());
	}
	
	/**
	 * Tries to get the subcommand the specified arguments point to.
	 * Returns null in case none could be determined.
	 *
	 * @param args the arguments that may or may not point to a subcommand;
	 * the array might be empty
	 * @return the subcommand if found, otherwise null
	 */
	@Contract(pure = true)
	private @Nullable SubCommand getSubCommand(@NotNull String @NotNull [] args) {
		return args.length == 0 ? null : subCommands.get(args[0].toLowerCase());
	}
}
