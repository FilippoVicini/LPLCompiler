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
class ASTTest {

    @ParameterizedTest
    @MethodSource("sourceFilePaths")
    void astTest(String testFilePath) throws IOException {
        Utils.doAST_Test(testFilePath);
    }

    private static Stream<String> sourceFilePaths() {
        return Utils.testFilePaths("data/handbuilt");
    }

}