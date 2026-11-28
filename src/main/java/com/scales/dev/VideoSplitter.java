package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.util.function.Function;
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
    this.FRAME_CONVERTER = new Java2DFrameConverter();

    try{
      this.FRAME_GRABBER = FFmpegFrameGrabber.createDefault(input.getAbsoluteFile());
      this.FRAME_GRABBER.start();
    } catch (FFmpegFrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try{
      this.FRAME_GRABBER.close();
      this.FRAME_CONVERTER.close();
    } catch (FrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void getFrames(double fps, Function<BufferedImage, Boolean> frame) {
    int increment = (int) (this.FRAME_GRABBER.getFrameRate() * (1 / fps));
    int frameCount = Math.floorDiv(this.FRAME_GRABBER.getLengthInVideoFrames(), increment);

    try {
      for (int i = 0; i <= frameCount; i++) {
        BufferedImage bi = this.getFrame(i * increment, false);
        if (frame.apply(bi)) return;
      }
    } catch (FFmpegFrameGrabber.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public BufferedImage getFrame(int frame, boolean duplicate) throws FFmpegFrameGrabber.Exception {
    this.FRAME_GRABBER.setFrameNumber(frame);

    BufferedImage bi = this.FRAME_CONVERTER.convert(this.FRAME_GRABBER.grabImage());

    if (!duplicate) return bi;

    // Source - https://stackoverflow.com/a/3514297
    // Posted by Klark
    // Retrieved 2026-11-22, License - CC BY-SA 2.5
    ColorModel cm = bi.getColorModel();
    return new BufferedImage(cm, bi.copyData(null), cm.isAlphaPremultiplied(), null);
  }

  public Rectangle getBounds() {
    return new Rectangle(
        0, 0, this.FRAME_GRABBER.getImageWidth(), this.FRAME_GRABBER.getImageHeight());
  }
}
