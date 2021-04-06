package hu.trigary.dragonhatchery;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.logging.Logger;

/**
 * Class that should be used a base class by test classes that depend on the Bukkit API:
 * this class mocks the Bukkit API, see eg. {@link #getServer()}.
 *
 * <br><br>
 * By using {@link TestInstance.Lifecycle#PER_CLASS} the Bukkit mocking
 * is only initialized once per class, and tests in those classes are
 * executed sequentially, even if tests are executed concurrently by default.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BukkitTestBase {
	private Server server;
	private DragonHatcheryPlugin plugin;
	
	/**
	 * Sets up the mocking of {@link #getServer()} and {@link #getPlugin()}.
	 */
	@BeforeAll
	final void setUp() {
		server = Mockito.mock(Server.class);
		Mockito.when(server.getPluginManager())
				.thenReturn(new SimplePluginManager(server, new SimpleCommandMap(server)));
		Logger serverLogger = Logger.getLogger("MockServer#" + hashCode());
		Mockito.when(server.getLogger()).thenReturn(serverLogger);
		Mockito.when(server.isPrimaryThread()).thenReturn(true);
		
		Mockito.when(server.createBlockData(Mockito.any(), Mockito.anyString())).then(invocation -> {
			Material material = invocation.getArgument(0, Material.class);
			String data = invocation.getArgument(1, String.class);
			if (material == null || data == null || (!data.isEmpty()
					&& (!data.startsWith("[") || !data.endsWith("]")))) {
				throw new IllegalArgumentException("Invalid parameters: "
						+ "material=" + material + "; data=" + data);
			} else {
				BlockData value = Mockito.mock(BlockData.class);
				Mockito.when(value.getMaterial()).thenReturn(material);
				Mockito.when(value.getAsString()).thenReturn(material.getKey() + data);
				Mockito.when(value.getAsString(Mockito.anyBoolean()))
						.thenReturn(material.getKey() + data);
				Mockito.when(value.clone())
						.then(ignored -> server.createBlockData(material, data));
				return value;
			}
		});
		
		plugin = Mockito.mock(DragonHatcheryPlugin.class);
		Mockito.when(plugin.isEnabled()).thenReturn(true);
		Mockito.when(plugin.getServer()).thenReturn(server);
		Mockito.when(plugin.getLogger()).thenReturn(serverLogger);
	}
	
	/**
	 * Gets the mocked {@link Server} instance.
	 * This instance is guaranteed to be valid.
	 *
	 * @return the {@link Server} mock
	 */
	@Contract(pure = true)
	public final @NotNull Server getServer() {
		return server;
	}
	
	/**
	 * Gets the mocked plugin instance.
	 * This instance is guaranteed to be valid.
	 *
	 * @return the plugin mock
	 */
	@Contract(pure = true)
	public final @NotNull DragonHatcheryPlugin getPlugin() {
		return plugin;
	}
}
