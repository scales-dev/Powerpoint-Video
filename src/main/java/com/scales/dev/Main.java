package com.scales.dev;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    double fps = 0.2;

    File input = new File(args[0]);
    VideoSplitter splitter = new VideoSplitter(new File(args[0]));
    int originalFps = splitter.getFPS();
    LinkedList<Clip> frames =
        Arrays.stream(splitter.split(fps)).collect(Collectors.toCollection(LinkedList::new));
    splitter.close();

    Transition transition = new Transition(20, originalFps, Transition.Transitions.FADE);

    File output = input.toPath().getParent().resolve("output.webm").toFile();
    Rectangle bounds = frames.getFirst().images[0].getData().getBounds();
    VideoAssembler assembler =
        new VideoAssembler(output, originalFps * fps, bounds.width, bounds.height, transition);
    assembler.assemble(frames);
    assembler.close();
  }
}