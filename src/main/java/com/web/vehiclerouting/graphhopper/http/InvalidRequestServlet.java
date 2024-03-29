/*
 * Copyright © 2011. Team Lazer Beez (http://teamlazerbeez.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.web.vehiclerouting.graphhopper.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class InvalidRequestServlet extends GHBaseServlet
{

    @Override
    protected void service( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException
    {
        try
        {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.setContentType("text/plain");
            res.setContentType("UTF-8");
            JSONObject json = new JSONObject();
            json.put("error_code", "404");
            writeJson(req, res, json);
        } catch (JSONException ex)
        {
            Logger.getLogger(InvalidRequestServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
