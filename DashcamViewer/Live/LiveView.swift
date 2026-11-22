import SwiftUI
import T8Kit

@MainActor
@Observable
final class LiveModel {
    enum Phase: Equatable {
        case idle
        case starting
        case playing
        case failed(String)
    }

    let renderer = H264Renderer()
    var phase: Phase = .idle
    var channel: T8Channel = .front
    var voiceRecording: Bool?
    var message: String?
    var busy = false
    private(set) var resolution = ""
    private(set) var framesShown = 0
    private var streamTask: Task<Void, Never>?
    /// The last stop, which a new start waits for so its PreViewOff cannot hit the new live view.
    private var stopTask: Task<Void, Never>?
    private var timeSynced = false

    func start(app: AppModel) {
        guard streamTask == nil else { return }
        guard app.activity == .idle || app.activity == .live else {
            phase = .failed(app.activity.busyMessage)
            return
        }
        app.activity = .live
        phase = .starting
        message = nil
        renderer.clear()
        framesShown = 0
        let previousStop = stopTask
        streamTask = Task { [weak self] in
            await previousStop?.value
            await self?.run(app: app)
        }
    }

    private func run(app: AppModel) async {
        do {
            let session = try await app.connectedSession()
            try Task.checkCancellation()
            let config = try await session.startPreview()
            // If stop() ran meanwhile, it sends PreViewOff once this task has returned.
            try Task.checkCancellation()
            app.updateConfig(config)
            voiceRecording = config.isVoiceRecordingOn
            // LogonT opens port 7778 1000 ms after FREEVIEW_START (MainFreeView.java:967-972).
            try await Task.sleep(for: .seconds(1))
            if !timeSynced {
                timeSynced = true
                // LogonT sets the camera clock once, 2 s after starting the preview.
                Task {
                    try? await Task.sleep(for: .seconds(1))
                    do {
                        try await session.setTime(T8DateTime(date: Date()))
                    } catch {
                        app.log.warning("Setting the camera clock failed: \(error)")
                    }
                }
            }
            let watchdog = Task { [weak self] in
                // LogonT gives up after 20 s without a picture ("Wi-Fi signal is weak").
                try? await Task.sleep(for: .seconds(20))
                guard !Task.isCancelled, let self, self.framesShown == 0, self.phase == .starting else { return }
                self.phase = .failed("No picture after 20 seconds. The Wi-Fi signal may be weak; move closer to the camera and try again.")
                self.streamTask?.cancel()
            }
            defer { watchdog.cancel() }
            for try await unit in app.media.accessUnits() {
                if Task.isCancelled { break }
                if renderer.enqueue(unit) {
                    framesShown = renderer.framesShown
                    if phase != .playing { phase = .playing }
                    resolution = renderer.resolutionText
                }
            }
            if !Task.isCancelled {
                phase = .failed("The camera ended the live stream.")
            }
        } catch is CancellationError {
        } catch {
            if !Task.isCancelled {
                phase = .failed(AppModel.message(for: error))
            }
        }
    }

    /// LogonT's onPause: RECORD_ON_OFF(0) then PreViewOff.
    func stop(app: AppModel) async {
        let running = streamTask
        streamTask = nil
        running?.cancel()
        if case .failed = phase {
            // Keep the error visible.
        } else {
            phase = .idle
        }
        let work = Task { [weak self] in
            // Wait until the stream task has returned, so a startPreview still in flight is finished.
            await running?.value
            if running != nil, let session = app.session, app.state.isConnected {
                do {
                    try await session.stopPreview()
                } catch {
                    app.log.warning("Stopping the live view failed: \(error)")
                }
            }
            // A new live view may have started while we waited; leave its activity alone.
            if app.activity == .live, self?.streamTask == nil { app.activity = .idle }
        }
        stopTask = work
        await work.value
    }

    func switchCamera(app: AppModel) async {
        await perform(app) { session in
            try await session.switchCamera()
            self.channel = self.channel == .front ? .rear : .front
        }
    }

