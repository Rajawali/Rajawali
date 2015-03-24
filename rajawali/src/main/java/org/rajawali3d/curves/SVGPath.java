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
package org.rajawali3d.curves;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.util.RajLog;
import android.content.Context;

/**
 * Parses a path from an SVG string. Please bear in mind that this is not a full XML parser.
 * This is also an incomplete implementation. If you encounter anything that's missing please
 * post an issue on Github and we'll add whatever you're after.
 * 
 * What you can use is either a path {@link String}:
 * 
 * <pre><code>
 * String path = "M22.395-127.223c-4.492,11.344-4.688,33.75,0,44.883" +
 * 	"c-11.328-4.492-33.656-4.579-44.789,0.109c4.491-11.354,4.688-33.75,0-44.892" +
 * 	"C-11.066-122.63,11.262-122.536,22.395-127.223z";
 * 
 * SVGPath svgPath = new SVGPath();
 * List<CompoundCurve3D> paths = svgPath.parseString(path);
 * </code></pre>
 * 
 * Or you could use a string resource from the "raw" folder:
 * 
 * <pre><code>
 * SVGPath svgPath = new SVGPath();
 * List<CompoundCurve3D> paths = svgPath.parseResourceString(mContext, R.raw.lavatories_svg_path);
 * </code></pre>
 *
 * To draw a path:
 * 
 * <pre><code>
 * for(int i=0; i<paths.size(); i++)
 * {
 * 	ICurve3D subPath = paths.get(i);
 * 	Stack<Vector3> points = new Stack<Vector3>();
 * 	int subdiv = 1000;
 * 	for(int j=0; j<=subdiv; j++)
 * 	{
 * 		points.add(subPath.calculatePoint(j / subdiv));
 * 	}
 * 	pathPoints.add(points);
 * 	Line3D line = new Line3D(points, 1);
 * 	SimpleMaterial material = new SimpleMaterial();
 * 	material.setUseSingleColor(true);
 * 	line.setMaterial(material);
 *	getCurrentScene().addChild(line);
 * }
 * </code></pre>
 * 
 * @author dennis.ippel
 *
 */
public class SVGPath {

	private Vector3 mPreviousPoint;
	private Vector3 mStartPoint;
	private Vector3 mPreviousControlPoint;
	private SVGCommand mCurrentCommand;
	private SVGCommand mPreviousCommand;
	private boolean mCurrentCommandIsRelative;

	private enum SVGCommand
	{
		MOVE_TO,
		CLOSE_PATH,
		LINE_TO,
		CURVE_TO,
		SMOOTH_CURVE_TO,
		HORIZONTAL,
		VERTICAL
	}

	/**
	 * Parses an SVG Path string
	 * 
	 * @param pathString
	 * @return
	 */
	public List<CompoundCurve3D> parseString(String pathString)
	{
		mPreviousPoint = new Vector3();
		mStartPoint = new Vector3();
		mPreviousControlPoint = new Vector3();
		return pathStringToLine(pathString);
	}

	/**
	 * Parses an SVG resource path string. The string needs to sit in a text file in "res/raw".
	 * 
	 * @param context
	 * @param resourceId
	 * @return
	 */
	public List<CompoundCurve3D> parseResourceString(Context context, int resourceId)
	{
		InputStream rawResource = context.getResources().openRawResource(resourceId);
		String l;
		BufferedReader r = new BufferedReader(new InputStreamReader(rawResource));
		StringBuilder s = new StringBuilder();
		try {
			while ((l = r.readLine()) != null) {
				s.append(l + "\n");
			}
			rawResource.close();
			r.close();
		} catch (IOException e) {}
		return parseString(s.toString());
	}

	private List<CompoundCurve3D> pathStringToLine(String path)
	{
		if (path == null || path.length() == 0)
			throw new RuntimeException("Path cannot be null or empty.");

		List<CompoundCurve3D> paths = new ArrayList<CompoundCurve3D>();
		CompoundCurve3D bezierPath = new CompoundCurve3D();
		mPreviousCommand = SVGCommand.CLOSE_PATH;

		path = path.replaceAll("\\s+", "");
		path = path.replaceAll("\\d-", "$0,-");
		path = path.replaceAll("-,", ",");
		path = path.replaceAll("[a-zA-Z]", "\n$0\n");

		String[] components = path.split("\n");

		for (int i = 0; i < components.length; i++)
		{
			if (components[i].length() == 0)
				continue;

			if (components[i].matches("[a-zA-Z]"))
			{
				inspectCommand(components[i].charAt(0));

				boolean shouldClosePath = mCurrentCommand == SVGCommand.CLOSE_PATH ||
						(mCurrentCommand == SVGCommand.MOVE_TO && mPreviousCommand != SVGCommand.CLOSE_PATH);

				if (shouldClosePath)
				{
					SVGCommand pc = mCurrentCommand;
					boolean pr = mCurrentCommandIsRelative;

					mCurrentCommand = SVGCommand.LINE_TO;
					mCurrentCommandIsRelative = false;
					inspectValues(bezierPath, mStartPoint.x + "," + -mStartPoint.y);
					paths.add(bezierPath);
					bezierPath = new CompoundCurve3D();

					if (pc == SVGCommand.MOVE_TO)
					{
						mCurrentCommand = pc;
						mCurrentCommandIsRelative = pr;
					}
				}

				mPreviousCommand = mCurrentCommand;
			}
			else
			{
				inspectValues(bezierPath, components[i]);
			}
		}

		return paths;
	}

