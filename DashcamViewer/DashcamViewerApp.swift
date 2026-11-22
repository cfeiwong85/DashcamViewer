import SwiftUI

@main
struct DashcamViewerApp: App {
    @StateObject private var settings = CameraSettings()

    var body: some Scene {
        WindowGroup {
            TabView {
                LiveView().tabItem { Label("Live", systemImage: "video.fill") }
                FilesView().tabItem { Label("Recordings", systemImage: "film.stack") }
                SettingsView().tabItem { Label("Settings", systemImage: "gearshape") }
            }
            .environmentObject(settings)
        }
    }
}
