package com.scales.dev;

import java.awt.image.BufferedImage;

public class Clip {
  public BufferedImage[] images;
  public int frames;

  public Clip(BufferedImage[] images, int frames) {
    this.images = images;
    this.frames = frames;
  }
}
