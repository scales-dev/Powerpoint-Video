package com.scales.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.*;

// evil VideoSplitter be like:
public class VideoAssembler {
  private final FFmpegFrameRecorder FRAME_RECORDER;
  private final Java2DFrameConverter FRAME_CONVERTER;

  public VideoAssembler(File output, double fps, int width, int height) {
    System.out.println("Video assembler joins the battle");
    this.FRAME_CONVERTER = new Java2DFrameConverter();

    try {
      this.FRAME_RECORDER =
          FFmpegFrameRecorder.createDefault(output.getAbsoluteFile(), width, height);
      this.FRAME_RECORDER.setFrameRate(fps);
      this.FRAME_RECORDER.start();
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try {
      this.FRAME_RECORDER.flush();
      this.FRAME_RECORDER.close();
      System.out.println("Video assembler dies immediately after the war ended");
    } catch (FrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void assemble(BufferedImage[] frames) {
    try {
      for (int i = 0; i < frames.length; i++) {
        this.FRAME_RECORDER.setFrameNumber(i);
        this.FRAME_RECORDER.record(FRAME_CONVERTER.convert(frames[i]));
      }
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }
}
