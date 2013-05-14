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
 * 
 * https://code.google.com/p/awd/source/browse/doc/spec/AWD_format_specification.odt
 * 
 * Currently compression is not supported, files will need to be formatted with compression off. This is an option in
 * MAX and should be an option using other exporters.
 * 
 * @author TencenT
 * 
 */
public class AWDParser extends AParser {

	enum Compression {
		NONE,
		ZLIB,
		LZMA
	}

	protected final SparseArray<Class<? extends ABlockParser>> blockParserClassesMap = new SparseArray<Class<? extends ABlockParser>>();
	protected final List<Integer> blockIds = new ArrayList<Integer>();

	protected BaseObject3D baseObject3D;
	protected int awdHeaderVersion;
	protected int awdHeaderRevision;
	protected boolean awdHeaderFlagStreaming;
	protected int awdHeaderCompression;
	protected int awdHeaderBodyLength;

	private boolean mDebug;

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

	protected void init() {
		blockParserClassesMap.put(getClassID(0, 1), BlockTriangleGeometry.class);
	}

	@Override
	public IParser parse() throws ParsingException {
		super.parse();

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
			awdHeaderFlagStreaming = (dis.readShort() & 0x0001) == 1;
			awdHeaderCompression = dis.read();
			// INFO Body length is for integrity checking, ignored when streaming
			awdHeaderBodyLength = dis.readInt();

			// Debug Headers
			if (mDebug) {
				RajLog.i("AWD Header Data");
				RajLog.i(" Version: " + awdHeaderVersion + "." + awdHeaderRevision);
				RajLog.i(" Is Streaming: " + awdHeaderFlagStreaming);
				RajLog.i(" Compression: " + getCompression());
				RajLog.i(" Body Length: " + awdHeaderBodyLength);
			}

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
					int blockID = dis.readInt();
					int blockNamespace = dis.read();
					int blockType = dis.read();
					int blockFlags = dis.read();
					int blockLength = dis.readInt();

					// Debug
					if (mDebug) {
						RajLog.i("Reading Block");
						RajLog.i(" Block ID: " + blockID);
						RajLog.i(" Block Namespace: " + blockNamespace);
						RajLog.i(" Block Type: " + blockType);
						RajLog.i(" Block Highprecision: " + ((blockFlags & 0x0001) == 1));
						RajLog.i(" Block Length: " + blockLength);
					}

					// Store the blockID
					blockIds.add(blockID);

					// Look for the Block Parser class.
					@SuppressWarnings("unchecked")
					final Class<ABlockParser> blockClass = (Class<ABlockParser>) blockParserClassesMap.get(getClassID(
							blockNamespace, blockType));

					// Instantiate the block parser and call parseBlock if found otherwise skip the block
					if (blockClass != null) {
						final ABlockParser parser = (ABlockParser) Class.forName(blockClass.getName()).getConstructor()
								.newInstance();
						parser.parseBlock(dis, blockLength, mDebug);
					} else {
						if (mDebug)
							RajLog.i(" Skipping unknown block.");

						dis.skip(blockLength);
					}
				} while (true);
			} catch (IOException e) {
				// End of blocks reached
			}

		} catch (Exception e) {
			throw new ParsingException("Unexpected header. File is not in AWD format.");
		}

		if (mDebug)
			RajLog.i("Finished Parsing in " + (SystemClock.elapsedRealtime() - startTime));

		return this;
	}

	/**
	 * Enable debugging for AWD parsing. This is useful for finding and correcting problems in parser and malformed
	 * documents.
	 */
	public void enableDebug() {
		mDebug = true;
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
	 * Get the parsed {@link BaseObject3D}. This will typically be a blank or broken object if parsing failed.
	 * 
	 * @return
	 */
	public BaseObject3D getParsedObject() {
		return baseObject3D;
	}

	/**
	 * Get the parsed {@link RajawaliScene}. This is not yet implemented and instead throws a runtime error.
	 * 
	 * @return
	 */
	public RajawaliScene getParsedScene() {
		// TODO Add some awesome.
		throw new RuntimeException("Scene parsing not yet implemented.");
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
	public void onBlockParsingFinished(List<ABlockParser> blockParsers) {}

	/**
	 * If necessary, register additional {@link ABlockParser} classes here.
	 */
	public void onRegisterBlockClasses() {}

	/**
	 * Interface implemented by {@link ABlockParser}. This interface should not be implemented directly, instead extend
	 * {@link ABlockParser}.
	 */
	public interface IBlockParser {

		void parseBlock(LittleEndianDataInputStream dis, int blockLength, boolean debug) throws Exception;
	}

}
