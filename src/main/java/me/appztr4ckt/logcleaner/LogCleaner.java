package me.appztr4ckt.logcleaner;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;

public class LogCleaner extends JavaPlugin {

    private String mode;
    private int days;
    private int keepLogs;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mode = getConfig().getString("log-cleaning.mode", "days");
        days = getConfig().getInt("log-cleaning.days", 3);  // Standard: 3 Tage
        keepLogs = getConfig().getInt("log-cleaning.keep-logs", 3);  // Standard: 3 Logs

        getLogger().log(Level.INFO, "LogCleaner enabled! Mode: " + mode);
        cleanLogs();
        getServer().getScheduler().runTaskTimer(this, this::cleanLogs, 0L, 1728000L);
    }

    private void cleanLogs() {
        File logDir = new File(getServer().getWorldContainer(), "logs");
        if (!logDir.exists()) {
            getLogger().log(Level.WARNING, "Logs directory does not exist!");
            return;
        }

        File[] logFiles = logDir.listFiles((dir, name) -> name.endsWith(".log.gz") || name.equals("latest.log"));
        if (logFiles == null || logFiles.length == 0) {
            getLogger().log(Level.WARNING, "No log files found!");
            return;
        }

        Arrays.sort(logFiles, Comparator.comparingLong(File::lastModified).reversed());

        if ("days".equalsIgnoreCase(mode)) {
            cleanLogsByDays(logFiles);
        } else if ("amount".equalsIgnoreCase(mode)) {
            cleanLogsByAmount(logFiles);
        }
    }

    private void cleanLogsByDays(File[] logFiles) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        for (File logFile : logFiles) {
            if (logFile.getName().equals("latest.log")) {
                continue;
            }

            if (logFile.getName().endsWith(".log.gz") && logFile.lastModified() < cutoff.toEpochMilli()) {
                if (logFile.delete()) {
                    getLogger().log(Level.INFO, "Deleted log file (older than " + days + " days): " + logFile.getName());
                } else {
                    getLogger().log(Level.WARNING, "Failed to delete log file: " + logFile.getName());
                }
            }
        }
    }

    private void cleanLogsByAmount(File[] logFiles) {
        int logGzFilesKept = 0;
        for (File logFile : logFiles) {
            if (logFile.getName().equals("latest.log")) {
                continue;
            }

            if (logFile.getName().endsWith(".log.gz")) {
                logGzFilesKept++;
                if (logGzFilesKept > keepLogs) {
                    if (logFile.delete()) {
                        getLogger().log(Level.INFO, "Deleted log file (exceeded " + keepLogs + " files): " + logFile.getName());
                    } else {
                        getLogger().log(Level.WARNING, "Failed to delete log file: " + logFile.getName());
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "LogCleaner has been disabled!");
    }
}
