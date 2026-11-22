package com.scales.dev;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.InputStream;

// Source - https://stackoverflow.com/a/67492899
// Posted by Felipe Oviedo
// Retrieved 2026-11-22, License - CC BY-SA 4.0
public class VideoSplitter {
  private final FFmpegFrameGrabber FRAME_GRABBER;
  private final Java2DFrameConverter FRAME_CONVERTER;

  public VideoSplitter(File input) {
    FRAME_GRABBER = new FFmpegFrameGrabber(input.getAbsoluteFile());

    FRAME_CONVERTER = new Java2DFrameConverter();

    try{
      this.FRAME_GRABBER.start();
    } catch (FFmpegFrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try{
      FRAME_GRABBER.close();
      FRAME_CONVERTER.close();
    } catch (FrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  // secondsPerFrame lol
  public BufferedImage[] split(int secondsPerFrame) {
    int increment = (int) (FRAME_GRABBER.getFrameRate() * secondsPerFrame);
    int frameCount = Math.floorDiv(FRAME_GRABBER.getLengthInVideoFrames(), increment);
    BufferedImage[] frames = new BufferedImage[frameCount+1];

    try {
      for (int i = 0; i <= frameCount; i++) {
        System.out.println(i + ":" + frameCount);
        FRAME_GRABBER.setFrameNumber(i*increment);

        // Source - https://stackoverflow.com/a/3514297
        // Posted by Klark
        // Retrieved 2026-11-22, License - CC BY-SA 2.5
        BufferedImage bi = FRAME_CONVERTER.convert(FRAME_GRABBER.grabImage());
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
