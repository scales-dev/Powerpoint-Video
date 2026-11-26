package com.scales.dev;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.stream.IntStream;

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
  FADE_TO_BLACK {
    @Override
    public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
      BufferedImage result = getDummyImage(from);
      Graphics2D g = result.createGraphics();

      g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, 1 - (progress * 3))));
      g.drawImage(from, 0, 0, null);

      g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0, -2 + (progress * 3))));
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
      g.scale(Math.max(0.05, Math.abs(Math.cos(angle))), 1);
      g.shear(0, Math.sin(angle) * 0.15);
      g.drawImage(angle > Math.PI / 2 ? to : from, -width / 2, -height / 2, null);

      g.dispose();
      return result;
    }
  },
  CUBE {
    @Override
    public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
      int width = from.getWidth();
      int height = from.getHeight();

      BufferedImage result = getDummyImage(from);
      Graphics2D g = result.createGraphics();

      // when stealing code for the flip animation
      // i found out Math.cos([0-1] * Math.PI) outputs 1 for 0 and -1 for 1
      // this is obvious, but i just never thought of it before
      double n = Math.abs(1 - (progress * 2));
      double scale = n + ((1 - n) * 0.5);

      g.translate((width / 2f) - ((width * scale) / 2), (height / 2f) - ((height * scale) / 2));
      g.scale(scale, scale);

      AffineTransform transform = g.getTransform();
      float scaleX = 1 - progress;
      g.scale(scaleX, 1);
      g.drawImage(from, 0, 0, null);
      g.setTransform(transform);

      g.translate(from.getWidth() * scaleX, 0);
      g.scale(progress, 1);
      g.drawImage(to, 0, 0, null);

      g.dispose();
      return result;
    }
  },
  RANDOM {
    @Override
    public BufferedImage handle(BufferedImage from, BufferedImage to, float progress, Clip clip) {
      int transitionIndex = (int) (new Random(clip.hashCode()).nextFloat() * (values().length - 1));
      return values()[transitionIndex].handle(from, to, progress, clip);
    }
  };

  private static BufferedImage getDummyImage(BufferedImage from) {
    return new BufferedImage(from.getWidth(), from.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
  }

  public abstract BufferedImage handle(
      BufferedImage from, BufferedImage to, float progress, Clip clip);
}
