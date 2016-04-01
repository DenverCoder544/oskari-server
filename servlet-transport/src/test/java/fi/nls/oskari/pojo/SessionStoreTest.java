package fi.nls.oskari.pojo;

import fi.nls.oskari.util.JSONHelper;
import fi.nls.test.util.TestHelper;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class SessionStoreTest {
    private static final String JSON = "{\"client\":\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"route\":\"\",\"uuid\":\"\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":\"216\",\"styleName\":\"default\",\"visible\":true}}}";
    private static final String JSON_FAIL = "{\"client\"\"71k229bstn5ub1fbv1xzwnlnmz\",\"session\":\"49E8CFEF9A310C76438952F8FCD9FF2D\",\"language\":\"fi\",\"browser\":\"mozilla\",\"browserVersion\":20,\"location\":{\"srs\":\"EPSG:3067\",\"bbox\":[509058.0,6858054.0,513578.0,6860174.0],\"zoom\":8},\"grid\":{\"rows\":4,\"columns\":6,\"bounds\":[[508928.0,6859776.0,509952.0,6860800.0],[509952.0,6859776.0,510976.0,6860800.0],[510976.0,6859776.0,512000.0,6860800.0],[512000.0,6859776.0,513024.0,6860800.0],[513024.0,6859776.0,514048.0,6860800.0],[514048.0,6859776.0,515072.0,6860800.0],[508928.0,6858752.0,509952.0,6859776.0],[509952.0,6858752.0,510976.0,6859776.0],[510976.0,6858752.0,512000.0,6859776.0],[512000.0,6858752.0,513024.0,6859776.0],[513024.0,6858752.0,514048.0,6859776.0],[514048.0,6858752.0,515072.0,6859776.0],[508928.0,6857728.0,509952.0,6858752.0],[509952.0,6857728.0,510976.0,6858752.0],[510976.0,6857728.0,512000.0,6858752.0],[512000.0,6857728.0,513024.0,6858752.0],[513024.0,6857728.0,514048.0,6858752.0],[514048.0,6857728.0,515072.0,6858752.0],[508928.0,6856704.0,509952.0,6857728.0],[509952.0,6856704.0,510976.0,6857728.0],[510976.0,6856704.0,512000.0,6857728.0],[512000.0,6856704.0,513024.0,6857728.0],[513024.0,6856704.0,514048.0,6857728.0],[514048.0,6856704.0,515072.0,6857728.0]]},\"tileSize\":{\"width\":256,\"height\":256},\"mapSize\":{\"width\":1130,\"height\":530},\"mapScales\":[5669294.4,2834647.2,1417323.6,566929.44,283464.72,141732.36,56692.944,28346.472,11338.5888,5669.2944,2834.6472,1417.3236,708.6618],\"layers\":{\"216\":{\"id\":\"216\",\"styleName\":\"default\",\"visible\":true},\"134\":{\"id\":134,\"styleName\":\"default\",\"visible\":true}}}";

    @Test
    public void testJSON() throws IOException {
        // check that we have redis connectivity (redis server running)
        assumeTrue(TestHelper.redisAvailable());
        final SessionStore store = SessionStore.setJSON(JSON);
        final String jsonResult = store.getAsJSON();
        assertTrue("the logical JSON structure should be identical",
                JSONHelper.isEqual(
                        JSONHelper.createJSONObject(JSON),
                        JSONHelper.createJSONObject(jsonResult)));
    }

    @Test(expected = IOException.class)
    public void testJSONIOException() throws IOException {
        SessionStore.setJSON(JSON_FAIL);
    }

}