	private void inspectValues(CompoundCurve3D bezierPath, String values)
	{
		String[] vals = values.split(",");
		Vector3 c, p, cp1, cp2;

		if (vals.length == 0)
			throw new RuntimeException("Empty values found.");

		switch (mCurrentCommand)
		{
		case MOVE_TO:
			c = new Vector3(Double.parseDouble(vals[0]), -Double.parseDouble(vals[1]), 0);
			p = mCurrentCommandIsRelative ? c.addAndSet(mPreviousPoint, c) : c;
			break;
		case VERTICAL:
			c = new Vector3(0, -Double.parseDouble(vals[0]), 0);
			if (mCurrentCommandIsRelative)
				p = c.addAndSet(mPreviousPoint, c);
			else
			{
				c.x = mPreviousPoint.x;
				p = c;
			}
			bezierPath.addCurve(new LinearBezierCurve3D(mPreviousPoint.clone(), p));
			break;
		case HORIZONTAL:
			c = new Vector3(Double.parseDouble(vals[0]), 0, 0);
			if (mCurrentCommandIsRelative)
				p = c.addAndSet(mPreviousPoint, c);
			else
			{
				c.y = mPreviousPoint.y;
				p = c;
			}
			bezierPath.addCurve(new LinearBezierCurve3D(mPreviousPoint.clone(), p));
			break;
		case CURVE_TO:
			c = new Vector3(Double.parseDouble(vals[4]), -Double.parseDouble(vals[5]), 0);
			p = mCurrentCommandIsRelative ? c.addAndSet(mPreviousPoint, c) : c;
			cp1 = new Vector3(Double.parseDouble(vals[0]), -Double.parseDouble(vals[1]), 0);
			if (mCurrentCommandIsRelative)
				cp1.add(mPreviousPoint);
			cp2 = new Vector3(Double.parseDouble(vals[2]), -Double.parseDouble(vals[3]), 0);
			if (mCurrentCommandIsRelative)
				cp2.add(mPreviousPoint);
			mPreviousControlPoint.setAll(cp2);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case SMOOTH_CURVE_TO:
			c = new Vector3(Double.parseDouble(vals[2]), -Double.parseDouble(vals[3]), 0);
			p = mCurrentCommandIsRelative ? c.addAndSet(mPreviousPoint, c) : c;
			cp1 = reflect(mPreviousControlPoint, mPreviousPoint);
			cp2 = new Vector3(Double.parseDouble(vals[0]), -Double.parseDouble(vals[1]), 0);
			if (mCurrentCommandIsRelative)
				cp2.add(mPreviousPoint);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case LINE_TO:
			c = new Vector3(Double.parseDouble(vals[0]), -Double.parseDouble(vals[1]), 0);
			p = mCurrentCommandIsRelative ? c.addAndSet(mPreviousPoint, c) : c;
			bezierPath.addCurve(new LinearBezierCurve3D(mPreviousPoint.clone(), p));
			break;
		default:
			return;
		}
		if (bezierPath.getNumCurves() == 0)
			mStartPoint.setAll(p);
		mPreviousPoint.setAll(p);
	}

	private Vector3 reflect(Vector3 point, Vector3 mirror)
	{
		double x = mirror.x + (mirror.x - point.x);
		double y = mirror.y + (mirror.y - point.y);

		return new Vector3(x, y, 0);
	}
	
	private void inspectCommand(char command)
	{
		switch (command)
		{
		case 'M':
		case 'm':
			mCurrentCommand = SVGCommand.MOVE_TO;
			mCurrentCommandIsRelative = command == 'm';
			break;
		case 'Z':
		case 'z':
			mCurrentCommand = SVGCommand.CLOSE_PATH;
			break;
		case 'L':
		case 'l':
			mCurrentCommand = SVGCommand.LINE_TO;
			mCurrentCommandIsRelative = command == 'l';
			break;
		case 'H':
		case 'h':
			mCurrentCommand = SVGCommand.HORIZONTAL;
			mCurrentCommandIsRelative = command == 'h';
			break;
		case 'V':
		case 'v':
			mCurrentCommand = SVGCommand.VERTICAL;
			mCurrentCommandIsRelative = command == 'v';
			break;
		case 'C':
		case 'c':
			mCurrentCommand = SVGCommand.CURVE_TO;
			mCurrentCommandIsRelative = command == 'c';
			break;
		case 'S':
		case 's':
			mCurrentCommand = SVGCommand.SMOOTH_CURVE_TO;
			mCurrentCommandIsRelative = command == 's';
			break;
		default:
			RajLog.e("SVG command not recognized: " + command);
		}
	}
}
