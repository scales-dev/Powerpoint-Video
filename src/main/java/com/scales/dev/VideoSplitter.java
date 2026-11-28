package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
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

  public int currentFrame;
  private final int frameIncrement;
  private final int frameCount;

  public VideoSplitter(File input, double fps) {
    this.FRAME_CONVERTER = new Java2DFrameConverter();

    try{
      this.FRAME_GRABBER = FFmpegFrameGrabber.createDefault(input.getAbsoluteFile());
      this.FRAME_GRABBER.start();

      this.currentFrame = 0;
      this.frameIncrement = (int) (this.FRAME_GRABBER.getFrameRate() * (1 / fps));
      this.frameCount = Math.floorDiv(this.FRAME_GRABBER.getLengthInVideoFrames(), frameIncrement);

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

  public BufferedImage getFrame(int frame, boolean duplicate) throws FFmpegFrameGrabber.Exception {
    if (frame > this.frameCount) return null;

    this.FRAME_GRABBER.setFrameNumber(frame * frameIncrement);

    BufferedImage bi = this.FRAME_CONVERTER.convert(this.FRAME_GRABBER.grabImage());

    if (!duplicate) return bi;

    // Source - https://stackoverflow.com/a/3514297
    // Posted by Klark
    // Retrieved 2026-11-22, License - CC BY-SA 2.5
    ColorModel cm = bi.getColorModel();
    return new BufferedImage(cm, bi.copyData(null), cm.isAlphaPremultiplied(), null);
  }

  public BufferedImage getFrame(boolean duplicate) throws FFmpegFrameGrabber.Exception {
    return this.getFrame(this.currentFrame++, duplicate);
  }

  public void skip() {
    this.currentFrame++;
  }

  public void skip(int frames) {
    this.currentFrame += frames;
  }

  public Rectangle getBounds() {
    return new Rectangle(this.FRAME_GRABBER.getImageWidth(), this.FRAME_GRABBER.getImageHeight());
  }
}
