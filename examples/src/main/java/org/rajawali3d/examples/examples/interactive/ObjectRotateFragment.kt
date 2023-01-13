package org.rajawali3d.examples.examples.interactive

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.bmwgroup.apinext.facedetection.utils.round
import org.rajawali3d.Object3D
import org.rajawali3d.examples.R
import org.rajawali3d.examples.databinding.ObjectRoateOverlayBinding
import org.rajawali3d.examples.examples.AExampleFragment
import org.rajawali3d.lights.DirectionalLight
import org.rajawali3d.loader.LoaderAWD
import org.rajawali3d.materials.Material
import org.rajawali3d.materials.methods.DiffuseMethod.Lambert
import org.rajawali3d.math.vector.Vector3
import org.rajawali3d.renderer.ISurfaceRenderer
import org.rajawali3d.util.ObjectColorPicker

@SuppressLint("SetTextI18n")
class ObjectRotateFragment : AExampleFragment() {

    private var dataRenderer: ObjectRotateRenderer? = null
    private var _binding: ObjectRoateOverlayBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = ObjectRoateOverlayBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textMixed.setOnClickListener { binding.seekBarMixed.progress = 0 }
        binding.textY.setOnClickListener { binding.seekBarY.progress = 0 }
        binding.textHorizontal.setOnClickListener { binding.seekBarHoizontal.progress = 0 }

        binding.seekBarMixed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dataRenderer?.rotateDataX(progress.toDouble())
                binding.textMixed.text = "Z=${progress}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        binding.seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dataRenderer?.rotateDataY(progress.toDouble())
                binding.textY.text = "Y=${progress}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })
        binding.seekBarHoizontal.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                dataRenderer?.rotateDataZ(progress.toDouble())
                binding.textHorizontal.text = "X=${progress}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
        })

    }

    override fun createRenderer(): ISurfaceRenderer {
        dataRenderer = ObjectRotateRenderer(requireActivity(), this)
        return dataRenderer as ObjectRotateRenderer
    }

    private inner class ObjectRotateRenderer(context: Context, fragment: AExampleFragment) : AExampleRenderer(context, fragment) {
        private lateinit var light: DirectionalLight
        private lateinit var monkey: Object3D
        private lateinit var picker: ObjectColorPicker
        override fun initScene() {
            try {
                picker = ObjectColorPicker(this)
                light = DirectionalLight(-1.0, 0.0, 1.0)
                light.setPosition(0.0, 0.0, -4.0)
                light.power = 1.5f
                currentScene.addLight(light)
                currentCamera.setPosition(0.0, 0.0, 7.0)
                val material = Material()
                material.enableLighting(true)
                material.diffuseMethod = Lambert()
                val parser = LoaderAWD(mContext.resources, mTextureManager, R.raw.awd_suzanne)
                parser.parse()

                // Inserting a couple nested containers to test child picking;
                // should appear/behave the same
                val container = Object3D()
                currentScene.addChild(container)
                val container1 = Object3D()
                container1.setScale(.7)
                container1.setPosition(0.0, 0.0, 0.0)
                container.addChild(container1)
                monkey = parser.parsedObject
                monkey.rotY = 0.0
                monkey.material = material
                monkey.setColor(0x0000ff)
                container1.addChild(monkey)
                picker.registerObject(monkey)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onRender(elapsedRealtime: Long, deltaTime: Double) {
            super.onRender(elapsedRealtime, deltaTime)
            Log.d("rotation", "X=${monkey.rotX.round(1)} Y=${monkey.rotY.round(1)} Z=${monkey.rotZ.round(1)}")
        }

        fun rotateDataX(value: Double) {
            Log.d("set rotation", "X=$value")
            monkey.setRotation(Vector3(value, monkey.rotY, monkey.rotZ))
        }

        fun rotateDataY(value: Double) {
            Log.d("set rotation", "Y=$value")
            monkey.setRotation(Vector3(monkey.rotX, value, monkey.rotZ))
        }

        fun rotateDataZ(value: Double) {
            Log.d("set rotation", "Z=$value")
            monkey.setRotation(Vector3(monkey.rotX, monkey.rotY, value))
        }

    }
}