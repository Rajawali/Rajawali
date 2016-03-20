# Using a Texture Atlas

## Overview
The new class `TextureAtlas` is a collection of bitmaps, texture names, and texture coordinates.

`TexturePacker` is a utility to pack a group of textures into atlas "pages". Additional pages are created when the current page has no more space. The packing technique uses a "minimal heuristic" approach since research shows a small improvement in packing efficiency when additional heuristics are employed.


## Packing

To pack textures you have a couple of options:

<b>From Assets</b>
You can create a new atlas from files in the `assets` folder, or any sub-folder therein.

`TextureAtlas mAtlas = new TexturePacker(mContext).packTexturesFromAssets(1024, 1024, 0, false, "atlas");`

The parameters are: `int atlasWidth`, `int atlasHeight`, `int Padding`, `boolean useCompression`, `string SubFolder`

If `SubFolder` is blank/null the root of `assets` will be used.
`Padding` creates space around each packed texture in case you have troubles with the border edges of your tiles.


Compression is not currently supported, so the flag is for future expansion.

<b>From Resources</b>
If you prefer to use resources you can bundle them up into an array and pack them.

`TextureAtlas mAtlas = new TexturePacker(mContext).packTexturesFromAssets(1024, 1024, 0, false, resIDs);`

The parameters are: `int atlasWidth`, `int altasHeight`, `int padding`, `boolean useCompresison`, `int[] resourceIDs`


Compression is not currently supported, so the flag is for future expansion.

## Retrieval
Once packed you will need to be able to retrieve textures for use on your objects. To most simply achieve this the retrieval functionality is integrated into `BaseObject3D`. This allows the developer to retrieve the target texture in one step.

`BaseObject3D.setAtlasTile("myTexture", mAtlas);`

Parameters are: `string targetTextureName`, `TextureAtlas targetAtlas`

This function also scales/transforms the object UVs to correspond to the textures position on the atlas page.