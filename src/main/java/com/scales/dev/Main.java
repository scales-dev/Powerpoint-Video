package com.scales.dev;

import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class Main {
  public static void main(String[] args) {
    double splitFps = 0.2;

    File input = new File(args[0]);
    VideoSplitter splitter = new VideoSplitter(new File(args[0]));
    int originalFps = splitter.getFPS();
    LinkedList<Clip> frames =
        Arrays.stream(splitter.split(splitFps)).collect(Collectors.toCollection(LinkedList::new));
    splitter.close();

    int outputFps = (int) (originalFps / splitFps);
    Transition transition = new Transition(10, outputFps, Transition.Transitions.FADE);

    File output = input.toPath().getParent().resolve("output.webm").toFile();
    Rectangle bounds = frames.getFirst().images[0].getData().getBounds();
    VideoAssembler assembler =
        new VideoAssembler(output, outputFps, bounds.width, bounds.height, transition);
    assembler.assemble(frames);
    assembler.close();
  }
}