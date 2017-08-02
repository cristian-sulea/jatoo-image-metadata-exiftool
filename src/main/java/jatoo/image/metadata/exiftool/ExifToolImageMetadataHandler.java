/*
 * Copyright (C) Cristian Sulea ( http://cristian.sulea.net )
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

package jatoo.image.metadata.exiftool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jatoo.exec.Command;
import jatoo.image.ImageMetadata;
import jatoo.image.ImageMetadataHandler;

/**
 * ExifTool {@link ImageMetadataHelper} implementation.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.0, 2 August, 2017
 */
public class ExifToolImageMetadataHandler extends ImageMetadataHandler {

  /** The logger. */
  private static final Log logger = LogFactory.getLog(ExifToolImageMetadataHandler.class);

  private static final Command COMMAND = new Command("exiftool-10.52.exe");

  static {

    if (new File("pom.xml").exists() && new File("src").exists()) {
      COMMAND.setFolder(new File("target/temp"));
    } else {
      COMMAND.setFolder(new File(System.getProperty("launch4j.exedir", System.getProperty("user.home")), ".exiftool"));
    }

    File commandFile = new File(COMMAND.getFolder(), COMMAND.getProgram());
    if (!commandFile.exists()) {
      COMMAND.getFolder().mkdirs();
      try {
        copy(ExifToolImageMetadataHandler.class.getResourceAsStream(COMMAND.getProgram()), new FileOutputStream(commandFile), true);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void copy(final InputStream input, final OutputStream output, boolean close) throws IOException {

    try {

      final byte[] buffer = new byte[4 * 1024];

      int n;
      while ((n = input.read(buffer)) != -1) {
        output.write(buffer, 0, n);
      }
    }

    finally {
      if (close) {
        try {
          input.close();
        } catch (IOException e) {}
        try {
          output.close();
        } catch (IOException e) {}
      }
    }
  }

  private static String exec(String... arguments) {

    try {
      ByteArrayOutputStream buffer = new ByteArrayOutputStream();

      int code = COMMAND.exec(buffer, arguments);

      if (code == 0) {

        String exec = buffer.toString();

        if (exec.trim().length() == 0) {
          return null;
        }

        else {
          return exec;
        }
      }

      else {
        throw new IOException("anormal exec termination (code = " + code + "): " + buffer.toString());
      }
    }

    catch (IOException | InterruptedException e) {
      logger.error("error executing command (" + Arrays.toString(arguments) + ")", e);
      return null;
    }
  }

  private static List<String> execAndSplitForLines(String... arguments) {

    String exec = exec(arguments);

    if (exec == null) {
      return null;
    }

    String[] split = exec.split("\\r\\n|\\n|\\r");
    List<String> lines = new ArrayList<>(split.length);

    for (String line : split) {

      line = line.trim();

      if (line.length() > 0) {
        lines.add(line);
      }
    }

    return lines;
  }

  private static String encloseArgument(String argument) {
    return "\"" + argument + "\"";
  }

  private static String fileToArgument(File file) {
    return encloseArgument(file.getAbsolutePath());
  }

  //
  // ---
  //

  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

  @Override
  public ImageMetadata getMetadata(final File image) {

    List<String> lines = execAndSplitForLines("-S", fileToArgument(image));

    ImageMetadata metadata = new ImageMetadata();

    if (lines != null) {

      for (String line : lines) {

        try {

          int index = line.indexOf(':');
          String key = line.substring(0, index).trim();
          String value = line.substring(index + 1).trim();

          if ("DateTimeOriginal".equals(key)) {
            metadata.setDateTimeOriginal(SDF.parse(value));
          }

          else if ("ImageWidth".equals(key)) {
            metadata.setImageWidth(Integer.parseInt(value));
          } else if ("ImageHeight".equals(key)) {
            metadata.setImageHeight(Integer.parseInt(value));
          }

        }

        catch (Throwable t) {
          logger.warn("failed to parse the line: " + line, t);
        }
      }
    }

    return metadata;
  }

  @Override
  public Date getDateTimeOriginal(File image) {

    String date = exec("-DateTimeOriginal", "-s", "-S", fileToArgument(image));

    if (date == null) {
      return null;
    }

    try {
      return SDF.parse(date);
    }

    catch (Throwable t) {
      logger.info("failed to parse the response: " + date, t);
      return null;
    }
  }

  @Override
  public boolean setDateTimeOriginal(File image, Date date) {
    return null != exec(encloseArgument("-DateTimeOriginal=" + SDF.format(date)), fileToArgument(image), "-overwrite_original");
  }

  @Override
  public Map<File, Date> getDateTimeOriginalForFolder(File folder) {

    List<String> lines = execAndSplitForLines("-T", "-FileName", "-DateTimeOriginal", fileToArgument(folder));

    if (lines == null) {
      return null;
    }

    Map<File, Date> dates = new HashMap<>();

    for (String line : lines) {

      try {

        String[] split = line.split("\t");
        File file = new File(folder, split[0]);
        Date date = SDF.parse(split[1]);

        dates.put(file, date);
      }

      catch (Throwable t) {
        logger.warn("failed to parse the line: " + line, t);
      }
    }

    return dates;
  }

  @Override
  public boolean copyMetadata(File srcImage, File dstImage) {
    return null != exec("-tagsfromfile", fileToArgument(srcImage), "-all:all", "-overwrite_original", fileToArgument(dstImage));
  }

}
