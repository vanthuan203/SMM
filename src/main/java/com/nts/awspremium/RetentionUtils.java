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
        if (totalView <= 0) return new ThreadResult(minThread, 0);

        // 1️⃣ Chuẩn hóa progress 0 → 1
        double x = (double) currentView / totalView;
        x = Math.max(0, Math.min(1, x));

        // 2️⃣ Smooth U-shape
        double t = (x <= 0.5) ? (x / 0.5) : ((x - 0.5) / 0.5);
        double smooth = t * t * (3 - 2 * t);

        double base = (x <= 0.5)
                ? minThread + smooth * (maxThread - minThread)
                : maxThread - smooth * (maxThread - minThread);

        int target = (int) Math.round(base);

        // 3️⃣ Block nhỏ để random mượt
        int blockSize = Math.max(1, totalView / 200);
        int block = currentView / blockSize;

        // 4️⃣ Random deterministic ±1 theo orderId + block
        long seed = 31 * orderId + block;
        Random rand = new Random(seed);
        int randomStep = rand.nextBoolean() ? 1 : -1;

        // 5️⃣ Force momentum hướng về target + randomStep
        if (currentThread < target) momentum += randomStep;  // đang tăng
        else if (currentThread > target) momentum -= randomStep; // đang giảm

        // 6️⃣ Clamp momentum trong [-5, 5]
        momentum = Math.max(-5, Math.min(5, momentum));

        // 7️⃣ Threshold → nhảy thread ±1
        int threshold = 1;
        if (momentum >= threshold && currentThread < maxThread) {
            currentThread += 1;
            momentum -= threshold; // giữ phần dư
        } else if (momentum <= -threshold && currentThread > minThread) {
            currentThread -= 1;
            momentum -= -threshold; // giữ phần dư
        }

        // 8️⃣ Clamp thread
        currentThread = Math.max(minThread, Math.min(maxThread, currentThread));

        return new ThreadResult(currentThread, momentum);
    }

}
