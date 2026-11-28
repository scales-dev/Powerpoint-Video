package com.scales.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.*;

// evil VideoSplitter be like:
public class VideoAssembler {
  private final FFmpegFrameRecorder FRAME_RECORDER;
  private static final Java2DFrameConverter FRAME_CONVERTER = new Java2DFrameConverter();

  public VideoAssembler(File output, double fps, int width, int height) {
    try {
      this.FRAME_RECORDER = FFmpegFrameRecorder.createDefault(output, width, height);

      this.FRAME_RECORDER.setFrameRate(fps);

      // we need speed.
      this.FRAME_RECORDER.setVideoCodecName("libvpx-vp9");
      this.FRAME_RECORDER.setVideoOption("deadline", "realtime");
      // can be any number from -8 to 8, but 8 causes gross flickers for me, idk why
      this.FRAME_RECORDER.setVideoOption("cpu-used", "6");

      this.FRAME_RECORDER.start();
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void close() {
    try {
      this.FRAME_RECORDER.flush();
      this.FRAME_RECORDER.close();
    } catch (FrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static Frame getFrame(BufferedImage bufferedImage) {
    return FRAME_CONVERTER.convert(bufferedImage);
  }

  public void append(Frame frame) throws FFmpegFrameRecorder.Exception {
    this.FRAME_RECORDER.record(frame);
  }

  public void append(BufferedImage bufferedImage) throws FFmpegFrameRecorder.Exception {
    this.append(getFrame(bufferedImage));
  }
}