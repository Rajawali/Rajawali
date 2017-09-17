# 1. Intro

Two primary goals:

1. Clearly specify all of the actual 2.0 release Must-Haves in one place
  * Settle debates about requirements separately from/prior to design/code issues
  * Get a more complete picture of the real scope and the effort needed
  * Optional/nice-to-have/future requirements flagged as "[2.1]", regardless of any actual future version that might incorporate that requirement
  * "[?]" = still needs version goal...  
  * No version flag = Must-Have!
  * Many of the major new requirements were previously discussed in Git issue #1755, best to review that first...

2. Enumerate as many as possible of the relevant pre-existing, broken, in-progress, and future features and requirements that should (where practical) be accomdated and/or anticipated by the new architectural design (see Design Goals below).
   
<tl;dr> 

Punchline: given the available Rajawali dev resources, many/most of the "cool new" API features and even some existing features (and bug fixes) should be postponed in favor of building a robust and extensible architectural foundation on which those features can be incrementally implemented (or fixed and re-enabled) in future releases (as pure extensions) with little/no impact to the to-be-released Camden Hells API...

</tl;dr>

For reviews and updates:
  * Camden Hells Milestone issues in GitHub are called out where available. e.g. "(#1755)"
  * This is a living document; try to keep it updated in Git as agreements about reqs and priorities evolve up to and through the 2.0 release timeframe (and immediate follow-on fixes). Start a new 2.1 reqs doc after that... 
    * [Any "D" beer name for 2.1 yet? :sunglasses: ]
  * "Help!" and "?" mean "open/unresolved requirement issue, feel free to scope things out more fully/correctly, or delete" (please ~~strike-through~~ until delete is agreed); need to resolve/elaborate if 2.0...
  * Don't worry/comment about grammar or spelling, just improve at will if desired
  * Try to keep design decisions and details out except as examples and likely possibilities; place actual hard design decisions in DESIGN.md and/or code as appropriate and convenient... 
  * Careful about section/header/list numbers, they're manual, not automatic!

## 1.1 Terms

  * CH = the Rajawali Camden Hells (2.0) release being specified here
  * Engine = the Rajawali API and/or its implementation

# 2. System Requirements

1. Minimum Android OS version is SDK 19 (KitKat 4.4)
  * 15 is no longer supported with Google security updates
  * 19+ covers 90% of the distributed versions as of 08/2017
  * Should reduce on-going support load for older/more limited hardware
  * May increase opportunities for integrating other libraries
