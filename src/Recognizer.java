import hfr.BaselineStructureTree;
import hfr.SymbolList;
import hfr.SymbolNode;

import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

public class Recognizer {

	private static final int PARENT = 3;
	private static final int CHILD = 2;
	@SuppressWarnings("unused")
	private static final int PREV = 1;
	private static final int NEXT = 0;

	private static Map<String, List<Set<Integer>>> GLOBAL_MAPPING = null;

	public static void writeToFile(Mat img, String fname) {
		System.out.println("Writing image to file " + fname);
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(fname);
			fos.write(byteArray);
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows a CvMat image in a JFrame with a given title.
	 * 
	 * @param img
	 *            The image to show
	 * @param title
	 *            The title of the image.
	 */
	public static void imshow(Mat img, String title) {
		MatOfByte matOfByte = new MatOfByte();
		Highgui.imencode(".jpg", img, matOfByte);
		byte[] byteArray = matOfByte.toArray();
		BufferedImage bufImage = null;

		try {
			InputStream in = new ByteArrayInputStream(byteArray);
			bufImage = ImageIO.read(in);
		} catch (Exception e) {
			e.printStackTrace();
		}

		JFrame frame = new JFrame();
		JLabel label = new JLabel(new ImageIcon(bufImage));

		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(label);
		frame.pack();
		frame.setTitle(title);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Reads a set of mappings from a file
	 * 
	 * @param fname
	 *            the file to read from
	 * @return A map of strings to lists of valid points
	 * @throws IOException
	 */
	public static Map<String, List<Set<Integer>>> readMappingsFromFile(
			String fname) throws IOException {
		Map<String, List<Set<Integer>>> mappings = new HashMap<String, List<Set<Integer>>>();

		BufferedReader br = new BufferedReader(new FileReader(fname));
		String line = null;
		while ((line = br.readLine()) != null) {
			String colsplit[] = line.split(":");
			String prefix = colsplit[0];
			String integers[] = colsplit[1].split("[, ]+");

			List<Set<Integer>> mapping = null;
			if (!mappings.containsKey(prefix)) {
				mapping = new ArrayList<Set<Integer>>();
				mappings.put(prefix, mapping);
			}
			mapping = mappings.get(prefix);

			Set<Integer> given_mapping = new HashSet<Integer>();
			for (String Int : integers) {
				if (Int.length() > 0) {
					given_mapping.add(Integer.parseInt(Int));
				}
			}
			mapping.add(given_mapping);
			mappings.put(prefix, mapping);
		}
		br.close();

		return mappings;
	}

	/**
	 * Calculates the score of a given set of flags and correct flags
	 * 
	 * @param flags
	 *            the flags detected by the recognizer
	 * @param correct_flags
	 *            the correct flags
	 * @return a larger number for a better match
	 */
	public static double calc_score(boolean flags[], Set<Integer> correct_flags) {
		double score = 0.0;
		for (int i = 0; i < flags.length; i++) {
			if (correct_flags.contains(i)) {
				if (flags[i]) {
					score += 1;
				}
			} else {
				if (flags[i]) {
					score -= 0.5;
				}
			}
		}
		return score / correct_flags.size();
	}

	/**
	 * Attempts to find the best match for a given set of flags
	 * 
	 * @param flags
	 *            The flags to match against
	 * @return the string corresponding to those flags
	 */
	public static String match(boolean flags[]) {
		String bestKey = null;
		double bestScore = Double.NEGATIVE_INFINITY;

		for (String key : GLOBAL_MAPPING.keySet()) {
			List<Set<Integer>> possible_mappings = GLOBAL_MAPPING.get(key);

			double score = 0;
			for (Set<Integer> mapping : possible_mappings) {
				score = Math.max(score, calc_score(flags, mapping));
			}

			if (score > bestScore) {
				bestKey = key;
				bestScore = score;
			}
		}
		return bestKey;
	}

	/**
	 * Draws a given flag on an image (for debugging)
	 * 
	 * @param img
	 *            the image to draw on
	 * @param flag
	 *            the flag index
	 */
	public static void drawFlag(Mat img, int flag) {
		Point p1 = null;
		Point p2 = null;

		double HALF_WIDTH = img.cols() * 1.0 / 2;
		double HALF_HEIGHT = img.rows() * 1.0 / 2;

		switch (flag) {
		case 0:
			p1 = new Point(0, 0);
			p2 = new Point(HALF_WIDTH, 0);
			break;
		case 1:
			p1 = new Point(img.cols(), 0);
			p2 = new Point(HALF_WIDTH, 0);
			break;
		case 2:
			p1 = new Point(0, 0);
			p2 = new Point(0, HALF_HEIGHT);
			break;
		case 3:
			p1 = new Point(0, 0);
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 4:
			p1 = new Point(HALF_WIDTH, 0);
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 5:
			p1 = new Point(img.cols(), 0);
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 6:
			p1 = new Point(img.cols(), 0);
			p2 = new Point(img.cols(), HALF_HEIGHT);
			break;
		case 7:
			p1 = new Point(0, HALF_HEIGHT);
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 8:
			p1 = new Point(img.cols(), HALF_HEIGHT);
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 9:
			p1 = new Point(0, img.rows());
			p2 = new Point(0, HALF_HEIGHT);
			break;
		case 10:
			p1 = new Point(0, img.rows());
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 11:
			p1 = new Point(HALF_WIDTH, img.rows());
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 12:
			p1 = new Point(img.cols(), img.rows());
			p2 = new Point(HALF_WIDTH, HALF_HEIGHT);
			break;
		case 13:
			p1 = new Point(img.cols(), HALF_HEIGHT);
			p2 = new Point(img.cols(), img.rows());
			break;
		case 14:
			p1 = new Point(0, img.rows());
			p2 = new Point(HALF_WIDTH, img.rows());
			break;
		case 15:
			p1 = new Point(img.cols(), img.rows());
			p2 = new Point(HALF_WIDTH, img.rows());
			break;
		}

		Core.line(img, p1, p2, new Scalar(0, 255, 0), 3);
	}

	/**
	 * Determines which flags ought to be set
	 * 
	 * @param angles
	 *            list of normalized angles from the hough transforms
	 * @param lengths
	 *            list of normalized lengths from the hough transforms
	 * @param yintercepts
	 *            list of y-intercepts from the hough transforms
	 * @param xintercepts
	 *            list of x-intercepts from the hough transforms
	 * @return set of flags detected.
	 */
	public static boolean[] setFlags(List<Double> angles, List<Double> lengths,
			List<Double> yintercepts, List<Double> xintercepts) {
		double ONE_THIRD = 1.0 / 3;
		double TWO_THIRDS = 2.0 / 3;

		boolean flags[] = new boolean[16];
		for (int i = 0; i < flags.length; i++) {
			flags[i] = false;
		}

		for (int i = 0; i < angles.size(); i++) {
			double ang = angles.get(i);
			double len = lengths.get(i);
			double yintercept = yintercepts.get(i);
			double xintercept = xintercepts.get(i);

			if (ang == 0) {
				boolean left = false;
				boolean right = false;
				// horizontal
				if (xintercept < ONE_THIRD) {
					// left
					left = true;
				}
				if (xintercept >= TWO_THIRDS || xintercept + len > TWO_THIRDS) {
					// right
					right = true;
				}

				if (yintercept < ONE_THIRD) {
					// top
					flags[0] = flags[0] || left;
					flags[1] = flags[1] || right;
				} else if (yintercept < TWO_THIRDS) {
					// middle
					flags[7] = flags[7] || left;
					flags[8] = flags[8] || right;
				} else {
					// bottom
					flags[14] = flags[14] || left;
					flags[15] = flags[15] || right;
				}
			} else if (Double.isNaN(ang)) {
				boolean top = false;
				boolean bottom = false;
				// vertical
				if (yintercept < ONE_THIRD) {
					top = true;
				}
				if (yintercept >= TWO_THIRDS || yintercept + len >= TWO_THIRDS) {
					bottom = true;
				}

				if (xintercept < ONE_THIRD) {
					// left
					flags[2] = flags[2] || top;
					flags[9] = flags[9] || bottom;
				} else if (xintercept < TWO_THIRDS) {
					// middle
					flags[4] = flags[4] || top;
					flags[11] = flags[11] || bottom;
				} else {
					// right
					flags[6] = flags[6] || top;
					flags[13] = flags[13] || bottom;
				}
			} else {
				// is diagonal

				boolean left = false;
				boolean right = false;

				if (ang > 0) {
					// upward sloping /
					if (yintercept > TWO_THIRDS) {
						left = true;
					}
					if (yintercept < ONE_THIRD
							|| yintercept - len / Math.sqrt(2) < ONE_THIRD) {
						right = true;
					}
					flags[10] = flags[10] || left;
					flags[5] = flags[5] || right;
				} else {
					// downward sloping \
					if (yintercept < ONE_THIRD) {
						left = true;
					}

					if (yintercept > 0.5
							|| yintercept + len / Math.sqrt(2) > 0.5) {
						right = true;
					}
					flags[3] = flags[3] || left;
					flags[12] = flags[12] || right;
				}
			}
		}

		return flags;
	}

	/**
	 * Constructs the BST of a formula by position
	 * 
	 * @param sym
	 *            List of symbol names
	 * @param symRect
	 *            List of symbol bounding boxes
	 * @return Baseline structure tree to find formulae.
	 */
	public static BaselineStructureTree constructFormula(
			final List<String> sym, final List<Rect> symRect) {
		if (sym.isEmpty()) {
			return null;
		}

		final SymbolList sl = new SymbolList();
		for (int i = 0; i < sym.size(); i++) {
			final String s = sym.get(i);
			final Rect r = symRect.get(i);
			sl.add(new SymbolNode(r.x, r.y, r.x + r.width, r.y + r.height, s));
		}
		sl.sort();
		final BaselineStructureTree bst = new BaselineStructureTree(sl);
		try {
			bst.buildTree();
		} catch (Exception e) {
			return null;
		}
		return bst;
	}

	/**
	 * Processes a given file with OCR.
	 * 
	 * @param fname
	 *            the file to process
	 */
	public static void process(String fname) {
		Mat img = Highgui.imread(fname, 0);

		Mat smallerImg = new Mat();
		double aspect_ratio = img.cols() * 1.0 / img.rows();

		int size = 1024;
		Imgproc.resize(img, smallerImg, new Size(size, size / aspect_ratio), 0,
				0, 0);

		Mat thresholdImg = new Mat();
		Imgproc.threshold(smallerImg, thresholdImg, 90, 255,
				Imgproc.THRESH_BINARY_INV);

		// imshow(thresholdImg, "Thresholded Image");

		Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
				new Size(3, 3));
		Imgproc.dilate(thresholdImg, thresholdImg, element);

		List<MatOfPoint> contourList = new ArrayList<MatOfPoint>();

		Mat hierarchy = new Mat();
		Mat tempImg = new Mat();
		thresholdImg.copyTo(tempImg);
		Imgproc.findContours(tempImg, contourList, hierarchy,
				Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

		System.out.println("Contours: " + contourList.size());

		Scalar color = new Scalar(255, 0, 255);

		List<List<Integer>> topLevelList = new ArrayList<List<Integer>>();

		for (int i = 0; i < contourList.size(); i++) {
			double vals[] = hierarchy.get(0, i);

			if ((int) vals[PARENT] < 0) {
				Mat c = contourList.get(i);
				if (Imgproc.contourArea(c) > 10) {
					List<Integer> obj = new ArrayList<Integer>();
					obj.add(i);

					int ptr = (int) vals[CHILD];
					while (ptr != -1) {
						obj.add(ptr);
						ptr = (int) hierarchy.get(0, ptr)[NEXT];
					}
					topLevelList.add(obj);
				}
			}
		}

		Mat drawn = Mat.zeros(thresholdImg.size(), CvType.CV_8UC3);
		for (List<Integer> obj : topLevelList) {
			for (Integer i : obj) {
				Imgproc.drawContours(drawn, contourList, i, color);
			}
		}
		// imshow(drawn, "Contours");

		List<Rect> rectangles = new ArrayList<Rect>();
		List<String> keys = new ArrayList<String>();

		Mat houghImg = Mat.zeros(thresholdImg.size(), CvType.CV_8UC3);

		for (List<Integer> obj : topLevelList) {
			MatOfPoint outline = contourList.get(obj.get(0));
			Rect r = Imgproc.boundingRect(outline);
			Mat raw = Mat.zeros(r.size(), CvType.CV_8UC3);

			raw = thresholdImg.submat(r);

			Mat lines = new Mat();
			Imgproc.HoughLinesP(raw, lines, 2, Math.PI / 4, 30);

			Mat hough = houghImg.submat(r);
			List<Double> angles = new ArrayList<Double>();
			List<Double> xintercepts = new ArrayList<Double>();
			List<Double> yintercepts = new ArrayList<Double>();
			List<Double> lengths = new ArrayList<Double>();

			for (int i = 0; i < lines.cols(); i++) {
				double val[] = lines.get(0, i);

				int dy = (int) (val[1] - val[3]);
				int dx = (int) (val[0] - val[2]);
				double len = Math.sqrt(dy * dy + dx * dx);

				double percent = 0.0;
				double divisor = 0;
				double rad;
				double yintercept;
				double xintercept;

				if (dy == 0) {
					divisor = r.width;
					yintercept = val[1] / r.height; // y-intercept in %
					xintercept = Math.min(val[0], val[2]) / r.width; // x-intercept
																		// lower
					rad = 0;
				} else if (dx == 0) {
					divisor = r.height;
					yintercept = Math.min(val[1], val[3]) / r.height; // y-intercept
																		// lower
					xintercept = val[0] / r.width; // x-intercept in %
					rad = Double.NaN;
				} else {
					rad = Math.atan2(dy, dx);
					// This is going to be a diagonal at n * pi / 4 because of
					// magic
					divisor = Math
							.sqrt(r.width * r.width + r.height * r.height);
					yintercept = (val[1] - dy * val[0] / dx) / r.height;
					xintercept = (val[0] - dx * val[1] / dy) / r.width;
				}
				percent = Math.abs(len / divisor);

				if (percent > 0.333) {
					angles.add(rad);
					yintercepts.add(yintercept);
					xintercepts.add(xintercept);
					lengths.add(percent);
					Core.line(hough, new Point(val[0], val[1]), new Point(
							val[2], val[3]), new Scalar(255, 0, 255), 2);
				}
			}

			boolean flags[] = setFlags(angles, lengths, yintercepts,
					xintercepts);
			String key = match(flags);
			for (int i = 0; i < flags.length; i++) {
				if (flags[i]) {
					drawFlag(hough, i);
				}
			}
			System.out.println("Is it... " + key + "?");

			rectangles.add(r);
			keys.add(key);
		}

		if (keys.size() > 0) {
			BaselineStructureTree bst = constructFormula(keys, rectangles);
			if (bst != null) {
				System.out.println("Latex");
				System.out.println(bst.interpretLaTeX());
				System.out.println("Mathematica");
				System.out.println(bst.interpretMathematica());
			}
		} else {
			System.err.println("Could not recognize image");
		}
		// imshow(houghImg, "Hough transformed image");
		// imshow(thresholdImg, "post-processed image");
	}

	public static void main(String[] args) throws IOException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		GLOBAL_MAPPING = readMappingsFromFile("config/mappings.txt");
		process("test/really_hard.jpg");
	}
}