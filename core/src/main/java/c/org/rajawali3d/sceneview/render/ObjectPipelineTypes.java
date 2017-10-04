package c.org.rajawali3d.sceneview.render;

import android.support.annotation.IntDef;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Object-specific pipeline function/operation types; defined per Subpass
 *
 * TODO how to support client-defined pipelines without extending this interface
 *
 * @author Randy Picolet
 */
public interface ObjectPipelineTypes {

    /**
     *   No object-specific rendering or pipeline state changes
     */
    int NO_OPERATION = 0x00;

    /**
     *  No Object3D geometry; all vertex data specified directly in (vertex/tesselation/geometry) shader(s)
     */
    int FIXED_OPERATION = 0x80;

    /**
     * Object3D geometry; vertex/fragment shaders with no lighting
     */
    int OBJECT_UNLIT = 0x100;

    /**
     * Object3D geometry; vertex/fragment shaders with lighting
     */
    int OBJECT_LIT_FORWARD = 0x200;

    /**
     * Object3D geometry; render G-buffer images required for QUAD_LIT_DEFERRED
     */
    int OBJECT_G_BUFFER = 0x300;

    /**
     * Quad2D geometry; using G-buffer to optimize lighting computations
     */
    int QUAD_LIT_DEFERRED = 0x400;

    /**
     * Object3D geometry; creating a map for shadow generation
     */
    int OBJECT_MAP_SHADOWS = 0x500;

    /**
     * Object3D geometry; vertex/fragment shaders with lighting & shadows
     */
    int OBJECT_SHADOWS_FORWARD = 0x600;

    /**
     * Quad2D geometry; using G-buffer to optimize lighting computations with shadows
     */
    int QUAD_SHADOWS_DEFERRED = 0x7000;

    /**
     * Quad2D geometry; textured using an input attachment output by a prior RenderPass/Subpass
     * TODO this one is probably useless, need actual post-processes, e.g. BLEND, BLUR, etc.
     */
    int QUAD_POST_PROCESSING = 0x1000;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            NO_OPERATION, FIXED_OPERATION, OBJECT_UNLIT, OBJECT_LIT_FORWARD, OBJECT_G_BUFFER, QUAD_LIT_DEFERRED,
            OBJECT_MAP_SHADOWS, OBJECT_SHADOWS_FORWARD, QUAD_SHADOWS_DEFERRED, QUAD_POST_PROCESSING
    })
    @interface ObjectPipelineFunction {}
}
