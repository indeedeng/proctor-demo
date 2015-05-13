package com.indeed.demo.proctor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    public static final String DEFAULT_DEFINITION = "http://opensource.indeedeng.io/proctor/demo.json";

    private Map<String, Proctor> proctorCache = Maps.newHashMap();
    private Random random = new Random();

    public DefinitionManager() {
        try {
            final URL u = new URL(DEFAULT_DEFINITION);
            final URLConnection uc = u.openConnection();
            uc.setDefaultUseCaches(false);
            System.out.println("Set default use of caches to " + uc.getDefaultUseCaches());
        } catch (Exception e) {
            System.err.println("Failed to disable caching");
            e.printStackTrace(System.err);
        }
    }

    public Proctor load(String definitionUrl, boolean forceReload) {
        Proctor proctor = proctorCache.get(definitionUrl);
        if (proctor != null && !forceReload) {
            System.out.println("reusing cached " + definitionUrl);
            return proctor;
        }
        try {
            HttpURLConnection.setFollowRedirects(true); // for demo purposes, allow Java to follow redirects
            ProctorSpecification spec = ProctorUtils.readSpecification(DefinitionManager.class.getResourceAsStream(DEFAULT_SPEC));
            UrlProctorLoader loader = new UrlProctorLoader(spec, definitionUrl + "?r=" + random.nextInt());
            proctor = loader.doLoad();
            System.out.println("loaded definition from " + definitionUrl);
            proctorCache.put(definitionUrl, proctor);
        } catch (Throwable t) {
            logger.error("Failed to load test spec/definition", t);
            t.printStackTrace();
        }
        return proctor;
    }
}
