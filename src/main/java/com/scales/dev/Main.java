package com.scales.dev;

import java.awt.image.BufferedImage;
import java.io.File;

public class Main {
  private static final double FPS = 1;

  public static void main(String[] args) {
    File input = new File(args[0]);
    VideoSplitter splitter = new VideoSplitter(new File(args[0]));
    BufferedImage[] frames = splitter.split(FPS);
    splitter.close();

    File output = input.toPath().getParent().resolve("output.mp4").toFile();
    int width = frames[0].getWidth();
    int height = frames[0].getHeight();
    VideoAssembler assembler = new VideoAssembler(output, FPS, width, height);
    assembler.assemble(frames);
    assembler.close();
  }
}