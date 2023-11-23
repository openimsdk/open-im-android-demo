package io.openim.android.ouimeeting.widget

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.viewbinding.BindableItem
import com.xwray.groupie.viewbinding.GroupieViewHolder
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import io.livekit.android.room.participant.ConnectionQuality
import io.livekit.android.room.participant.Participant
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import io.livekit.android.util.flow
import io.openim.android.ouicore.R
import io.openim.android.ouicore.base.vm.State
import io.openim.android.ouicore.entity.ParticipantMeta
import io.openim.android.ouicore.net.bage.GsonHel
import io.openim.android.ouimeeting.databinding.LayoutUserStatusBinding
import io.openim.android.ouimeeting.databinding.ViewSingleTextureBinding
import io.openim.android.ouimeeting.entity.RoomMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ParticipantItem(
    private val room: Room,
    private val participant: Participant,
    private val roomMetadata: State<RoomMetadata>,
    private val parents: RecyclerView,

    ) : BindableItem<ViewSingleTextureBinding>() {

    private var boundVideoTrack: VideoTrack? = null
    private var coroutineScope: CoroutineScope? = null

    override fun initializeViewBinding(view: View): ViewSingleTextureBinding {
        val binding = ViewSingleTextureBinding.bind(view)
        room.initVideoRenderer(binding.textureView)

        return binding
    }

    private fun ensureCoroutineScope() {
        if (coroutineScope == null) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        }
    }

    private fun isHostUser(participant: Participant): Boolean {
        return if (null == roomMetadata.value) false
        else null != participant.identity && (participant.identity == roomMetadata.value!!.hostUserID)
    }

    private fun getMetaUserName(participantMeta: ParticipantMeta): String? {
        try {
            var name = participantMeta.groupMemberInfo.nickname
            if (TextUtils.isEmpty(name)) name = participantMeta.userInfo.nickname
            return name
        } catch (ignore: Exception) {
        }
        return ""
    }

    private fun bindConnectionQuality(holder: LayoutUserStatusBinding, quality: ConnectionQuality) {
        when (quality) {
            ConnectionQuality.UNKNOWN,
            ConnectionQuality.EXCELLENT -> holder.net.setImageResource(R.mipmap.ic_net_excellent)
            ConnectionQuality.GOOD -> holder.net.setImageResource(R.mipmap.ic_net_good)
            else
            -> holder.net.setImageResource(R.mipmap.ic_net_poor)
        }
    }

    private fun bindUserStatus(view: ViewSingleTextureBinding, participant: Participant) {
        view.userStatus.mc.visibility = if (isHostUser(participant)) View.VISIBLE else View.GONE
        view.userStatus.mic.setImageResource(if (participant.isMicrophoneEnabled()) R.mipmap.ic__mic_on else R.mipmap.ic__mic_off)
        val meta = GsonHel.fromJson(participant.metadata, ParticipantMeta::class.java)
        view.userStatus.name.text = getMetaUserName(meta)
        bindConnectionQuality(view.userStatus, participant.connectionQuality)
    }

    override fun bind(viewBinding: ViewSingleTextureBinding, position: Int) {
        ensureCoroutineScope()
        bindUserStatus(viewBinding,participant)
        val params: ViewGroup.LayoutParams = viewBinding.root.layoutParams
        params.height = parents.height/2

        coroutineScope?.launch {
            participant.events.collect {
                bindUserStatus(viewBinding,participant)
            }

        }
        coroutineScope?.launch {
//            participant::isSpeaking.flow.collect { isSpeaking ->
//                viewBinding.speakingIndicator.visibility =
//                    if (isSpeaking)View.VISIBLE else View.GONE
//            }
        }
//        coroutineScope?.launch {
//            participant::audioTracks.flow
//                .flatMapLatest { tracks ->
//                    val audioTrack = tracks.firstOrNull()?.first
//                    if (audioTrack != null) {
//                        audioTrack::muted.flow
//                    } else {
//                        flowOf(true)
//                    }
//                }
//                .collect { muted ->
//                    viewBinding.muteIndicator.visibility = if (muted) View.VISIBLE else View.INVISIBLE
//                }
//        }
//        coroutineScope?.launch {
//            participant::connectionQuality.flow
//                .collect { quality ->
//                    viewBinding.connectionQuality.visibility =
//                        if (quality == ConnectionQuality.POOR) View.VISIBLE else View.INVISIBLE
//                }
//        }

        // observe videoTracks changes.
        val videoTrackPubFlow = participant::videoTracks.flow
            .map { participant to it }
            .flatMapLatest { (participant, videoTracks) ->
                // Prioritize any screenshare streams.
                val trackPublication = participant.getTrackPublication(Track.Source.SCREEN_SHARE)
                    ?: participant.getTrackPublication(Track.Source.CAMERA)
                    ?: videoTracks.firstOrNull()?.first

                flowOf(trackPublication)
            }

        coroutineScope?.launch {
            val videoTrackFlow = videoTrackPubFlow
                .flatMapLatestOrNull { pub -> pub::track.flow }

            // Configure video view with track
            launch {
                videoTrackFlow.collectLatest { videoTrack ->
                    setupVideoIfNeeded(videoTrack as? VideoTrack, viewBinding)
                }
            }

            // For local participants, mirror camera if using front camera.
            if (participant == room.localParticipant) {
                launch {
                    videoTrackFlow
                        .flatMapLatestOrNull { track -> (track as LocalVideoTrack)::options.flow }
                        .collectLatest { options ->
                            viewBinding.textureView.setMirror(options?.position == CameraPosition.FRONT)
                        }
                }
            }
        }

        // Handle muted changes
        coroutineScope?.launch {
            videoTrackPubFlow
                .flatMapLatestOrNull { pub -> pub::muted.flow }
                .collectLatest { muted ->
                    viewBinding.textureView.visibleOrInvisible(!(muted ?: true))
                }
        }
        val existingTrack = getVideoTrack()
        if (existingTrack != null) {
            setupVideoIfNeeded(existingTrack, viewBinding)
        }
    }

    private fun getVideoTrack(): VideoTrack? {
        return participant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
    }

    private fun setupVideoIfNeeded(videoTrack: VideoTrack?, viewBinding: ViewSingleTextureBinding) {
        if (boundVideoTrack == videoTrack) {
            return
        }
        boundVideoTrack?.removeRenderer(viewBinding.textureView)
        boundVideoTrack = videoTrack
        videoTrack?.addRenderer(viewBinding.textureView)
    }

    override fun unbind(viewHolder: GroupieViewHolder<ViewSingleTextureBinding>) {
        coroutineScope?.cancel()
        coroutineScope = null
        super.unbind(viewHolder)
        boundVideoTrack?.removeRenderer(viewHolder.binding.textureView)
        boundVideoTrack = null
    }

    override fun getLayout(): Int =
        io.openim.android.ouimeeting.R.layout.view_single_texture
//        if (speakerView) {
//            R.layout.speaker_view
//        } else {
//            R.layout.participant_item
//        }
}


private fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}



private inline fun <T, R> Flow<T?>.flatMapLatestOrNull(
    crossinline transform: suspend (value: T) -> Flow<R>
): Flow<R?> {
    return flatMapLatest {
        if (it == null) {
            flowOf(null)
        } else {
            transform(it)
        }
    }
}


