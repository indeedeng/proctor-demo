package com.indeed.demo.proctor;

import java.net.HttpURLConnection;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.indeed.demo.ProctorGroups;
import com.indeed.proctor.common.Proctor;
import com.indeed.proctor.common.ProctorSpecification;
import com.indeed.proctor.common.ProctorUtils;
import com.indeed.proctor.common.UrlProctorLoader;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class DefinitionManager {
    private static final Logger logger = Logger.getLogger(DefinitionManager.class);

    private static final String DEFAULT_SPEC = "/com/indeed/demo/ProctorGroups.json";
    public static final String DEFAULT_DEFINITION = "https://goo.gl/WCWBRJ";

    private Map<String, Proctor> proctorCache = Maps.newHashMap();

    public Proctor load(String definitionUrl, boolean forceReload) {
        Proctor proctor = proctorCache.get(definitionUrl);
        if (proctor != null && !forceReload) {
            return proctor;
        }
        try {
            HttpURLConnection.setFollowRedirects(true); // for demo purposes, allow Java to follow redirects
            ProctorSpecification spec = ProctorUtils.readSpecification(DefinitionManager.class.getResourceAsStream(DEFAULT_SPEC));
            UrlProctorLoader loader = new UrlProctorLoader(spec, definitionUrl);
            proctor = loader.doLoad();
            proctorCache.put(definitionUrl, proctor);
        } catch (Throwable t) {
            logger.error("Failed to load test spec/definition", t);
            t.printStackTrace();
        }
        return proctor;
    }
}
