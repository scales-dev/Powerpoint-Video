package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;

public class Main {
  public static void main(String[] args)
      throws FFmpegFrameGrabber.Exception, FFmpegFrameRecorder.Exception {
    float splitFps = 1;
    float outputFps = 60;

    File input = new File(args[0]);
    File output = input.toPath().getParent().resolve("output.webm").toFile();

    VideoSplitter splitterA = new VideoSplitter(input, splitFps);
    // this second splitter is ahead of the game! (THE GAME!!!!!)
    VideoSplitter splitterB = new VideoSplitter(input, splitFps);
    splitterB.skip();

    Rectangle bounds = splitterA.getBounds();

    VideoAssembler assembler = new VideoAssembler(output, outputFps, bounds.width, bounds.height);

    int frames = 0;

    BufferedImage currentFrame = null;
    BufferedImage nextFrame;
    while ((nextFrame = splitterB.getFrame(false)) != null) {
      currentFrame = splitterA.getFrame(false);

      int remaining = (int) ((outputFps * splitterA.currentFrame) / splitFps);

      while (frames++ < remaining) {
        int framesLeft = remaining - frames;
        float progress = 1 - (framesLeft / outputFps);

        BufferedImage transitionFrame =
            Transitions.FADE.handle(currentFrame, nextFrame, progress, 0);
        assembler.append(transitionFrame);
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