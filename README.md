# <img src="./src/main/resources/META-INF/pluginIcon.svg" width="32" /> LoC Change Count Detector JetBrains Plugin

[![GitHub License](https://img.shields.io/github/license/ChrisCarini/loc-change-count-detector-jetbrains-plugin?style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/blob/main/LICENSE)
[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/19113-lines-of-code-change-observer?label=Latest%20Plugin%20Release&style=flat-square)](https://plugins.jetbrains.com/plugin/19113-lines-of-code-change-observer)
[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/r/rating/19113-lines-of-code-change-observer?style=flat-square)](https://plugins.jetbrains.com/plugin/19113-lines-of-code-change-observer)
[![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/d/19113-lines-of-code-change-observer?style=flat-square)](https://plugins.jetbrains.com/plugin/19113-lines-of-code-change-observer)
[![All Contributors](https://img.shields.io/github/all-contributors/ChrisCarini/loc-change-count-detector-jetbrains-plugin?color=ee8449&style=flat-square)](#contributors)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/ChrisCarini/loc-change-count-detector-jetbrains-plugin/build.yml?branch=main&logo=GitHub&style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/actions/workflows/build.yml)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/ChrisCarini/loc-change-count-detector-jetbrains-plugin/compatibility.yml?branch=main&label=IntelliJ%20Plugin%20Compatibility&logo=GitHub&style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/actions/workflows/compatibility.yml)

<!-- Plugin description -->
A plugin for JetBrains IDEs providing a visual indicator as to your changeset size.

Smaller changes typically yield faster code reviews!
<!-- Plugin description end -->

The plugin was created purely as a PoC, and might not yield anything useful.

## Configuration

Use this plugin in a corporate environment?

You can configure the plugin to use your own internal thresholds via a 'sidecar' configuration plugin. The 'sidecar'
plugin can configure this plugin for all your companies projects, or configure individual projects - it is all based on
how you decide to implement the 'sidecar' plugin.

See [ChrisCarini/example-loc-plugin-config-plugin](https://github.com/ChrisCarini/example-loc-plugin-config-plugin/pulls?q=is%3Apr+is%3Aclosed+label%3A%22example+for+docs%22)
for a reference implementation on automatic configuration.

## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center" valign="top" width="14.28%"><a href="https://github.com/bansalsulabh"><img src="https://avatars.githubusercontent.com/u/7255818?v=4?s=100" width="100px;" alt="bansalsulabh"/><br /><sub><b>bansalsulabh</b></sub></a><br /><a href="#code-bansalsulabh" title="Code">💻</a> <a href="#doc-bansalsulabh" title="Documentation">📖</a> <a href="#example-bansalsulabh" title="Examples">💡</a> <a href="#ideas-bansalsulabh" title="Ideas, Planning, & Feedback">🤔</a> <a href="#review-bansalsulabh" title="Reviewed Pull Requests">👀</a> <a href="#userTesting-bansalsulabh" title="User Testing">📓</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->