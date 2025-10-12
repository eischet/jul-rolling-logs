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

import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * A metered stream is a subclass of OutputStream that:
 *
 * <pre>
 * (a) forwards all its output to a target stream
 * (b) keeps track of how many bytes have been written
 * </pre>
 *
 * It is used to meter the limit of a log file.
 *
 * @author Moran Avigdor (original author, from the GigaSpaces XAP project)
 * @author Stefan Eischet
 */
class MeteredStream extends OutputStream {
    private final FileOutputStream fileOut;
    private final OutputStream out;
    private final File outputFile;
    public long written;
    private boolean closed;

    MeteredStream(final File outputFile) throws FileNotFoundException {
        this.outputFile = outputFile;
        this.written = outputFile.length();
        this.fileOut = new FileOutputStream(outputFile, true);
        this.out = new BufferedOutputStream(fileOut);
    }

    @Override
    public String toString() {
        return "MeteredStream{filename=" + outputFile.getAbsolutePath() + "}";
    }


    @Override
    public void write(int b) throws IOException {
        if (!closed) {
            out.write(b);
            written++;
        }
    }

    @Override
    public void write(byte @NotNull [] buff) throws IOException {
        if (!closed) {
            out.write(buff);
            written += buff.length;
        }
    }

    @Override
    public void write(byte @NotNull [] buff, int off, int len) throws IOException {
        if (!closed) {
            out.write(buff, off, len);
            written += len;
        }
    }

    @Override
    public void flush() throws IOException {
        if (!closed) {
            out.flush();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (!closed) {

            System.out.println("closing log stream of " + this);
            /* DEBUGGING
            try {
                throw new Exception();
            } catch (Exception e) {
                e.printStackTrace();
            }
             */

            fileOut.close();
            out.close();
            System.out.println("closed");
            closed = true;
        }
    }
}