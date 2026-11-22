package com.scales.dev;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.IntStream;

public record Transition(int transitionFrames, int fps, Transitions transition) {
  public Clip transition(Clip current, Clip next) {
    BufferedImage[] output = new BufferedImage[fps];

    int transitionIndex = fps - transitionFrames;

    double imageIndex = 0;
    double imageIncrement = (double) current.images.length / fps;

    BufferedImage nextImage =
        next == null ? current.images[current.images.length - 1] : next.images[0];

    for (int i = 0; i < fps; i++) {
      BufferedImage imageAtFrame = current.images[(int) Math.floor(imageIndex)];
      imageIndex += imageIncrement;

      if (i < transitionIndex) {
        output[i] = imageAtFrame;
        continue;
      }

      int transitionFrame = i - transitionIndex;
      float transitionProgress = (float) transitionFrame / transitionFrames;

      output[i] = transition.handle(imageAtFrame, nextImage, transitionProgress, current);
    }

    return new Clip(output, fps);
  }

  public enum Transitions {
    NONE {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        return from;
      }
    },
    FADE {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        BufferedImage result = getDummyImage(from);
        Graphics2D g = result.createGraphics();

        g.setComposite(AlphaComposite.Src);
        g.drawImage(from, 0, 0, null);

        g.setComposite(AlphaComposite.SrcOver.derive(progress));
        g.drawImage(to, 0, 0, null);

        g.dispose();
        return result;
      }
    },
    DISSOLVE {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        BufferedImage result = getDummyImage(from);
        Graphics2D g = result.createGraphics();

        int squareSize = from.getWidth() / 20;
        int maxHoles = (from.getWidth() * from.getHeight()) / (squareSize * squareSize);
        int holes = (int) (progress * maxHoles);

        g.drawImage(from, 0, 0, null);

        int columns = (from.getWidth() + squareSize - 1) / squareSize;
        int rows = (from.getHeight() + squareSize - 1) / squareSize;

        int totalSquares = columns * rows;

        // gives an array from 0 to totalSquares
        int[] order = IntStream.range(0, totalSquares).toArray();

        // ensures we get the same random pattern between frames
        Random random = new Random(clip.hashCode());
        for (int i = order.length - 1; i > 0; i--) {
          // swaps numbers around randomly!
          int j = random.nextInt(i + 1);
          int temp = order[i];
          order[i] = order[j];
          order[j] = temp;
        }

        for (int i = 0; i < holes; i++) {
          int index = order[i];

          int x = (index % columns) * squareSize;
          int y = (index / columns) * squareSize;

          g.drawImage(
              to, x, y, x + squareSize, y + squareSize, x, y, x + squareSize, y + squareSize, null);
        }
        g.dispose();

        return result;
      }
    },
    LEFT_TO_RIGHT {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        BufferedImage result = getDummyImage(from);
        Graphics2D g = result.createGraphics();

        g.translate(from.getWidth() * progress, 0);
        g.drawImage(from, 0, 0, null);
        g.drawImage(to, -from.getWidth(), 0, null);

        g.dispose();
        return result;
      }
    },
    RIGHT_TO_LEFT {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        BufferedImage result = getDummyImage(from);
        Graphics2D g = result.createGraphics();

        g.translate(-from.getWidth() * progress, 0);
        g.drawImage(from, 0, 0, null);
        g.drawImage(to, from.getWidth(), 0, null);

        g.dispose();
        return result;
      }
    },
    FLIP {
      @Override
      public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
        int width = from.getWidth();
        int height = from.getHeight();

        BufferedImage result = getDummyImage(from);
        Graphics2D g = result.createGraphics();

        double angle = progress * Math.PI;

        g.translate(width / 2.0, height / 2.0);
        g.scale(Math.abs(Math.cos(angle)), 1);
        g.shear(0, Math.sin(angle) * 0.15);
        g.drawImage(angle > Math.PI / 2 ? to : from, -width / 2, -height / 2, null);

        g.dispose();
        return result;
      }
    };

    private static BufferedImage getDummyImage(BufferedImage from) {
      return new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    }

    public abstract BufferedImage handle(
        BufferedImage from, BufferedImage to, float progress, Clip clip);
  }
}
