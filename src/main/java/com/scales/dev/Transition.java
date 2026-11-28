package com.scales.dev;

import java.awt.image.BufferedImage;
import org.bytedeco.javacv.Frame;

public class Transition {
  /// number between 0-1 for minimum progress ratio before starting transition to next frame
  private final float THRESHOLD;

  private final Transitions TRANSITION;

  private final BufferedImage FROM;
  private final BufferedImage TO;

  private final Frame FROM_RENDERED;

  public Transition(float threshold, Transitions transition, BufferedImage from, BufferedImage to) {
    this.THRESHOLD = threshold;
    this.TRANSITION = transition;
    this.FROM = from;
    this.FROM_RENDERED = VideoAssembler.getFrame(this.FROM);

    this.TO = to;
  }

  public Frame handle(float progress) {
    if (progress < THRESHOLD) return FROM_RENDERED;
    progress = (progress - THRESHOLD) / (1 - THRESHOLD);

    return VideoAssembler.getFrame(TRANSITION.handle(FROM, TO, progress, FROM.hashCode()));
  }
}
