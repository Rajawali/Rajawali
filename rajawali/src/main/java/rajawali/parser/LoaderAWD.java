package rajawali.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rajawali.Object3D;
import rajawali.materials.textures.TextureManager;
import rajawali.parser.awd.ABlockParser;
import rajawali.parser.awd.AExportableBlockParser;
import rajawali.parser.awd.BlockAnimationSet;
import rajawali.parser.awd.BlockAnimator;
import rajawali.parser.awd.BlockBitmapTexture;
import rajawali.parser.awd.BlockCamera;
import rajawali.parser.awd.BlockCommand;
import rajawali.parser.awd.BlockContainer;
import rajawali.parser.awd.BlockCubeTexture;
import rajawali.parser.awd.BlockLight;
import rajawali.parser.awd.BlockLightPicker;
import rajawali.parser.awd.BlockMeshInstance;
import rajawali.parser.awd.BlockMeshPose;
import rajawali.parser.awd.BlockMeshPoseAnimation;
import rajawali.parser.awd.BlockMetaData;
import rajawali.parser.awd.BlockNamespace;
import rajawali.parser.awd.BlockPrimitiveGeometry;
import rajawali.parser.awd.BlockScene;
import rajawali.parser.awd.BlockShadowMethod;
import rajawali.parser.awd.BlockSharedMethod;
import rajawali.parser.awd.BlockSimpleMaterial;
import rajawali.parser.awd.BlockSkeleton;
import rajawali.parser.awd.BlockSkeletonAnimation;
import rajawali.parser.awd.BlockSkeletonPose;
import rajawali.parser.awd.BlockSkybox;
import rajawali.parser.awd.BlockTextureProjector;
import rajawali.parser.awd.BlockTriangleGeometry;
import rajawali.parser.awd.BlockUVAnimation;
import rajawali.parser.awd.exceptions.NotImplementedParsingException;
import rajawali.renderer.RajawaliRenderer;
import rajawali.scene.RajawaliScene;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;
import android.content.res.Resources;
import android.os.SystemClock;
import android.util.SparseArray;

/**
 * AWD File parser written using the AWD File Format specification. All future additions to the format should adhere to
 * all specification requirements for maximum forward and backward compatibility.
 * <p>
 * Currently compression is not supported, files will need to be formatted with compression off. This is an option in
 * MAX and should be an option using other exporters.
 * <p>
 * 
 * <b>Example AWD parsing</b>
 * 
 * <code><pre>
 * final AWDParser parser = new AWDParser(this, new File(Environment.getExternalStorageDirectory(), "cube.awd"));
 * parser.parse();
 * parser.setAlwaysUseContainer(false);
 * final BaseObject3D obj = parser.getParsedObject();
 * </pre></code>
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 * @see <a href="http://awaytools.com/">Away Tools Homepage</a> <p>
 * @see <a href="https://github.com/awaytools/awd-sdk/blob/master/docs/AWD_format_specification2_1_Alpha.pdf">Official
 *      AWD Documentation</a> <p>
 * @see <a
 *      href="https://github.com/awaytools/AwayBuilder/blob/master/awaybuilder-core/src/awaybuilder/utils/encoders/AWDEncoder.as">Official
 *      AWD Encoder</a>
 * 
 */
public class LoaderAWD extends AMeshLoader {

	protected static final int FLAG_HEADER_STREAMING = 1;
	protected static final int FLAG_HEADER_MATRIX_STORAGE_PRECISION = 2;
	protected static final int FLAG_HEADER_GEOMETRY_STORAGE_PRECISION = 4;
	protected static final int FLAG_HEADER_PROPERTIES_STORAGE_PRECISION = 8;
	protected static final int FLAG_HEADER_COMPRESSION = 8;

	protected static final int FLAG_BLOCK_HEADER_PRECISION = 4;

	protected static final byte NS_AWD = 0;

	enum Compression {
		NONE,
		ZLIB,
		LZMA
	}

	protected final List<Object3D> baseObjects = new ArrayList<Object3D>();
	protected final SparseArray<BlockHeader> blockDataList = new SparseArray<BlockHeader>();

	private final List<IBlockParser> blockParsers = new ArrayList<IBlockParser>();
	private final SparseArray<Class<? extends ABlockParser>> blockParserClassesMap = new SparseArray<Class<? extends ABlockParser>>();

