package com.eischet.jul.rolling;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * A backup policy which renames the current log file to a new file, and optionally removes older files.
 *
 * @author Moran Avigdor (original author, from the GigaSpaces XAP project)
 * @author Stefan Eischet
 */
public class RenamingBackupPolicy implements BackupPolicy {

    private final DateTimeFormatter backupFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");
    private final String pattern;
    private final int keepFiles;
    private final Pattern regex;
    private final Consumer<File> remover;

    private File currentFile;
    private LocalDateTime currentStart;

    public RenamingBackupPolicy(final String pattern, final int keepFiles, final Consumer<File> remover) {
        this.pattern = pattern;
        this.keepFiles = keepFiles;
        this.regex = Pattern.compile(pattern.formatted(".+"));
        this.remover = remover != null ? remover : file -> {
            System.out.println("Removing old log file: " + file.getAbsolutePath());
            if (!file.delete()) {
                System.err.println("Could not delete old log file: " + file.getAbsolutePath());
            }
        };
    }

    public RenamingBackupPolicy(final String pattern, final int keepFiles) {
        this(pattern, keepFiles, null);
    }

    @Override
    public void rollOver(final File file) {
        System.out.println("RenamingBackupPolicy: tracking new file " + file.getAbsolutePath());
        if (currentFile != null) {
            final File newFile = new File(file.getParentFile(), pattern.formatted(backupFormat.format(currentStart)));
            System.out.println("RenamingBackupPolicy: renaming current file to " + newFile.getAbsolutePath());
            if (!currentFile.renameTo(newFile)) {
                System.err.printf("error renaming log file %s to %s!%n", currentFile.getAbsolutePath(), newFile.getAbsolutePath());
            }
            currentFile = file;
            currentStart = LocalDateTime.now();
            if (keepFiles > 0) {
                final File folder = file.getParentFile();
                if (folder != null) {
                    final File[] myFiles = folder.listFiles((dir, name) -> regex.matcher(name).matches());
                    if (myFiles != null) {
                        System.out.println("checking for stale log files, found " + myFiles.length);
                        Arrays.stream(myFiles)
                                .sorted(Comparator.comparing(File::getName).reversed())
                                .skip(keepFiles)
                                .forEach(remover);
                    }
                }
            }
        } else {
            System.out.println("RenamingBackupPolicy: starting with log file " + file.getAbsolutePath());
            currentFile = file;
            currentStart = LocalDateTime.now();
        }
    }
}
