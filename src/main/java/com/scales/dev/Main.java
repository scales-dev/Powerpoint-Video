package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class Main {
  private static final float SPLIT_FPS = 0.5f;
  private static final float OUTPUT_FPS = 60;
  // number between 0-1 for minimum progress before starting transition to next frame
  private static final float TRANSITION_THRESHOLD = 0.5f;

  private static final float INTERPOLATION_FRAMES = OUTPUT_FPS / SPLIT_FPS;

  public static void main(String[] args)
      throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
    File input = new File(args[0]);
    File output = input.toPath().getParent().resolve("output.webm").toFile();

    VideoSplitter splitterA = new VideoSplitter(input, SPLIT_FPS);
    // this second splitter is ahead of the game! (THE GAME!!!!!)
    VideoSplitter splitterB = new VideoSplitter(input, SPLIT_FPS);
    splitterB.skip();

    Rectangle bounds = splitterA.getBounds();

    VideoAssembler assembler = new VideoAssembler(output, OUTPUT_FPS, bounds.width, bounds.height);

    int frames = 0;

    BufferedImage currentFrame = null;
    BufferedImage nextFrame;
    while ((nextFrame = splitterB.getFrame(false)) != null) {
      currentFrame = splitterA.getFrame(false);

      int remaining = (int) (INTERPOLATION_FRAMES * splitterA.currentFrame);

      while (frames++ < remaining) {
        int framesLeft = remaining - frames;
        float progress = 1 - (framesLeft / OUTPUT_FPS);
        if (progress < TRANSITION_THRESHOLD) {
          assembler.append(currentFrame);
          continue;
        }

        progress = (progress - TRANSITION_THRESHOLD) / (1 - TRANSITION_THRESHOLD);

        assembler.append(
            Transitions.FADE.handle(currentFrame, nextFrame, progress, currentFrame.hashCode()));
      }
    }

    // current frame will only be null if the video is shorter than the splitter's fps
    assert currentFrame != null;
    assembler.append(currentFrame);

    splitterA.close();
    splitterB.close();
    assembler.close();
  }
}