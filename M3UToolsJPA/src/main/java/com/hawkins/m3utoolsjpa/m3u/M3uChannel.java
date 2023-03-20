package com.hawkins.m3utoolsjpa.m3u;

import java.util.Map;
import java.util.Set;

import com.hawkins.m3utoolsjpa.utils.Utils;

public class M3uChannel {
    private final String url;

    private final String name;
    
    private final String type;

    private final Set<String> groups;

    private final Map<String, String> props;

    public M3uChannel(String url, String name, Set<String> groups, Map<String, String> props) {
        this.url = url;
        this.type = Utils.deriveGroupTypeByUrl(url);
        this.name = name;
        this.groups = groups;
        this.props = props;
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public String getProp(String key) {
        return props.get(key);
    }

    public Map<String, String> getProps() {
        return props;
    }
    
    public String getType() {
    	return type;
    }
}
