package rajawali.parser.awd;

import rajawali.Object3D;
import rajawali.parser.LoaderAWD.AWDLittleEndianDataInputStream;
import rajawali.parser.LoaderAWD.BlockHeader;
import rajawali.parser.ParsingException;
import rajawali.util.RajLog;

/**
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 */
public class BlockPrimitiveGeometry extends ABaseObjectBlockParser {

	enum PrimitiveType {
		// These are in order of the specification [1-7] as of 07/09/2013 specification
		PLANE, CUBE, SPHERE, CYLINDER, CONE, CAPSULE, TORUS;
	}

	protected Object3D mBaseObject;
	protected int mPrimitiveType;
	protected String mLookupName;

	@Override
	public Object3D getBaseObject3D() {
		return mBaseObject;
	}

	@SuppressWarnings("unused")
	public void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception {
		// FIXME The primitive type requires more features than what the Rajawali primitives provide.

		// Lookup name, not sure why this is useful.
		mLookupName = dis.readVarString();
		RajLog.d("  Lookup Name: " + mLookupName);

		// Read the primitive type
		mPrimitiveType = dis.readUnsignedByte();
		RajLog.d("  Primitive Type: " + mPrimitiveType);

		// Read primitive properties and construct a new base object determined by the primitive type
		switch (PrimitiveType.values()[mPrimitiveType - 1]) {
		case CUBE: {
			final float width = dis.readFloat();
			final float height = dis.readFloat();
			final float depth = dis.readFloat();
			final int segmentsW = dis.readUnsignedShort();
			final int segmentsH = dis.readUnsignedShort();
			final int segmentsD = dis.readUnsignedShort();
			final boolean tile6 = dis.readBoolean();
			throw new ParsingException("Type of Cube is not yet supported!");
		}
		case CAPSULE:
			throw new ParsingException("Type of Capsule is not yet supported!");
		case CONE:
			throw new ParsingException("Type of Cone is not yet supported!");
		case CYLINDER: {
			final float radiusTop = dis.readFloat();
			final float radiusBottom = dis.readFloat();
			final float height = dis.readFloat();
			final int segmentsW = dis.readUnsignedShort();
			final int segmentsH = dis.readUnsignedShort();
			final boolean topClosed = dis.readBoolean();
			final boolean bottomClosed = dis.readBoolean();
			final boolean yUp = dis.readBoolean();
			final boolean surfaceClosed = dis.readBoolean();
			throw new ParsingException("Type of Cylinder is not yet supported!");
		}
		case PLANE: {
			final float width = dis.readFloat();
			final float height = dis.readFloat();
			final int segmentsW = dis.readUnsignedShort();
			final int segmentsH = dis.readUnsignedShort();
			final boolean yUp = dis.readBoolean();
			final boolean doubleSided = dis.readBoolean();
			throw new ParsingException("Type of Plane is not yet supported!");
		}
		case SPHERE: {
			final float radius = dis.readFloat();
			final int segmentsW = dis.readUnsignedShort();
			final int segmentsH = dis.readUnsignedShort();
			final boolean yUp = dis.readBoolean();
			throw new ParsingException("Type of Cylinder is not yet supported!");
		}
		case TORUS:
			throw new ParsingException("Type of Torus is not yet supported!");
		}
	}

}
