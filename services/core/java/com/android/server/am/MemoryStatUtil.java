/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.server.am;

import static com.android.server.am.ActivityManagerDebugConfig.TAG_AM;
import static com.android.server.am.ActivityManagerDebugConfig.TAG_WITH_CLASS_NAME;
import static com.android.server.wm.ActivityTaskManagerDebugConfig.DEBUG_METRICS;

import android.annotation.Nullable;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.system.Os;
import android.system.OsConstants;
import android.util.Slog;

import com.android.internal.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility methods related to {@link MemoryStat}.
 */
public final class MemoryStatUtil {
    static final int BYTES_IN_KILOBYTE = 1024;
    static final int PAGE_SIZE = 4096;
    static final long JIFFY_NANOS = 1_000_000_000 / Os.sysconf(OsConstants._SC_CLK_TCK);

    private static final String TAG = TAG_WITH_CLASS_NAME ? "MemoryStatUtil" : TAG_AM;

    /** True if device has per-app memcg */
    private static final boolean DEVICE_HAS_PER_APP_MEMCG =
            SystemProperties.getBoolean("ro.config.per_app_memcg", false);

    /** Path to memory stat file for logging app start memory state */
    private static final String MEMORY_STAT_FILE_FMT = "/dev/memcg/apps/uid_%d/pid_%d/memory.stat";
    /** Path to procfs stat file for logging app start memory state */
    private static final String PROC_STAT_FILE_FMT = "/proc/%d/stat";
    /** Path to procfs status file for logging app memory state */
    private static final String PROC_STATUS_FILE_FMT = "/proc/%d/status";
    /** Path to procfs cmdline file. Used with pid: /proc/pid/cmdline. */
    private static final String PROC_CMDLINE_FILE_FMT = "/proc/%d/cmdline";

    private static final Pattern PGFAULT = Pattern.compile("total_pgfault (\\d+)");
    private static final Pattern PGMAJFAULT = Pattern.compile("total_pgmajfault (\\d+)");
    private static final Pattern RSS_IN_BYTES = Pattern.compile("total_rss (\\d+)");
    private static final Pattern CACHE_IN_BYTES = Pattern.compile("total_cache (\\d+)");
    private static final Pattern SWAP_IN_BYTES = Pattern.compile("total_swap (\\d+)");

    private static final Pattern RSS_HIGH_WATERMARK_IN_BYTES =
            Pattern.compile("VmHWM:\\s*(\\d+)\\s*kB");

    private static final int PGFAULT_INDEX = 9;
    private static final int PGMAJFAULT_INDEX = 11;
    private static final int START_TIME_INDEX = 21;
    private static final int RSS_IN_PAGES_INDEX = 23;

    private MemoryStatUtil() {}

    /**
     * Reads memory stat for a process.
     *
     * Reads from per-app memcg if available on device, else fallback to procfs.
     * Returns null if no stats can be read.
     */
    @Nullable
    public static MemoryStat readMemoryStatFromFilesystem(int uid, int pid) {
        return hasMemcg() ? readMemoryStatFromMemcg(uid, pid) : readMemoryStatFromProcfs(pid);
    }

    /**
     * Reads memory.stat of a process from memcg.
     *
     * Returns null if file is not found in memcg or if file has unrecognized contents.
     */
    @Nullable
    static MemoryStat readMemoryStatFromMemcg(int uid, int pid) {
        final String statPath = String.format(Locale.US, MEMORY_STAT_FILE_FMT, uid, pid);
        return parseMemoryStatFromMemcg(readFileContents(statPath));
    }

    /**
     * Reads memory stat of a process from procfs.
     *
     * Returns null if file is not found in procfs or if file has unrecognized contents.
     */
    @Nullable
    public static MemoryStat readMemoryStatFromProcfs(int pid) {
        final String statPath = String.format(Locale.US, PROC_STAT_FILE_FMT, pid);
        return parseMemoryStatFromProcfs(readFileContents(statPath));
    }

    /**
     * Reads RSS high-water mark of a process from procfs. Returns value of the VmHWM field in
     * /proc/PID/status in bytes or 0 if not available.
     */
    public static long readRssHighWaterMarkFromProcfs(int pid) {
        final String statusPath = String.format(Locale.US, PROC_STATUS_FILE_FMT, pid);
        return parseVmHWMFromProcfs(readFileContents(statusPath));
    }

