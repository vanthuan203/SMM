package com.nts.awspremium;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RetentionUtils {
    private static double easeInOut(double t) {
        return t * t * (3 - 2 * t);
    }



    public static double getRetentionPercentUShape(
            int currentView,
            int totalView,
            double minPercent,   // ví dụ 0.5
            double maxPercent    // ví dụ 1.0
    ) {
        if (totalView <= 0) return maxPercent;

        // chuẩn hóa 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // Giảm cả min và max tới midpoint (giảm 40%)
        double midMin = minPercent * 0.6; // giảm 40% ở midpoint
        double midMax = maxPercent * 0.6;

        double currentMin, currentMax;

        if (x <= 0.5) {
            // từ 0 → 0.5 giảm từ minPercent/maxPercent xuống midMin/midMax
            double t = x / 0.5; // 0 → 1
            currentMin = minPercent - (minPercent - midMin) * t;
            currentMax = maxPercent - (maxPercent - midMax) * t;
        } else {
            // từ 0.5 → 1 tăng từ midMin/midMax lên lại minPercent/maxPercent
            double t = (x - 0.5) / 0.5; // 0 → 1
            currentMin = midMin + (minPercent - midMin) * t;
            currentMax = midMax + (maxPercent - midMax) * t;
        }

        // U-shape base percent (giống trước)
        double base;
        if (x <= 0.5) {
            double t = x / 0.5;
            base = currentMax - (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        } else {
            double t = (x - 0.5) / 0.5;
            base = currentMin + (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        }

        // 🔥 dao động ±10% nhưng KHÔNG phá shape
        double variation = (currentMax - currentMin) * 0.1;
        double percent;
        if (base >= currentMax) {
            percent = base - Math.random() * variation;
        } else if (base <= currentMin) {
            percent = base + Math.random() * variation;
        } else {
            percent = base + (Math.random() * 2 - 1) * variation;
        }

        // clamp
        percent = Math.max(currentMin, Math.min(currentMax, percent));

        return percent;
    }





    public static double getRetentionPercentV4Block(
            long orderId,
            int currentView,
            int totalView,
            double minPercent,
            double maxPercent,
            int currentThread,
            int maxThread
    ) {
        if (totalView <= 0) return maxPercent;

        // =========================
        // 🔥 AUTO SCALE BLOCK (CHỈ THÊM MỚI)
        // =========================
        int numBlocks = Math.max(2, Math.min(10, totalView / 1500));
        Random globalRand = new Random(orderId);

        int[] blockSizes = new int[numBlocks];
        int remaining = totalView;

        for (int i = 0; i < numBlocks; i++) {
            int blocksLeft = numBlocks - i;
            int avg = remaining / blocksLeft;

            int min = (int) (avg * 0.7);
            int max = (int) (avg * 1.3);

            int size;
            if (i == numBlocks - 1) {
                size = remaining;
            } else {
                size = min + globalRand.nextInt(Math.max(1, max - min));
            }

            blockSizes[i] = size;
            //System.out.println(blockSizes[i]);
            remaining -= size;
        }

        // =========================
        // 🔥 MAP currentView → LOCAL VIEW
        // =========================
        int tempView = currentView;
        int block = 0;

        while (block < numBlocks - 1 && tempView >= blockSizes[block]) {
            tempView -= blockSizes[block];
            block++;
        }

        int localCurrent = tempView;
        int localTotal = blockSizes[block];
        //System.out.println("Local Tottal" + localTotal+ " Local Current "+localCurrent);

        // ⚠️ QUAN TRỌNG: thay currentView + totalView bằng LOCAL
        currentView = localCurrent;
        totalView = localTotal;

        // =========================
        // 🔹 TỪ ĐÂY GIỮ NGUYÊN LOGIC CŨ
        // =========================

        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        double dropFactor = (x <= 0.5)
                ? (1.0 - 0.4 * (x / 0.5))
                : 0.6;

        double currentMin = Math.max(0.05, minPercent * dropFactor);
        double currentMax = Math.max(currentMin, maxPercent * dropFactor);

        double base;
        if (x <= 0.5) {
            double t = x / 0.5;
            base = currentMax - (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        } else {
            double t = (x - 0.5) / 0.5;
            base = currentMin + (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        }

        Random rand = new Random(orderId * 31 + block);

        if (x < 0.05) {
            base = Math.max(base, currentMax * (0.85 + rand.nextDouble() * 0.15));
        }

        double noise;
        if (x < 0.2) {
            noise = -Math.pow(rand.nextDouble(), 2) * 0.25;
        } else if (x < 0.8) {
            noise = (rand.nextDouble() - 0.5) * 0.1;
        } else {
            if (rand.nextDouble() < 0.5) {
                noise = Math.pow(rand.nextDouble(), 2) * 0.25;
            } else {
                noise = -Math.pow(rand.nextDouble(), 2) * 0.25;
            }
        }

        double variation = (maxPercent - minPercent) * 0.1;
        double percent = base + noise * variation;

        // =========================
        // 🔥 THREAD (GIỮ NGUYÊN)
        // =========================
        if (maxThread > 1) {
            double threadRatio = (double) currentThread / maxThread;

            double timeScale = 1.0 - 0.3 * Math.pow(threadRatio, 0.8);
            double threadRand = 0.9 + rand.nextDouble() * 0.2;

            percent *= timeScale * threadRand;

            if (x > 0.2 && x < 0.6) {
                percent *= (1.0 - 0.2 * threadRatio);
            }

            if (x < 0.05) {
                percent /= timeScale;
            }
        }

        // =========================
        // 🔹 CLAMP
        // =========================
        percent = Math.max(currentMin, Math.min(currentMax * 1.2, percent));
        percent = Math.max(percent, 0.05);

        // =========================
        // 🔥 JITTER ±10%
        // =========================
        double jitter = percent * 0.1;
        percent += (Math.random() - 0.5) * 2 * jitter;

        percent = Math.max(percent, 0.05);

        return percent;
    }

    public static double getRetentionPercentV4Block(
            long orderId,
            int currentView,
            int totalView,
            double minPercent,
            double maxPercent
    ) {
        if (totalView <= 0) return maxPercent;

        int numBlocks = Math.max(2, Math.min(10, totalView / 1500));
        Random globalRand = new Random(orderId);

        int[] blockSizes = new int[numBlocks];
        int remaining = totalView;

        for (int i = 0; i < numBlocks; i++) {
            int blocksLeft = numBlocks - i;
            int avg = remaining / blocksLeft;

            int min = (int) (avg * 0.7);
            int max = (int) (avg * 1.3);

            int size;
            if (i == numBlocks - 1) {
                size = remaining;
            } else {
                size = min + globalRand.nextInt(Math.max(1, max - min));
            }

            blockSizes[i] = size;
            remaining -= size;
        }

        int tempView = currentView;
        int block = 0;

        while (block < numBlocks - 1 && tempView >= blockSizes[block]) {
            tempView -= blockSizes[block];
            block++;
        }

        int localCurrent = tempView;
        int localTotal = blockSizes[block];

        currentView = localCurrent;
        totalView = localTotal;

        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        double dropFactor = (x <= 0.5)
                ? (1.0 - 0.4 * (x / 0.5))
                : 0.6;

        double currentMin = Math.max(0.05, minPercent * dropFactor);
        double currentMax = Math.max(currentMin, maxPercent * dropFactor);

        double base;
        if (x <= 0.5) {
            double t = x / 0.5;
            base = currentMax - (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        } else {
            double t = (x - 0.5) / 0.5;
            base = currentMin + (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        }

        Random rand = new Random(orderId * 31 + block);

        if (x < 0.05) {
            base = Math.max(base, currentMax * (0.85 + rand.nextDouble() * 0.15));
        }

        double noise;
        if (x < 0.2) {
            noise = -Math.pow(rand.nextDouble(), 2) * 0.25;
        } else if (x < 0.8) {
            noise = (rand.nextDouble() - 0.5) * 0.1;
        } else {
            if (rand.nextDouble() < 0.5) {
                noise = Math.pow(rand.nextDouble(), 2) * 0.25;
            } else {
                noise = -Math.pow(rand.nextDouble(), 2) * 0.25;
            }
        }

        double variation = (maxPercent - minPercent) * 0.1;
        double percent = base + noise * variation;

        // ❌ ĐÃ XÓA THREAD EFFECT

        percent = Math.max(currentMin, Math.min(currentMax * 1.2, percent));
        percent = Math.max(percent, 0.05);

        double jitter = percent * 0.1;
        percent += (Math.random() - 0.5) * 2 * jitter;

        return Math.max(percent, 0.05);
    }

    public static double getRetentionPercentUltraHumanV2(
            long orderId,
            int currentView,
            int totalView,
            double minPercent,
            double maxPercent
    ) {
        if (totalView <= 0) return maxPercent;

        // ===== PROGRESS =====
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        Random liveRand = new Random(orderId + currentView * 131);

        double percent;

        // =========================
        // PHASE 1: HOOK (0 → 5%)
        // =========================
        if (x < 0.05) {
            percent = maxPercent * (0.9 + liveRand.nextDouble() * 0.3);
        }

        // =========================
        // PHASE 2: DROP (5 → 20%)
        // =========================
        else if (x < 0.2) {
            double t = (x - 0.05) / 0.15;
            percent = maxPercent * (0.85 - 0.5 * t) * (0.9 + liveRand.nextDouble() * 0.2);
        }

        // =========================
        // PHASE 3: MID (20 → 60%)
        // =========================
        else if (x < 0.6) {
            double t = (x - 0.2) / 0.4;

            double decay = 0.45 - 0.35 * Math.pow(t, 0.6);
            decay *= (0.7 + liveRand.nextDouble() * 0.6);

            percent = maxPercent * decay;
        }

        // =========================
        // PHASE 4: LATE (60 → 85%)
        // =========================
        else if (x < 0.85) {
            double dynamic = 0.15 + 0.25 * (1 - x);
            percent = maxPercent * (dynamic + liveRand.nextDouble() * 0.2);
        }

        // =========================
        // PHASE 5: END (85 → 100%)
        // =========================
        else {
            percent = maxPercent * (0.2 + liveRand.nextDouble() * 0.4);
        }

        // =========================
        // 🔥 WAVE (phá cứng theo view)
        // =========================
        percent += Math.sin(currentView * 0.05 + orderId) * 0.1;

        // =========================
        // 🔥 BREAK DEAD ZONE (0.3–0.4)
        // =========================
        if (percent > 0.3 && percent < 0.4) {
            percent += (liveRand.nextDouble() - 0.5) * 0.3;
        }

        // ❌ ĐÃ LOẠI BỎ THREAD EFFECT

        // =========================
        // 🔹 CLAMP MỀM
        // =========================
        double floor = Math.max(0.05, minPercent * 0.7);
        double ceil = maxPercent * 1.3;

        percent = Math.max(floor, Math.min(ceil, percent));

        // =========================
        // 🔥 JITTER ±10%
        // =========================
        percent += percent * (Math.random() - 0.5) * 0.2;

        return Math.max(0.05, percent);
    }

    public static int getSpeedLevelOFF(int currentView, int totalView,
                                     int minThread, int maxThread) {

        if (totalView <= 0) return minThread;

        // 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // U-shape NGƯỢC với percent
        double base;
        if (x <= 0.5) {
            double t = x / 0.5;
            base = minThread + (t * t * (3 - 2 * t)) * (maxThread - minThread);
        } else {
            double t = (x - 0.5) / 0.5;
            base = maxThread - (t * t * (3 - 2 * t)) * (maxThread - minThread);
        }

        // 🔥 dao động ±10% giống hệt percent
        double variation = (maxThread - minThread) * 0.1;

        double thread;

        if (base >= maxThread) {
            // chỉ giảm
            thread = base - Math.random() * variation;
        } else if (base <= minThread) {
            // chỉ tăng
            thread = base + Math.random() * variation;
        } else {
            // dao động quanh base
            thread = base + (Math.random() * 2 - 1) * variation;
        }

        // clamp
        thread = Math.max(minThread, Math.min(maxThread, thread));

        return (int) Math.round(thread);
    }

    public static class ThreadResult {
        public int thread;
        public int momentum;

        public ThreadResult(int thread, int momentum) {
            this.thread = thread;
            this.momentum = momentum;
        }
    }

    public static ThreadResult getSpeedLevelOFF(
            long orderId,
            int currentView,
            int totalView,
            int minThread,
            int maxThread,
            int currentThread,
            int momentum
    ) {
        if (totalView <= 0) {
            return new ThreadResult(minThread, 0);
        }

        // =========================
        // 🔥 COPY Y NGUYÊN BLOCK LOGIC
        // =========================
        int numBlocks = Math.max(2, Math.min(10, totalView / 1500));
        Random globalRand = new Random(orderId);

        int[] blockSizes = new int[numBlocks];
        int remaining = totalView;

        for (int i = 0; i < numBlocks; i++) {
            int blocksLeft = numBlocks - i;
            int avg = remaining / blocksLeft;

            int min = (int) (avg * 0.7);
            int max = (int) (avg * 1.3);

            int size;
            if (i == numBlocks - 1) {
                size = remaining;
            } else {
                size = min + globalRand.nextInt(Math.max(1, max - min));
            }

            blockSizes[i] = size;
            remaining -= size;
        }

        int tempView = currentView;
        int block = 0;

        while (block < numBlocks - 1 && tempView >= blockSizes[block]) {
            tempView -= blockSizes[block];
            block++;
        }

        int localCurrent = tempView;
        int localTotal = blockSizes[block];

        // ⚠️ thay bằng local (GIỐNG HÀM PERCENT)
        currentView = localCurrent;
        totalView = localTotal;

        // =========================
        // 🔻 GIỮ NGUYÊN LOGIC THREAD CỦA BẠN
        // =========================

        // 1️⃣ Progress
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // 2️⃣ U-shape
        double t = (x <= 0.5) ? (x / 0.5) : ((x - 0.5) / 0.5);
        double smooth = t * t * (3 - 2 * t);

        double base = (x <= 0.5)
                ? minThread + smooth * (maxThread - minThread)
                : maxThread - smooth * (maxThread - minThread);

        int target = (int) Math.round(base);

        // 3️⃣ Random theo block (QUAN TRỌNG)
        long seed = 31 * orderId + block;
        Random rand = new Random(seed);

        // 4️⃣ Momentum
        int delta = target - currentThread;
        if (delta > 0) momentum += 1;
        else if (delta < 0) momentum -= 1;

        // 5️⃣ Random ±1
        if (rand.nextBoolean()) {
            momentum += (delta >= 0) ? 1 : -1;
        }

        // 6️⃣ Clamp momentum
        momentum = Math.max(-5, Math.min(5, momentum));

        // 7️⃣ Jump
        int threshold = 2;
        if (momentum >= threshold && currentThread < maxThread) {
            currentThread += 1;
            momentum = 0;
        } else if (momentum <= -threshold && currentThread > minThread) {
            currentThread -= 1;
            momentum = 0;
        }

        // 8️⃣ Force về target
        if (Math.abs(target - currentThread) > 1) {
            currentThread += (target > currentThread) ? 1 : -1;
            momentum = 0;
        }

        // 9️⃣ Clamp
        currentThread = Math.max(minThread, Math.min(maxThread, currentThread));

        return new ThreadResult(currentThread, momentum);
    }

    public static ThreadResult getSpeedLevel(
            long orderId,
            int currentView,
            int totalView,
            int minThread,
            int maxThread,
            int currentThread,
            int momentum
    ) {
        if (totalView <= 0) {
            return new ThreadResult(minThread, 0);
        }

        int numBlocks = Math.max(2, Math.min(10, totalView / 1500));
        Random globalRand = new Random(orderId);

        int[] blockSizes = new int[numBlocks];
        int remaining = totalView;

        for (int i = 0; i < numBlocks; i++) {
            int blocksLeft = numBlocks - i;
            int avg = remaining / blocksLeft;

            int min = (int) (avg * 0.7);
            int max = (int) (avg * 1.3);

            int size;
            if (i == numBlocks - 1) {
                size = remaining;
            } else {
                size = min + globalRand.nextInt(Math.max(1, max - min));
            }

            blockSizes[i] = size;
            System.out.println(blockSizes[i]);
            remaining -= size;
        }

        int tempView = currentView;
        int blocks = 0;

        while (blocks < numBlocks - 1 && tempView >= blockSizes[blocks]) {
            tempView -= blockSizes[blocks];
            blocks++;
        }

        int localCurrent = tempView;
        int localTotal = blockSizes[blocks];

        // ⚠️ thay bằng local (GIỐNG HÀM PERCENT)
        currentView = localCurrent;
        totalView = localTotal;
        System.out.println("Local Tottal" + localTotal+ " Local Current "+localCurrent);
        // =========================
        // 🔻 GIỮ NGUYÊN LOGIC THREAD CỦA BẠN
        // =========================


        // 1️⃣ Chuẩn hóa tiến độ 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // 2️⃣ Smooth U-shape (giống percent)
        double t = (x <= 0.5) ? (x / 0.5) : ((x - 0.5) / 0.5);
        double smooth = t * t * (3 - 2 * t);

        double base = (x <= 0.5)
                ? minThread + smooth * (maxThread - minThread)
                : maxThread - smooth * (maxThread - minThread);

        int target = (int) Math.round(base);

        // 3️⃣ Seed deterministic theo block (chia ~100 block)
        int blockSize = Math.max(1, totalView / 100);
        int block = currentView / blockSize;
        long seed = 31 * orderId + block;
        Random rand = new Random(seed);

        // 4️⃣ Cập nhật momentum hướng về target
        int delta = target - currentThread;
        if (delta > 0) momentum += 1;
        else if (delta < 0) momentum -= 1;

        // 5️⃣ Random nhẹ ±1 deterministic
        if (rand.nextBoolean()) {
            momentum += (delta >= 0) ? 1 : -1;
        }

        // 6️⃣ Clamp momentum ±5
        momentum = Math.max(-5, Math.min(5, momentum));

        // 7️⃣ Nhảy thread nếu momentum vượt threshold
        int threshold = 2; // bạn có thể điều chỉnh cho mượt hơn
        if (momentum >= threshold && currentThread < maxThread) {
            currentThread += 1;
            momentum = 0; // reset sau khi nhảy
        } else if (momentum <= -threshold && currentThread > minThread) {
            currentThread -= 1;
            momentum = 0;
        }

        // 8️⃣ Nếu khoảng cách lớn, force thread tiến dần về target
        if (Math.abs(target - currentThread) > 1) {
            currentThread += (target > currentThread) ? 1 : -1;
            momentum = 0; // reset momentum
        }

        // 9️⃣ Clamp thread
        currentThread = Math.max(minThread, Math.min(maxThread, currentThread));

        return new ThreadResult(currentThread, momentum);
    }

}
