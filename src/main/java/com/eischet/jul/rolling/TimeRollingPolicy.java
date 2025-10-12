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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A rolling policy based on time. If the specified time has elapsed, {@link #needsRollover()}
 * returns <code>true</code>. The time policy can be set to either one of: daily, weekly, monthly or
 * yearly.
 *
 * @author Moran Avigdor (original author, from the GigaSpaces XAP project)
 * @author Stefan Eischet
 */
public class TimeRollingPolicy {

    public enum TimeBoundary {
        MINUTELY, HOURLY, DAILY;
    }

    private final TimeBoundary timeBoundary;
    private LocalDateTime nextRollover;

    public TimeRollingPolicy(TimeBoundary policy) {
        timeBoundary = policy;
    }

    /**
     * Sets the timestamp of the next rollover event.
     */
    void setTimestamp() {
        nextRollover = switch (timeBoundary) {
            case MINUTELY -> LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
            case HOURLY -> LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1);
            case DAILY -> LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).plusDays(1);
        };
        System.out.println("next log rollover: " + nextRollover);
    }

    /**
     * If it is time to rollover, calculate next rollover event and return true.
     *
     * @return <code>true</code> if the time has elapsed; <code>false</code> otherwise.
     */
    boolean needsRollover() {
        if (LocalDateTime.now().isAfter(nextRollover)) {
            System.out.println("time to roll over");
            setTimestamp();
            return true;
        } else {
            return false;
        }
    }
}