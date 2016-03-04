package org.rajawali3d.examples;

import android.app.Application;
import org.rajawali3d.examples.examples.AExampleFragment;
import org.rajawali3d.examples.examples.about.CommunityFeedFragment;
import org.rajawali3d.examples.examples.about.MeetTheTeamFragment;
import org.rajawali3d.examples.examples.animation.AnimationFragment;
import org.rajawali3d.examples.examples.animation.BezierFragment;
import org.rajawali3d.examples.examples.animation.CatmullRomFragment;
import org.rajawali3d.examples.examples.animation.CoalesceAnimationFragment;
import org.rajawali3d.examples.examples.animation.ColorAnimationFragment;
import org.rajawali3d.examples.examples.animation.MD2Fragment;
import org.rajawali3d.examples.examples.animation.SkeletalAnimationAWDFragment;
import org.rajawali3d.examples.examples.animation.SkeletalAnimationBlendingFragment;
import org.rajawali3d.examples.examples.animation.SkeletalAnimationMD5Fragment;
import org.rajawali3d.examples.examples.general.ArcballCameraFragment;
import org.rajawali3d.examples.examples.general.BasicFragment;
import org.rajawali3d.examples.examples.general.ChaseCameraFragment;
import org.rajawali3d.examples.examples.general.CollisionDetectionFragment;
import org.rajawali3d.examples.examples.general.ColoredLinesFragment;
import org.rajawali3d.examples.examples.general.CurvesFragment;
import org.rajawali3d.examples.examples.general.DebugRendererFragment;
import org.rajawali3d.examples.examples.general.DebugVisualizerFragment;
import org.rajawali3d.examples.examples.general.LinesFragment;
import org.rajawali3d.examples.examples.general.OrthographicFragment;
import org.rajawali3d.examples.examples.general.SVGPathFragment;
import org.rajawali3d.examples.examples.general.SkyboxFragment;
import org.rajawali3d.examples.examples.general.SpiralsFragment;
import org.rajawali3d.examples.examples.general.TerrainFragment;
import org.rajawali3d.examples.examples.general.ThreeSixtyImagesFragment;
import org.rajawali3d.examples.examples.general.UniformDistributionFragment;
import org.rajawali3d.examples.examples.general.UsingGeometryDataFragment;
import org.rajawali3d.examples.examples.interactive.AccelerometerFragment;
import org.rajawali3d.examples.examples.interactive.FirstPersonCameraFragment;
import org.rajawali3d.examples.examples.interactive.ObjectPickingFragment;
import org.rajawali3d.examples.examples.interactive.TouchAndDragFragment;
import org.rajawali3d.examples.examples.lights.DirectionalLightFragment;
import org.rajawali3d.examples.examples.lights.MultipleLightsFragment;
import org.rajawali3d.examples.examples.lights.PointLightFragment;
import org.rajawali3d.examples.examples.lights.SpotLightFragment;
import org.rajawali3d.examples.examples.loaders.AsyncLoadModelFragment;
import org.rajawali3d.examples.examples.loaders.AwdFragment;
import org.rajawali3d.examples.examples.loaders.FBXFragment;
import org.rajawali3d.examples.examples.loaders.LoadModelFragment;
import org.rajawali3d.examples.examples.loaders.LoaderGCodeFragment;
import org.rajawali3d.examples.examples.materials.AnimatedGIFTextureFragment;
import org.rajawali3d.examples.examples.materials.BumpMappingFragment;
import org.rajawali3d.examples.examples.materials.CustomMaterialShaderFragment;
import org.rajawali3d.examples.examples.materials.CustomVertexShaderFragment;
import org.rajawali3d.examples.examples.materials.MaterialsFragment;
import org.rajawali3d.examples.examples.materials.RawShaderFilesFragment;
import org.rajawali3d.examples.examples.materials.SpecularAndAlphaFragment;
import org.rajawali3d.examples.examples.materials.SphereMapFragment;
import org.rajawali3d.examples.examples.materials.ToonShadingFragment;
import org.rajawali3d.examples.examples.materials.VideoTextureFragment;
import org.rajawali3d.examples.examples.optimizations.ETC1TextureCompressionFragment;
import org.rajawali3d.examples.examples.optimizations.ETC2TextureCompressionFragment;
import org.rajawali3d.examples.examples.optimizations.Optimized2000PlanesFragment;
import org.rajawali3d.examples.examples.optimizations.TextureAtlasFragment;
import org.rajawali3d.examples.examples.optimizations.UpdateVertexBufferFragment;
import org.rajawali3d.examples.examples.postprocessing.BloomEffectFragment;
import org.rajawali3d.examples.examples.postprocessing.FogFragment;
import org.rajawali3d.examples.examples.postprocessing.GaussianBlurFilterFragment;
import org.rajawali3d.examples.examples.postprocessing.GreyScaleFilterFragment;
import org.rajawali3d.examples.examples.postprocessing.MultiPassFragment;
import org.rajawali3d.examples.examples.postprocessing.RenderToTextureFragment;
import org.rajawali3d.examples.examples.postprocessing.SepiaFilterFragment;
import org.rajawali3d.examples.examples.postprocessing.ShadowMappingFragment;
import org.rajawali3d.examples.examples.scene.SceneFrameCallbackFragment;
import org.rajawali3d.examples.examples.ui.AnimatedTextureViewFragment;
import org.rajawali3d.examples.examples.ui.CanvasTextFragment;
import org.rajawali3d.examples.examples.ui.ScrollingTextureViewFragment;
import org.rajawali3d.examples.examples.ui.TransparentSurfaceFragment;
import org.rajawali3d.examples.examples.ui.TwoDimensionalFragment;
import org.rajawali3d.examples.examples.ui.UIElementsFragment;
import org.rajawali3d.examples.examples.ui.ViewToTextureFragment;
import org.rajawali3d.examples.examples.vr_ar.VuforiaExampleFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ExamplesApplication extends Application {

	static enum Category {

		// @formatter:off
		GENERAL("General")
		, LIGHTS("Lights")
		, INTERACTIVE("Interactive")
		, UI("UI")
		, OPTIMIZATIONS("Optimizations")
		, LOADERS("Loaders")
		, ANIMATION("Animation")
		, MATERIALS("Materials")
		, POSTPROCESSING("Post Processing")
        , SCENE("Scenes")
		, VR_AR("VR and AR")
		, ABOUT("About");
		// @formatter:on

		private String name;

		Category(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

	}

	public static final Map<Category, ExampleItem[]> ITEMS = new HashMap<Category, ExamplesApplication.ExampleItem[]>();
	public static final ArrayList<TeamMember> TEAM_MEMBERS = new ArrayList<ExamplesApplication.TeamMember>();
	public static final String BASE_EXAMPLES_URL = "https://github.com/MasDennis/RajawaliExamples/blob/master/src/com/monyetmabuk/rajawali/tutorials/examples";

	@Override
	public void onCreate() {
		super.onCreate();

		// @formatter:off
		ITEMS.put(Category.GENERAL, new ExampleItem[] {
				new ExampleItem("Getting Started", BasicFragment.class)
				, new ExampleItem("Skybox", SkyboxFragment.class)
				, new ExampleItem("Collision Detection", CollisionDetectionFragment.class)
				, new ExampleItem("Lines", LinesFragment.class)
				, new ExampleItem("Colored Lines", ColoredLinesFragment.class)
				, new ExampleItem("Chase Camera", ChaseCameraFragment.class)
				, new ExampleItem("Using Geometry Data", UsingGeometryDataFragment.class)
				, new ExampleItem("360 Images", ThreeSixtyImagesFragment.class)
				, new ExampleItem("Terrain", TerrainFragment.class)
				, new ExampleItem("Curves", CurvesFragment.class)
                , new ExampleItem("Spirals", SpiralsFragment.class)
				, new ExampleItem("SVG Path", SVGPathFragment.class)
				, new ExampleItem("Uniform Distribution", UniformDistributionFragment.class)
				, new ExampleItem("Orthographic Camera", OrthographicFragment.class)
                , new ExampleItem("Arcball Camera", ArcballCameraFragment.class)
                , new ExampleItem("Debug Renderer", DebugRendererFragment.class)
                , new ExampleItem("Debug Visualizer", DebugVisualizerFragment.class)
			});
		ITEMS.put(Category.LIGHTS, new ExampleItem[]{
				new ExampleItem("Directional Light", DirectionalLightFragment.class)
				, new ExampleItem("Point Light", PointLightFragment.class)
				, new ExampleItem("Spot Light", SpotLightFragment.class)
				, new ExampleItem("Multiple Lights", MultipleLightsFragment.class)
			});
		/*
		ITEMS.put(Categories.EFFECTS, new ExampleItem[] {
				new ExampleItem("Particles", ParticlesFragment.class)
				// Post processing is broken, removed until fixed.
				//, new ExampleItem("Touch Ripples", TouchRipplesFragment.class)
				, new ExampleItem("Fog", FogFragment.class)
			});
			*/
		ITEMS.put(Category.INTERACTIVE, new ExampleItem[] {
				new ExampleItem("Using The Accelerometer", AccelerometerFragment.class)
				, new ExampleItem("Object Picking", ObjectPickingFragment.class)
				, new ExampleItem("Touch & Drag", TouchAndDragFragment.class)
                , new ExampleItem("First Person Camera", FirstPersonCameraFragment.class)
			});
		ITEMS.put(Category.UI, new ExampleItem[] {
				new ExampleItem("UI Elements", UIElementsFragment.class)
				, new ExampleItem("2D Renderer", TwoDimensionalFragment.class)
				, new ExampleItem("Transparent SurfaceView", TransparentSurfaceFragment.class)
                , new ExampleItem("TextureView/XML", AnimatedTextureViewFragment.class)
				, new ExampleItem("Scrolling TextureView", ScrollingTextureViewFragment.class)
				, new ExampleItem("View To Texture", ViewToTextureFragment.class)
			});
		ITEMS.put(Category.OPTIMIZATIONS, new ExampleItem[] {
				new ExampleItem("2000 Textured Planes", Optimized2000PlanesFragment.class)
				, new ExampleItem("Update Vertex Buffer", UpdateVertexBufferFragment.class)
				, new ExampleItem("ETC1 Texture Compression", ETC1TextureCompressionFragment.class)
				, new ExampleItem("Texture Atlas", TextureAtlasFragment.class)
                , new ExampleItem("ETC2 Texture Compression", ETC2TextureCompressionFragment.class)
			});
		ITEMS.put(Category.LOADERS, new ExampleItem[] {
				new ExampleItem("Load AWD Model", AwdFragment.class)
                , new ExampleItem("Async Load Model", AsyncLoadModelFragment.class)
				, new ExampleItem("Load OBJ Model", LoadModelFragment.class)
				, new ExampleItem("FBX Scene Importer", FBXFragment.class)
				, new ExampleItem("GCode Toolpaths", LoaderGCodeFragment.class)
			});
		ITEMS.put(Category.ANIMATION, new ExampleItem[] {
				new ExampleItem("Animation", AnimationFragment.class)
				, new ExampleItem("Bezier Path Animation", BezierFragment.class)
                , new ExampleItem("Coalesce Animation", CoalesceAnimationFragment.class)
				, new ExampleItem("MD2 Animation", MD2Fragment.class)
				, new ExampleItem("Catmul-Rom Splines", CatmullRomFragment.class)
				//, new ExampleItem("Animated Sprites", AnimatedSpritesFragment.class)
				, new ExampleItem("Skeletal Animation (MD5)", SkeletalAnimationMD5Fragment.class)
				, new ExampleItem("Skeletal Animation (AWD)", SkeletalAnimationAWDFragment.class)
				, new ExampleItem("Skeletal Animation Blending", SkeletalAnimationBlendingFragment.class)
				, new ExampleItem("Color Animation", ColorAnimationFragment.class)
			});
		ITEMS.put(Category.MATERIALS, new ExampleItem[] {
				new ExampleItem("Materials", MaterialsFragment.class)
				, new ExampleItem("Custom Material", CustomMaterialShaderFragment.class)
				, new ExampleItem("Normal Mapping", BumpMappingFragment.class)
				, new ExampleItem("Toon Shading", ToonShadingFragment.class)
				, new ExampleItem("Custom Vertex Shader", CustomVertexShaderFragment.class)
				, new ExampleItem("Sphere Mapping", SphereMapFragment.class)
				, new ExampleItem("Canvas Text to Material", CanvasTextFragment.class)
				, new ExampleItem("Specular Alpha", SpecularAndAlphaFragment.class)
				, new ExampleItem("Video Texture", VideoTextureFragment.class)
				, new ExampleItem("Loading Shader Textfiles", RawShaderFilesFragment.class)
				, new ExampleItem("Animated GIF Texture", AnimatedGIFTextureFragment.class)
			});
		ITEMS.put(Category.POSTPROCESSING, new ExampleItem[] {
                new ExampleItem("Fog", FogFragment.class)
				, new ExampleItem("Sepia Filter", SepiaFilterFragment.class)
				, new ExampleItem("Greyscale Filter", GreyScaleFilterFragment.class)
				, new ExampleItem("Gaussian Blur Filter", GaussianBlurFilterFragment.class)
				, new ExampleItem("Multi Pass", MultiPassFragment.class)
				, new ExampleItem("Render to Texture", RenderToTextureFragment.class)
				, new ExampleItem("Bloom Effect", BloomEffectFragment.class)
				, new ExampleItem("Shadow Mapping", ShadowMappingFragment.class)
		});
        ITEMS.put(Category.SCENE, new ExampleItem[] {
                new ExampleItem("Frame Callbacks", SceneFrameCallbackFragment.class)
        });
		ITEMS.put(Category.VR_AR, new ExampleItem[] {
				new ExampleItem("Vuforia Integration", VuforiaExampleFragment.class)
		});
		ITEMS.put(Category.ABOUT, new ExampleItem[] {
			new ExampleItem("Community Stream", CommunityFeedFragment.class)
			, new ExampleItem("Meet The Team", MeetTheTeamFragment.class)
		});

		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_rajawali3dcommunity
				, "Rajawali 3D Community"
				, null
				, "https://plus.google.com/communities/116529974266844528013"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_andrewjo
				, "Andrew Jo"
				, null
				, "https://plus.google.com/103571530640762510321/posts"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_davidtroustine
				, "David Trounstine"
				, "Blue Moon, Spaten, Dos Equis"
				, "https://plus.google.com/100061339163339558529/posts"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_dennisippel
				, "Dennis Ippel"
				, "Innis & Gunn, La Chouffe, Duvel"
				, "https://plus.google.com/110899192955767806500/posts"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_ianthomas
				, "Ian Thomas"
				, "New Castle, Sapporo, Hopsecutioner"
				, "https://plus.google.com/117877053554468827150/posts"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_jaredwoolston
				, "Jared Woolston"
				, "Guinness, Smithwicks, Batch 19, Boston Lager"
				, "https://plus.google.com/111355740389558136627/posts"
			));
		TEAM_MEMBERS.add(new TeamMember(
				R.drawable.photo_jayweisskopf
				, "Jay Weisskopf"
				, " Allagash, Corona, Hoegaarden"
				, "https://plus.google.com/101121628537383400065/posts"
			));
		// @formatter:on
	}

	public static final class ExampleItem {

		public final Class<? extends AExampleFragment> exampleClass;
		public final String                            title;
		public final String                            url;

		public ExampleItem(String title, Class<? extends AExampleFragment> exampleClass) {
			this.title = title;
			this.exampleClass = exampleClass;
			this.url = exampleClass.getSimpleName() + ".java";
		}

		public String getUrl(Category category) {
			switch (category) {
			case ABOUT:
				// About category has no example links
				return null;
			default:
				return BASE_EXAMPLES_URL + "/"
						+ category.name.toLowerCase(Locale.US) + "/" + url;
			}
		}
	}

	public static final class TeamMember {
		public int photo;
		public String name;
		public String favoriteBeer;
		public String link;

		protected TeamMember(int photo, String name, String about, String link) {
			this.photo = photo;
			this.name = name;
			this.favoriteBeer = about;
			this.link = link;
		}
	}

}
