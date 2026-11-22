package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;

public record Transition(int transitionFrames, int fps, Transitions transition) {
  public Clip transition(Clip current, Clip next) {
    BufferedImage[] output = new BufferedImage[fps];

    int transitionIndex = fps - transitionFrames;

    double imageIndex = 0;
    double imageIncrement = (double) current.images.length / fps;

    BufferedImage nextImage =
        next == null ? current.images[current.images.length - 1] : next.images[0];

    for (int i = 0; i < fps; i++) {
      BufferedImage imageAtFrame = current.images[(int) Math.floor(imageIndex)];
      imageIndex += imageIncrement;

      if (i < transitionIndex) {
        output[i] = imageAtFrame;
        continue;
      }

      int transitionFrame = i - transitionIndex;
      float transitionProgress = (float) transitionFrame / transitionFrames;

      output[i] = transition.handle(imageAtFrame, nextImage, transitionProgress, current);
    }

    return new Clip(output, fps);
  }
}
