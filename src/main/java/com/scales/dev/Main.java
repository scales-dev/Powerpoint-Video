package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class Main {
  private static final float SPLIT_FPS = 0.5f;
  private static final float OUTPUT_FPS = 60;
  private static final float TRANSITION_THRESHOLD = 0.5f;

  private static final float INTERPOLATION_FRAMES = OUTPUT_FPS / SPLIT_FPS;

  public static void main(String[] args)
      throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
    File input = new File(args[0]);
    File output = input.toPath().getParent().resolve("output.webm").toFile();

    VideoSplitter splitter = new VideoSplitter(input, SPLIT_FPS);

    Rectangle bounds = splitter.getBounds();

    VideoAssembler assembler = new VideoAssembler(output, OUTPUT_FPS, bounds.width, bounds.height);

    int frames = 0;

    BufferedImage currentFrame = splitter.getFrame(true);
    while (currentFrame != null) {
      int remaining = (int) (INTERPOLATION_FRAMES * splitter.currentFrame);

      BufferedImage nextFrame = splitter.getFrame(true);
      Transition transition =
          new Transition(TRANSITION_THRESHOLD, Transitions.random(), currentFrame, nextFrame);

      while (frames++ < remaining) {
        int framesLeft = remaining - frames;
        float progress = 1 - (framesLeft / OUTPUT_FPS);

        assembler.append(transition.handle(progress));
      }

      currentFrame = nextFrame;
    }

    splitter.close();
    assembler.close();
  }
}