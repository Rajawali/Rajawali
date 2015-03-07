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
package rajawali;

public class OrthographicCamera extends Camera {
	private double mZoom = 1;
	
	public OrthographicCamera()
	{
		setZ(4.0f);
	}
	
	public void setProjectionMatrix(int width, int height) 
	{
		double aspect = (double) width / (double) height;
		mProjMatrix.setToOrthographic(-aspect, aspect, -1, 1, mNearPlane, mFarPlane);
		mProjMatrix.setCoordinateZoom(mZoom);
	}
	
	public void setZoom(double zoom)
	{
		mZoom = zoom;
		mProjMatrix.setCoordinateZoom(zoom);
	}
	
	public double getZoom()
	{
		return mZoom;
	}
}
