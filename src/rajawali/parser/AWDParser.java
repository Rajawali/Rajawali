package rajawali.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rajawali.BaseObject3D;
import rajawali.parser.awd.ABlockParser;
import rajawali.parser.awd.BlockTriangleGeometry;
import rajawali.renderer.RajawaliRenderer;
import rajawali.scene.RajawaliScene;
import rajawali.util.LittleEndianDataInputStream;
import rajawali.util.RajLog;
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
 * final BaseObject3D obj = parser.getParsedObject(false);
 * </pre></code>
 * 
 * @author Ian Thomas (toxicbakery@gmail.com)
 * 
 * @see <a
 *      href="https://code.google.com/p/awd/source/browse/doc/spec/AWD_format_specification.odt">https://code.google.com/p/awd/source/browse/doc/spec/AWD_format_specification.odt</a>
 * 
 */
public class AWDParser extends AParser {

	enum Compression {
		NONE,
		ZLIB,
		LZMA
	}

	protected final List<BaseObject3D> baseObjects = new ArrayList<BaseObject3D>();
	protected final SparseArray<BlockHeader> blockDataList = new SparseArray<BlockHeader>();

	private final List<IBlockParser> blockParsers = new ArrayList<IBlockParser>();
	private final SparseArray<Class<? extends ABlockParser>> blockParserClassesMap = new SparseArray<Class<? extends ABlockParser>>();

	protected int awdHeaderVersion;
	protected int awdHeaderRevision;
	protected boolean awdHeaderFlagStreaming;
	protected int awdHeaderCompression;
	protected int awdHeaderBodyLength;

	public AWDParser(RajawaliRenderer renderer, File file) {
		super(renderer, file);
		init();
	}

	public AWDParser(RajawaliRenderer renderer, int resourceId) {
		super(renderer, resourceId);
		init();
	}

	public AWDParser(RajawaliRenderer renderer, String fileOnSDCard) {
		super(renderer, fileOnSDCard);
		init();
	}

	protected void init() {
		blockParserClassesMap.put(getClassID(0, 1), BlockTriangleGeometry.class);
	}

	@Override
	public IParser parse() throws ParsingException {
		super.parse();

		onRegisterBlockClasses(blockParserClassesMap);

		long startTime = SystemClock.elapsedRealtime();

		// Open the file or resource for reading
		// TODO Compare parsing speeds at different buffer sizes.
		final LittleEndianDataInputStream dis;
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
				throw new ParsingException();

			// Read remaining header data
			awdHeaderVersion = dis.read();
			awdHeaderRevision = dis.read();
			awdHeaderFlagStreaming = (dis.readShort() & 0x1) == 0x1;
			awdHeaderCompression = dis.read();
			// INFO Body length is for integrity checking, ignored when streaming
			awdHeaderBodyLength = dis.readInt();

			// Debug Headers
			RajLog.d("AWD Header Data");
			RajLog.d(" Version: " + awdHeaderVersion + "." + awdHeaderRevision);
			RajLog.d(" Is Streaming: " + awdHeaderFlagStreaming);
			RajLog.d(" Compression: " + getCompression());
			RajLog.d(" Body Length: " + awdHeaderBodyLength);

			// Check streaming
			if (awdHeaderFlagStreaming)
				throw new ParsingException("Streaming not supported.");

			// Only compression setting of NONE is currently supported.
			if (getCompression() != Compression.NONE)
				throw new ParsingException("Compression is not currently supported. Document compressed as: "
						+ getCompression());

			// Read file blocks
			try {
				do {
					// Read header data
					final BlockHeader blockHeader = new BlockHeader();
					blockHeader.id = dis.readInt();
					blockHeader.namespace = dis.read();
					blockHeader.type = dis.read();
					blockHeader.flags = dis.read();
					blockHeader.dataLength = dis.readInt();

					if ((blockHeader.flags & 0x1) == 0x1)
						throw new ParsingException("High precision models are not supported.");

					// Add the block to the list of blocks for reference
					blockDataList.put(blockHeader.id, blockHeader);

					// Debug
					RajLog.d("Reading Block");
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

					// Add the parser to the list of block parsers
					blockParsers.add(parser);

					// Assign the parser for future reference
					blockHeader.parser = parser;

					RajLog.d(" Parsing block with: " + parser.getClass().getSimpleName());

					// Begin parsing
					parser.parseBlock(dis, blockHeader);
				} while (true);
			} catch (IOException e) {
				// End of blocks reached
				RajLog.d("End of blocks reached.");
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
			throw new ParsingException("Unexpected header. File is not in AWD format.");
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
	public BaseObject3D getParsedObject(boolean alwaysUseContainer) {
		// If only one object
		if (!alwaysUseContainer && baseObjects.size() == 1)
			return baseObjects.get(0);

		final BaseObject3D container = new BaseObject3D();
		container.isContainer(true);
		for (int i = 0, j = baseObjects.size(); i < j; i++)
			container.addChild(baseObjects.get(i));

		return container;
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
	 * Get the compression level set in the AWD header
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
	 * Get the AWD file revision specified in the header of the document.
	 * 
	 * @return
	 */
	public int getRevision() {
		return awdHeaderRevision;
	}

	/**
	 * Get the AWD file version specified in the header of the document.
	 * 
	 * @return
	 */
	public int getVersion() {
		return awdHeaderVersion;
	}

	/**
	 * This is called when all blocks have finished parsing. This is the time to modify any block data as needed from
	 * the passed list before conversion to {@link BaseObject3D} or {@link RajawaliScene} occurs.
	 */
	public void onBlockParsingFinished(List<IBlockParser> blockParsers) {
		BaseObject3D temp;
		for (int i = 0, j = blockParsers.size(); i < j; i++) {
			temp = blockParsers.get(i).getBaseObject3D();
			if (temp != null)
				baseObjects.add(temp);
		}
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

		BaseObject3D getBaseObject3D();

		void parseBlock(LittleEndianDataInputStream dis, BlockHeader blockHeader) throws Exception;
	}

	public static final class BlockHeader {

		public int id;
		public int namespace;
		public int type;
		public int flags;
		public int dataLength;
		public IBlockParser parser;

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append(" Block ID: ").append(id).append("\n");
			sb.append(" Block Namespace: ").append(namespace).append("\n");
			sb.append(" Block Type: ").append(type).append("\n");
			sb.append(" Block Highprecision: ").append((flags & 0x0001) == 1).append("\n");
			sb.append(" Block Length: ").append(dataLength).append("\n");
			return sb.toString();
		}
	}

}
