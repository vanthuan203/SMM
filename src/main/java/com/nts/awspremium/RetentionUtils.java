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

    public static double getRetentionPercentOFF(int currentView, int totalView,
                                             double minPercent, double maxPercent) {

        if (totalView <= 0) return maxPercent;

        // chuẩn hóa 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        double percent;

        if (x <= 0.5) {
            // giảm: max → min
            double t = x / 0.5;
            percent = maxPercent - easeInOut(t) * (maxPercent - minPercent);
        } else {
            // tăng: min → max
            double t = (x - 0.5) / 0.5;
            percent = minPercent + easeInOut(t) * (maxPercent - minPercent);
        }

        // 🔥 random bias (thiên về cao)
        double randomFactor = 0.6 + Math.pow(Math.random(), 0.7) * 0.4;
        percent *= randomFactor;

        // 🔥 noise nhẹ
        percent += (Math.random() - 0.5) * 0.05;

        // clamp lại
        percent = Math.max(minPercent, Math.min(maxPercent, percent));

        return percent;
    }




    public static double getRetentionPercent(int currentView, int totalView,
                                             double minPercent, double maxPercent) {

        if (totalView <= 0) return maxPercent;

        // 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // U-shape base
        double base;
        if (x <= 0.5) {
            double t = x / 0.5;
            base = maxPercent - (t * t * (3 - 2 * t)) * (maxPercent - minPercent);
        } else {
            double t = (x - 0.5) / 0.5;
            base = minPercent + (t * t * (3 - 2 * t)) * (maxPercent - minPercent);
        }

        // 🔥 dao động ±10% nhưng KHÔNG phá shape
        double variation = (maxPercent - minPercent) * 0.1;

        double percent;

        if (base >= maxPercent) {
            // chỉ giảm
            percent = base - Math.random() * variation;
        } else if (base <= minPercent) {
            // chỉ tăng
            percent = base + Math.random() * variation;
        } else {
            // dao động quanh base
            percent = base + (Math.random() * 2 - 1) * variation;
        }

        // clamp
        percent = Math.max(minPercent, Math.min(maxPercent, percent));

        return percent;
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


    public static double getRetentionPercentV4(
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
        // 🔹 Progress
        // =========================
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // =========================
        // 🔹 Random theo ORDER + BLOCK (tránh bị lặp)
        // =========================
        int blockSize = Math.max(1, totalView / 50);
        int block = currentView / blockSize;
        Random rand = new Random(orderId * 31 + block);

        // =========================
        // 🔹 Giảm dần theo progress (REAL)
        // =========================
        double decay = 1.0 - 0.5 * Math.pow(x, 0.7); // mượt hơn tuyến tính

        double currentMin = Math.max(0.05, minPercent * decay);
        double currentMax = Math.max(currentMin, maxPercent * decay);

        // =========================
        // 🔹 Base curve (U-shape nhẹ)
        // =========================
        double base;
        if (x < 0.5) {
            double t = x / 0.5;
            base = currentMax - (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        } else {
            double t = (x - 0.5) / 0.5;
            base = currentMin + (t * t * (3 - 2 * t)) * (currentMax - currentMin);
        }

        // =========================
        // 🔥 HOOK đầu video (rất quan trọng)
        // =========================
        if (x < 0.05) {
            base = Math.max(base, currentMax * (0.85 + rand.nextDouble() * 0.15));
        }

        // =========================
        // 🔥 Noise hành vi người
        // =========================
        double noise;
        if (x < 0.2) {
            noise = -Math.pow(rand.nextDouble(), 2) * 0.2;
        } else if (x < 0.8) {
            noise = (rand.nextDouble() - 0.5) * 0.15;
        } else {
            noise = (rand.nextBoolean() ? 1 : -1) * Math.pow(rand.nextDouble(), 2) * 0.25;
        }

        double percent = base + noise * (currentMax - currentMin);

        // =========================
        // 🔥 Spike (real user hay có)
        // =========================
        if (rand.nextDouble() < 0.05) {
            percent *= (1.05 + rand.nextDouble() * 0.15);
        }

        // =========================
        // 🔥 Rewatch cuối video
        // =========================
        if (x > 0.85 && rand.nextDouble() < 0.1) {
            percent *= (1.05 + rand.nextDouble() * 0.25);
        }

        // =========================
        // 🔥 THREAD SCALE (QUAN TRỌNG)
        // =========================
        if (maxThread > 1) {
            double threadRatio = (double) currentThread / maxThread;

            // scale thời gian xem
            double timeScale = 1.0 - 0.3 * Math.pow(threadRatio, 0.8);

            // random mỗi user
            double threadRand = 0.9 + rand.nextDouble() * 0.2;

            percent *= timeScale * threadRand;

            // mid drop mạnh hơn
            if (x > 0.2 && x < 0.6) {
                percent *= (1.0 - 0.2 * threadRatio);
            }

            // giữ hook đầu
            if (x < 0.05) {
                percent /= timeScale;
            }
        }

        // =========================
        // 🔹 Clamp mềm (trước jitter)
        // =========================
        percent = Math.max(currentMin, Math.min(currentMax * 1.3, percent));

        // =========================
        // 🔥 JITTER ±10% (KHÔNG bị cứng)
        // =========================
        double jitter = percent * 0.1;
        percent += (Math.random() - 0.5) * 2 * jitter;

        // =========================
        // 🔹 Final clamp nhẹ
        // =========================
        percent = Math.max(0.05, percent);

        return percent;
    }

    public static double getRetentionPercentUltraHumanV2(
            long orderId,
            int currentView,
            int totalView,
            double minPercent,
            double maxPercent,
            int currentThread,
            int maxThread
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
        // PHASE 3: MID (20 → 60%) ❗ FIX CHÍNH
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

        // =========================
        // 🔥 THREAD EFFECT
        // =========================
        if (maxThread > 1 && x > 0.1) {
            double threadRatio = (double) currentThread / maxThread;
            percent *= (1.0 - 0.08 * threadRatio);
        }

        // =========================
        // 🔹 CLAMP MỀM
        // =========================
        double floor = Math.max(0.05, minPercent * 0.7);
        double ceil = maxPercent * 1.3;

        percent = Math.max(floor, Math.min(ceil, percent));

        // =========================
        // 🔥 JITTER ±10% (QUAN TRỌNG)
        // =========================
        percent += percent * (Math.random() - 0.5) * 0.2;

        // hard floor
        percent = Math.max(0.05, percent);

        return percent;
    }

    public static double getRetentionPercentUltraHuman(
            long orderId,
            int currentView,
            int totalView,
            double minPercent,
            double maxPercent,
            int currentThread,
            int maxThread
    ) {
        if (totalView <= 0) return maxPercent;

        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // 🔥 CLUSTER (giữ hành vi)
        int blockSize = Math.max(1, totalView / 70);
        int block = currentView / blockSize;
        Random rand = new Random(31 * orderId + block);

        double percent;

        // =========================
        // 🔥 PHASE 1: HOOK (0–3%)
        // =========================
        if (x < 0.03) {
            percent = maxPercent * (0.95 + rand.nextDouble() * 0.3); // 0.95 → 1.25
        }

        // =========================
        // 🔥 PHASE 2: DROP NHANH (3–15%)
        // =========================
        else if (x < 0.15) {
            double t = (x - 0.03) / 0.12;
            percent = maxPercent * (1.0 - 0.6 * t); // drop mạnh
        }

        // =========================
        // 🔥 PHASE 3: DECAY CHẬM (15–60%)
        // =========================
        else if (x < 0.6) {
            double t = (x - 0.15) / 0.45;
            percent = maxPercent * (0.4 - 0.2 * t); // giảm từ 0.4 → 0.2
        }

        // =========================
        // 🔥 PHASE 4: ỔN ĐỊNH (60–85%)
        // =========================
        else if (x < 0.85) {
            percent = maxPercent * (0.2 + rand.nextDouble() * 0.05);
        }

        // =========================
        // 🔥 PHASE 5: END SPIKE (85–100%)
        // =========================
        else {
            percent = maxPercent * (0.2 + rand.nextDouble() * 0.3);
        }

        // 🔥 MICRO BEHAVIOR (quan trọng)
        // spike nhỏ ngẫu nhiên giống user tua / chú ý
        if (rand.nextDouble() < 0.08) {
            percent *= (1.05 + rand.nextDouble() * 0.25);
        }

        // 🔥 NOISE theo phase (đã cân bằng)
        double noise = (rand.nextDouble() - 0.5) * 0.15;
        percent += noise * (maxPercent - minPercent);

        // 🔥 THREAD (giảm nhẹ thôi)
        if (maxThread > 1 && x > 0.1) {
            double threadRatio = (double) currentThread / maxThread;
            percent *= (1.0 - 0.08 * threadRatio);
        }

        // 🔥 GLOBAL JITTER (phá pattern)
        percent += (Math.random() - 0.5) * 0.2;

        // 🔥 CLAMP thông minh
        double floor = Math.max(0.05, minPercent * 0.8);
        double ceil = maxPercent * 1.3;

        percent = Math.max(floor, Math.min(ceil, percent));

        return percent;
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
