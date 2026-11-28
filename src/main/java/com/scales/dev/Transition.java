package com.scales.dev;

import java.awt.image.BufferedImage;

/// threshold number between 0-1 for minimum progress ratio before starting transition to next frame
public record Transition(float threshold, Transitions transition) {
  public BufferedImage handle(BufferedImage from, BufferedImage to, float progress) {
    if (progress < threshold) return from;

    return transition.handle(from, to, (progress - threshold) / (1 - threshold), this.hashCode());
  }
}
