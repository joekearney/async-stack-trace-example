package joe.async;

import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AsyncStuffTest {

  private AsyncStuff asyncStuff;

  @Before
  public void setUp() {
    asyncStuff = new AsyncStuff();
  }

  @After
  public void tearDown() {
    asyncStuff.close();
  }

  @Test
  public void testCustom() throws ExecutionException, InterruptedException {
    int input = 3;
    long result = asyncStuff.roundTrip(input);
    Assert.assertEquals(input, result);
  }

  @Test
  public void testExecutor() throws ExecutionException, InterruptedException {
    int input = 3;
    long result = asyncStuff.roundTripViaExecutor(input);
    Assert.assertEquals(input, result);
  }
}
