package test;

// Maven: org.junit.jupiter:junit-jupiter:5.9.3

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Timeout.ThreadMode.SEPARATE_THREAD;


@Timeout(value = 1000, unit = MILLISECONDS, threadMode = SEPARATE_THREAD)
class LPLCompilerTest {

    @ParameterizedTest
    @MethodSource("testFilePathsA")
    void compileA(String testFilePath) throws IOException {
        Utils.doCompileTest(testFilePath);
    }

    private static Stream<String> testFilePathsA() {
        return test.Utils.testFilePaths(Utils.TEST_FILES_ROOT + "/compiler/A");
    }

}