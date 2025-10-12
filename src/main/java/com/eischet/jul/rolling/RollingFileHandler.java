/*
 * Copyright (c) 2008-2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.eischet.jul.rolling;

import java.io.File;
import java.io.IOException;
import java.util.logging.ErrorManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * A rolling file handler which uses a {@link SizeRollingPolicy} or a {@link TimeRollingPolicy} to
 * roll over log files.
 *
 * @author Moran Avigdor (original author, from the GigaSpaces XAP project)
 * @author Stefan Eischet
 */
public class RollingFileHandler extends StreamHandler {

    private final SizeRollingPolicy sizeRollingPolicy;
    private final TimeRollingPolicy timeRollingPolicy;
    private final BackupPolicy backupPolicy;
    private final File logFile;

    /* An output stream which could not be configured properly */
    private boolean corruptedOutputStream = false;

    public RollingFileHandler(final File folder,
                              final String filename,
                              final int sizeLimit,
                              final TimeRollingPolicy timeRollingPolicy,
                              final BackupPolicy backupPolicy) {
        super();
        this.sizeRollingPolicy = new SizeRollingPolicy(sizeLimit);
        this.timeRollingPolicy = timeRollingPolicy;
        this.backupPolicy = backupPolicy;

        this.logFile = new File(folder, filename);
        backupPolicy.rollOver(logFile); // initial rollover is required to initialize the backup policy

        configureOutputStream();
    }

    /**
     * Configure the output stream to use: 1. generate a filename 2. acquire a unique file (if a
     * filename by this name already exists) 3. open a stream 4. set the policies to monitor this
     * stream
     */
    private void configureOutputStream() {
        try {
            System.out.println("Logging to: " + logFile.getAbsolutePath());
            MeteredStream meteredStream = new MeteredStream(logFile);
            setOutputStream(meteredStream); //meteredStream is closed using #closeMeteredStream()
            sizeRollingPolicy.setMeteredStream(meteredStream);
            timeRollingPolicy.setTimestamp();
        } catch (IOException ioe) {
            reportError("Failed while configuring output file: " + logFile.getAbsolutePath(), ioe, ErrorManager.OPEN_FAILURE);
            corruptedOutputStream = true;
            if (sizeRollingPolicy != null) {
                sizeRollingPolicy.closeMeteredStream();
            }
        }
    }

    /**
     * Applies the policies before writing to the stream; re-configures the stream if policy is
     * triggered.
     *
     * @see StreamHandler#publish(LogRecord)
     */
    @Override
    public synchronized void publish(LogRecord record) {
        if (corruptedOutputStream) {
            return;
        }
        if (sizeRollingPolicy.hasReachedLimit() || timeRollingPolicy.needsRollover()) {
            sizeRollingPolicy.closeMeteredStream();
            backupPolicy.rollOver(logFile);
            configureOutputStream();
        }
        super.publish(record);
        super.flush();
    }

    @Override
    public synchronized void close() throws SecurityException {
        super.close();
        if (sizeRollingPolicy != null) {
            sizeRollingPolicy.closeMeteredStream();
        }
    }
}