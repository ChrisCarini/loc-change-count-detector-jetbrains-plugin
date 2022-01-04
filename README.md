# LoC Change Count Detector JetBrains Plugin

[![GitHub License](https://img.shields.io/github/license/ChrisCarini/loc-change-count-detector-jetbrains-plugin?style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/blob/master/LICENSE)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ChrisCarini/loc-change-count-detector-jetbrains-plugin/JetBrains%20Plugin%20CI?logo=GitHub&style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/actions?query=workflow%3A%22JetBrains+Plugin+CI%22)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/ChrisCarini/loc-change-count-detector-jetbrains-plugin/IntelliJ%20Plugin%20Compatibility?label=IntelliJ%20Plugin%20Compatibility&logo=GitHub&style=flat-square)](https://github.com/ChrisCarini/loc-change-count-detector-jetbrains-plugin/actions?query=workflow%3A%22IntelliJ+Plugin+Compatibility%22)

<!-- Plugin description -->
A JetBrains plugin providing a visual indicator as to your changeset size.

Smaller changes typically yield faster code reviews!
<!-- Plugin description end -->

The plugin was created purely as a PoC, and might not yield anything useful.





so, it's gonna take a bit, right now the zip it's downloading is the IJ version that this plugin is built against. it's essentially like redownloading IJ, but it's needed for plugin development.


haha, i use em for typing notes really, but not code :P 

so essentially, gradle is going to do all the heavy lifting for us, on thr right, we'll use the gradle toolview for most things like building plugin and running plugin in sandbox


more stuff will show there (ie, all the gradle tasks) once this 'sync' is done :)


cool, ok yeah it must have been gradle wrapper version

indexing going to take a bit, they have a shit load of code for IJ of course


these two tasks are what i use a ton - building, and running ide - it will spin up another IJ instance w/ your plugin automatically loaded into it :) :tada:


i will run the ide task now to show you - it's really the best way to test and get some fast feedback on changes :)

yup!

i have another 'sample notification' plugin too you can check out - it's a very basic and simple plugin


it's a *VERY* simple notification :D

yeah, IJ has a *SHIT TON* of API's to hook into different parts of lifecycle

so, for example, the 'simple' one i was thinking hooks into all file changes... so you type a char (or remove), and you can have some java code of your choosing run - that code could be "go calculate the LoC changed", and then another bit of code could be in charge of showing how many LoC changed in bottom right

and then of course, once you exceed X LoC, we could fire a notification popup like the one you just saw for project open :)

yup - what we can do too as PoC is just spin up a background process that every 20 secnds or so it just goes to run the CLI and compute the LoC changed for us

i thought there is way for git status or w/e to show LoC changed too git diff maybe?

simple way, idea is to get some basic PoC, we can refine later :D


anything we want it to be, tbh - i have another example of plugin that does this sorta thing - LI IDEA also has several examples

luckily, as far as the java code goes, using LI IDEA is good for many examples since it does a whole bunch of shit :D

well, you can look at the •code for LI IDEA*

essentially, there's api's for it in IJ SDK - and we can look in listudio( the plugin that makes LI IDEA what it is) for examples, and can also look in OSS land as well :)

sure - let me quickly show you what i have in this plugin so far - it's really simple, just icon lol

