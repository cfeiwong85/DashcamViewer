// swift-tools-version: 6.0
import PackageDescription

let package = Package(
    name: "T8Kit",
    platforms: [.macOS(.v13), .iOS(.v17)],
    products: [
        .library(name: "T8Kit", targets: ["T8Kit"]),
        .library(name: "T8SimulatorKit", targets: ["T8SimulatorKit"]),
        .executable(name: "t8sim", targets: ["t8sim"]),
    ],
    targets: [
        .target(name: "T8Kit"),
        .target(name: "T8SimulatorKit", dependencies: ["T8Kit"]),
        .executableTarget(name: "t8sim", dependencies: ["T8SimulatorKit"]),
        .testTarget(name: "T8KitTests", dependencies: ["T8Kit"]),
        .testTarget(name: "T8SimulatorTests", dependencies: ["T8Kit", "T8SimulatorKit"]),
    ]
)
