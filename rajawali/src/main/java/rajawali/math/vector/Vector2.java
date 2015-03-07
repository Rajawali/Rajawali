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
package rajawali.math.vector;

public class Vector2 {
	private double mX;
	private double mY;
	
	public Vector2() {
		
	}
	
	public Vector2(double x, double y) {
		mX = x;
		mY = y;
	}
	
	public void setX(double x)
	{
		mX = x;
	}
	
	public double getX() {
		return mX;
	}
	
	public void setY(double y)
	{
		mY = y;
	}
	
	public double getY()
	{
		return mY;
	}
	
	public void setAll(double x, double y)
	{
		mX = x;
		mY = y;
	}
	
	public Vector2(String[] vals) {
		mX = Float.parseFloat(vals[0]);
		mY = Float.parseFloat(vals[1]);
	}
}
