/*
  Copyright 2019 dumptruckman
 
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of
  the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
  THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
  TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
  
  Source: https://gist.github.com/dumptruckman/4f2bf416f8557c7aec1310bb346a3c46
 */

package hu.trigary.dragonhatchery.util;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A logger handler for a bukkit plugin that will cause messages below {@link Level#INFO} to be logged normally when
 * the plugin's logger is set to the level of the message or lower.
 * <p/>
 * To enable logging of messages below {@link Level#INFO}, the plugin's logger must be set to an appropriately lower
 * level. For example, to log all message levels: <code>plugin.getLogger().setLevel(Level.ALL);</code>
 */
public class DebugLogHandler extends Handler {
	
	private static final String DEFAULT_DEBUG_PREFIX_FORMAT = "[%1$s-%2$s] %3$s";
	private static final String DEFAULT_DEBUG_LOG_PREFIX = "DEBUG";
	
	/**
	 * Attaches a DebugLogHandler to the given plugin's logger.
	 *
	 * @param plugin the plugin to add debug logging for.
	 */
	public static void attachDebugLogger(@NotNull Plugin plugin) {
		plugin.getLogger().addHandler(new DebugLogHandler(plugin));
	}
	
	private final Plugin plugin;
	private final String pluginName;
	
	@Contract(pure = true)
	private DebugLogHandler(@NotNull Plugin plugin) {
		this.plugin = plugin;
		String prefix = plugin.getDescription().getPrefix();
		this.pluginName = prefix != null ? prefix : plugin.getDescription().getName();
	}
	
	@Override
	public void publish(@NotNull LogRecord record) {
		if (plugin.getLogger().getLevel().intValue() <= record.getLevel().intValue()
				&& record.getLevel().intValue() < Level.INFO.intValue()) {
			record.setLevel(Level.INFO);
			record.setMessage(String.format(DEFAULT_DEBUG_PREFIX_FORMAT,
					pluginName,
					DEFAULT_DEBUG_LOG_PREFIX,
					record.getMessage().substring(pluginName.length() + 3)));
		}
	}
	
	@Override
	public void flush() { }
	
	@Override
	public void close() { }
}
