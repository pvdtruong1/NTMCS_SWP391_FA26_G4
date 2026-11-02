package dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import model.SLAConfig;

/**
 * Stores SLA settings outside the deployed WAR so they survive redeployment
 * and Tomcat restarts without changing the 19-table NTMCS database schema.
 */
public class SLAConfigDAO {
    private static final String FILE_NAME = "ntmcs-sla.properties";
    private static final Object LOCK = new Object();

    public SLAConfig getConfig() throws IOException {
        synchronized (LOCK) {
            Path file = getConfigPath();
            if (!Files.exists(file)) return new SLAConfig();

            Properties properties = new Properties();
            try (InputStream input = Files.newInputStream(file)) {
                properties.load(input);
            }

            SLAConfig config = new SLAConfig(
                Boolean.parseBoolean(properties.getProperty("inAppEnabled", "true")),
                parseInt(properties, "thresholdHours1", 24),
                parseInt(properties, "thresholdHours2", 12),
                parseInt(properties, "thresholdHours3", 2),
                properties.getProperty("earlyWarningTemplate", new SLAConfig().getEarlyWarningTemplate()),
                properties.getProperty("warningTemplate", new SLAConfig().getWarningTemplate()),
                properties.getProperty("urgentTemplate", new SLAConfig().getUrgentTemplate()),
                properties.getProperty("nudgeTemplate", new SLAConfig().getNudgeTemplate())
            );
            config.validate();
            return config;
        }
    }

    public void saveConfig(SLAConfig config) throws IOException {
        if (config == null) throw new IllegalArgumentException("Thiếu cấu hình SLA.");
        config.validate();

        synchronized (LOCK) {
            Path file = getConfigPath();
            Path directory = file.getParent();
            Files.createDirectories(directory);

            Properties properties = new Properties();
            properties.setProperty("inAppEnabled", String.valueOf(config.isInAppEnabled()));
            properties.setProperty("thresholdHours1", String.valueOf(config.getThresholdHours1()));
            properties.setProperty("thresholdHours2", String.valueOf(config.getThresholdHours2()));
            properties.setProperty("thresholdHours3", String.valueOf(config.getThresholdHours3()));
            properties.setProperty("earlyWarningTemplate", config.getEarlyWarningTemplate());
            properties.setProperty("warningTemplate", config.getWarningTemplate());
            properties.setProperty("urgentTemplate", config.getUrgentTemplate());
            properties.setProperty("nudgeTemplate", config.getNudgeTemplate());

            Path temporary = Files.createTempFile(directory, "ntmcs-sla-", ".tmp");
            try {
                try (OutputStream output = Files.newOutputStream(temporary)) {
                    properties.store(output, "NTMCS WF-04 SCR-4.6 SLA configuration");
                }
                try {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING,
                               StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        }
    }

    private int parseInt(Properties properties, String key, int defaultValue) throws IOException {
        String value = properties.getProperty(key);
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IOException("Giá trị cấu hình SLA không hợp lệ: " + key, e);
        }
    }

    private Path getConfigPath() {
        String catalinaBase = System.getProperty("catalina.base");
        Path root = (catalinaBase != null && !catalinaBase.trim().isEmpty())
            ? Paths.get(catalinaBase)
            : Paths.get(System.getProperty("user.home"), ".ntmcs");
        return root.resolve("data").resolve(FILE_NAME);
    }
}
