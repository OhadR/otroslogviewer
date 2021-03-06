package pl.otros.logview.gui.renderers;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class TimeDeltaRendererTest {

  public static final int SECOND = 1000;
  public static final int MINUTE = 60 * SECOND;
  public static final int HOUR = 60 * MINUTE;

  @DataProvider(name = "data")
  public Object[][] data() {
    return new Object[][]{
        {0, "0ms"},
        {SECOND, "1s"},
        {21*SECOND, "21s"},
        {MINUTE, "1m"},
        {39*MINUTE, "39m"},
        {HOUR, "1h"},
        {4*HOUR, "4h"},
        {SECOND+100, "1,1s"},
        {SECOND+234, "1,2s"},
        {SECOND+254, "1,3s"},
        {MINUTE+3*SECOND, "1m 3s"},
        {5*MINUTE, "5m"},
        {5*MINUTE+3*SECOND, "5m"},
        {HOUR + MINUTE, "1h 1m"},
        {6*HOUR + MINUTE, "6h"},
        {-SECOND-100, "-1,1s"},
        {-HOUR - MINUTE, "-1h 1m"},
        {-6*HOUR - MINUTE, "-6h"},
    };

  }


  @Test(dataProvider = "data")
  public void testFormatDelta(long deltaInMs, String expected) throws Exception {
    final String delta = new TimeDeltaRenderer().formatDelta(deltaInMs);
    assertEquals(delta, expected);
  }
}