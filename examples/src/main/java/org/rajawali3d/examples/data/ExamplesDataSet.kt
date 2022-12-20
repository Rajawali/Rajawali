package org.rajawali3d.examples.data

import org.rajawali3d.examples.R
import org.rajawali3d.examples.examples.general.BasicFragment
import org.rajawali3d.examples.examples.general.SkyboxFragment
import org.rajawali3d.examples.examples.general.CollisionDetectionFragment
import org.rajawali3d.examples.examples.general.LinesFragment
import org.rajawali3d.examples.examples.general.ColoredLinesFragment
import org.rajawali3d.examples.examples.general.ChaseCameraFragment
import org.rajawali3d.examples.examples.general.UsingGeometryDataFragment
import org.rajawali3d.examples.examples.general.ThreeSixtyImagesFragment
import org.rajawali3d.examples.examples.general.TerrainFragment
import org.rajawali3d.examples.examples.general.CurvesFragment
import org.rajawali3d.examples.examples.general.SpiralsFragment
import org.rajawali3d.examples.examples.general.SVGPathFragment
import org.rajawali3d.examples.examples.general.UniformDistributionFragment
import org.rajawali3d.examples.examples.general.OrthographicFragment
import org.rajawali3d.examples.examples.general.ArcballCameraFragment
import org.rajawali3d.examples.examples.general.DebugRendererFragment
import org.rajawali3d.examples.examples.general.DebugVisualizerFragment
import org.rajawali3d.examples.examples.lights.DirectionalLightFragment
import org.rajawali3d.examples.examples.lights.PointLightFragment
import org.rajawali3d.examples.examples.lights.SpotLightFragment
import org.rajawali3d.examples.examples.lights.MultipleLightsFragment
import org.rajawali3d.examples.examples.interactive.AccelerometerFragment
import org.rajawali3d.examples.examples.interactive.ObjectPickingFragment
import org.rajawali3d.examples.examples.interactive.ObjectRotateFragment
import org.rajawali3d.examples.examples.interactive.TouchAndDragFragment
import org.rajawali3d.examples.examples.interactive.FirstPersonCameraFragment
import org.rajawali3d.examples.examples.ui.UIElementsFragment
import org.rajawali3d.examples.examples.ui.TwoDimensionalFragment
import org.rajawali3d.examples.examples.ui.TransparentSurfaceFragment
import org.rajawali3d.examples.examples.ui.AnimatedTextureViewFragment
import org.rajawali3d.examples.examples.ui.ScrollingTextureViewFragment
import org.rajawali3d.examples.examples.ui.ViewToTextureFragment
import org.rajawali3d.examples.examples.optimizations.Optimized2000PlanesFragment
import org.rajawali3d.examples.examples.optimizations.UpdateVertexBufferFragment
import org.rajawali3d.examples.examples.optimizations.ETC1TextureCompressionFragment
import org.rajawali3d.examples.examples.optimizations.TextureAtlasFragment
import org.rajawali3d.examples.examples.optimizations.ETC2TextureCompressionFragment
import org.rajawali3d.examples.examples.loaders.AwdFragment
import org.rajawali3d.examples.examples.loaders.AsyncLoadModelFragment
import org.rajawali3d.examples.examples.loaders.LoadModelFragment
import org.rajawali3d.examples.examples.loaders.FBXFragment
import org.rajawali3d.examples.examples.loaders.LoaderGCodeFragment
import org.rajawali3d.examples.examples.animation.AnimationFragment
import org.rajawali3d.examples.examples.animation.BezierFragment
import org.rajawali3d.examples.examples.animation.CoalesceAnimationFragment
import org.rajawali3d.examples.examples.animation.MD2Fragment
import org.rajawali3d.examples.examples.animation.CatmullRomFragment
import org.rajawali3d.examples.examples.animation.AnimatedSpritesFragment
import org.rajawali3d.examples.examples.animation.SkeletalAnimationMD5Fragment
import org.rajawali3d.examples.examples.animation.SkeletalAnimationAWDFragment
import org.rajawali3d.examples.examples.animation.SkeletalAnimationBlendingFragment
import org.rajawali3d.examples.examples.animation.ColorAnimationFragment
import org.rajawali3d.examples.examples.materials.*
import org.rajawali3d.examples.examples.ui.CanvasTextFragment
import org.rajawali3d.examples.examples.postprocessing.FogFragment
import org.rajawali3d.examples.examples.postprocessing.SepiaFilterFragment
import org.rajawali3d.examples.examples.postprocessing.GreyScaleFilterFragment
import org.rajawali3d.examples.examples.postprocessing.GaussianBlurFilterFragment
import org.rajawali3d.examples.examples.postprocessing.MultiPassFragment
import org.rajawali3d.examples.examples.postprocessing.RenderToTextureFragment
import org.rajawali3d.examples.examples.postprocessing.BloomEffectFragment
import org.rajawali3d.examples.examples.postprocessing.ShadowMappingFragment
import org.rajawali3d.examples.examples.postprocessing.FXAAFragment
import org.rajawali3d.examples.examples.scene.SceneFrameCallbackFragment
import org.rajawali3d.examples.examples.scene.ObjectAddRemoveFragment
import java.util.*

