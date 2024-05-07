package inthemix.httpcache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

@RestController
public class SomeController {

    record SomeModel(String id, String name) { }

    private Logger logger = LoggerFactory.getLogger(getClass());

    @GetMapping("/shallow/{id}")
    SomeModel shallowETag(@PathVariable String id) {
        logger.info("Returning new model");
        return new SomeModel(id, "someName");
    }















    @GetMapping("/deep/{id}")
    ResponseEntity<SomeModel> deepETag(ServletWebRequest request, @PathVariable String id) {
        String ifNoneMatch = request.getHeader(HttpHeaders.IF_NONE_MATCH);
        String etag = "etag-" + id; // pretend that this is like a cached hash, in this case without double quotes

        if (StringUtils.hasText(ifNoneMatch) && request.checkNotModified(etag)) {
            // contents unchanged. Check has set appropriate response headers & status already
            logger.info("Returning empty response");
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ETAG, '"' + etag + '"'); // only add quotes if etag value doesn't have them yet
        logger.info("Returning new model");
        return new ResponseEntity<>(new SomeModel(id, "someName"), headers, HttpStatus.OK);
    }
}
