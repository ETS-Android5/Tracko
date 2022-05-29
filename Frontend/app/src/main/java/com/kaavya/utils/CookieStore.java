package com.kaavya.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class CookieStore implements CookieJar {

    public static boolean addCookie = true;

    private boolean skipModalAdded = false;

    private final Set<Cookie> cookieStore = new HashSet<>();

    public void clearCookies() {
        cookieStore.clear();
    }

    public void addCookie(String domain, String path, String name, String value) {
        Cookie ck = new Cookie.Builder()
                .name(name)
                .domain(domain)
                .path(path)
                .value(value)
                .build();
        cookieStore.add(ck);
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        /**
         *Saves cookies from HTTP response
         * If the response includes a trailer this method is called second time
         */
        //Save cookies to the store

        if(addCookie) {
            cookieStore.addAll(cookies);

            if(!skipModalAdded) {
                Cookie modelCookie = cookies.get(0);
                Cookie ck = new Cookie.Builder()
                        .domain(modelCookie.domain())
                        .path(modelCookie.path())
                        .name("skipModal")
                        .value("true")
                        .expiresAt(modelCookie.expiresAt())
                        .build();

                cookieStore.add(ck);
                skipModalAdded = true;
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        /**
         * Load cookies from the jar for an HTTP request.
         * This method returns cookies that have not yet expired
         */
        List<Cookie> validCookies = new ArrayList<>();
        for (Cookie cookie : cookieStore) {
            if ((cookie.expiresAt() > 0) && (cookie.expiresAt() < System.currentTimeMillis())) {
                // invalid cookies
            } else {
                validCookies.add(cookie);
            }
        }

        return validCookies;
    }

    public List<Cookie> getCookies() {
        return loadForRequest(null);
    }

    //Print the values of cookies - Useful for testing
    private void LogCookie(Cookie cookie) {
        System.out.println("String: " + cookie.toString());
        System.out.println("Expires: " + cookie.expiresAt());
        System.out.println("Hash: " + cookie.hashCode());
        System.out.println("Path: " + cookie.path());
        System.out.println("Domain: " + cookie.domain());
        System.out.println("Name: " + cookie.name());
        System.out.println("Value: " + cookie.value());
    }
}
