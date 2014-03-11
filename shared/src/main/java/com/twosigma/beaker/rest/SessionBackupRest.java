/*
 *  Copyright 2014 TWO SIGMA INVESTMENTS, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.twosigma.beaker.rest;

import com.google.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * The service that backs up session to file that offers a RESTful API
 *
 * @author alee
 */
@Singleton
@Produces(MediaType.APPLICATION_JSON)
@Path("sessionbackup")
public class SessionBackupRest {

  private static String BEAKR_DIRECTORY_NAME = ".beaker";
  private static String BACKUP_DIRECTORY_NAME = "backups";
  private static File _backupDirectory;

  public SessionBackupRest() {
    File homeDirectory = new File(System.getProperty("user.home"));
    _backupDirectory = new File(
            homeDirectory,
            BEAKR_DIRECTORY_NAME + "/" + BACKUP_DIRECTORY_NAME);
    if (!_backupDirectory.exists()) {
      _backupDirectory.mkdirs();
    }
  }

  public static class Session {

    String notebookurl;
    String caption;
    String content;
    long openDate;  // millis
    boolean edited;

    private Session(String url, String contentAsString, String cap, long date, boolean ed) {
      notebookurl = url;
      content = contentAsString;
      caption = cap;
      openDate = date;
      edited = ed;
    }
  }

  public static class Plugin {

    String pluginName;
    String pluginUrl;

    private Plugin(String name, String url) {
      pluginName = name;
      pluginUrl = url;
    }
  }
  private Map<String, Session> _sessions = new HashMap<String, Session>();
  private List<Plugin> _plugins = new ArrayList<Plugin>();

  @POST
  @Path("backup")
  public void backup(
          @FormParam("sessionid") String sessionID,
          @FormParam("notebookurl") String notebookUrl,
          @FormParam("content") String contentAsString,
          @FormParam("caption") String caption,
          @FormParam("edited") boolean edited) {
    Session previous = _sessions.get(sessionID);
    long date;
    if (previous != null) {
      date = previous.openDate;
    } else {
      date = System.currentTimeMillis();
    }
    _sessions.put(sessionID, new Session(notebookUrl, contentAsString, caption, date, edited));
    try {
      recordToFile(sessionID, notebookUrl, contentAsString);
    } catch (IOException ex) {
      Logger.getLogger(SessionBackupRest.class.getName()).log(Level.SEVERE, null, ex);
    } catch (InterruptedException ex) {
      Logger.getLogger(SessionBackupRest.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private void recordToFile(String sessionID, String notebookUrl, String contentAsString)
          throws IOException, InterruptedException {
    if (notebookUrl == null) {
      notebookUrl = "NewNotebook";
    }
    final String fileName = sessionID + "_" + URLEncoder.encode(notebookUrl, "ISO-8859-1") + ".bkr.backup";
    final File file = new File(_backupDirectory, fileName);
    Writer writer = new OutputStreamWriter(new FileOutputStream(file));
    try {
      writer.write(contentAsString);
    } finally {
      writer.close();
    }

    file.setReadable(false, false);
    file.setWritable(false, false);
    file.setReadable(true, true);
    file.setWritable(true, true);
  }

  @GET
  @Path("load")
  public Session load(
          @QueryParam("sessionid") String sessionID) {
    return _sessions.get(sessionID);
  }

  @POST
  @Path("close")
  public void close(
          @FormParam("sessionid") String sessionID) {
    _sessions.remove(sessionID);
  }

  @GET
  @Path("getExistingSessions")
  public Map<String, Session> getExistingSessions() {
    return _sessions;
  }

  public static class SessionSerializer
          extends JsonSerializer<Session> {

    @Override
    public void serialize(
            Session t,
            JsonGenerator jgen,
            SerializerProvider sp)
            throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField("notebookurl", t.notebookurl);
      jgen.writeObjectField("caption", t.caption);
      jgen.writeObjectField("openDate", t.openDate);
      jgen.writeObjectField("content", t.content);
      jgen.writeObjectField("edited", t.edited);
      jgen.writeEndObject();
    }
  }

  @POST
  @Path("addPlugin")
  public void addPlugin(
          @FormParam("pluginname") String pluginName,
          @FormParam("pluginurl") String pluginUrl) {
    boolean existsAlready = false;
    for (int i = 0; i < _plugins.size(); ++i) {
      Plugin p = _plugins.get(i);
      if (p.pluginUrl.equals(pluginUrl)) {
        p.pluginName = pluginName;
        existsAlready = true;
        break;
      }
    }
    if (!existsAlready) {
      _plugins.add(new Plugin(pluginName, pluginUrl));
    }
  }

  @GET
  @Path("getExistingPlugins")
  public List<Plugin> getAllPlugins() {
    return _plugins;
  }

  public static class ExistingPlugins {

    final private List<Plugin> _plugins;

    public ExistingPlugins(List<Plugin> plugins) {
      _plugins = plugins;
    }

    public List<Plugin> getPlugins() {
      return _plugins;
    }
  }

  public static class PluginSerializer
          extends JsonSerializer<Plugin> {

    @Override
    public void serialize(
            Plugin t,
            JsonGenerator jgen,
            SerializerProvider sp)
            throws IOException, JsonProcessingException {
      jgen.writeStartObject();
      jgen.writeObjectField("name", t.pluginName);
      jgen.writeObjectField("url", t.pluginUrl);
      jgen.writeEndObject();
    }
  }
}