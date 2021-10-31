import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;

public class Main {
	
	public static void medianFilter(int size, String outputName, BufferedImage image) throws IOException {
		ArrayList<Integer> a = new ArrayList<>();
		BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Color c = new Color(0, 0, 0);
		Color c2 = new Color(0, 0, 0);
		int median = 0;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				a.clear();
				for (int i = x + (-1) * size / 2; i <= x + size / 2; i++) {
					for (int j = y + (-1) * size / 2; j <= y + size / 2; j++) {
						if (i > 0 && i < image.getWidth() - 1 && j > 0 && j < image.getHeight() - 1) {
							c = new Color(image.getRGB(i, j));
							a.add(c.getRed());
						}

					}

				}
				Collections.sort(a);
				median = a.get(a.size() / 2);
				c2 = new Color(median, median, median);
				output.setRGB(x, y, c2.getRGB());
			}

		}
		System.out.println(ImageIO.write(output, "png", new File(outputName + ".png")));
	}


	public static void gaussianFilter3(String outputName, BufferedImage image) throws IOException {
		double[][] a = { { 0.077847, 0.123317, 0.077847 }, { 0.123317, 0.195346, 0.123317 },
				{ 0.077847, 0.123317, 0.077847 } };
		BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Color c = new Color(0, 0, 0);
		Color c2 = new Color(0, 0, 0);
		int value = 0;
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				for (int i = x - 1, g1 = 0; i <= x + 1; i++, g1++) {
					for (int j = y - 1, g2 = 0; j <= y + 1; j++, g2++) {
						if (i > 0 && i < image.getWidth() - 1 && j > 0 && j < image.getHeight() - 1) {
							c = new Color(image.getRGB(i, j));
							value += ((int) c.getRed() * a[g2][g1]);
						}

					}

				}
				c2 = new Color(value, value, value);
				output.setRGB(x, y, c2.getRGB());
				value = 0;
			}

		}
		System.out.println(ImageIO.write(output, "png", new File(outputName + ".png")));
	}

	public static void thresholding(String outputName, int value, BufferedImage image) throws IOException {
		BufferedImage bi2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < image.getHeight(); x++) {
			for (int y = 0; y < image.getWidth(); y++) {
				Color c = new Color(image.getRGB(y, x));
				if (c.getRed() > value) {
					bi2.setRGB(y, x, 0xffffff);
				} else {
					bi2.setRGB(y, x, 0x000000);
				}
			}
		}
		System.out.println(ImageIO.write(bi2, "png", new File(outputName + ".png")));
	}

	public static void applyMask(String outputName, int value, BufferedImage image, BufferedImage mask) throws IOException {

		BufferedImage bi2 = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < image.getHeight(); x++) {
			for (int y = 0; y < image.getWidth(); y++) {
				Color c = new Color(mask.getRGB(y, x));
				if (c.getRed() == value) {
					bi2.setRGB(y, x, image.getRGB(y, x));
				} else {
					bi2.setRGB(y, x, 0x000000);
				}
			}
		}
		System.out.println(ImageIO.write(bi2, "png", new File(outputName + ".png")));

	}

	public static int[] histogram(BufferedImage bi) {
		int[] histogram = new int[256];

		for (int x = 0; x < bi.getWidth(); x++) {
			for (int y = 0; y < bi.getHeight(); y++) {
				Color c = new Color(bi.getRGB(x, y));
				histogram[c.getRed()]++;
			}
		}
		return histogram;
	}

	public static double entropy(BufferedImage image) {
		int[] histo = histogram(image);
		double cur = 0;
		double tot = image.getWidth() * image.getHeight() - histo[0];
		
		double res = 0.0;
		for (int i = 1; i < histo.length; i++) {
			cur = ((double) histo[i]) / tot;
			if (cur == 0)
				continue;
			res += cur * ((double) (Math.log(1.0 / cur)) / Math.log(2.0));
		}
		return res;

	}

	public static double informationGain(double imageEntropy, BufferedImage threshold, double foregroundEntropy,
			double backgroundEntropy) {

		double size = (double) (threshold.getWidth() * threshold.getHeight());
		int[] h = histogram(threshold);

		double bgp = h[0] / size;
		double fgp = h[255] / size;

		return imageEntropy - (backgroundEntropy * bgp + foregroundEntropy * fgp);
	}

	public static void main(String[] args) throws IOException {
		BufferedImage i = ImageIO.read(new File("sat_noisy.gif"));
		medianFilter(7,"median7x7", i);
		BufferedImage bi = ImageIO.read(new File("median7x7.png"));
		//BufferedImage i = ImageIO.read(new File("sat_noisy.gif"));
		thresholding("thresholdImage", 42, bi);
		BufferedImage binary_i = ImageIO.read(new File("thresholdImage.png"));
		applyMask("fg_i", 255, i, binary_i);
		BufferedImage fg_i = ImageIO.read(new File("fg_i.png"));
		applyMask("bg_i", 0, i, binary_i);
		BufferedImage bg_i = ImageIO.read(new File("bg_i.png"));
		double e_i = entropy(i);
		double e_fg_i = entropy(fg_i);
		double e_bg_i = entropy(bg_i);
		double infoGain = informationGain(e_i, binary_i, e_fg_i, e_bg_i);
		System.out.println("Image entropy: " + e_i);
		System.out.println("Foreground entropy: " + e_fg_i);
		System.out.println("Background entropy: " + e_bg_i);
		System.out.println("Information gain: " + infoGain);

	}

}