class ExamplesDataSet private constructor() {
    val categories: List<Category> = createCategories()

    companion object {
        @JvmStatic
        @get:Synchronized
        @Volatile
        var instance: ExamplesDataSet? = null
            get() {
                if (field == null) {
                    synchronized(ExamplesDataSet::class.java) {
                        if (field == null) {
                            field = ExamplesDataSet()
                        }
                    }
                }
                return field
            }
            private set

        private fun createCategories(): List<Category> {
            val categories: MutableList<Category> = LinkedList()
            categories.add(
                Category(
                    R.string.category_general, arrayOf(
                        Example(R.string.example_general_getting_started, BasicFragment::class.java),
                        Example(R.string.example_general_skybox, SkyboxFragment::class.java),
                        Example(R.string.example_general_collision_detection, CollisionDetectionFragment::class.java),
                        Example(R.string.example_general_lines, LinesFragment::class.java),
                        Example(R.string.example_general_colored_lines, ColoredLinesFragment::class.java),
                        Example(R.string.example_general_chase_camera, ChaseCameraFragment::class.java),
                        Example(R.string.example_general_using_geometry_data, UsingGeometryDataFragment::class.java),
                        Example(R.string.example_general_360_images, ThreeSixtyImagesFragment::class.java),
                        Example(R.string.example_general_terrain, TerrainFragment::class.java),
                        Example(R.string.example_general_curves, CurvesFragment::class.java),
                        Example(R.string.example_general_spirals, SpiralsFragment::class.java),
                        Example(R.string.example_general_svg_path, SVGPathFragment::class.java),
                        Example(R.string.example_general_uniform_distribution, UniformDistributionFragment::class.java),
                        Example(R.string.example_general_orthographic_camera, OrthographicFragment::class.java),
                        Example(R.string.example_general_arcball_camera, ArcballCameraFragment::class.java),
                        Example(R.string.example_general_debug_renderer, DebugRendererFragment::class.java),
                        Example(R.string.example_general_debug_visualizer, DebugVisualizerFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_lights, arrayOf(
                        Example(R.string.example_lights_directional_light, DirectionalLightFragment::class.java),
                        Example(R.string.example_lights_point_light, PointLightFragment::class.java),
                        Example(R.string.example_lights_spot_light, SpotLightFragment::class.java),
                        Example(R.string.example_lights_multiple_light, MultipleLightsFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_interactive, arrayOf(
                        Example(R.string.example_interactive_using_accelerometer, AccelerometerFragment::class.java),
                        Example(R.string.example_general_using_geometry_data, UsingGeometryDataFragment::class.java),
                        Example(R.string.example_interactive_object_picking, ObjectPickingFragment::class.java),
                        Example(R.string.example_interactive_object_rotate, ObjectRotateFragment::class.java),
                        Example(R.string.example_interactive_touch_drag, TouchAndDragFragment::class.java),
                        Example(R.string.example_interactive_first_person_camera, FirstPersonCameraFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_ui, arrayOf(
                        Example(R.string.example_ui_elements, UIElementsFragment::class.java),
                        Example(R.string.example_ui_2d_render, TwoDimensionalFragment::class.java),
                        Example(R.string.example_ui_transparent_surface_view, TransparentSurfaceFragment::class.java),
                        Example(R.string.example_ui_texture_view_xml, AnimatedTextureViewFragment::class.java),
                        Example(R.string.example_ui_scrolling_texture_view, ScrollingTextureViewFragment::class.java),
                        Example(R.string.example_ui_view_to_texture, ViewToTextureFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_optimizations, arrayOf(
                        Example(R.string.example_optimizations_2000_planes, Optimized2000PlanesFragment::class.java),
                        Example(R.string.example_optimizations_update_vertex_buffer, UpdateVertexBufferFragment::class.java),
                        Example(R.string.example_optimizations_etc1_compression, ETC1TextureCompressionFragment::class.java),
                        Example(R.string.example_optimizations_texture_atlas, TextureAtlasFragment::class.java),
                        Example(R.string.example_optimizations_etc2_compression, ETC2TextureCompressionFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_loaders, arrayOf(
                        Example(R.string.example_loaders_load_awd, AwdFragment::class.java),
                        Example(R.string.example_loaders_load_async, AsyncLoadModelFragment::class.java),
                        Example(R.string.example_loaders_load_obj, LoadModelFragment::class.java),
                        Example(R.string.example_loaders_load_fbx, FBXFragment::class.java),
                        Example(R.string.example_loaders_load_gcode, LoaderGCodeFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_animation, arrayOf(
                        Example(R.string.example_animation_basic, AnimationFragment::class.java),
                        Example(R.string.example_animation_bezier, BezierFragment::class.java),
                        Example(R.string.example_animation_coalesce, CoalesceAnimationFragment::class.java),
                        Example(R.string.example_animation_md2, MD2Fragment::class.java),
                        Example(R.string.example_animation_catmul_rom, CatmullRomFragment::class.java),
                        Example(R.string.example_animation_sprites, AnimatedSpritesFragment::class.java),
                        Example(R.string.example_animation_skeletal_md5, SkeletalAnimationMD5Fragment::class.java),
                        Example(R.string.example_animation_skeletal_awd, SkeletalAnimationAWDFragment::class.java),
                        Example(R.string.example_animation_skeletal_blending, SkeletalAnimationBlendingFragment::class.java),
                        Example(R.string.example_animation_color, ColorAnimationFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_materials, arrayOf(
                        Example(R.string.example_materials_basic, MaterialsFragment::class.java),
                        Example(R.string.example_materials_custom, CustomMaterialShaderFragment::class.java),
                        Example(R.string.example_materials_normal, BumpMappingFragment::class.java),
                        Example(R.string.example_materials_toon, ToonShadingFragment::class.java),
                        Example(R.string.example_materials_vertex, CustomVertexShaderFragment::class.java),
                        Example(R.string.example_materials_sphere, SphereMapFragment::class.java),
                        Example(R.string.example_materials_canvas, CanvasTextFragment::class.java),
                        Example(R.string.example_materials_specular_alpha, SpecularAndAlphaFragment::class.java),
                        Example(R.string.example_materials_video, VideoTextureFragment::class.java),
                        Example(R.string.example_materials_from_files, RawShaderFilesFragment::class.java),
                        Example(R.string.example_materials_animated_gif, AnimatedGIFTextureFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_post_processing, arrayOf(
                        Example(R.string.example_post_processing_fog, FogFragment::class.java),
                        Example(R.string.example_post_processing_sepia_filter, SepiaFilterFragment::class.java),
                        Example(R.string.example_post_processing_greyscale_filter, GreyScaleFilterFragment::class.java),
                        Example(R.string.example_post_processing_gaussian_blur_filter, GaussianBlurFilterFragment::class.java),
                        Example(R.string.example_post_processing_5th_element_multi_pass, MultiPassFragment::class.java),
                        Example(R.string.example_post_processing_render_to_texture, RenderToTextureFragment::class.java),
                        Example(R.string.example_post_processing_bloom_effect, BloomEffectFragment::class.java),
                        Example(R.string.example_post_processing_shadow_mapping, ShadowMappingFragment::class.java),
                        Example(R.string.example_post_processing_fxaa, FXAAFragment::class.java)
                    )
                )
            )
            categories.add(
                Category(
                    R.string.category_scenes, arrayOf(
                        Example(R.string.example_scene_frame_callbacks, SceneFrameCallbackFragment::class.java),
                        Example(R.string.example_scene_add_remove_objects, ObjectAddRemoveFragment::class.java)
                    )
                )
            )
            return categories
        }
    }
}