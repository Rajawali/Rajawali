/**
 * Copyright 2013 Dennis Ippel
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.rajawali3d.terrain;

import org.rajawali3d.math.vector.Vector3;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

/**
 * Is a Factory Class to generate Terrain
 * 
 * @author Ivan Battistella (info@fenicesoftware.com)
 * 
 */

public class TerrainGenerator {

	/**
	 * Generate a Square Terrain using Bitmap as depth map (green component of ARGB)
	 * 
	 * @param Parameters
	 *            object that specify: ARGB Bitmap (R is temparature G is depth, A and B not used) Color Bitmpa
	 *            (Optional) Number of divisions Scale of x,y,z coordinate TextureMult Specify the grid/texture relation
	 *            Basecolor start color in relation with depth Middlecolor middle color Upcolor max depth color
     * @param createVBOs
	 * @return
	 */
	public static SquareTerrain createSquareTerrainFromBitmap(SquareTerrain.Parameters prs, boolean createVBOs) {

		int divisions = prs.divisions;

		if (!((prs.divisions != 0) && ((prs.divisions & (prs.divisions - 1)) == 0))) {
			throw new RuntimeException("Divisions must be x^2");
		}

		double[][] terrain = new double[divisions + 1][divisions + 1];
		double[][] temperature = new double[divisions + 1][divisions + 1];
		Vector3[][] normals = new Vector3[divisions + 1][divisions + 1];

		boolean useColorBitmap = prs.colorMapBitmap != null;

		int colorpixels[] = null;
		Bitmap bnew = Bitmap.createBitmap(divisions + 1, divisions + 1, Bitmap.Config.ARGB_8888);

		Canvas cnv = new Canvas(bnew);
		cnv.drawBitmap(prs.heightMapBitmap,
				new Rect(0, 0, prs.heightMapBitmap.getWidth(), prs.heightMapBitmap.getHeight()), new Rect(0, 0,
						divisions + 1, divisions + 1), null);

		int pixels[] = new int[(divisions + 1) * (divisions + 1)];
		bnew.getPixels(pixels, 0, divisions + 1, 0, 0, divisions + 1, divisions + 1);

		if (useColorBitmap) {
			colorpixels = new int[(divisions + 1) * (divisions + 1)];
			Paint clearPaint = new Paint();
			clearPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
			cnv.drawRect(0, 0, prs.colorMapBitmap.getWidth(), prs.colorMapBitmap.getHeight(), clearPaint);
			cnv.drawBitmap(prs.colorMapBitmap,
					new Rect(0, 0, prs.colorMapBitmap.getWidth(), prs.colorMapBitmap.getHeight()), new Rect(0, 0,
							divisions + 1, divisions + 1), null);
			bnew.getPixels(colorpixels, 0, divisions + 1, 0, 0, divisions + 1, divisions + 1);
		}
		bnew.recycle();

		int color;
		int cols = divisions + 1;
		double min, max;
		terrain[0][0] = Color.green(0) / 255f * prs.scale.y;
		min = max = terrain[0][0];
		double alt;
		double temp;
		float oneover255 = 1f / 255f;

		for (int i = 0; i <= divisions; ++i) {
			for (int j = 0; j <= divisions; ++j) {
				color = pixels[i + j * cols];
				alt = Color.green(color) * oneover255 * prs.scale.y;
				temp = Color.red(color) * oneover255 * (prs.maxTemp - prs.minTemp) + prs.minTemp;
				if (i > 0 && j > 0) {
					temp = ((temperature[i - 1][j] + temperature[i][j - 1]) * 0.5f + temp) * 0.5f;
					alt = ((terrain[i - 1][j] + terrain[i][j - 1]) * 0.5f + alt) * 0.5f;
				}
				else if (j > 0) {
					temp = (temperature[i][j - 1] + temp) * 0.5f;
					alt = (terrain[i][j - 1] + alt) * 0.5f;
				}
				else if (i > 0) {
					temp = (temperature[i - 1][j] + temp) * 0.5f;
					alt = (terrain[i - 1][j] + alt) * 0.5f;
				}
				temperature[i][j] = temp;
				terrain[i][j] = alt;

				if (alt < min)
					min = alt;
				else if (alt > max)
					max = alt;
				normals[i][j] = new Vector3(0f, 1f, 0f);
			}
		}

		Vector3 scale = prs.scale;

		Vector3 v0 = new Vector3();
		Vector3 v1 = new Vector3();
		Vector3 v2 = new Vector3();
		Vector3 na = new Vector3();
		Vector3 nb = new Vector3();
		Vector3 nc = new Vector3();
		Vector3 nd = new Vector3();

		for (int x = 1; x < divisions; x++) {
			for (int z = 1; z < divisions; z++) {
				// O z-1
				// /|\
				// / | \
				// x-1 O--O--O x+1
				// \ | /
				// \|/
				// O z+1
				v0.x = (x - 1) * scale.x;
				v0.z = z * scale.z;
				v0.y = terrain[x - 1][z];

				v1.x = x * scale.x;
				v1.z = (z - 1) * scale.z;
				v1.y = terrain[x][z - 1];

				v2.x = x * scale.x;
				v2.z = z * scale.z;
				v2.y = terrain[x][z];

				na = v1.subtract(v0).cross(v2.subtract(v0));
				na.inverse();

				v0.x = x * scale.x;
				v0.z = z * scale.z;
				v0.y = terrain[x][z];

				v1.x = x * scale.x;
				v1.z = (z - 1) * scale.z;
				v1.y = terrain[x][z - 1];

				v2.x = (x + 1) * scale.x;
				v2.z = z * scale.z;
				v2.y = terrain[x + 1][z];

				nb = v1.subtract(v0).cross(v2.subtract(v0));
				nb.inverse();

				v0.x = x * scale.x;
				v0.z = z * scale.z;
				v0.y = terrain[x][z];

				v1.x = (x + 1) * scale.x;
				v1.z = z * scale.z;
				v1.y = terrain[x + 1][z];

				v2.x = x * scale.x;
				v2.z = (z + 1) * scale.z;
				v2.y = terrain[x][z + 1];

				nc = v1.subtract(v0).cross(v2.subtract(v0));
				nc.inverse();

				v0.x = x * scale.x;
				v0.z = z * scale.z;
				v0.y = terrain[x][z];

				v1.x = x * scale.x;
				v1.z = (z + 1) * scale.z;
				v1.y = terrain[x][z + 1];

				v2.x = (x - 1) * scale.x;
				v2.z = z * scale.z;
				v2.y = terrain[x - 1][z];

				nd = v1.subtract(v0).cross(v2.subtract(v0));
				nd.inverse();

				normals[x][z].y = 0f; // pre-set to 1
				normals[x][z].add(na);
				normals[x][z].add(nb);
				normals[x][z].add(nc);
				normals[x][z].add(nd);

			}
		}

		SquareTerrain sq = new SquareTerrain(divisions, terrain, normals, temperature, scale.x, scale.z);

		float[] vertices = new float[(divisions + 1) * (divisions + 1) * 3];
		float[] nors = new float[(divisions + 1) * (divisions + 1) * 3];
		float[] colors = new float[(divisions + 1) * (divisions + 1) * 4];
		float[] textureCoords = new float[(divisions + 1) * (divisions + 1) * 2];
		int[] indices = new int[(divisions) * (divisions) * 6];
		int ii = 0;
		int nn = 0;
		int tt = 0;
		int xx = 0;
		int cc = 0;
		double maxtt = 1f / (divisions + 1);

		double xmid = (divisions * scale.x) / 2f;
		double zmid = (divisions * scale.z) / 2f;
		double percalt = 0;
		float r, g, b, a;
		a = 1f;

		float a_basecolor = (float) Color.alpha(prs.basecolor) * oneover255;
		float a_middlecolor = (float) Color.alpha(prs.middlecolor) * oneover255;
		float a_upcolor = (float) Color.alpha(prs.upcolor) * oneover255;

		float g_basecolor = (float) Color.green(prs.basecolor) * oneover255;
		float g_middlecolor = (float) Color.green(prs.middlecolor) * oneover255;
		float g_upcolor = (float) Color.green(prs.upcolor) * oneover255;

		float b_basecolor = (float) Color.blue(prs.basecolor) * oneover255;
		float b_middlecolor = (float) Color.blue(prs.middlecolor) * oneover255;
		float b_upcolor = (float) Color.blue(prs.upcolor) * oneover255;

		float r_basecolor = (float) Color.red(prs.basecolor) * oneover255;
		float r_middlecolor = (float) Color.red(prs.middlecolor) * oneover255;
		float r_upcolor = (float) Color.red(prs.upcolor) * oneover255;

		int bmpcolor;
		float a_bmp;
		float r_bmp;
		float g_bmp;
		float b_bmp;
		for (int i = 0; i <= divisions; ++i) {
			for (int j = 0; j <= divisions; ++j) {

				vertices[ii++] = (float) (i * scale.x - xmid);
				vertices[ii++] = (float) terrain[i][j];
				vertices[ii++] = (float) (j * scale.z - zmid);

				percalt = sq.getPercAltitude(i, j);

				if (percalt < 0.5) {
					temp = (percalt - 0.0) * 2;
					r = (float) (r_basecolor + (r_middlecolor - r_basecolor) * temp);
					g = (float) (g_basecolor + (g_middlecolor - g_basecolor) * temp);
					b = (float) (b_basecolor + (b_middlecolor - b_basecolor) * temp);
					a = (float) (a_basecolor + (a_middlecolor - a_basecolor) * temp);
				}
				else {
					temp = (percalt - 0.5) * 2;
					r = (float) (r_middlecolor + (r_upcolor - r_middlecolor) * temp);
					g = (float) (g_middlecolor + (g_upcolor - g_middlecolor) * temp);
					b = (float) (b_middlecolor + (b_upcolor - b_middlecolor) * temp);
					a = (float) (a_middlecolor + (a_upcolor - a_middlecolor) * temp);

				}
				if (useColorBitmap) {
					bmpcolor = colorpixels[i + j * cols];
					a_bmp = (float) Color.alpha(bmpcolor) * oneover255;

					r_bmp = (float) Color.red(bmpcolor) * oneover255;
					g_bmp = (float) Color.green(bmpcolor) * oneover255;
					b_bmp = (float) Color.blue(bmpcolor) * oneover255;

					r = r * (1f - a_bmp) + a_bmp * r_bmp;
					g = g * (1f - a_bmp) + a_bmp * g_bmp;
					b = b * (1f - a_bmp) + a_bmp * b_bmp;

				}

				r = r < 0f ? 0f : r;
				r = r > 1f ? 1f : r;
				g = g < 0f ? 0f : g;
				g = g > 1f ? 1f : g;
				b = b < 0f ? 0f : b;
				b = b > 1f ? 1f : b;
				a = a < 0f ? 0f : a;
				a = a > 1f ? 1f : a;

				colors[cc++] = r;
				colors[cc++] = g;
				colors[cc++] = b;
				colors[cc++] = a;

				normals[i][j].normalize();
				nors[nn++] = (float) normals[i][j].x;
				nors[nn++] = (float) normals[i][j].y;
				nors[nn++] = (float) normals[i][j].z;

				textureCoords[tt++] = (float) (i * maxtt * prs.textureMult);
				textureCoords[tt++] = (float) (j * maxtt * prs.textureMult);

			}
		}

		for (int i = 0; i < divisions; i += 2) {
			for (int j = 0; j < divisions; j += 2) {
				// O--O--O--O--O
				// |A/|\D| /|\ |
				// |/B|C\|/ | \|
				// O--O--O--O--O
				// |\F|G/|\ | /|
				// |E\|/H| \|/ |
				// O--O--O--O--O
				// A
				indices[xx++] = (i) + (j) * cols;
				indices[xx++] = (i + 1) + (j) * cols;
				indices[xx++] = (i) + (j + 1) * cols;
				// B
				indices[xx++] = (i + 1) + (j) * cols;
				indices[xx++] = (i + 1) + (j + 1) * cols;
				indices[xx++] = (i) + (j + 1) * cols;
				// C
				indices[xx++] = (i + 1) + (j) * cols;
				indices[xx++] = (i + 2) + (j + 1) * cols;
				indices[xx++] = (i + 1) + (j + 1) * cols;
				// D
				indices[xx++] = (i + 1) + (j) * cols;
				indices[xx++] = (i + 2) + (j) * cols;
				indices[xx++] = (i + 2) + (j + 1) * cols;
				// E
				indices[xx++] = (i) + (j + 1) * cols;
				indices[xx++] = (i + 1) + (j + 2) * cols;
				indices[xx++] = (i) + (j + 2) * cols;
				// F
				indices[xx++] = (i) + (j + 1) * cols;
				indices[xx++] = (i + 1) + (j + 1) * cols;
				indices[xx++] = (i + 1) + (j + 2) * cols;
				// G
				indices[xx++] = (i + 1) + (j + 1) * cols;
				indices[xx++] = (i + 2) + (j + 1) * cols;
				indices[xx++] = (i + 1) + (j + 2) * cols;
				// H
				indices[xx++] = (i + 2) + (j + 1) * cols;
				indices[xx++] = (i + 2) + (j + 2) * cols;
				indices[xx++] = (i + 1) + (j + 2) * cols;

			}

		}

		sq.setData(vertices, nors, textureCoords, colors, indices, createVBOs);
		nors = null;
		colors = null;
		indices = null;
		textureCoords = null;
		vertices = null;

		return sq;
	}

}
