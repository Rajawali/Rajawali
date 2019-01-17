package org.rajawali3d.vuforia

import android.app.Activity
import com.vuforia.Device
import com.vuforia.Vuforia
import org.rajawali3d.util.RajLog
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class VuforiaManager(
        private val activity: Activity,
        private val glVersion: GLVersion,
        private val license: String
) {

    private val threadPoolExecutor: ThreadPoolExecutor = ThreadPoolExecutor(
            0,
            1,
            60L, TimeUnit.SECONDS,
            LinkedBlockingQueue()
    )

    private val device: Device
        get() = Device.getInstance()

    @Volatile
    var state: State = State.Init

    fun onConfigurationChanged() {
        if (state == State.Resumed) {
            RajLog.i("Vuforia setConfigurationChanged")
            device.setConfigurationChanged()
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        RajLog.i("Vuforia onSurfaceChanged($width, $height)")
        Vuforia.onSurfaceChanged(width, height)
    }

    fun onSurfaceCreated() {
        RajLog.i("Vuforia onSurfaceCreated")
        Vuforia.onSurfaceCreated()
    }

    /**
     * Request changes to the state of Vuforia. All changes are handled on a background thread in the order they are
     * requested.
     *
     * @param action an action to take on Vuforia to transition between states
     * @param callback a callback for listening to a successful transition.
     */
    @JvmOverloads
    fun request(
            action: Action,
            callback: () -> Unit = {}
    ): Unit = when (action) {
        Action.Prepare -> performTask({ prepare() }, callback)
        Action.Destroy -> performTask({ destroy() }, callback)
        Action.Resume -> performTask({ resume() }, callback)
        Action.Pause -> performTask({ pause() }, callback)
    }

    /**
     * Perform a custom task in the background. All tasks are handled in the order they are requested.
     */
    @JvmOverloads
    fun performTask(
            task: () -> Unit,
            callback: () -> Unit = {}
    ) = threadPoolExecutor.execute {
        task()
        callback()
    }

    private fun prepare() = migrate(
            requiredState = State.Init,
            newState = State.Initialized,
            forAction = Action.Prepare
    ) {
        Vuforia.setInitParameters(activity, glVersion.version, license)
        Vuforia.init()
    }

    private fun destroy() = migrate(
            permissibleStates = listOf(State.Initialized, State.Paused),
            newState = State.Init,
            forAction = Action.Destroy
    ) {
        Vuforia.deinit()
    }

    private fun pause() = migrate(
            permissibleStates = listOf(State.Initialized, State.Resumed),
            newState = State.Paused,
            forAction = Action.Pause
    ) {
        Vuforia.onPause()
    }

    private fun resume() = migrate(
            permissibleStates = listOf(State.Initialized, State.Paused),
            newState = State.Resumed,
            forAction = Action.Pause
    ) {
        Vuforia.onResume()
    }

    private fun migrate(requiredState: State, newState: State, forAction: Action, func: () -> Unit) =
            migrate(listOf(requiredState), newState, forAction, func)

    private fun migrate(permissibleStates: List<State>, newState: State, forAction: Action, func: () -> Unit) {
        state.let { state ->
            if (!permissibleStates.contains(state))
                throw Exception("Illegal state $state for action $forAction")
        }
        func()
        RajLog.i("Vuforia migrated from ${state.javaClass.simpleName} to ${newState.javaClass.simpleName}")
        state = newState
    }

    enum class GLVersion(val version: Int) {
        GL_20(Vuforia.GL_20),
        GL_30(Vuforia.GL_30);
    }

    sealed class Action {
        object Prepare : Action()
        object Destroy : Action()
        object Pause : Action()
        object Resume : Action()
    }

    sealed class State {
        object Init : State()
        object Initialized : State()
        object Resumed : State()
        object Paused : State()
    }

}
