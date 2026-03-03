// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_internal_linkage_SwiftPMImport",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_internal_linkage_SwiftPMImport",
      type: .none,
      targets: ["_internal_linkage_SwiftPMImport"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/_radioplayer")
  ],
  targets: [
    .target(
      name: "_internal_linkage_SwiftPMImport",
      dependencies: [
        .product(name: "_radioplayer", package: "_radioplayer")
      ]
    )
  ]
)