    func snapshot(app: AppModel) async {
        await perform(app) { session in
            try await session.takeSnapshot()
            self.message = "Photo taken. It is in the camera's Photo folder."
        }
    }

    func toggleVoice(app: AppModel) async {
        let enable = !(voiceRecording ?? true)
        await perform(app) { session in
            try await session.setVoiceRecording(enable)
            self.voiceRecording = enable
            self.message = enable ? "Voice recording is on." : "Voice recording is off."
        }
    }

    private func perform(_ app: AppModel, _ action: @escaping (T8ControlSession) async throws -> Void) async {
        guard let session = app.session, app.state.isConnected else {
            message = "Not connected."
            return
        }
        busy = true
        defer { busy = false }
        do {
            try await action(session)
        } catch {
            message = AppModel.message(for: error)
        }
    }
}

struct LiveView: View {
    @Environment(AppModel.self) private var app
    @State private var model = LiveModel()
    @AppStorage("mirrorRear") private var mirrorRear = false

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ConnectionBanner()
                ZStack {
                    Color.black
                    VideoSurface(renderer: model.renderer)
                        .scaleEffect(x: model.channel == .rear && mirrorRear ? -1 : 1, y: 1)
                    overlay
                }
                .aspectRatio(16 / 9, contentMode: .fit)
                .frame(maxWidth: .infinity)

                HStack {
                    Label(model.channel.title, systemImage: model.channel == .front ? "car.front.waves.up" : "car.rear.waves.up")
                    Spacer()
                    if !model.resolution.isEmpty {
                        Text(model.resolution).monospacedDigit()
                    }
                }
                .font(.footnote)
                .foregroundStyle(.secondary)
                .padding(.horizontal)
                .padding(.vertical, 6)

                controls
                if let message = model.message {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding()
                }
                Spacer()
            }
            .navigationTitle("Live")
            .navigationBarTitleDisplayMode(.inline)
        }
        .onAppear { model.start(app: app) }
        .onDisappear { Task { await model.stop(app: app) } }
    }

    @ViewBuilder
    private var overlay: some View {
        switch model.phase {
        case .idle:
            Button("Start live view") { model.start(app: app) }
                .buttonStyle(.borderedProminent)
        case .starting:
            ProgressView("Connecting…").tint(.white).foregroundStyle(.white)
        case .playing:
            EmptyView()
        case .failed(let text):
            VStack(spacing: 12) {
                Text(text)
                    .font(.callout)
                    .foregroundStyle(.white)
                    .multilineTextAlignment(.center)
                Button("Try again") {
                    Task {
                        await model.stop(app: app)
                        model.start(app: app)
                    }
                }
                .buttonStyle(.borderedProminent)
            }
            .padding()
        }
    }

    private var controls: some View {
        HStack(spacing: 28) {
            control("Front/Rear", systemImage: "arrow.triangle.2.circlepath.camera") {
                await model.switchCamera(app: app)
            }
            control("Photo", systemImage: "camera.shutter.button") {
                await model.snapshot(app: app)
            }
            control(model.voiceRecording == false ? "Voice off" : "Voice on",
                    systemImage: model.voiceRecording == false ? "mic.slash" : "mic") {
                await model.toggleVoice(app: app)
            }
            Button {
                mirrorRear.toggle()
            } label: {
                VStack {
                    Image(systemName: "arrow.left.and.right.righttriangle.left.righttriangle.right").font(.title2)
                    Text("Mirror rear").font(.caption2)
                }
            }
            .disabled(model.channel != .rear)
        }
        .padding(.vertical, 8)
        .disabled(model.busy || model.phase != .playing)
    }

    private func control(_ title: String, systemImage: String, action: @escaping () async -> Void) -> some View {
        Button {
            Task { await action() }
        } label: {
            VStack {
                Image(systemName: systemImage).font(.title2)
                Text(title).font(.caption2)
            }
        }
    }
}
