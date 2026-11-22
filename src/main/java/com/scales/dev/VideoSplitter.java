package com.scales.dev;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

// Source - https://stackoverflow.com/a/67492899
// Posted by Felipe Oviedo
// Retrieved 2026-11-22, License - CC BY-SA 4.0
public class VideoSplitter {
  private final FFmpegFrameGrabber FRAME_GRABBER;
  private final Java2DFrameConverter FRAME_CONVERTER;

  public VideoSplitter(File input) {
    System.out.println("Video splitter initialized!");
    this.FRAME_GRABBER = new FFmpegFrameGrabber(input.getAbsoluteFile());
    this.FRAME_CONVERTER = new Java2DFrameConverter();

    try{
      this.FRAME_GRABBER.start();
    } catch (FFmpegFrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try{
      this.FRAME_GRABBER.close();
      this.FRAME_CONVERTER.close();
      System.out.println("Video splitter closed :pensive:");
    } catch (FrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BufferedImage[] split(double fps) {
    int increment = (int) (this.FRAME_GRABBER.getFrameRate() * (1 / fps));
    int frameCount = Math.floorDiv(this.FRAME_GRABBER.getLengthInVideoFrames(), increment);
    BufferedImage[] frames = new BufferedImage[frameCount+1];

    try {
      for (int i = 0; i <= frameCount; i++) {
        this.FRAME_GRABBER.setFrameNumber(i * increment);

        BufferedImage bi = this.FRAME_CONVERTER.convert(this.FRAME_GRABBER.grabImage());
        // Source - https://stackoverflow.com/a/3514297
        // Posted by Klark
        // Retrieved 2026-11-22, License - CC BY-SA 2.5
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        BufferedImage clone = new BufferedImage(cm, raster, isAlphaPremultiplied, null);

        frames[i] = clone;
      }

      return frames;
    } catch (FFmpegFrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }
}
