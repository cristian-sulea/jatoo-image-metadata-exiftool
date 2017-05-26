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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

import jatoo.image.ImageMetadataHandler;

/**
 * JUnit tests for {@link ExifToolImageMetadataHelper}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.0, May 26, 2017
 */
public class ExifToolImageMetadataHelperTest {

  private static final String image1 = "target\\test-classes\\jatoo\\image\\metadata\\20141109144518-0.jpg";
  private static final String image2 = "target\\test-classes\\jatoo\\image\\metadata\\20141109144518-400x300.jpg";

  private ImageMetadataHandler handler = ImageMetadataHandler.getInstance();

  @Test
  public void test1() throws Throwable {

    Date date1 = handler.getMetadata(image1).getDateTimeOriginal();
    Date date2 = handler.getDateTimeOriginal(image1);

    Assert.assertEquals(date1, date2);
  }

  @Test
  public void test2() throws Throwable {

    handler.copyMetadata(image1, image2);

    Assert.assertNotNull(handler.getDateTimeOriginal(image2));

    Date date1 = handler.getMetadata(image1).getDateTimeOriginal();
    Date date2 = handler.getDateTimeOriginal(image2);

    Assert.assertEquals(date1, date2);
  }

  @Test
  public void test3() throws Throwable {

    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    Date date1, date2;

    date1 = new Date();
    handler.setDateTimeOriginal(image2, date1);
    date2 = handler.getDateTimeOriginal(image2);
    Assert.assertEquals(dateFormatter.format(date1), dateFormatter.format(date2));

    handler.setDateTimeOriginal(image2, 2011, 11, 11, 11, 11, 11);
    date2 = handler.getDateTimeOriginal(image2);
    Assert.assertEquals("2011:11:11 11:11:11", dateFormatter.format(date2));
  }

}
