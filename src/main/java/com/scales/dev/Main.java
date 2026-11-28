package com.scales.dev;

import java.awt.*;
import java.io.File;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class Main {
  public static void main(String[] args) {
    double fps = 0.5;

    File input = new File(args[0]);
    File output = input.toPath().getParent().resolve("output.webm").toFile();

    VideoSplitter splitter = new VideoSplitter(new File(args[0]));
    Rectangle bounds = splitter.getBounds();

    VideoAssembler assembler = new VideoAssembler(output, fps, bounds.width, bounds.height);

    splitter.getFrames(
        fps,
        bi -> {
          try {
            assembler.append(bi, 1);
          } catch (FFmpegFrameRecorder.Exception e) {
            throw new RuntimeException(e);
          }

          return false;
        });

    splitter.close();
    assembler.close();
  }
}