	protected int awdHeaderVersion;
	protected int awdHeaderRevision;
	protected int awdHeaderFlags;
	protected int awdHeaderCompression;
	protected long awdHeaderBodyLength;
	protected boolean awdHeaderAccuracyMatrix;
	protected boolean awdHeaderAccuracyGeo;
	protected boolean awdHeaderAccuracyProps;
	protected boolean mAlwaysUseContainer;

	public LoaderAWD(RajawaliRenderer renderer, File file) {
		super(renderer, file);
		init();
	}

	public LoaderAWD(Resources resources, TextureManager textureManager, int resourceId) {
		super(resources, textureManager, resourceId);
		init();
	}

	public LoaderAWD(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		init();
	}

	protected void init() {
		// Blocks are identified in the AWD documentation under the title 'Block Types'
		blockParserClassesMap.put(getClassID(NS_AWD, 1), BlockTriangleGeometry.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 11), BlockPrimitiveGeometry.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 21), BlockScene.class); // Not yet supported in the specification.
		blockParserClassesMap.put(getClassID(NS_AWD, 22), BlockContainer.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 23), BlockMeshInstance.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 31), BlockSkybox.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 41), BlockLight.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 42), BlockCamera.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 43), BlockTextureProjector.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 51), BlockLightPicker.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 81), BlockSimpleMaterial.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 82), BlockBitmapTexture.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 83), BlockCubeTexture.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 91), BlockSharedMethod.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 92), BlockShadowMethod.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 101), BlockSkeleton.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 102), BlockSkeletonPose.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 103), BlockSkeletonAnimation.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 111), BlockMeshPose.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 112), BlockMeshPoseAnimation.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 113), BlockAnimationSet.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 121), BlockUVAnimation.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 122), BlockAnimator.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 253), BlockCommand.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 254), BlockNamespace.class);
		blockParserClassesMap.put(getClassID(NS_AWD, 255), BlockMetaData.class);
	}

	@Override
	public AMeshLoader parse() throws ParsingException {
		super.parse();

		onRegisterBlockClasses(blockParserClassesMap);

		long startTime = SystemClock.elapsedRealtime();

		// Open the file or resource for reading
		// TODO Compare parsing speeds at different buffer sizes.
		final AWDLittleEndianDataInputStream dis;
		try {
			dis = getLittleEndianInputStream(8192);
		} catch (Exception e) {
			throw new ParsingException(e);
		}

		// Verify the header
		try {
			// Header should begin with AWD, immediately throw exception if this is not true
			final byte[] buf = new byte[3];
			dis.read(buf);
			if (!new String(buf).equals("AWD"))
				throw new ParsingException("Invalid header designation: " + new String(buf));

			// Read remaining header data
			awdHeaderVersion = dis.readUnsignedByte();
			awdHeaderRevision = dis.readUnsignedByte();
			awdHeaderFlags = dis.readUnsignedShort();

			if (awdHeaderVersion == 2 && awdHeaderRevision == 1) {
				awdHeaderAccuracyMatrix = (awdHeaderFlags & FLAG_HEADER_MATRIX_STORAGE_PRECISION) == FLAG_HEADER_MATRIX_STORAGE_PRECISION;
				awdHeaderAccuracyGeo = (awdHeaderFlags & FLAG_HEADER_GEOMETRY_STORAGE_PRECISION) == FLAG_HEADER_GEOMETRY_STORAGE_PRECISION;
				awdHeaderAccuracyProps = (awdHeaderFlags & FLAG_HEADER_PROPERTIES_STORAGE_PRECISION) == FLAG_HEADER_PROPERTIES_STORAGE_PRECISION;
			}

			awdHeaderCompression = dis.read();
			// Body length is for integrity checking, ignored when streaming; not a guaranteed value
			awdHeaderBodyLength = dis.readUnsignedInt();

			// Calculate the end of the file
			final long endOfFile = dis.getPosition() + awdHeaderBodyLength;

			// Debug Headers
			RajLog.d("AWD Header Data");
			RajLog.d(" Version: " + awdHeaderVersion + "." + awdHeaderRevision);
			RajLog.d(" Flags: " + awdHeaderFlags);
			RajLog.d(" Compression: " + getCompression());
			RajLog.d(" Body Length: " + awdHeaderBodyLength);
			RajLog.d(" End Of File: " + endOfFile);

			// Check streaming
			if ((awdHeaderFlags & FLAG_HEADER_STREAMING) == FLAG_HEADER_STREAMING)
				throw new ParsingException("Streaming not supported.");

			// Check the length
			if (awdHeaderBodyLength < 1)
				throw new ParsingException(
						"AWD Body length not provided which indicates model is streaming or corrupt.");

			// Compression is not supported as this is unnecessary overhead given the limited resources on mobile
			if (getCompression() != Compression.NONE)
				throw new ParsingException("Compression is not currently supported. Document compressed as: "
						+ getCompression());

			// Read file blocks
			try {
				do {
					// Read header data
					final BlockHeader blockHeader = new BlockHeader();
					blockHeader.blockHeaders = blockDataList;
					blockHeader.awdVersion = awdHeaderVersion;
					blockHeader.awdRevision = awdHeaderRevision;
					blockHeader.id = dis.readInt();
					blockHeader.namespace = dis.read();
					blockHeader.type = dis.read();
					blockHeader.flags = dis.read();
					blockHeader.dataLength = dis.readUnsignedInt();
					blockHeader.globalPrecisionGeo = (blockHeader.flags & BlockHeader.FLAG_ACCURACY_GEO) == BlockHeader.FLAG_ACCURACY_GEO;
					blockHeader.globalPrecisionMatrix = (blockHeader.flags & BlockHeader.FLAG_ACCURACY_MATRIX) == BlockHeader.FLAG_ACCURACY_MATRIX;
					blockHeader.globalPrecisionProps = (blockHeader.flags & BlockHeader.FLAG_ACCURACY_PROPS) == BlockHeader.FLAG_ACCURACY_PROPS;
					blockHeader.blockEnd = dis.getPosition() + blockHeader.dataLength;

					// Flag the input stream with the correct property precision flag
					dis.setPropertyPrecision(blockHeader.globalPrecisionProps);

					// Add the block to the list of blocks for reference. Id of 0 indicates no references will be made
					// to the block.
					if (blockHeader.id != 0)
						blockDataList.put(blockHeader.id, blockHeader);

					// Debug
					RajLog.d(blockHeader.toString());

					// Look for the Block Parser class.
					final Class<? extends ABlockParser> blockClass = (Class<? extends ABlockParser>) blockParserClassesMap
							.get(getClassID(
									blockHeader.namespace, blockHeader.type));

					// Skip unknown blocks
					if (blockClass == null) {
						RajLog.d(" Skipping unknown block " + blockHeader.namespace + " " + blockHeader.type);
						dis.skip(blockHeader.dataLength);
						continue;
					}

					// Instantiate the block parser
					final ABlockParser parser = (ABlockParser) Class.forName(blockClass.getName()).getConstructor()
							.newInstance();

					if (blockHeader.id != 0)
						blockHeader.parser = parser;

					// Add the parser to the list of block parsers
					blockParsers.add(parser);

					RajLog.d(" Parsing block with: " + parser.getClass().getSimpleName());

					// Begin parsing
					try {
						parser.parseBlock(dis, blockHeader);
					} catch (NotImplementedParsingException e) {
						RajLog.d(" Skipping block as not implemented.");
						dis.skip(blockHeader.blockEnd - dis.getPosition());
					}

					// Validate block end
					if (blockHeader.blockEnd != dis.getPosition())
						throw new ParsingException("Block did not end in the correct location. Expected : "
								+ blockHeader.blockEnd
								+ " Ended : " + dis.getPosition());

				} while (dis.getPosition() < endOfFile);

				// End of blocks reached
				RajLog.d("End of blocks reached.");
			} catch (IOException e) {
				throw new ParsingException("Buffer overrun; unexpected end of file.", e);
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ParsingException("Unexpected error. File is not in a supported AWD format.", e);
		}

		onBlockParsingFinished(blockParsers);

		RajLog.d("Finished Parsing in " + (SystemClock.elapsedRealtime() - startTime));

		return this;
	}

	/**
	 * Get the parsed object or objects. This is returns each model independent of a scene regardless of if a scene
	 * exists or not.
	 * 
	 * @param alwaysUseContainer
	 *            When false, a single model will be returned as a BaseObject3D. When true, or when more than one model
	 *            exists, the models will be returned as children of a container.
	 * @return
	 */
	@Override
	public Object3D getParsedObject() {
		// If only one object
		if (!mAlwaysUseContainer && baseObjects.size() == 1)
			return baseObjects.get(0);

		mRootObject.isContainer(true);
		for (int i = 0, j = baseObjects.size(); i < j; i++)
			mRootObject.addChild(baseObjects.get(i));

		return mRootObject;
	}

	@Override
	protected AWDLittleEndianDataInputStream getLittleEndianInputStream(int size) throws FileNotFoundException {
		return new AWDLittleEndianDataInputStream(getBufferedInputStream(size));
	}

	/**
	 * Get the block header for the given block id. Block id is determined by the AWD file and dependencies must be
	 * parsed before referencing is possible.
	 * 
	 * @param blockID
	 * @return
	 */
	public BlockHeader getBlockByID(int blockID) {
		if (blockDataList.indexOfKey(blockID) < 0)
			throw new RuntimeException("Block parsing referenced non existant id: " + blockID);

		return blockDataList.get(blockID);
	}

	/**
	 * Get the compression level set in the AWD header.
	 * 
	 * @return
	 */
	public Compression getCompression() {
		try {
			return Compression.values()[awdHeaderCompression];
		} catch (Exception e) {
			throw new RuntimeException("Unknown compression setting detected!");
		}
	}

	/**
	 * This is called when all blocks have finished parsing. This is the time to modify any block data as needed from
	 * the passed list before conversion to {@link BaseObject3D} or {@link RajawaliScene} occurs.
	 */
	public void onBlockParsingFinished(List<IBlockParser> blockParsers) {
		Object3D temp;
		IBlockParser blockParser;
		for (int i = 0, j = blockParsers.size(); i < j; i++) {
			blockParser = blockParsers.get(i);
			if (!(blockParser instanceof AExportableBlockParser))
				continue;

			temp = ((AExportableBlockParser) blockParser).getBaseObject3D();
			if (temp != null)
				baseObjects.add(temp);
		}
	}

	/**
	 * Determine if {@link #getParsedObject()} will force the model to use a container. When more than one model is in
	 * an AWD file, the models will be wrapped in a container, this flag can force single containers to be wrapped in a
	 * container as well.
	 * 
	 * @param flag
	 */
	public void setAlwaysUseContainer(boolean flag) {
		mAlwaysUseContainer = flag;
	}

	/**
	 * Get the class identifier for the provided block namespace and typeID. This is useful for finding and setting
	 * classes in the parser map.
	 * 
	 * @param namespace
	 * @param typeID
	 * @return
	 */
	protected static int getClassID(int namespace, int typeID) {
		return (short) ((namespace << 8) | typeID);
	}

	/**
	 * If necessary, register additional {@link ABlockParser} classes here.
	 * 
	 * @param blockParserClassesMap
	 */
	protected void onRegisterBlockClasses(SparseArray<Class<? extends ABlockParser>> blockParserClassesMap) {}

	/**
	 * Interface implemented by {@link ABlockParser}. This interface should not be implemented directly, instead extend
	 * {@link ABlockParser}.
	 */
	public interface IBlockParser {

		void parseBlock(AWDLittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception;
	}

	/**
	 * Block headers are consistent across all blocks and hold useful information that various blocks need for parsing
	 * purposes.
	 * 
	 * @author Ian Thomas (toxicbakery@gmail.com)
	 * 
	 */
	public static final class BlockHeader {

		public static final int FLAG_ACCURACY_MATRIX = 0x01;
		public static final int FLAG_ACCURACY_GEO = 0x02;
		public static final int FLAG_ACCURACY_PROPS = 0x04;

		public SparseArray<BlockHeader> blockHeaders;
		public ABlockParser parser;

		public int awdVersion;
		public int awdRevision;

		public int id;
		public int namespace;
		public int type;
		public int flags;
		public long dataLength;
		public long blockEnd;

		public boolean globalPrecisionGeo;
		public boolean globalPrecisionMatrix;
		public boolean globalPrecisionProps;

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(" Block ID: ").append(id).append("\n");
			sb.append(" Block Namespace: ").append(namespace).append("\n");
			sb.append(" Block Type: ").append(type).append("\n");
			sb.append(" Block Precision Geo: ").append(globalPrecisionGeo).append("\n");
			sb.append(" Block Precision Matrix: ").append(globalPrecisionMatrix).append("\n");
			sb.append(" Block Precision Props: ").append(globalPrecisionProps).append("\n");
			sb.append(" Block Length: ").append(dataLength).append("\n");
			sb.append(" Block End: ").append(blockEnd).append("\n");
			return sb.toString();
		}

	}

	/**
	 * Helper class adding specific features resused across AWD blocks such as VarString, UserAttributes, and
	 * Properties.
	 * 
	 * @author Ian Thomas (toxicbakery@gmail.com)
	 * 
	 */
	public static final class AWDLittleEndianDataInputStream extends LittleEndianDataInputStream {

		public enum Precision {
			GEO, MATRIX, PROPS
		}

		/**
		 * NR is a custom type that indicates reading to the super block specified precision.
		 */
		public static final short TYPE_NR = -1;
		public static final short TYPE_INT8 = 1;
		public static final short TYPE_INT16 = 2;
		public static final short TYPE_INT32 = 3;
		/**
		 * NOTE: Even though this is just a byte, {@link LittleEndianDataInputStream} returns integer type to prevent
		 * casting in math compared to returning short.
		 */
		public static final short TYPE_UINT8 = 4;
		public static final short TYPE_UINT16 = 5;
		public static final short TYPE_UINT32 = 6;
		public static final short TYPE_FLOAT32 = 7;
		public static final short TYPE_FLOAT64 = 8;
		public static final short TYPE_BOOL = 21;
		public static final short TYPE_COLOR = 22;
		public static final short TYPE_BADDR = 23;
		public static final short TYPE_AWDSTRING = 31;
		public static final short TYPE_AWDBYTEARRAY = 32;
		public static final short TYPE_VECTOR2x1 = 41;
		public static final short TYPE_VECTOR3x1 = 42;
		public static final short TYPE_VECTOR4x1 = 43;
		public static final short TYPE_MTX3x2 = 44;
		public static final short TYPE_MTX3x3 = 45;
		public static final short TYPE_MTX4x3 = 46;
		public static final short TYPE_MTX4x4 = 47;

		private boolean mPropPrecision;

		public AWDLittleEndianDataInputStream(InputStream in) {
			super(in);
		}

		public void setPropertyPrecision(boolean flag) {
			mPropPrecision = flag;
		}

		/**
		 * Read in a 2D matrix. Passed array must be of size 6.
		 * 
		 * @param matrix
		 * @throws IOException
		 * @throws ParsingException
		 */
		public void readMatrix2D(float[] matrix) throws IOException, ParsingException {
			if (matrix == null || matrix.length != 6)
				throw new ParsingException("Matrix array must be of size 6");

			matrix[0] = readFloat();
			matrix[1] = readFloat();
			matrix[2] = readFloat();
			matrix[3] = readFloat();
			matrix[4] = readFloat();
			matrix[5] = readFloat();
		}

		/**
		 * Read in 3D matrix. Passed array must be of size 16 Positions 3, 7, 11, and 15 are constants and not read.
		 * 
		 * @param matrix
		 * @throws ParsingException
		 * @throws IOException
		 */
		public void readMatrix3D(float[] matrix, boolean usePrecision) throws ParsingException, IOException {
			if (matrix == null || matrix.length != 16)
				throw new ParsingException("Matrix array must be of size 16");

			matrix[0] = (float) readPrecisionNumber(usePrecision);
			matrix[1] = (float) readPrecisionNumber(usePrecision);
			matrix[2] = (float) readPrecisionNumber(usePrecision);
			matrix[3] = 0f;
			matrix[4] = (float) readPrecisionNumber(usePrecision);
			matrix[5] = (float) readPrecisionNumber(usePrecision);
			matrix[6] = (float) readPrecisionNumber(usePrecision);
			matrix[7] = 0f;
			matrix[8] = (float) readPrecisionNumber(usePrecision);
			matrix[9] = (float) readPrecisionNumber(usePrecision);
			matrix[10] = (float) readPrecisionNumber(usePrecision);
			matrix[11] = 0f;
			matrix[12] = (float) readPrecisionNumber(usePrecision);
			matrix[13] = (float) readPrecisionNumber(usePrecision);
			matrix[14] = (float) readPrecisionNumber(usePrecision);
			matrix[15] = 1f;
		}

		/**
		 * Read a precision number determined by the high definition flag in the block header.
		 * 
		 * @return
		 * @throws IOException
		 * @throws ParsingException
		 */
		public double readPrecisionNumber(boolean usePrecision) throws IOException, ParsingException {
			return usePrecision ? readDouble() : readFloat();
		}

		/**
		 * Skip reading of block properties. Same as calling skip(properties.length).
		 * 
		 * @param dis
		 * @throws IOException
		 */
		public void readProperties() throws IOException {
			readProperties(null);
		}

		/**
		 * Read the user properties and place the values into the passed {@link SparseArray}. If the passed array is
		 * null, the properties will be skipped.
		 * 
		 * @param expected
		 * @throws IOException
		 */
		public AwdProperties readProperties(SparseArray<Short> expected) throws IOException {
			// Determine the length of the properties
			final long propsLength = readUnsignedInt();

			// Skip properties if null is passed
			if (expected == null) {
				RajLog.d("  Skipping property values.");
				skip(propsLength);
			}

			final AwdProperties props = new AwdProperties();

			// No properties to read
			if (propsLength == 0)
				return props;

			final long endPosition = mPosition + propsLength;
			short propKey;
			long propLength;

			// Read the properties, skip the remaining values if an error is encountered
			while (mPosition < endPosition) {
				propKey = (short) readUnsignedShort();
				propLength = readUnsignedInt();

				if (mPosition + propLength > endPosition) {
					RajLog.e("Unexpected properties length. Properties attemped to read past total properties length.");
					if (endPosition > mPosition)
						skip(endPosition - mPosition);

					return props;
				}

				if (expected.indexOfKey(propKey) > -1) {
					props.put(propKey, parseAttrValue(expected.get(propKey), propLength));
				} else {
					skip(propLength);
				}
			}

			return props;

		}

		/**
		 * Read user attributes into a list of objects.
		 * 
		 * @throws IOException
		 */
		public HashMap<String, Object> readUserAttributes(HashMap<String, Object> attributes) throws IOException {
			final long attributesLength = readUnsignedInt();
			final long endPosition = mPosition + attributesLength;

			if (attributesLength == 0)
				return attributes;

			// If the passed attributes map is null, skip the attributes entirely.
			if (attributes == null) {
				skip(attributesLength);
				return attributes;
			}

			@SuppressWarnings("unused")
			short attrNameSpace; // namespace is not used yet
			String attrKey;
			short attrType;
			long attrLength;

			// Read the attributes, skip the remaining values if an error is encountered.
			while (mPosition < endPosition) {
				attrNameSpace = (short) readUnsignedByte();
				attrKey = readVarString();
				attrType = (short) readUnsignedByte();
				attrLength = readUnsignedInt();

				if (mPosition + attrLength > endPosition) {
					RajLog.e("Unexpected attribute length. Attributes attempted to read past total attributes length.");
					if (endPosition > mPosition)
						skip(endPosition - mPosition);

					return attributes;
				}

				attributes.put(attrKey, parseAttrValue(attrType, attrLength));
			}

			return attributes;
		}

		private Object parseAttrValue(short attrType, long attrLength) throws IOException {
			Object attrValue = null;
			switch (attrType) {
			case TYPE_AWDSTRING:
				attrValue = readString((int) attrLength);
				break;
			case TYPE_INT8:
				attrValue = readByte();
				break;
			case TYPE_INT16:
				attrValue = readShort();
				break;
			case TYPE_INT32:
				attrValue = readInt();
				break;
			case TYPE_BOOL:
				attrValue = readBoolean();
				break;
			case TYPE_UINT8:
				attrValue = readUnsignedByte();
				break;
			case TYPE_UINT16:
				attrValue = readUnsignedShort();
				break;
			case TYPE_UINT32:
			case TYPE_BADDR:
				attrValue = readUnsignedInt();
				break;
			case TYPE_FLOAT32:
				attrValue = readFloat();
				break;
			case TYPE_FLOAT64:
				attrValue = readDouble();
				break;
			case TYPE_NR:
				attrValue = mPropPrecision ? readDouble() : readFloat();
			default:
				RajLog.e("Skipping unknown attribute (" + attrType + ")");
				skip(attrLength);
				break;
			}

			return attrValue;
		}

		/**
		 * Read a variable length String from the file.
		 * 
		 * @return
		 * @throws IOException
		 */
		public String readVarString() throws IOException {
			final int varStringLength = readUnsignedShort();
			return varStringLength == 0 ? "" : readString(varStringLength);
		}

	}

	public static final class AwdProperties extends HashMap<Short, Object> {

		private static final long serialVersionUID = 221100798331514427L;

		public Object get(short key, Object fallback) {
			return containsKey(key) ? get(key) : fallback;
		}

	}

}
