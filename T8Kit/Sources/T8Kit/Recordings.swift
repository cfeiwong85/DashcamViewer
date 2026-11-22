import Foundation

/// The four folders LogonT lists; the raw value is both the 40997 index and the 8200 type
/// (MainFileListView_blackbox2..5.java, MainFileFreeView.java:618-626).
public enum T8Folder: Int, CaseIterable, Sendable, Hashable, Identifiable {
    case driving = 0
    case event = 1
    case parking = 2
    case photo = 3

    public var id: Int { rawValue }

    public var title: String {
        switch self {
        case .driving: "Driving"
        case .event: "Event"
        case .parking: "Parking"
        case .photo: "Photo"
        }
    }

    /// The letter at position 16 of a record name.
    public var typeLetter: Character {
        switch self {
        case .driving: "I"
        case .event: "E"
        case .parking: "P"
        case .photo: "J"
        }
    }

    public var isVideo: Bool { self != .photo }
}

/// One entry of a 40998 page: 32 bytes, name in bytes 0-20, byte 31 = 0 means front camera only.
public struct T8FileListEntry: Hashable, Sendable {
    public static let byteCount = 32
    public static let nameLength = 21

    public var name: String
    public var hasRear: Bool

    public init(name: String, hasRear: Bool) {
        self.name = name
        self.hasRear = hasRear
    }

    public init(record: ArraySlice<UInt8>) {
        let base = record.startIndex
        name = T8Bytes.cString(record[base..<(base + Self.nameLength)])
        hasRear = record[base + 31] != 0
    }

    /// LogonT keeps a record only if its name starts with "2" (MainFreeView.java:1331).
    public var isKeptByLogonT: Bool { name.first == "2" }

    public var encoded: [UInt8] {
        var out = T8Bytes.fixedField(name, size: Self.nameLength)
        out += [UInt8](repeating: 0, count: 10)
        out.append(hasRear ? 1 : 0)
        return out
    }
}

/// One 40998 reply: `u32 total_pages, u32 current_page`, then 32-byte records (MainFreeView.java:1318-1358).
public struct T8FileListPage: Hashable, Sendable {
    public var totalPages: UInt32
    public var currentPage: UInt32
    public var entries: [T8FileListEntry]

    public init(totalPages: UInt32, currentPage: UInt32, entries: [T8FileListEntry]) {
        self.totalPages = totalPages
        self.currentPage = currentPage
        self.entries = entries
    }

    public init?(payload: [UInt8]) {
        guard let total = T8Bytes.u32LE(payload, at: 0), let current = T8Bytes.u32LE(payload, at: 4) else { return nil }
        totalPages = total
        currentPage = current
        var list: [T8FileListEntry] = []
        var offset = 8
        // LogonT reads 21 name bytes and byte 31 of each record; a trailing partial record is ignored.
        while offset + T8FileListEntry.byteCount <= payload.count {
            list.append(T8FileListEntry(record: payload[offset..<(offset + T8FileListEntry.byteCount)]))
            offset += T8FileListEntry.byteCount
        }
        entries = list
    }

    /// LogonT stops waiting when `current + 1 >= total` (MainFreeView.java:1343).
    public var isLast: Bool { currentPage &+ 1 >= totalPages }

    public var payload: [UInt8] {
        T8Bytes.le(totalPages) + T8Bytes.le(currentPage) + entries.flatMap(\.encoded)
    }
}

/// A recording on the camera, with the index the camera expects in 8200.
public struct T8Recording: Hashable, Sendable, Identifiable {
    public let folder: T8Folder
    /// 0-based position among the kept records, in the order the camera sent them.
    public let index: Int
    public let name: String
    public let hasRear: Bool

    public init(folder: T8Folder, index: Int, name: String, hasRear: Bool) {
        self.folder = folder
        self.index = index
        self.name = name
        self.hasRear = hasRear
    }

    public var id: String { "\(folder.rawValue)/\(index)/\(name)" }

    public var channels: [T8Channel] { hasRear ? [.front, .rear] : [.front] }

    /// LogonT's download name stem: the first 17 characters (MainFileListView_blackbox2.java:611).
    public var baseName: String { String(name.prefix(17)) }

    public func fileStem(for channel: T8Channel) -> String {
        baseName + channel.fileSuffix
    }

    /// Recording time from a name like `20180511_052412_...`, in the camera's local time.
    public func date(in timeZone: TimeZone = .current) -> Date? {
        guard name.count >= 15 else { return nil }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = timeZone
        formatter.dateFormat = "yyyyMMdd_HHmmss"
        return formatter.date(from: String(name.prefix(15)))
    }
}

/// Builds `T8Recording`s from pages the way LogonT numbers them.
public struct T8FileListAssembler: Sendable {
    public let folder: T8Folder
    public private(set) var recordings: [T8Recording] = []
    public private(set) var pagesReceived = 0
    public private(set) var isComplete = false
    public private(set) var skippedNames: [String] = []

    public init(folder: T8Folder) {
        self.folder = folder
    }

    public mutating func add(_ page: T8FileListPage) {
        pagesReceived += 1
        for entry in page.entries {
            if entry.isKeptByLogonT {
                recordings.append(T8Recording(folder: folder, index: recordings.count, name: entry.name, hasRear: entry.hasRear))
            } else {
                skippedNames.append(entry.name)
            }
        }
        if page.isLast { isComplete = true }
    }
}
