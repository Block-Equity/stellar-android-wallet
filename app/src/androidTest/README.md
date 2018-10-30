# Android Test

## Trouble shooting
Test running startedTest running failed: Unable to find instrumentation info for: ComponentInfo{tests.xxx.xxx.xxx.xxx.test/android.test.InstrumentationTestRunner}
Empty test suite.

Make sure you uninstall the app and any other previous test
./adb uninstall package_name_of_the_app
./adb uninstall package_name_of_the_app.tests
