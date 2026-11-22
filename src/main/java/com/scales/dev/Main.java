package com.scales.dev;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    VideoSplitter splitter = new VideoSplitter(new File(args[0]));
    BufferedImage[] frames = splitter.split(5);

    splitter.close();

    try {
      for(int i = 0; i < frames.length; i++) {
        ImageIO.write(frames[i], "png", new File("/home/scale/Downloads/output/" + i + ".png"));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}