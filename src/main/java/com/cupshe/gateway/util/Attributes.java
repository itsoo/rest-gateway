package com.cupshe.gateway.util;

import com.cupshe.ak.text.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attributes
 *
 * @author zxy
 */
@Getter
public class Attributes {

    private final String id;

    private final String host;

    private final HttpMethod method;

    private final MediaType contentType;

    private final HttpHeaders headers;

    private final MultiValueMap<String, String> cookies;

    private final MultiValueMap<String, String> queryParams;

    private final Object body;

    private Attributes(
            String id, String host, HttpMethod method, MediaType contentType, HttpHeaders headers,
            MultiValueMap<String, String> cookies, MultiValueMap<String, String> queryParams, Object body) {

        this.id = id;
        this.host = host;
        this.method = method;
        this.contentType = contentType;
        this.headers = headers;
        this.cookies = cookies;
        this.queryParams = queryParams;
        this.body = body;
    }

    public static AttributeBuilder attributeBuilder() {
        return new AttributeBuilder();
    }

    @Setter
    @Accessors(chain = true)
    public static class AttributeBuilder {

        private String id;

        private String host;

        private HttpMethod method;

        private MediaType contentType;

        private HttpHeaders headers;

        private MultiValueMap<String, HttpCookie> cookies;

        private MultiValueMap<String, String> queryParams;

        private Object body;

        private AttributeBuilder() {}

        public Attributes build() {
            Assert.notNull(method, "'httpMethod' cannot be null.");
            host = StringUtils.getOrEmpty(host);
            return new Attributes(id, host, method, contentType, headers, getCookies(), queryParams, body);
        }

        private MultiValueMap<String, String> getCookies() {
            MultiValueMap<String, String> result = new LinkedMultiValueMap<>();
            for (Map.Entry<String, List<HttpCookie>> me : cookies.entrySet()) {
                result.addAll(me.getKey(), me.getValue()
                        .stream()
                        .map(HttpCookie::getValue)
                        .collect(Collectors.toList()));
            }

            return result;
        }
    }
}
