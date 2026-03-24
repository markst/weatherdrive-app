// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "_radioplayer",
  platforms: [
    .iOS("15.0")
  ],
  products: [
    .library(
      name: "_radioplayer",
      type: .none,
      targets: ["_radioplayer"]
    )
  ],
  dependencies: [
    .package(
      path: "../../../../radioplayer-kt/radioplayer/native",
    )
  ],
  targets: [
    .target(
      name: "_radioplayer",
      dependencies: [
        .product(
          name: "RadioPlayer",
          package: "native",
        )
      ]
    )
  ]
)
