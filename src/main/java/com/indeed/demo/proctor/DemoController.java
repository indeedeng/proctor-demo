package com.indeed.demo.proctor;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.common.base.Suppliers;
import com.indeed.demo.ProctorGroups;
import com.indeed.demo.ProctorGroupsManager;
import com.indeed.proctor.common.*;
import com.indeed.proctor.common.model.*;

import com.google.common.collect.ImmutableMap;

import com.indeed.proctor.consumer.ProctorConsumerUtils;
import com.indeed.web.useragents.UserAgent;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "/")
public class DemoController {

    private static final String USER_ID_COOKIE = "UserId";
    private static final String USER_AGENT_HEADER = "User-Agent";
    private static final String TEST_DEFINITION_URL_COOKIE = "TestDefn";
    private static final String TEST_DEFINITION_URL_PARAM = "defn";

    @Autowired
    protected DefinitionManager definitionManager;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView handle(@Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nullable @CookieValue(required = false, value = USER_ID_COOKIE) String userId,
            @Nullable @RequestHeader(required = false, value = USER_AGENT_HEADER) String userAgentHeader,
            @Nullable @CookieValue(required = false, value = TEST_DEFINITION_URL_COOKIE) String testDefnUrlCookie,
            @Nullable @RequestParam(required = false, value = TEST_DEFINITION_URL_PARAM) String testDefnUrlParam) {
        if (userId == null) {
            userId = UUID.randomUUID().toString();
            response.addCookie(new Cookie(USER_ID_COOKIE, userId));
        }
        final String definitionUrl;
        if (!Strings.isNullOrEmpty(testDefnUrlParam)) {
            definitionUrl = testDefnUrlParam;
            response.addCookie(createCookie(TEST_DEFINITION_URL_COOKIE, definitionUrl, Integer.MAX_VALUE));
        } else if (!Strings.isNullOrEmpty(testDefnUrlCookie)) {
            definitionUrl = testDefnUrlCookie;
        } else {
            definitionUrl = DefinitionManager.DEFAULT_DEFINITION;
        }
        final Proctor proctor = definitionManager.load(definitionUrl, false);
        final ProctorGroupsManager groupsManager = new ProctorGroupsManager(Suppliers.ofInstance(proctor));
        final Identifiers identifiers = new Identifiers(TestType.USER, userId);
        final UserAgent userAgent = UserAgent.parseUserAgentStringSafely(userAgentHeader);
        final boolean allowForceGroups = true;
        final ProctorResult result = groupsManager.determineBuckets(
                request, response, identifiers, allowForceGroups, userAgent);
        final ProctorGroups groups = new ProctorGroups(result);
        String groupsJson = "";
        try {
            groupsJson = new ObjectMapper().writeValueAsString(groups);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ModelAndView("demo", ImmutableMap.of(
                "definitionUrl", definitionUrl,
                "defaultDefinition", definitionUrl.equals(DefinitionManager.DEFAULT_DEFINITION),
                "userId", userId,
                "groups", groups,
                "groupsJson", groupsJson));

    }

    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    public String resetUserId(@Nonnull final HttpServletResponse response) {
        // reset user id
        final String userId = UUID.randomUUID().toString();
        response.addCookie(new Cookie(USER_ID_COOKIE, userId));

        // clear force groups cookie
        response.addCookie(createCookie(ProctorConsumerUtils.FORCE_GROUPS_COOKIE_NAME, "", -1));

        return "redirect:/";
    }

    @RequestMapping(value = "/change-definition", method = { RequestMethod.GET, RequestMethod.POST })
    public String changeDefinition(@Nonnull final HttpServletResponse response,
            @Nullable @CookieValue(required = false, value = TEST_DEFINITION_URL_COOKIE) String testDefnUrlCookie,
            @Nullable @RequestParam(required = false, value = TEST_DEFINITION_URL_PARAM) String testDefnUrlParam) {
        final String definitionUrl;
        if (!Strings.isNullOrEmpty(testDefnUrlParam)) {
            definitionUrl = testDefnUrlParam;
            response.addCookie(createCookie(TEST_DEFINITION_URL_COOKIE, definitionUrl, Integer.MAX_VALUE));
        } else {
            definitionUrl = DefinitionManager.DEFAULT_DEFINITION;
            if (testDefnUrlCookie != null) {
                // clear cookie
                response.addCookie(createCookie(TEST_DEFINITION_URL_COOKIE, "", -1));
            }
        }
        definitionManager.load(definitionUrl, true);
        return "redirect:/";
    }

    @RequestMapping(value = "/reload", method = RequestMethod.GET )
    public ModelAndView reload(@Nonnull final HttpServletRequest request,
                @Nonnull final HttpServletResponse response,
                @Nullable @CookieValue(required = false, value = USER_ID_COOKIE) String userId,
                @Nullable @RequestHeader(required = false, value = USER_AGENT_HEADER) String userAgentHeader,
                @Nullable @CookieValue(required = false, value = TEST_DEFINITION_URL_COOKIE) String testDefnUrlCookie) {
        final String definitionUrl = testDefnUrlCookie != null ? testDefnUrlCookie : DefinitionManager.DEFAULT_DEFINITION;
        definitionManager.load(definitionUrl, true);
        return handle(request, response, userId, userAgentHeader, testDefnUrlCookie, null);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(maxAge);
        return cookie;
    }

}
