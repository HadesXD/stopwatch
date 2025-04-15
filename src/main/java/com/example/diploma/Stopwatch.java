package com.example.diploma;

import javafx.animation.AnimationTimer;

import java.time.Duration;
import java.time.Instant;

public class Stopwatch {
    private Instant startTime;
    private Duration elapsedTime = Duration.ZERO;
    private boolean running = false;
    private AnimationTimer timer;
    private Runnable onUpdate;

    public Stopwatch(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (running) {
                    onUpdate.run();
                }
            }
        };
    }

    public void start() {
        if (!running) {
            startTime = Instant.now();
            running = true;
            timer.start();
        }
    }

    public void stop() {
        if (running) {
            elapsedTime = Duration.between(startTime, Instant.now());
            running = false;
            timer.stop();
        }
    }

    public String getElapsedTime() {
        if (running) {
            Duration currentElapsed = Duration.between(startTime, Instant.now());
            return formatDuration(currentElapsed);
        } else {
            return formatDuration(elapsedTime);
        }
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


}
