package dlx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DLXSymmetry {

	/** Reference to DLXConfig. Used for dealing with duplicated tiles. */
	private static DLXConfig Config;

	/**
	 * Determine if two solutions are symmetric
	 * @param cur
	 * @param pattern
	 * @return
	 */
	public static boolean isAsymmetric(int cur[][], int pattern[][]) {

		/* Width == height. At most 8 symmetric patterns.*/
		if (cur.length == cur[0].length) {
			if (equalValue(cur, pattern)
					||equalValue(cur, rotateC1(pattern))
					||equalValue(cur, rotateC2(pattern))
					||equalValue(cur, rotateC3(pattern))
					||equalValue(cur, frotateC0(pattern))
					||equalValue(cur, frotateC1(pattern))
					||equalValue(cur, frotateC2(pattern))
					||equalValue(cur, frotateC3(pattern)))
				return false;
			else
				return true;
		}
		/* Width != height. At most 4 symmetric patterns.*/
		else {
			if (equalValue(cur, pattern)
					||equalValue(cur, rotateC2(pattern))
					||equalValue(cur, frotateC0(pattern))
					||equalValue(cur, frotateC2(pattern)))
				return false;
			else
				return true;
		}
	}

	/**
	 * Determine if a new solution is symmetric to existing solutions.
	 * @param cur
	 * @param pattern
	 * @param config
	 * @return
	 */
	public static boolean isAsymmetricList(int cur[][], List<int[][]> pattern,
			DLXConfig config) {
		Config = config;
		for (int i = 0; i < pattern.size(); i++) {
			if (!isAsymmetric(cur, pattern.get(i))) {
				if (config.verb) System.out.println("Symmetric solution.");
				return false;
			}
		}
		return true;
	}


	/**
	 * Flip the matrix horizontally, rotate clockwise by 0.
	 *
	 * @return result
	 */
	private static  int[][] frotateC0(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[w][l];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[i][l - j - 1] = data[i][j];
		}
		return result;
	}

	/**
	 * Flip the matrix horizontally, rotate clockwise by pi/2.
	 *
	 * @return result
	 */
	private static  int[][] frotateC1(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[l][w];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[j][w - i - 1] = data[i][l - 1 - j];
		}
		return result;
	}

	/**
	 * Flip the matrix horizontally, rotate clockwise by pi.
	 *
	 * @return result
	 */
	private static  int[][] frotateC2(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[w][l];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[w - i - 1][l - j - 1] = data[i][l - 1 - j];
		}
		return result;
	}

	/**
	 * Flip the matrix horizontally, rotate clockwise by 3*pi/2.
	 *
	 * @return result
	 */
	private static  int[][] frotateC3(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[l][w];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[l - 1 - j][i] = data[i][l - 1 - j];
		}
		return result;
	}

	/**
	 * Rotate the matrix clockwise by 0.
	 *
	 * @return result
	 */
	@SuppressWarnings("unused")
	private static  int[][] rotateC0(int data[][]) {
		return data;
	}

	/**
	 * Rotate the matrix clockwise by pi/2.
	 *
	 * @return result
	 */
	private static  int[][] rotateC1(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[l][w];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[j][w - i - 1] = data[i][j];
		}
		return result;
	}

	/**
	 * Rotate the matrix clockwise by pi.
	 *
	 * @return result
	 */
	private static int[][] rotateC2(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[w][l];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[w - i - 1][l - j - 1] = data[i][j];
		}
		return result;
	}

	/**
	 * Rotate the matrix clockwise by 3*pi/2.
	 *
	 * @return result
	 */
	private static  int[][] rotateC3(int data[][]) {
		int w = data.length;
		int l = data[0].length;
		int result[][] = new int[l][w];

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < l; j++)
				result[l - 1 - j][i] = data[i][j];
		}
		return result;
	}


	/**
	 * Determine if two 2D int arrays are equal
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean equalValue(int[][] a, int[][] b) {
		if (a.length != b.length || a[0].length != b[0].length)
			return false;

		// For detecting tile duplication
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();

		for (int i = 0; i < a.length; i++) {
			for (int j = 0; j < a[0].length; j++) {

				/* If there exists duplicated tiles, a map is needed. */
				if (a[i][j] >= 0 && Config.eliminateDuplica()) {

					if (Config.duplica()[a[i][j]] == a[i][j]) { //unique tile
						if (a[i][j] != b[i][j]) return false;
					} else { //duplicated tile
						if (map.containsKey(a[i][j])) {
							if (map.get(a[i][j]) != b[i][j]) return false;
						} else {
							boolean same = false;
							if (a[i][j] == b[i][j]) { // map to itself
								same = true;
								map.put(a[i][j], b[i][j]);
							} else for (int k = Config.duplica()[a[i][j]];
											k != a[i][j];
											k = Config.duplica()[k]) {
								if (k == b[i][j]) {
									same = true;
									map.put(a[i][j], b[i][j]);
									break;
								}
							}
							if (!same) return false;
						}
					}

				} else { //just compare them
					if (a[i][j] != b[i][j]) {
						return false;
					}
				}

			}
		}
		return true;
	}

	public static void printMatrix(int d[][]) {
		System.out.println();
		for (int i = 0; i < d.length; i++)
			System.out.println(Arrays.toString(d[i]));
	}

}
