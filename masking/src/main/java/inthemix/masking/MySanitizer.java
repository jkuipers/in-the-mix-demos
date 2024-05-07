package inthemix.masking;

import org.springframework.boot.actuate.endpoint.SanitizableData;
import org.springframework.boot.actuate.endpoint.SanitizingFunction;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

//@Component
public class MySanitizer implements SanitizingFunction {

    private SanitizingProperties sanitizing;

    public MySanitizer(SanitizingProperties sanitizing) {
        this.sanitizing = sanitizing;
    }

    @Override
    public SanitizableData apply(SanitizableData data) {
        for (Pattern pattern : sanitizing.getKeyPatternsToMask()) {
            if (pattern.matcher(data.getKey()).matches()) {
                return data.withSanitizedValue();
                // could also do custom masking, e.g. to show a few characters for identification purpose
            }
        }
        return data;
    }
}
