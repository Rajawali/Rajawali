package rajawali.curves;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import rajawali.math.Vector3;
import rajawali.util.RajLog;
import android.content.Context;

/**
 * Parses a path from an SVG string. Please bear in mind that this is not a full XML parser.
 * What you can use is either a path {@link String}:
 * 
 * <code>
 * 
 * </code>
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

	public List<CompoundCurve3D> parseString(String pathString)
	{
		mPreviousPoint = new Vector3();
		mStartPoint = new Vector3();
		mPreviousControlPoint = new Vector3();
		return pathStringToLine(pathString);
	}

	public List<CompoundCurve3D> resourcePathStringToLine(Context context, int resourceId)
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
		return pathStringToLine(s.toString());
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
		float distX, distY;

		if (vals.length == 0)
			throw new RuntimeException("Empty values found.");

		switch (mCurrentCommand)
		{
		case MOVE_TO:
			c = new Vector3(Float.parseFloat(vals[0]), -Float.parseFloat(vals[1]), 0);
			p = mCurrentCommandIsRelative ? Vector3.add(mPreviousPoint, c) : c;
			break;
		case VERTICAL:
			c = new Vector3(0, -Float.parseFloat(vals[0]), 0);
			if (mCurrentCommandIsRelative)
				p = Vector3.add(mPreviousPoint, c);
			else
			{
				c.x = mPreviousPoint.x;
				p = c;
			}
			distY = mPreviousPoint.y - p.y;
			distY /= 3f;
			cp1 = new Vector3(mPreviousPoint.x, mPreviousPoint.y - distY, 0);
			cp2 = new Vector3(p.x, p.y + distY, 0);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case HORIZONTAL:
			c = new Vector3(Float.parseFloat(vals[0]), 0, 0);
			if (mCurrentCommandIsRelative)
				p = Vector3.add(mPreviousPoint, c);
			else
			{
				c.y = mPreviousPoint.y;
				p = c;
			}
			distX = mPreviousPoint.x - p.x;
			distX /= 3f;
			cp1 = new Vector3(mPreviousPoint.x - distX, mPreviousPoint.y, 0);
			cp2 = new Vector3(p.x + distX, p.y, 0);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case CURVE_TO:
			c = new Vector3(Float.parseFloat(vals[4]), -Float.parseFloat(vals[5]), 0);
			p = mCurrentCommandIsRelative ? Vector3.add(mPreviousPoint, c) : c;
			cp1 = new Vector3(Float.parseFloat(vals[0]), -Float.parseFloat(vals[1]), 0);
			if (mCurrentCommandIsRelative)
				cp1.add(mPreviousPoint);
			cp2 = new Vector3(Float.parseFloat(vals[2]), -Float.parseFloat(vals[3]), 0);
			if (mCurrentCommandIsRelative)
				cp2.add(mPreviousPoint);
			mPreviousControlPoint.setAllFrom(cp2);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case SMOOTH_CURVE_TO:
			c = new Vector3(Float.parseFloat(vals[2]), -Float.parseFloat(vals[3]), 0);
			p = mCurrentCommandIsRelative ? Vector3.add(mPreviousPoint, c) : c;
			cp1 = new Vector3(2 * mPreviousPoint.x - mPreviousControlPoint.x, 2 * mPreviousPoint.y
					- mPreviousControlPoint.y, 0);
			cp2 = new Vector3(Float.parseFloat(vals[0]), -Float.parseFloat(vals[1]), 0);
			if (mCurrentCommandIsRelative)
				cp2.add(mPreviousPoint);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		case LINE_TO:
			c = new Vector3(Float.parseFloat(vals[0]), -Float.parseFloat(vals[1]), 0);
			p = mCurrentCommandIsRelative ? Vector3.add(mPreviousPoint, c) : c;
			distX = (mPreviousPoint.x - p.x) / 3;
			distY = (mPreviousPoint.y - p.y) / 3;
			cp1 = new Vector3(mPreviousPoint.x - distX, mPreviousPoint.y - distY, 0);
			cp2 = new Vector3(p.x + distX, p.y + distY, 0);
			bezierPath.addCurve(new CubicBezierCurve3D(mPreviousPoint.clone(), cp1, cp2, p));
			break;
		default:
			return;
		}
		if (bezierPath.getNumCurves() == 0)
			mStartPoint.setAllFrom(p);
		mPreviousPoint.setAllFrom(p);
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
