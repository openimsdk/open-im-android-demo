package io.openim.android.ouicalling

import android.content.Context
import android.view.View
import io.livekit.android.ConnectOptions
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack

import io.openim.android.sdk.models.SignalingCertificate

abstract class Continuation<in T> : kotlin.coroutines.Continuation<T> {
    abstract fun resume(value: T)
    abstract fun resumeWithException(exception: Throwable)
    override fun resumeWith(result: Result<T>) = result.fold(::resume, ::resumeWithException)
}

object LiveKitLaunch {

      suspend fun launch(context: Context, signalingCertificate: SignalingCertificate, view: TextureViewRenderer) {
        val room = LiveKit.create(
            context,
            RoomOptions(),
        )
        // Setup event handling.
        room.events.collect { event ->
            view
            when (event) {
                is RoomEvent.TrackSubscribed -> onTrackSubscribed(event, view)
                else -> {}
            }
        }
        // Connect to server.
        room.connect(
            signalingCertificate.liveURL,
            signalingCertificate.token,
            ConnectOptions()
        )

        // Turn on audio/video recording.
        val localParticipant = room.localParticipant
        localParticipant.setMicrophoneEnabled(true)
        localParticipant.setCameraEnabled(true)
    }

    private fun onTrackSubscribed(event: RoomEvent.TrackSubscribed, view: TextureViewRenderer) {
        if (event.track is VideoTrack) {
            attachVideo(event.track as VideoTrack, view)
        }
    }

    private fun attachVideo(videoTrack: VideoTrack, view: TextureViewRenderer) {
        // viewBinding.renderer is a `io.livekit.android.renderer.SurfaceViewRenderer` in your
        // layout
        videoTrack.addRenderer(view)
    }

}
