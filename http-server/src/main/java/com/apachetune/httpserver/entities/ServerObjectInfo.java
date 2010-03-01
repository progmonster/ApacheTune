package com.apachetune.httpserver.entities;

import java.io.*;

/**
 * Contains server object info.
 *
 * A server object are either a config file, a log file or any another entity that can be
 * represented both in the object tree view and a editor in the content manager.
 *
 * @author <a href="mailto:aleksey.katorgin@trustverse.com">Aleksey V. Katorgin</a>
 * @version 1.0
 */
public class ServerObjectInfo {
    public static enum ServerObjectType {
        CONFIG_FILE,
    }

    private final HttpServer httpServer;

    private final ServerObjectType serverObjectType;

    private final File location;

    private final String title;

    public ServerObjectInfo(HttpServer httpServer, File location, ServerObjectType serverObjectType, String title) {
        if (httpServer == null) {
            throw new NullPointerException("Argument httpServer cannot be a null [this = " + this + "]");
        }

        if (location == null) {
            throw new NullPointerException("Argument location cannot be a null [this = " + this + "]");
        }

        if (serverObjectType == null) {
            throw new NullPointerException("Argument serverObjectType cannot be a null [this = " + this + "]");
        }

        if (title == null) {
            throw new NullPointerException("Argument title cannot be a null [this = " + this + "]");
        }
        
        this.httpServer = httpServer;
        this.location = location;
        this.serverObjectType = serverObjectType;
        this.title = title;
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }
    
    // TODO Make abiltity of remote access to file (via special layer over ssl or another protocols?)
    public File getLocation() {
        return location;
    }

    public ServerObjectType getServerObjectType() {
        return serverObjectType;
    }

    public String getTitle() {
        return title;
    }
}