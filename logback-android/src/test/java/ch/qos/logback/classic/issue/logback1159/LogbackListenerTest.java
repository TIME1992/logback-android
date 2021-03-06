package ch.qos.logback.classic.issue.logback1159;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.StaticLoggerBinderFriend;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.CoreTestConstants;

@RunWith(RobolectricTestRunner.class)
public class LogbackListenerTest {
    private File logFile = new File(CoreTestConstants.OUTPUT_DIR_PREFIX, "test.log");

    private void doConfigure() throws JoranException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(new File(CoreTestConstants.TEST_INPUT_PREFIX, "issue/logback-1159.xml"));
    }

    @After
    public void after() {
        logFile.delete();
        StaticLoggerBinderFriend.reset();
    }

    private void disableLogFileAccess() throws IOException {
        logFile.getParentFile().mkdirs();
        logFile.createNewFile();
        logFile.deleteOnExit();
        Path path = Paths.get(logFile.toURI());
        Set<PosixFilePermission> permissions = Collections.emptySet();
        try {
            Files.setPosixFilePermissions(path, permissions);
        } catch (UnsupportedOperationException e) {
            path.toFile().setReadOnly();
        }
    }

    @Test(expected = LoggingError.class)
    public void testThatErrorIsDetectedAtLogInit() throws Exception {
        disableLogFileAccess();
        doConfigure();
    }

    @Test
    public void assertThatNonFailSafeAppendersNotAffected() throws JoranException {
        doConfigure();
        Logger logger = LoggerFactory.getLogger("NOTJOURNAL");
        logger.error("This should not fail");
    }

}