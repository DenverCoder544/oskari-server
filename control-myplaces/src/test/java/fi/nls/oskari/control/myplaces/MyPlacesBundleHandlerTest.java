package fi.nls.oskari.control.myplaces;

import fi.mml.map.mapwindow.service.db.MyPlacesService;
import fi.mml.map.mapwindow.service.db.MyPlacesServiceIbatisImpl;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.domain.map.MyPlaceCategory;
import fi.nls.oskari.map.myplaces.domain.ProxyRequest;
import fi.nls.oskari.map.myplaces.service.GeoServerProxyService;
import fi.nls.test.control.JSONActionRouteTest;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Created with IntelliJ IDEA.
 * User: SMAKINEN
 * Date: 9.4.2014
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
public class MyPlacesBundleHandlerTest extends JSONActionRouteTest {

    private final static MyPlacesBundleHandler handler = new MyPlacesBundleHandler();
    private MyPlacesService service = null;
    private GeoServerProxyService proxyService = null;

    private static final String SUCCESS_TEXT = "Great success";

    @Before
    public void setUp() throws Exception {

        service = mock(MyPlacesServiceIbatisImpl.class);
        handler.setMyPlacesService(service);
        final List<MyPlaceCategory> list = new ArrayList<MyPlaceCategory>();
        MyPlaceCategory cat = new MyPlaceCategory();
        cat.setId(1);
        cat.setUuid("category uuid");
        list.add(cat);

        doReturn(list).when(service).getMyPlaceLayersById(anyList());
        doReturn(false).when(service).canInsert(getGuestUser(), 2);
        doReturn(true).when(service).canInsert(getGuestUser(), 1);

        proxyService = mock(GeoServerProxyService.class);
        handler.setGeoServerProxyService(proxyService);
        doReturn(SUCCESS_TEXT).when(proxyService).proxy(any(ProxyRequest.class));

        handler.init();
    }

    /**
     * Tests that route doesn't accept calls without payload XML
     */
    @Test(expected = ActionParamsException.class)
    public void testWithoutPostData() throws Exception {
        handler.handleAction(createActionParams());
        fail("ActionParamsException should have been thrown with no payload");
    }
    /**
     * Tests that guest users can't call the action route with invalid content
     */
    @Test(expected = ActionParamsException.class)
    public void testWithGuestInvalidContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-invalid-content.txt");
        handler.handleAction(createActionParams(payload));
        fail("ActionDeniedException should have been thrown with invalid content");
    }

    /**
     * Tests that guest users can't call the action route with invalid xml
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertWithGuestInvalidXMLContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-invalid-xml-content.xml");
        handler.handleAction(createActionParams(payload));
        fail("ActionDeniedException should have been thrown with invalid content");
    }
    /**
     * Tests that guest users can't call the action route with invalid content
     */
    @Test(expected = ActionException.class)
    public void testInsertWithGuestMissingUUID() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-missing-uuid.xml");
        handler.handleAction(createActionParams(payload));
        fail("ActionException should have been thrown with invalid content");
    }

    /**
     * Tests that guest users can't call the action route with non-published categoryId
     * NOTE! categoryId is mocked to be non-writable in setup!!
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertWithGuestInvalidCategoryId() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-invalid-id.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with invalid categoryId");
    }

    /**
     * Tests that guest users can call the action route with valid content
     * NOTE! categoryId is mocked to be writable in setup!!
     */
    @Test
    public void testInsertWithGuestValidContent() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-guest-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }


    /**
     * Tests that guest users can't call the action route with get categories
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithGuestGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get categories");
    }

    /**
     * Tests that users can't call the action route with  get categories using other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithInvalidUserGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get categories");
    }

    /**
     * Tests that users can get categories with their own id
     */
    @Test
    public void testWithGetCategories() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-categories-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }


    /**
     * Tests that guest users can't call the action route with get places
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithGuestGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get places");
    }

    /**
     * Tests that users can't call the action route with get places using other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testWithInvalidUserGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-invalid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown for Guest calling get places");
    }

    /**
     * Tests that users can get places with their own id
     */
    @Test
    public void testWithGetPlaces() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-get-places-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

    /**
     * Tests that guest users can't call the action route with random users uuid
     */
    @Test(expected = ActionParamsException.class)
    public void testInsertCategoryWithGuest() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-category-valid.xml");
        ActionParameters params = createActionParams(payload);
        handler.handleAction(params);
        fail("ActionParamsException should have been thrown with guest since the category");
    }

    /**
     * Tests that users can't call the action route with other users uuid
     */
    @Test(expected = ActionDeniedException.class)
    public void testInsertCategoryWithUserInvalidUUID() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-category-invalid-uuid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        fail("ActionDeniedException should have been thrown with invalid uuid");
    }

    /**
     * Tests that users can call the action route with valid content and their own uuid
     */
    @Test
    public void testInsertCategoryWithValidUser() throws Exception {
        InputStream payload = getClass().getResourceAsStream("MyPlacesBundleHandlerTest-input-user-insert-category-valid.xml");
        ActionParameters params = createActionParams(getLoggedInUser(), payload);
        handler.handleAction(params);
        verifyResponseWritten(params);
        assertEquals("Should write '"+ SUCCESS_TEXT + "' if request is proxied to geoserver", SUCCESS_TEXT, getResponseString());
    }

}
