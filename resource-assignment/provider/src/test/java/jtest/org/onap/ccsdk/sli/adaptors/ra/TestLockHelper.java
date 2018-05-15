package jtest.org.onap.ccsdk.sli.adaptors.ra;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestLockHelper {

    private static final Logger log = LoggerFactory.getLogger(TestLockHelper.class);

    @Autowired
    private LockHelper lockHelper;

    @Test
    public void test1() throws Exception {
        LockThread t1 = new LockThread("req1");
        LockThread t2 = new LockThread("req2");
        LockThread t3 = new LockThread("req3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }

    private class LockThread extends Thread {
        private String requester;

        public LockThread(String requester) {
            this.requester = requester;
        }

        @Override
        public void run() {
            lockHelper.lock("resource1", requester, 20);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.warn("Thread interrupted: " + e.getMessage(), e);
            }

            lockHelper.unlock("resource1", false);
        }
    }
}
