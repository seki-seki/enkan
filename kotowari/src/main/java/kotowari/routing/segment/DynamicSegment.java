package kotowari.routing.segment;

import enkan.collection.OptionMap;
import kotowari.routing.RegexpUtils;
import kotowari.routing.RouteBuilder;
import kotowari.routing.Segment;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author kawasima
 */
public class DynamicSegment extends Segment {
    private String key;
    private String defaultValue;
    private Pattern regexp;
    private boolean wrapParentheses = false;

    public DynamicSegment(String key) {
        this(key, new OptionMap());
    }
    public DynamicSegment(String key, OptionMap options) {
        this.key = key;
        if (options.containsKey("default"))
            this.defaultValue = options.getString("default");
        if (options.containsKey("regexp"))
            this.regexp = Pattern.compile(options.getString("regexp"));
        if (options.containsKey("wrapParentheses"))
            this.wrapParentheses = options.getBoolean("wrapParentheses");
    }

    @Override
    public String toString() {
        return wrapParentheses ? "(:" + key +")" : ":" + key;
    }

    @Override
    public boolean hasKey() {
        return true;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String regexpChunk() {
        return regexp != null ? "(" + regexp.pattern() + ")" : defaultRegexpChunk();
    }

    public String defaultRegexpChunk() {
        return "([^" + RegexpUtils.escape(String.join("", RouteBuilder.SEPARATORS)) + "]+)";
    }

    @Override
    public boolean hasDefault() {
        return true;
    }
    @Override
    public String getDefault() {
        return defaultValue;
    }

    @Override
    public void setDefault(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void setRegexp(Pattern regexp) {
        this.regexp = regexp;
    }

    @Override
    public void matchExtraction(OptionMap params, Matcher match, int nextCapture) {
        String m = match.group(nextCapture);
        String value;
        if (m != null) {
            try {
                value = URLEncoder.encode(m, "UTF-8");
            } catch(UnsupportedEncodingException e) {
                value = m;
            }
        } else {
            value = defaultValue;
        }
        params.put(key, value);
    }

    @Override
    public String buildPattern(String pattern) {
        pattern = regexpChunk() + pattern;
        return isOptional() ? RegexpUtils.optionalize(pattern) : pattern;
    }

    @Override
    public String interpolationChunk(OptionMap hash) {
        String value = hash.getString(getKey());
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            return value;
        }
    }

    @Override
    public String stringStructure(List<Segment> list, OptionMap hash) {
        if (isOptional()) {
            if (hash.getString(getKey()).equals(getDefault())) {
                return continueStringStructure(list, hash);
            } else {
                return interpolationStatement(list, hash);
            }
        } else {
            return interpolationStatement(list, hash);
        }

    }
}
