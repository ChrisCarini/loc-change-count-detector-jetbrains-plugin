<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# IntelliJ Platform Plugin Template Changelog

## [Unreleased]
### Added

### Changed
- Upgrading IntelliJ to 2022.2

### Deprecated

### Removed

### Fixed

### Security

## [0.0.3] - 2022-07-15
### Changed
- Renaming widget and factory classes (#51)
    - This enables other plugins to set a dependency on `ChrisCarini/loc-change-count-detector-jetbrains-plugin` (e.g. https://github.com/ChrisCarini/example-loc-plugin-config-plugin/pull/1) to configure the thresholds.

## [0.0.2] - 2022-05-06
### Added
- Localization for German, Spanish and French (#34)
- Error Report Submitter to easily open GitHub issues when this plugin has issues (#35, #43)

### Changed
- Upgrading IntelliJ to 2022.1 (#47)

### Fixed
- Show LoC text widget on project open (#38, #45)
- Fix LoC computation bug (#44)

## [0.0.1] - 2022-01-02
### Added
- Initial Revision. Laying the foundation. It doesn't do much useful right now; this is not a released version, but
  might be one day.