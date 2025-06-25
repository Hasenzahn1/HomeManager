package me.hasenzahn1.homemanager;

import me.hasenzahn1.homemanager.config.DefaultConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Enum for logging messages with different log levels such as INFO, SUCCESS, WARN, ERROR, and DEBUG.
 * Each log level has a specific prefix and logs messages to both the console and a log file.
 */
public enum Logger {

    INFO("[HomeManager] Info: "),
    SUCCESS("[HomeManager] Success: "),
    WARN("[HomeManager] Warn: "),
    ERROR("[HomeManager] Error: "),
    DEBUG("[HomeManager] Debug: ");

    private final String prefix;
    private final SimpleDateFormat formater;

    /**
     * Constructor for Logger enum. Initializes the prefix for each log level and the date format for timestamps.
     *
     * @param prefix the prefix to be used in log messages for this log level.
     */
    Logger(String prefix) {
        this.prefix = prefix;
        formater = new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * Logs a message with the appropriate log level prefix.
     * <p>
     * The message is sent to the console and appended to a log file in the plugin's data folder.
     * If the log level is DEBUG, the message will only be logged if the debug logging is enabled.
     *
     * @param msg the message to log
     */
    public void log(String msg) {
        // Skip logging DEBUG messages if debugging is disabled
        if (this == DEBUG && !DefaultConfig.DEBUG_LOGGING) {
            return;
        }

        // Log to the console
        Bukkit.getConsoleSender().sendMessage(Component.text(prefix + msg));

        // Log to a file
        try {
            File file = new File(HomeManager.getInstance().getDataFolder(), "/logs/" + LocalDate.now() + ".txt");
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), formater.format(new Date()) + " | " + msg + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Handle error while writing to log file
            Bukkit.getConsoleSender().sendMessage(Component.text(ERROR.prefix + "Error writing to log file " + e.getLocalizedMessage()));
        }
    }

    /**
     * Logs the details of an exception.
     * <p>
     * This method logs the stack trace of the given exception as a string.
     *
     * @param e the exception to log
     */
    public void logException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        log(sw.toString());
    }
}
