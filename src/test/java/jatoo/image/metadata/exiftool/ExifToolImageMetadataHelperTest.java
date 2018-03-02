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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import jatoo.image.ImageMetadata;
import jatoo.image.ImageMetadataHandler;

/**
 * JUnit tests for {@link ExifToolImageMetadataHelper}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.1, January 12, 2018
 */
public class ExifToolImageMetadataHelperTest {

  private static final String folder = "target\\test-classes\\jatoo\\image\\metadata";
  private static final String image1 = folder + "\\20141109144518-0.jpg";
  private static final String image2 = folder + "\\20141109144518-400x300.jpg";

  private ImageMetadataHandler handler = ImageMetadataHandler.getInstance();

  @Test
  public void testGetMetadata() throws Throwable {

    ImageMetadata metadata = handler.getMetadata(image1);

    Assert.assertEquals(metadata.getDateTaken(), handler.getDateTaken(image1));
    Assert.assertEquals(metadata.getOrientation(), handler.getOrientation(image1));
  }

  @Test
  public void testCopyMetadata() throws Throwable {

    Assert.assertNull(handler.getDateTaken(image2));

    handler.copyMetadata(image1, image2);

    Assert.assertNotNull(handler.getDateTaken(image2));

    Date date1 = handler.getMetadata(image1).getDateTaken();
    Date date2 = handler.getDateTaken(image2);

    Assert.assertEquals(date1, date2);
  }

  @Test
  public void testRemoveMetadata() throws Throwable {

    Assert.assertNotNull(handler.getMetadata(image1).getDateTaken());
    Assert.assertNotNull(handler.getDateTaken(image1));

    handler.removeMetadata(image1);

    Assert.assertNull(handler.getMetadata(image1).getDateTaken());
    Assert.assertNull(handler.getDateTaken(image1));
  }

  @Test
  public void testDateTimeOriginal() throws Throwable {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    Date date1, date2;

    date1 = new Date();
    handler.setDateTaken(image2, date1);
    date2 = handler.getDateTaken(image2);
    Assert.assertEquals(dateFormatter.format(date1), dateFormatter.format(date2));

    handler.setDateTaken(image2, 2011, 11, 11, 11, 11, 11);
    date2 = handler.getDateTaken(image2);
    Assert.assertEquals("2011:11:11 11:11:11", dateFormatter.format(date2));
  }

  @Test
  public void testDateTimeOriginalForFolder() throws Throwable {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    handler.setDateTaken(image1, 2011, 11, 11, 11, 11, 11);
    handler.setDateTaken(image2, 2012, 12, 12, 12, 12, 12);

    Map<File, Date> dates = handler.getDateTakenForFolder(folder);

    Assert.assertEquals("2011:11:11 11:11:11", dateFormatter.format(dates.get(new File(image1))));
    Assert.assertEquals("2012:12:12 12:12:12", dateFormatter.format(dates.get(new File(image2))));
  }

}
