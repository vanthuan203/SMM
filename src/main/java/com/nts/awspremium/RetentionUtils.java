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
