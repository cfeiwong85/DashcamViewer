import SwiftUI
import AVKit
#if canImport(MobileVLCKit)
import MobileVLCKit
#endif

/// Plays a URL. HTTP(S)/HLS/MP4 use AVPlayer; RTSP uses MobileVLCKit when linked.
struct PlayerView: View {
    let url: URL

    var body: some View {
        if url.scheme?.lowercased() == "rtsp" {
            #if canImport(MobileVLCKit)
            VLCPlayerView(url: url).background(Color.black)
            #else
            VStack(spacing: 8) {
                Image(systemName: "exclamationmark.triangle")
                Text("RTSP needs MobileVLCKit. Add it via CocoaPods (see README).")
                    .multilineTextAlignment(.center)
            }.padding()
            #endif
        } else {
            AVPlayerView(url: url)
        }
    }
}

private struct AVPlayerView: View {
    let url: URL
    @State private var player = AVPlayer()

    var body: some View {
        VideoPlayer(player: player)
            .onAppear { player.replaceCurrentItem(with: AVPlayerItem(url: url)); player.play() }
            .onDisappear { player.pause() }
    }
}

#if canImport(MobileVLCKit)
private struct VLCPlayerView: UIViewRepresentable {
    let url: URL

    func makeCoordinator() -> VLCMediaPlayer { VLCMediaPlayer() }

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let p = context.coordinator
        p.drawable = view
        let media = VLCMedia(url: url)
        media.addOptions(["network-caching": 300, "rtsp-tcp": true])
        p.media = media
        p.play()
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {}

    static func dismantleUIView(_ uiView: UIView, coordinator: VLCMediaPlayer) {
        coordinator.stop()
    }
}
#endif
