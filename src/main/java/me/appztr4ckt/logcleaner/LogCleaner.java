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
    private boolean deleteCrashReports;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mode = getConfig().getString("log-cleaning.mode", "days");
        days = getConfig().getInt("log-cleaning.days", 3);
        keepLogs = getConfig().getInt("log-cleaning.keep-logs", 3);
        deleteCrashReports = getConfig().getBoolean("log-cleaning.delete-crash-reports", true); // Read new config

        getLogger().log(Level.INFO, "LogCleaner enabled! Mode: " + mode);
        cleanLogs();
        getServer().getScheduler().runTaskTimer(this, this::cleanLogs, 0L, 1728000L);
    }

    private void cleanLogs() {
        cleanDirectory(new File(getServer().getWorldContainer(), "logs"));

        if (deleteCrashReports) {
            cleanDirectory(new File(getServer().getWorldContainer(), "crash-reports"));
        }
    }

    private void cleanDirectory(File directory) {
        if (!directory.exists()) {
            getLogger().log(Level.WARNING, "Directory does not exist: " + directory.getName());
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".log.gz") || name.equals("latest.log") || name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            getLogger().log(Level.WARNING, "No files found in directory: " + directory.getName());
            return;
        }

        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());

        if ("days".equalsIgnoreCase(mode)) {
            cleanFilesByDays(files);
        } else if ("amount".equalsIgnoreCase(mode)) {
            cleanFilesByAmount(files);
        }
    }

    private void cleanFilesByDays(File[] files) {
        Instant cutoff = Instant.now().minus(days, ChronoUnit.DAYS);
        for (File file : files) {
            if (file.getName().equals("latest.log")) {
                continue;
            }

            if (file.lastModified() < cutoff.toEpochMilli()) {
                if (file.delete()) {
                    getLogger().log(Level.INFO, "Deleted file (older than " + days + " days): " + file.getName());
                } else {
                    getLogger().log(Level.WARNING, "Failed to delete file: " + file.getName());
                }
            }
        }
    }

    private void cleanFilesByAmount(File[] files) {
        int filesKept = 0;
        for (File file : files) {
            if (file.getName().equals("latest.log")) {
                continue;
            }

            filesKept++;
            if (filesKept > keepLogs) {
                if (file.delete()) {
                    getLogger().log(Level.INFO, "Deleted file (exceeded " + keepLogs + " files): " + file.getName());
                } else {
                    getLogger().log(Level.WARNING, "Failed to delete file: " + file.getName());
                }
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "LogCleaner has been disabled!");
    }
}
