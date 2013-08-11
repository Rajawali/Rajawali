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

public class Camera2D extends Camera {
	private double mWidth, mHeight;
	public Camera2D() {
		super();
		mWidth = 1.0f;
		mHeight = 1.0f;
		setZ(4.0f);
		setLookAt(0, 0, 0);
	}

	public void setProjectionMatrix(int widthNotUsed, int heightNotUsed) {
		mProjMatrix.setToOrthographic((-mWidth/2.0)+mPosition.x, (mWidth/2.0)+mPosition.x, (-mHeight/2.0)+mPosition.y, (mHeight/2.0)+mPosition.y, mNearPlane, mFarPlane);
	}
	
	public void setWidth(double width) {
		this.mWidth = width;
	}
	
	public double getWidth() {
		return mWidth;
	}

	public void setHeight(double height) {
		this.mHeight = height;
	}

	public double getHeight() {
		return mHeight;
	}
}
