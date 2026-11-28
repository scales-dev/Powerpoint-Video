package com.scales.dev;

import java.awt.image.BufferedImage;
import java.io.File;
import org.bytedeco.javacv.*;

// evil VideoSplitter be like:
public class VideoAssembler {
  private final FFmpegFrameRecorder FRAME_RECORDER;
  private final Java2DFrameConverter FRAME_CONVERTER;
  private final Transition transition;

  private Clip lastClip;

  public VideoAssembler(File output, double fps, int width, int height) {
    this.FRAME_CONVERTER = new Java2DFrameConverter();
    this.transition = new Transition(20, 60, Transitions.FLIP);

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

  public void append(Clip clip) throws FFmpegFrameRecorder.Exception {
    if (lastClip == null) {
      lastClip = clip;
      return;
    }

    Clip transitionedClip = this.transition.transition(lastClip, clip);

    double imageIndex = 0;
    double imageIncrement = (double) transitionedClip.images.length / transitionedClip.frames;
    int prevIndex = -1;
    Frame frame = null;

    while (imageIndex < transitionedClip.images.length) {
      int i = (int) Math.floor(imageIndex);
      if (prevIndex != i) {
        frame = FRAME_CONVERTER.convert(transitionedClip.images[i]);
        prevIndex = i;
      }

      this.FRAME_RECORDER.record(frame);
      imageIndex += imageIncrement;
    }

    lastClip = clip;
  }

  public void append(BufferedImage bufferedImage, int frames) throws FFmpegFrameRecorder.Exception {
    Frame frame = FRAME_CONVERTER.convert(bufferedImage);

    for (int i = 0; i < frames; i++) {
      this.FRAME_RECORDER.record(frame);
    }
  }
}