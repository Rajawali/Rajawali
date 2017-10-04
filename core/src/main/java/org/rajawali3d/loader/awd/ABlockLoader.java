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
package org.rajawali3d.loader.awd;

import java.io.IOException;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderAWD;
import org.rajawali3d.loader.LoaderAWD.IBlockParser;
import org.rajawali3d.util.LittleEndianDataInputStream;

/**
 * Base class for parsing blocks. Blocks are instantiated by the {@link LoaderAWD} directly and are not intended for any
 * other use case.
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public abstract class ABlockLoader implements IBlockParser {

	public Object3D getBaseObject3D() {
		return null;
	}

	protected final void readProperties(LittleEndianDataInputStream dis) throws IOException {
		// Determine the length of the properties
		final long propsLength = dis.readUnsignedInt();

		// TODO need to figure out what uses and needs this so I can better understand implementation

		// skip properties until an implementation can be determined
		dis.skip(propsLength);
	}

}
