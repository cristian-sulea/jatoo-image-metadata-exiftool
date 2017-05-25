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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jatoo.image.ImageMetadata;
import jatoo.image.ImageMetadataHandler;

/**
 * JUnit tests for {@link ExifToolImageMetadataHelper}.
 * 
 * @author <a href="http://cristian.sulea.net" rel="author">Cristian Sulea</a>
 * @version 1.0, May 23, 2017
 */
public class ExifToolImageMetadataHelperTest {

  @Before
  public void initialize() throws Throwable {
    new File("target\\CommandExecutorTest.java").delete();
    new File("target\\CommandExecutorTest.txt").delete();
  }

  @After
  public void cleanup() throws Throwable {
    new File("target\\CommandExecutorTest.java").delete();
    new File("target\\CommandExecutorTest.txt").delete();
  }

  @Test
  public void test1() throws Throwable {

    ImageMetadataHandler handler = ImageMetadataHandler.getInstance();

    ImageMetadata metadata = handler.getMetadata(new File("target\\test-classes\\jatoo\\image\\metadata\\20141109144518 1.jpg"));

    System.out.println(metadata.getDateTimeOriginal());

    System.out.println(handler.getDateTaken("target\\test-classes\\jatoo\\image\\metadata\\20141109144518 1.jpg"));
  }

}
