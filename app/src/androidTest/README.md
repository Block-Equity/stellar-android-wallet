# Android Test

## Trouble shooting
- Test running failed: Unable to find instrumentation target package: blockeq.com.stellarwallet. onError: commandError=true message=INSTRUMENTATION_FAILED: blockeq.com.stellarwallet.test/android.support.test.runner.AndroidJUnitRunner
Empty test suite.

Make sure you uninstall the app and any other previous test
./adb uninstall package_name_of_the_app
./adb uninstall package_name_of_the_app.tests

- Some animations, specially the infinity ones, could make the espresso test to wait for the animation to finish. Please disable animations in developer options or by adb or programmatically.

- the devices has to be awake without the lock screen to be able to run the espresso test.

- espresso does not clean the storage of the previous test or app. if you need to do test that requires to be a fresh install take a look at 
https://developer.android.com/training/testing/junit-runner#using-android-test-orchestrator
Attention the runner 1.0.2 has an issue where you can not debug
https://issuetracker.google.com/u/1/issues/78658117 
