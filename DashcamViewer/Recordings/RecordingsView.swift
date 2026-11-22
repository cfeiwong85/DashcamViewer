import SwiftUI
import T8Kit

@MainActor
@Observable
final class RecordingsModel {
    var folder: T8Folder = .driving
    var lists: [T8Folder: [T8Recording]] = [:]
    var incomplete: Set<T8Folder> = []
    var loading = false
    var error: String?

    func load(app: AppModel, force: Bool = false) async {
        let folder = self.folder
        guard force || lists[folder] == nil else { return }
        guard app.activity == .idle else {
            error = "Stop the live view, playback or download first."
            return
        }
        loading = true
        error = nil
        defer { loading = false }
        do {
            let session = try await app.connectedSession()
            let result = try await session.listRecordings(in: folder)
            // Newest first, like LogonT's default sort; indexes stay the camera's.
            lists[folder] = result.recordings.reversed()
            if result.isComplete { incomplete.remove(folder) } else { incomplete.insert(folder) }
        } catch {
            self.error = AppModel.message(for: error)
        }
    }
}

struct RecordingsView: View {
    @Environment(AppModel.self) private var app
    @State private var model = RecordingsModel()
    @State private var transfer = TransferModel()
    @State private var playing: PlaybackRequest?
    @State private var choosing: ChannelChoice?
    @AppStorage("alsoSaveToPhotos") private var alsoSaveToPhotos = true

    struct PlaybackRequest: Identifiable {
        let recording: T8Recording
        let channel: T8Channel
        var id: String { recording.id + "/\(channel.rawValue)" }
    }

    struct ChannelChoice: Identifiable {
        enum Purpose { case play, save }
        let recording: T8Recording
        let purpose: Purpose
        var id: String { recording.id + "\(purpose)" }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                ConnectionBanner()
                Picker("Folder", selection: $model.folder) {
                    ForEach(T8Folder.allCases) { folder in
                        Text(folder.title).tag(folder)
                    }
                }
                .pickerStyle(.segmented)
                .padding()
                if transfer.isRunning || !transfer.status.isEmpty || transfer.error != nil {
                    transferStatus
                }
                list
            }
            .navigationTitle("Recordings")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                Button {
                    Task { await model.load(app: app, force: true) }
                } label: {
                    Image(systemName: "arrow.clockwise")
                }
                .disabled(model.loading)
            }
            .task(id: model.folder) { await model.load(app: app) }
            .fullScreenCover(item: $playing) { request in
                PlaybackView(recording: request.recording, channel: request.channel)
            }
            .confirmationDialog(
                choosing?.purpose == .save ? "Save which camera?" : "Play which camera?",
                isPresented: Binding(get: { choosing != nil }, set: { if !$0 { choosing = nil } }),
                titleVisibility: .visible,
                presenting: choosing
            ) { choice in
                ForEach(choice.recording.channels, id: \.self) { channel in
                    Button(channel.title) { act(choice.purpose, choice.recording, channel) }
                }
            }
        }
    }

    private var list: some View {
        List {
            if model.loading && model.lists[model.folder] == nil {
                HStack {
                    ProgressView()
                    Text("Loading the file list…")
                }
            }
            if let error = model.error {
                Text(error).foregroundStyle(.red)
            }
            if let recordings = model.lists[model.folder] {
                if recordings.isEmpty {
                    Text("No recordings in this folder (or the camera did not answer).")
                        .foregroundStyle(.secondary)
                }
                if model.incomplete.contains(model.folder) {
                    Text("The camera stopped sending the list early; some recordings may be missing. Pull to refresh.")
                        .font(.footnote)
                        .foregroundStyle(.orange)
                }
                Section {
                    ForEach(recordings) { recording in
                        row(recording)
                    }
                } footer: {
                    Text("Deleting recordings on the camera is not possible: the camera's app protocol (LogonT) has no delete command. Saved copies on this iPhone can be deleted in the Saved tab.")
                }
            }
        }
        .refreshable { await model.load(app: app, force: true) }
    }

    private func row(_ recording: T8Recording) -> some View {
        Button {
            if recording.hasRear {
                choosing = ChannelChoice(recording: recording, purpose: .play)
            } else {
                act(.play, recording, .front)
            }
        } label: {
            HStack {
                Image(systemName: recording.folder.isVideo ? "film" : "photo")
                    .foregroundStyle(.tint)
                VStack(alignment: .leading) {
                    Text(Self.title(for: recording))
                    Text(recording.name)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
                Spacer()
                if recording.hasRear {
                    Text("F + R")
                        .font(.caption2)
                        .padding(4)
                        .background(.quaternary, in: RoundedRectangle(cornerRadius: 4))
                }
            }
        }
        .foregroundStyle(.primary)
        .swipeActions(edge: .trailing) {
            Button {
                if recording.hasRear {
                    choosing = ChannelChoice(recording: recording, purpose: .save)
                } else {
                    act(.save, recording, .front)
                }
            } label: {
                Label("Save", systemImage: "square.and.arrow.down")
            }
            .tint(.blue)
        }
        .contextMenu {
            ForEach(recording.channels, id: \.self) { channel in
                Button("Save \(channel.title) to iPhone", systemImage: "square.and.arrow.down") {
                    act(.save, recording, channel)
                }
            }
        }
        .disabled(transfer.isRunning)
    }

    private func act(_ purpose: ChannelChoice.Purpose, _ recording: T8Recording, _ channel: T8Channel) {
        choosing = nil
        switch purpose {
        case .play:
            playing = PlaybackRequest(recording: recording, channel: channel)
        case .save:
            Task { await transfer.save(recording, channel: channel, app: app, alsoToPhotos: alsoSaveToPhotos) }
        }
    }

    private var transferStatus: some View {
        VStack(alignment: .leading, spacing: 6) {
            if transfer.isRunning {
                if let progress = transfer.progress {
                    ProgressView(value: progress)
                } else {
                    ProgressView()
                }
            }
            if !transfer.status.isEmpty {
                Text(transfer.status).font(.footnote)
            }
            if let error = transfer.error {
                Text(error).font(.footnote).foregroundStyle(.red)
            }
        }
        .padding(.horizontal)
        .padding(.bottom, 8)
    }

    static func title(for recording: T8Recording) -> String {
        guard let date = recording.date() else { return recording.name }
        return date.formatted(date: .abbreviated, time: .standard)
    }
}
