# Android Test

## Trouble shooting
- Test running failed: Unable to find instrumentation target package: blockeq.com.stellarwallet. onError: commandError=true message=INSTRUMENTATION_FAILED: blockeq.com.stellarwallet.test/android.support.test.runner.AndroidJUnitRunner
Empty test suite.

Make sure you uninstall the app and any other previous test
./adb uninstall package_name_of_the_app
./adb uninstall package_name_of_the_app.tests

- Some animations, could make the espresso test wait for the animation to finish. Please disable animations in developer options or by adb or programmatically.
- The devices have to be awake unlocked to be able to run the espresso test.
Two solve the two issues above I would recommend checking the custom runner in the following gist
https://gist.github.com/riggaroo/7f1e6cd4a52c61920b564c6465d1f1d9

- Espresso does not clean the storage of the previous test or app. If you need to perform a test that requires a fresh install take a look at: 
https://developer.android.com/training/testing/junit-runner#using-android-test-orchestrator
Attention the runner 1.0.2 has an issue where you can not debug
https://issuetracker.google.com/u/1/issues/78658117 

- java.lang.RuntimeException: Method e in android.util.Log not mocked. See http://g.co/androidstudio/not-mocked for details.
      at android.util.Log.e(Log.java)
      at com.xxx.demo.utils.UtilsTest.setUp(UtilsTest.java:41)
      at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
      at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
      at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
      at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
      at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
      at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:24)
      at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
      at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
      at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
      at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)

Solutions:
- Best solution is to stop using LOG and use Timber  
- Provide a Log.java Implementation and place it in the package `app/src/test/java/android/util`
(https://stackoverflow.com/a/46793567)
- Add it in the android gradle
```
 testOptions {
        unitTests.returnDefaultValues = true
    }
    
```
From the docs: Caution: Setting the returnDefaultValues property to true should be done with care. The null/zero return values can introduce regressions in your tests, which are hard to debug and might allow failing tests to pass. Only use it as a last resort
