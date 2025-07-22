# Release Notes

This file documents the release history of the SonarQube Issue Hooks Plugin.

## [Unreleased]

### Added
- Initial release setup with automated GitHub Actions workflows
- Comprehensive CI/CD pipeline with multi-platform testing
- Automated release process with changelog generation

### Changed
- Updated to Maven development version format (1.1.0-SNAPSHOT)

### Fixed
- Added proper Maven plugins for release management

## How to Release

1. **Prepare Release**: Ensure all features are merged to `main` branch
2. **Run Release Workflow**: Go to Actions → Release → Run workflow
3. **Input Version**: Enter the version number (e.g., `1.1.0`)
4. **Optional Notes**: Add any additional release notes
5. **Execute**: The workflow will:
   - Run all tests
   - Update version in POM
   - Build the plugin JAR
   - Create a git tag
   - Generate changelog from commits
   - Create GitHub release with assets
   - Update to next development version

## Version Strategy

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes to plugin API or SonarQube compatibility
- **MINOR**: New features, new SonarQube version support
- **PATCH**: Bug fixes, security updates

Examples:
- `1.0.0` → `1.0.1`: Bug fix release
- `1.0.0` → `1.1.0`: New features added
- `1.0.0` → `2.0.0`: Breaking changes or new major SonarQube support

## Compatibility Matrix

| Plugin Version | SonarQube Version | Java Version |
|----------------|------------------|--------------|
| 1.0.x          | 9.4+             | 11+          |
| 1.1.x          | 9.4+             | 11+          |

---

For detailed release information, see the [GitHub Releases](https://github.com/movares/sonar-hooks/releases) page. 