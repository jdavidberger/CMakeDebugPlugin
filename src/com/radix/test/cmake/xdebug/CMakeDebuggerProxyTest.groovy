package com.radix.test.cmake.xdebug

import com.radix.cmake.xdebug.CMakeDebuggerListener
import com.radix.cmake.xdebug.CMakeDebuggerProxy
import org.jetbrains.annotations.NotNull
import org.junit.Test

/**
 * Created by J on 2/1/2017.
 */
class CMakeDebuggerProxyTest extends GroovyTestCase {
    @Test
    void testBasicSetup() {
        CMakeDebuggerProxy proxy = new CMakeDebuggerProxy(8080)

        proxy.AddListener( new CMakeDebuggerListener() {

            @Override
            void OnStateChange(@NotNull String newState, @NotNull String file, @NotNull int line) {
                println("State: " + newState + " at " + file + ":" + line)
                if(newState == "Paused")
                    proxy.step()
            }
        }

        )

        proxy.startClient()
    }


}
