package jatoo.image.metadata.exiftool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jatoo.exec.Command;
import jatoo.image.ImageMetadata;
import jatoo.image.ImageMetadataHandler;

/**
 * ExifTool {@link ImageMetadataHelper} implementation.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.0-SNAPSHOT, May 26, 2017
 */
public class ExifToolImageMetadataHandler extends ImageMetadataHandler {

  /** The logger. */
  private static final Log logger = LogFactory.getLog(ExifToolImageMetadataHandler.class);

  private static final Command COMMAND = new Command("exiftool-10.52.exe");

  static {

    if (new File("pom.xml").exists() && new File("src").exists()) {
      COMMAND.setFolder(new File("target/temp"));
    } else {
      COMMAND.setFolder(new File("temp"));
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
        return buffer.toString();
      } else {
        throw new IOException("anormal exec termination (code = " + code + "): " + buffer.toString());
      }
    }

    catch (IOException | InterruptedException e) {
      logger.error("error executing command (" + Arrays.toString(arguments) + ")", e);
      return null;
    }
  }

  private static String fileToArgument(File file) {
    return "\"" + file.getAbsolutePath() + "\"";
  }

  //
  // ---
  //

  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

  @Override
  public ImageMetadata getMetadata(final File image) {

    String exec = exec("-S", fileToArgument(image));

    if (exec == null) {
      return null;
    }

    ImageMetadata metadata = new ImageMetadata();

    for (String line : exec.split("\\r\\n|\\n|\\r")) {

      line = line.trim();

      if (line.length() == 0) {
        continue;
      }

      try {

        int index = line.indexOf(':');
        String key = line.substring(0, index).trim();
        String value = line.substring(index + 1).trim();

        if ("DateTimeOriginal".equals(key)) {
          metadata.setDateTimeOriginal(SDF.parse(value));
        }
      }

      catch (Throwable t) {
        logger.warn("failed to parse the line: " + line, t);
      }
    }

    return metadata;
  }

  @Override
  public Date getDateTimeOriginal(File image) {

    String exec = exec("-DateTimeOriginal", "-s", "-S", fileToArgument(image));

    if (exec == null) {
      return null;
    }

    try {
      return SDF.parse(exec);
    }

    catch (Throwable t) {
      logger.warn("failed to parse the response: " + exec, t);
      return null;
    }
  }

  @Override
  public boolean copyMetadata(File srcImage, File dstImage) {

    String exec = exec("-tagsfromfile", fileToArgument(srcImage), "-all:all", "-overwrite_original", fileToArgument(dstImage));

    if (exec == null) {
      return false;
    }

    else {
      return true;
    }
  }

}
