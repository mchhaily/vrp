/*
 *  Licensed to GraphHopper and Peter Karich under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.web.vehiclerouting.graphhopper.http;

import com.google.inject.servlet.ServletModule;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;

/**
 * @author Peter Karich
 */
public class GHServletModule extends ServletModule
{
    protected Map<String, String> params = new HashMap<String, String>();

    public GHServletModule()
    {
        params.put("mimeTypes", "text/html,"
                + "text/plain,"
                + "text/xml,"
                + "application/xhtml+xml,"
                + "text/css,"
                + "application/json,"
                + "application/javascript,"
                + "image/svg+xml");
    }

    @Override
    protected void configureServlets()
    {
        filter("*").through(GHGZIPHook.class, params);
        bind(GHGZIPHook.class).in(Singleton.class);
        
        filter("*").through(CORSFilter.class, params);
        bind(CORSFilter.class).in(Singleton.class);

        serve("/i18n*").with(I18NServlet.class);
        bind(I18NServlet.class).in(Singleton.class);

        serve("/info*").with(InfoServlet.class);
        bind(InfoServlet.class).in(Singleton.class);

        serve("/route*").with(GraphHopperServlet.class);
        bind(GraphHopperServlet.class).in(Singleton.class);
    }
}
