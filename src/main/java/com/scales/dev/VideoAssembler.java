package com.scales.dev;

import java.io.File;
import java.util.LinkedList;
import org.bytedeco.javacv.*;

// evil VideoSplitter be like:
public class VideoAssembler {
  private final FFmpegFrameRecorder FRAME_RECORDER;
  private final Java2DFrameConverter FRAME_CONVERTER;
  private final Transition transition;

  public VideoAssembler(File output, double fps, int width, int height, Transition transition) {
    System.out.println("Video assembler joins the battle");
    this.FRAME_CONVERTER = new Java2DFrameConverter();
    this.transition = transition;

    try {
      this.FRAME_RECORDER = FFmpegFrameRecorder.createDefault(output, width, height);

      this.FRAME_RECORDER.setFrameRate(fps);

      // we need speed.
      this.FRAME_RECORDER.setFormat("webm");
      this.FRAME_RECORDER.setVideoCodecName("libvpx-vp9");
      this.FRAME_RECORDER.setVideoOption("deadline", "realtime");
      this.FRAME_RECORDER.setVideoOption("cpu-used", "8");
      this.FRAME_RECORDER.setVideoOption("row-mt", "1");
      this.FRAME_RECORDER.setVideoOption("tile-columns", "2");
      this.FRAME_RECORDER.setVideoOption(
          "threads", String.valueOf(Runtime.getRuntime().availableProcessors()));

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

  public void assemble(LinkedList<Clip> clips) {
    try {
      Clip current;
      while ((current = clips.poll()) != null) {
        Clip next = clips.peek();

        Clip transitionedClip = this.transition.transition(current, next);

        System.out.println(clips.size() + " clips remaining!!");

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
      }
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }
}
