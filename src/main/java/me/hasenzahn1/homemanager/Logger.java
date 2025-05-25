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

public enum Logger {

    INFO("[HomeManager] Info: "),
    SUCCESS("[HomeManager] Success: "),
    WARN("[HomeManager] Warn: "),
    ERROR("[HomeManager] Error: "),
    DEBUG("[HomeManager] Debug: ");

    private final String prefix;
    private final SimpleDateFormat formater;

    Logger(String prefix) {
        this.prefix = prefix;
        formater = new SimpleDateFormat("HH:mm:ss");
    }

    public void log(String msg) {
        if (this == DEBUG && !DefaultConfig.DEBUG_LOGGING) {
            return;
        }

        Bukkit.getConsoleSender().sendMessage(Component.text(prefix + msg));

        try {
            File file = new File(HomeManager.getInstance().getDataFolder(), "/logs/" + LocalDate.now() + ".txt");
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), formater.format(new Date()) + " | " + msg + System.lineSeparator(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage(Component.text(ERROR.prefix + "Error writing to log file " + e.getLocalizedMessage()));
        }
    }

    public void logException(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        log(sw.toString());
    }
}