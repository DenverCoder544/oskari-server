package fi.nls.oskari.spatineo.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.spatineo.dto.OskariMapLayerDto;
import fi.nls.oskari.spatineo.dto.SpatineoMonitoringResponseDto;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Data access to the Spatineo new geographical data monitoring service
 */
public class SpatineoMonitoringDao {

    private static final Logger log = LogFactory.getLogger(SpatineoServalDao.class);

    private final String serviceUrl;
    private final HttpClient httpClient;

    private static final HttpMethodRetryHandler NO_RETRY =  new HttpMethodRetryHandler() {
        @Override
        public boolean retryMethod(HttpMethod method, IOException exception, int executionCount) {
            return false;
        }
    };

    public SpatineoMonitoringDao(final String serviceUrl, HttpClient client) {
        this.serviceUrl = serviceUrl;
        this.httpClient = client;
    }

    public SpatineoMonitoringResponseDto checkServiceStatus(final List<OskariMapLayerDto> layers) {
        httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, NO_RETRY);

        if (null != System.getProperty("http.proxyHost") && null != System.getProperty("http.proxyPort")) {
            httpClient.getHostConfiguration().setProxy(System.getProperty("http.proxyHost"),
                    Integer.parseInt(System.getProperty("http.proxyPort")));
        }

        final PostMethod post = new PostMethod(serviceUrl);
        
        
        
        
//        final List<NameValuePair> pairs = new LinkedList<NameValuePair>();
//        int i = 0;
//        for (final OskariMapLayerDto layer : layers) {
//            pairs.add(new NameValuePair(String.format("service[%s][type]", i), "WMS"));
//            pairs.add(new NameValuePair(String.format("service[%s][url]", i), layer.url));
//            pairs.add(new NameValuePair(String.format("service[%s][offering]", i), layer.name));
//            i++;
//        }
//        post.setRequestBody(pairs.toArray(new NameValuePair[pairs.size()]));
        
        
        
        
        post.addRequestHeader("Origin", "http://www.paikkatietoikkuna.fi");
        post.addRequestHeader("X-Requested-With", getClass().getName());
        try {
            final int status = httpClient.executeMethod(post);
            if (200 == status) {
                return new ObjectMapper().readValue(post.getResponseBodyAsString(), SpatineoMonitoringResponseDto.class);
            } else {
                return null;
            }
        } catch (final HttpException e) {
            // ignore
        } catch (final IOException e) {
            // ignore
        } finally {
            post.releaseConnection();
        }
        return null;
    }
}
