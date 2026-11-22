import AVKit
import SwiftUI

/// Copies saved on this iPhone. Swipe to delete asks first, like LogonT's
/// "Are you sure you want to delete?" (MainFileListView2.java:168-189).
struct SavedView: View {
    @Environment(AppModel.self) private var app
    @State private var pendingDelete: SavedLibrary.Item?
    @State private var message: String?

    var body: some View {
        NavigationStack {
            List {
                if app.library.items.isEmpty {
                    Text("Nothing saved yet. In Recordings, swipe a recording left and tap Save.")
                        .foregroundStyle(.secondary)
                }
                ForEach(app.library.items) { item in
                    NavigationLink(value: item) {
                        HStack {
                            Image(systemName: item.isVideo ? "film" : "photo").foregroundStyle(.tint)
                            VStack(alignment: .leading) {
                                Text(item.name).lineLimit(1)
                                Text("\(ByteCountFormatter.string(fromByteCount: item.size, countStyle: .file)) · \(item.date.formatted(date: .abbreviated, time: .shortened))")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                    .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                        Button("Delete", systemImage: "trash", role: .destructive) {
                            pendingDelete = item
                        }
                    }
                }
                if let message {
                    Text(message).font(.footnote).foregroundStyle(.secondary)
                }
            }
            .navigationTitle("Saved on iPhone")
            .navigationDestination(for: SavedLibrary.Item.self) { item in
                SavedItemView(item: item)
            }
            .refreshable { app.library.reload() }
            .onAppear { app.library.reload() }
            .alert("Delete", isPresented: Binding(get: { pendingDelete != nil }, set: { if !$0 { pendingDelete = nil } }), presenting: pendingDelete) { item in
                Button("Delete", role: .destructive) {
                    do {
                        try app.library.delete(item)
                        message = "Deleted \(item.name)."
                    } catch {
                        message = "Could not delete: \(error.localizedDescription)"
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: { item in
                Text("Are you sure you want to delete \(item.name) from this iPhone? The recording on the camera is not affected.")
            }
        }
    }
}

struct SavedItemView: View {
    let item: SavedLibrary.Item
    @State private var player: AVPlayer?
    @State private var status: String?

    var body: some View {
        VStack(spacing: 16) {
            if item.isVideo {
                VideoPlayer(player: player)
                    .aspectRatio(16 / 9, contentMode: .fit)
            } else if let image = UIImage(contentsOfFile: item.url.path) {
                Image(uiImage: image).resizable().scaledToFit()
            }
            HStack(spacing: 24) {
                ShareLink(item: item.url) {
                    Label("Share", systemImage: "square.and.arrow.up")
                }
                Button {
                    Task {
                        do {
                            try await PhotosSaver.save(item.url, isVideo: item.isVideo)
                            status = "Saved to Photos."
                        } catch {
                            status = error.localizedDescription
                        }
                    }
                } label: {
                    Label("Save to Photos", systemImage: "photo.on.rectangle")
                }
            }
            if let status {
                Text(status).font(.footnote).foregroundStyle(.secondary)
            }
            Spacer()
        }
        .padding()
        .navigationTitle(item.name)
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            if item.isVideo { player = AVPlayer(url: item.url) }
        }
        .onDisappear { player?.pause() }
    }
}