    /**
     * Reads cmdline of a process from procfs.
     *
     * Returns content of /proc/pid/cmdline (e.g. /system/bin/statsd) or an empty string
     * if the file is not available.
     */
    public static String readCmdlineFromProcfs(int pid) {
        String path = String.format(Locale.US, PROC_CMDLINE_FILE_FMT, pid);
        String cmdline = readFileContents(path);
        return cmdline != null ? cmdline : "";
    }

    private static String readFileContents(String path) {
        final File file = new File(path);
        if (!file.exists()) {
            if (DEBUG_METRICS) Slog.i(TAG, path + " not found");
            return null;
        }

        try {
            return FileUtils.readTextFile(file, 0 /* max */, null /* ellipsis */);
        } catch (IOException e) {
            Slog.e(TAG, "Failed to read file:", e);
            return null;
        }
    }

    /**
     * Parses relevant statistics out from the contents of a memory.stat file in memcg.
     */
    @VisibleForTesting
    @Nullable
    static MemoryStat parseMemoryStatFromMemcg(String memoryStatContents) {
        if (memoryStatContents == null || memoryStatContents.isEmpty()) {
            return null;
        }

        final MemoryStat memoryStat = new MemoryStat();
        Matcher m;
        m = PGFAULT.matcher(memoryStatContents);
        memoryStat.pgfault = m.find() ? Long.parseLong(m.group(1)) : 0;
        m = PGMAJFAULT.matcher(memoryStatContents);
        memoryStat.pgmajfault = m.find() ? Long.parseLong(m.group(1)) : 0;
        m = RSS_IN_BYTES.matcher(memoryStatContents);
        memoryStat.rssInBytes = m.find() ? Long.parseLong(m.group(1)) : 0;
        m = CACHE_IN_BYTES.matcher(memoryStatContents);
        memoryStat.cacheInBytes = m.find() ? Long.parseLong(m.group(1)) : 0;
        m = SWAP_IN_BYTES.matcher(memoryStatContents);
        memoryStat.swapInBytes = m.find() ? Long.parseLong(m.group(1)) : 0;
        return memoryStat;
    }

    /**
     * Parses relevant statistics out from the contents of the /proc/pid/stat file in procfs.
     */
    @VisibleForTesting
    @Nullable
    static MemoryStat parseMemoryStatFromProcfs(String procStatContents) {
        if (procStatContents == null || procStatContents.isEmpty()) {
            return null;
        }

        final String[] splits = procStatContents.split(" ");
        if (splits.length < 24) {
            return null;
        }

        try {
            final MemoryStat memoryStat = new MemoryStat();
            memoryStat.pgfault = Long.parseLong(splits[PGFAULT_INDEX]);
            memoryStat.pgmajfault = Long.parseLong(splits[PGMAJFAULT_INDEX]);
            memoryStat.rssInBytes = Long.parseLong(splits[RSS_IN_PAGES_INDEX]) * PAGE_SIZE;
            memoryStat.startTimeNanos = Long.parseLong(splits[START_TIME_INDEX]) * JIFFY_NANOS;
            return memoryStat;
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Failed to parse value", e);
            return null;
        }
    }

    /**
     * Parses RSS high watermark out from the contents of the /proc/pid/status file in procfs. The
     * returned value is in bytes.
     */
    @VisibleForTesting
    static long parseVmHWMFromProcfs(String procStatusContents) {
        if (procStatusContents == null || procStatusContents.isEmpty()) {
            return 0;
        }
        Matcher m = RSS_HIGH_WATERMARK_IN_BYTES.matcher(procStatusContents);
        // Convert value read from /proc/pid/status from kilobytes to bytes.
        return m.find() ? Long.parseLong(m.group(1)) * BYTES_IN_KILOBYTE : 0;
    }

    /**
     * Returns whether per-app memcg is available on device.
     */
    static boolean hasMemcg() {
        return DEVICE_HAS_PER_APP_MEMCG;
    }

    public static final class MemoryStat {
        /** Number of page faults */
        public long pgfault;
        /** Number of major page faults */
        public long pgmajfault;
        /** Number of bytes of anonymous and swap cache memory */
        public long rssInBytes;
        /** Number of bytes of page cache memory */
        public long cacheInBytes;
        /** Number of bytes of swap usage */
        public long swapInBytes;
        /** Device time when the processes started. */
        public long startTimeNanos;
    }
}
