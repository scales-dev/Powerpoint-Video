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

  public void assemble(LinkedList<Clip> clips) {
    try {
      int frame = 0;

      Clip current;
      while ((current = clips.poll()) != null) {
        Clip next = clips.peek();

        Clip transitionedClip = this.transition.transition(current, next);

        System.out.println(clips.size() + " clips remaining!!");

        double imageIndex = 0;
        double imageIncrement = (double) transitionedClip.images.length / transitionedClip.frames;
        while (imageIndex < transitionedClip.images.length) {
          int i = (int) Math.floor(imageIndex);

          this.FRAME_RECORDER.setFrameNumber(frame++);
          this.FRAME_RECORDER.record(FRAME_CONVERTER.convert(transitionedClip.images[i]));
          imageIndex += imageIncrement;
        }
      }
    } catch (FFmpegFrameRecorder.Exception e) {
      throw new RuntimeException(e);
    }
  }
}