2. Support OpenGL ES versions 2.0, 3.0 (#665), 3.1, and 3.2
  * Support for a GL ES version:
    * Does not imply support for the entirety of that version's API
    * Does imply that engine features available in earlier versions are still available
  * Using an engine feature that depends on a version greater than is available on a device:
    * Will in general throw a runtime exception. 
    * Should be avoided by the application by checking versions first, or by manifest declarations
  * Using an engine feature that depends on a version less than is available on a device:
    * May automatically use a different implementation using the newer GL ES API features
    * Should in general only happen if it improves (or at least does not reduce) performance

# 3. Functional Requirements

## 3.1 Android and OpenGL ES integration

1. Provide (major and minor) version query for the current GL ES render context
2. Decouple Android views from the EGL context to allow offline rendering/initialization
3. Allow multiple `GlSurfaceViews` per Activity ? [2.1]
  ~* Only one render thread/context/Renderer can exist at once, so SurfaceViews are mutually exclusive~
  * Multiple render contexts can exist at once on some devices. Additionally, with some creative APIs, a single renderer could drive multiple SurfaceView/TextureView implementations.
    * E.g. `ViewPager` support (per #1619)
    * Is timing of render context switch still based on window attachment?
4. Wallpaper surface view multisampling fix (#1559)
5. Provide query/configuration constants (bit flags) for all Vendor GL ES extensions (#1742) [2.1]
  * CH implementation will include this for all texture extensions.
  
## 3.2 Scene Models and Views

### 3.2.1. Model files
 
1. ~Enable "artist control" e.g. material specs, transparency/blending... ?~ Proper loader support (#2) will take care of this.
2. Assimp support
3. ~OBJ fixes/improvements (#896 - multiple items!)~ Assimp integration will fix most of our loading problems.
4. ~Improved AWD support/bug fixes ? [2.1]  (#1185, #1465, #1468)~ Unfortunately, AWD and AwayBuilder are dead (they were tied to Flash). AWD is still an interesting format but content creation tools will be limited.
5. ~MD5 child naming (#1454) [2.1]~ See item 2.

### 3.2.2. Colors and Textures

Help!

1. New formats, details
 * CH - See section 3.1.5
2. New functions? e.g.
 * Compression/decompression? 
 * Layouts/transforms?
3. Font to texture support (#693) [2.1]
 * Implement as additional module
4. Fix vertex color setter (#1781)
5. Add KTX file support (#1823)

### 3.2.3. Coordinate systems and transforms

1. Provide world coordinate positions and orientations for all transformable elements
2. Provide well-defined/self-consistent coordinate systems, and translations between related systems and representations (#1618) :
  * Local frame 
  * World frame (position and orientation)
  * Light look-at fix (#1668) 
  * MD5/AWD skeletal model rotations fix (#1650) 
  * Arcball camera rotation fix (#1629)
  * Texture mapping/screen quads
3. Add `Quaternion.squad()` (#1737) [2.1]

### 3.2.4 Scene Views 

Provide separate specification of a rendered view for a scene model. Each scene view specifies:
1. Its scene model - immutable
2. One camera, switchable, and a dynamic set of any number/mix of lights
  * No inherent geometry/vertex data
  * Embedded in the scene, transformable 
  * World coordinates can be queried
  * "Containers", optional visualization geometries for debugging or app functions can be added
  * Switch betwen whole light sets at once ? [2.1]
3. Skybox texture/bitmaps and size, and/or background color and/or materials; all mutable
4. Target/viewport rectangle size and on-screen location (subsumes #1752)
5. Depth mask/stencil [2.1]
6. AR integration [2.1]
  * Camera/video stream (scene view quad)
  * Updated Vuforia integration
  * ARKit integration for free AR
7. Optional animations
  * Light positions
  * Light directions, powers, colors, spotlight parameters, etc. [2.1]
  * Camera position, orientation/look-at
  * Camera FOV [2.1]
  * Viewport position, size [2.1]

### 3.2.5 Future scene/object stuff [2.1]

1. Terrain generation (#905)
2. Skeletal/vertex animation callbacks (#895)
3. Modifiers (e.g. Bend a plane) (#652)
4. Advanced object (affine transform) animations?
  * Looping/cycling parameters - delay, length/duration, transform window, etc.
  * Ramped loop/cycle durations on starts/stops
  * Animating (dynamic, sparse) groups of objects
5. Advanced gesture support
  * Implemented as a separate module which utilizes a core interface.
  * Streamlined MotionEvent handling across threads
  * Drags, rotations, zooms, flings of any of these (with friction)

## 3.3 Rendering

### 3.3.1 Multiple "everything"

1. Multiple scenes
  * Example enabled use cases
    * Streamed 3D object catalog display with "recycler"-style selection
    * Parallel/independent storylines/settings
    * Analytics dashboards with interactive 3D graphs
  * Independent/isolated world coordinate systems, graphs, and object instances
    * Object geometries/buffers, material specs, textures, and shaders/programs can be shared across scenes
2. Multiple scene views/viewports per scene
  * Example enabled use cases
    * Multiple cameras - forward, left, right, rear, top, follow, etc.
    * Global/context map inset/PIP
    * Day vs night comparisons (side-by-side or PIP)
    * Natural vs artificial lighting (e.g. infra-red, UV, or other color-mapping) comparisons
    * Stereo VR left-right eyes/lenses; Cardboard
  * Scene view depth order can use simple back-to-front painter's algorithm
    * Performance impacts of overwrites is application responsibility for now
    * Overall surface view background color
      * Defaults to black
      * Can be overridden
      * Can be turned off if not needed
3. Multiple frame renders per scene view
  * Example enabled use cases
    * Normal/default on-screen rendering to the scene view's viewport location
    * Off-screen functions
      * Object picking and other application logic using e.g. pixel buffer queries
      * Bitmap images for application frame captures and debugging 
  * All frame renders use the (same) scene view's render target/viewport size
  * Zero or one frame render to the on-screen viewport 
    * Location constrained so that entire viewport is always within the 3D surface view
    * Can be hidden/temporarily removed from screen
      * Surface view background color can be specified (in case no other scene views are behind)
    * Can disable frame updates, independent of hiding
  * Any number of frame renders to readable off-screen image buffers
4. Multiple render targets per framebuffer (#1862)
  * E.g. for deferred lighting and ambient occlusion g-buffers
  * Different size attachments are supported per render target
5. Multiple (sub-)passes per render pass/framebuffer
  * E.g. for post-processing effects
  * Same framebuffer for all sub-passes
6. Multiple iterations for a specific sub-pass/sub-pass sequence
  * E.g. lens flare sprites, blurs
7. Multiple draws per sub-pass
  * E.g. per object
8. Multiple render passes per frame render
  * E.g. for shadow map creation vs use
  * Different sized targets/framebuffers

### 3.3.2 Pipeline programs and fixed functions

1. Hardware anti-aliasing (#1755)
  * MSAA
  * Coverage AA
  * TXAA ? [2.1]
2. Stencil test (#1863)
3. Scissor test (#1863)
4. Enable use of geometry shaders [2.1]
5. Enable use of tesselation evaluation and control shaders [2.1]
6. Enable use of compute shaders [2.1]
7. Create a unified shader program interface [2.0]

### 3.3.3 Lights and shadows [2.1]

1. Hundreds/thousands of lights (e.g. deferred renders)
2. Shadows cast by a directional, point, or spot light
3. Ambient occlusion (SSAO)
4. Shadows cast by multiple lights ?

### 3.3.4 Effects

* Fog [?]
* Blur
* Blend [?]
* Bloom
* FXAA
* SSAA
* Sepia
* Scanline
* Vignette
* Lens flare [2.1]
* Touch ripples (#1661) [2.1]
* Bumps/Parallax from height maps (#573) [2.1]

## 3.4 Performance Optimization Requirements

### 3.4.1 CPU and GPU Resources

1. Avoid unnecessary GL ES API calls to reduce CPU-GPU bus bottlenecks
  * E.g. track render context state to filter higher-frequency calls
2. Exploit GL ES API efficiency mechanisms where available and worthwhile, e.g.:
  * Interleaved vertex attribute buffer data - postion, normal, color (#773)
  * Uniform buffer objects (#1861) 
  * Program pipeline objects    
3. RenderScript (#633) ?
     
### 3.4.2 Memory Resources
  
1. Minimize allocations/enable re-use of memory resources where worthwhile, e.g. caching, pooling, and sharing of:
  * Client (Java/native C++) objects
    * Object geometries
    * Material specs
    * Vectors/matrices/quaternions (#1224)
  * Server (render context/GPU) objects
    * Program/shader sources/binaries
    * Source/sample textures
      * Fix memory leaks (#1801, #1805)
    * Render target `RenderBuffer` and `Texture2D` image buffers
  * Persistence scopes
    * Per App ? [2.1]
      * Store/load caches across processes, e.g. program binaries
    * Per Process
      * Restore GPU resources across context switches due to:
        * Activity/Fragment lifecycles
        * `SurfaceView` window attachments (#1619)
    * Per `SurfaceView` window attachment (render thread/render context/`Renderer`) ?
    * Per Scene model ?
    * Per Scene view ?
    * Per frame render and render pass
      * Share/re-use render target images/framebuffer attachments
      * Allow use of over-size images for smaller framebuffers 
2. Re-add `short` index buffers (#1816)

### 3.4.3 Data structures and algorithms

Some of these reqs could also be considered functional, but are only feasible if based on a sufficiently performant implementation and so listed here.   

1. Scene model graphs/trees
  * Enable simpler/faster culling for frame renders
  * Maintain current hierarchical parent-child affine transformations (local frame)
    * Enable parent tracking in child (#1751)
  * Enable future optimized functions [2.1], e.g.:
    * Collision detection (#1477)
    * Ray picking (#259)
    * Fractal instancing
    * "Infinite" order-of-magnitude zooming
  * Flat tree [2.0], quad tree [2.1], oct tree [2.1] 
2. Enable/disable frame renders per scene view
3. Z-order sorting - opaque, front to back, ~then transparent back to front~ (#789, #1236). [2.1]
  * Sorting of transparent objects in an automated fashion in 3D real time graphics is an open problem worthy of a PhD to any who solve it.
4. Deferred lighting [2.1]
5. ~On-the-fly ETC1 compression (#580) [2.1]~ The legitimate use case for this is somewhat sketchy. All texture compression has numerous implications on the end result, and they vary from type to type as well as based on the original content. This is very much a decision that needs to be made by the human composing the scene.

## 3.5 Interoperability Requirements

Where supported by the device and the GL ES API and not logically opposed, all functional and performance optimization requirements should:
  * Be simultaneously availble
  * Operate without functional conflicts
  
This does not preclude performance impacts; clearly there will be limits, but hardware keeps getting better/cheaper and we want to be able to take advantage of that without a lot of future rework...

## 3.6 Concurrency and Synchronization

### 3.6.1 Enable render thread safety
  * No public API fields; provide accessors
  * Document implicit default thread assumptions 
    * E.g. render thread-only (not safe) unless otherwise specified
  * Mark explicit thread-safety per type and/or per method with annotation
  * App responsible for observing thread safety markers, no built-in enforcement
   * Consider creating Android lint plugin

### 3.6.2  Simplify basic thread communication

  * Main/UI and other threads -> Render thread
    * Make it easy to post/queue message/events to the render thread
    * E.g. UI input, network, sensor, IO, and app model events sourced on any thread
  * Render thread -> Main/UI thread 
    * Make it easy to post/queue messages/events to the main/UI thread
    * E.g. from frame, animation, and transaction (see below) callbacks,
  * Render thread -> Other threads
    * Is app responsibility!

### 3.6.3 Provide transactional CRUD operations on overall render state

1. Enable app-level consistency (if properly used)
  * Allow any combination/granularity of operations on arbitrary subsets of the combined (Rajawali plus OpenGL ES) render-state
    * I.e. read and/or write any/all properties of scenes/graphs, scene views
  * All writes (Create/Updates/Deletes) in the same transaction:
    * Guaranteed to take effect as a group
    * Prior to the next render thread event (including other CRUD transactions)
  * Similar to current initScene(), but available whenever the render context is valid
2. Ensure specialized "ACID" semantics
  * Atomic - across combined state:
    * All layers of resources, including Rajawali Java/C++ instances and GL ES state
    * Either (atomic) success, or throw (runtime?/error?) exceptions if not possible
  * Consistent - internally 
    * Leave combined state operational, no crashes/exceptions
    * But no visual consistency guarantees, app responsibility
  * Isolated - from all (other) render thread/event processing
    * Frame draws, window re-sizes, queued events, other CRUD transactions, etc.
    * Implies running on the render thread as an event, queued from any thread
    * Queued transactions after window detach/loss of context are dropped
  * Durable - process persistence scope
    * Combined state preserved across Activities and render contexts
    * Essentially a requirement to provide automatic restores
3. Provide "successful completion" callback
  * Provide a variant that includes queueing to main/UI thread ? 

# 4. Testing, Debugging, and Reliability Requirements

1. Automated unit/integration test harness and tests 
  * Details ?
2. Internal runtime logging/tracing/assertions?
  * E.g. for integration testing and for debugging support issues
  * Judicious use of multi level logging with tags to signify source.
  * Proguard configuration to strip each logging level as desired for release builds
    * Removes code rather than simply using conditional checks
3. Compile-time API-usage validation, e.g. annotations ?
  * Are these enforceable e.g. via lint?
4. Runtime API-usage validation for the app, e.g. debug-only assertions/exceptions?
  * 
5. Debug visualizations (#1436)
  * Trident, camera eyeball/frustum, lighting rays/lines, object bounds
  * Are there (non-debug) app use cases for some of these as well? e.g.: 
    * Interactive camera/lighting selections from a global-view PIP inset 
    * Theater/stage lighting design apps
    * Interior design apps - door/window/furniture/light placements
  * If so perhaps just call them e.g. camera/light/bounds visualizations  

# 5. Modularity Requirements

  * Identify and implement separate library modules for optional/lower-frequency features
    * Core module vs add-ons:
      * VR 
      * AR
      * Modifiers, animations, rendering effects
      * Wear
      * TV?
      * Wallpaper
  * Still provide all-in-one module in addition to individual modules
  * Publish debug/development version in addition to release version of each module
    * Simplify app access to/visibility of engine-internal logging/tracing

# 6. Usage Documentation Requirements

1. JavaDocs
  * All public API types and methods
  * Non-obvious protected types. methods, and fields
  * Add JavaDocs publishing to build
2. Examples App
  * Per significant feature
    * Basic use case, not every variation
    * Multiple features per example OK
  * List TBD
  * Enable automatic updates to AppStore
    * We definately need this. We used to get a lot of traffic from the AppStore
3. Wiki Guideline .md docs
  * Major impact to content; new and refactored guides needed; priorities TBD
  * Complete migration to `docs` folder (#1633)

# 7. Design Goals and Constraints

## 7.1 Code consistency

Define and apply engine-wide naming, packaging, and coding conventions
  * Base on a widely-accepted existing set
    * E.g. start with [Google's Java style guide](https://google.github.io/styleguide/javaguide.html)
  * Override/add Rajawali specifics, including:
    * Eliminate Hungarian notation (#1581)
  
## 7.2 Architectural stability and extensibility

1. Favor the SOLID paradigm for OO design (especially for new code):
  * Single responsibility principle
    * Separate concerns to maximize functional cohesion and reduce couplings between modules, e.g. models vs views
    * Use chains/trees of responsibility, e.g. distribute/delegate rendering to a hierarchy of rendering components
  * Open/closed principle
    * Use abstract types (base classes and interfaces)
    * Extend them as needed (directly or by inheritance)
    * Avoid modifying them; when unavoidable, use formal deprecations
  * Liskov substitution principle
    * Avoid changing the observable state or behavior of a type in one of its subtypes
  * Interface segregation principle
    * Use more, smaller, role-specific interfaces rather than fewer, larger, multi-role interfaces
    * Specifically, use separate interfaces for public APIs vs engine internals
  * Dependency inversion principle
    * Use interfaces to access lower-level components from higher-level components 
    * Focus is on simplifying dynamic (re-)configuration, but could be also be useful for unit testing
    * Applications act as simple dependency injectors for the engine
2. Anticipate support for Vulkan 1.0
  * Eventual goal is for app compatibility across Vulkan and OpenGL ES devices
  * Identify/exploit common abstractions, e.g. render pass and sub-pass
  * Define formal GLES/Vulkan hinge points/substitutions, e.g. GPU resource allocations
  * A primary driver for use of SOLID principles, but no actual Vulkan API use yet!
3. Plan for incrementally incorporating/exploiting the bulk of the graphics system APIs over time
  * Reflect actual graphics APIs, wrap/delegate API abstractions directly
  * E.g. use Facades, stub in unused aspects when planned, implement when needed
  
## 7.3 Compatibility
  
1. NO requirement for backward compatibility of engine API types, methods, signatures, names, etc.
  * In general, expect much of the API to be impacted and to re-code existing apps accordingly
2. Maintain basic feature parity with prior versions
  * Explicitly document (here ? in DESIGN.md ?) any features working in 0.9 or 1.x that will be:
    * Postponed to later revisions, or
    * Dropped altogether for the foreseeable future    
3. Enable backward compatibility in all future engine releases
  * Plan for formal API deprecations
