//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package filecabinet;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class FakeFileSystemTest
{
  private FakeFileSystem fs;

  @Before
  public void setUp() throws Exception
  {
    fs = new FakeFileSystem();
  }
  
  @Test
  public void onlyRootExistsByDefault() throws Exception
  {
    assertEquals(true, fs.exists("/"));
    assertEquals(false, fs.exists("etc"));
    assertEquals(false, fs.exists("/foo"));
  }

  @Test
  public void creatingNestedDirectories() throws Exception
  {
    fs.createDirectory("etc/tmp/test");

    assertEquals(true, fs.exists("/etc"));
    assertEquals(true, fs.exists("/etc/tmp"));
    assertEquals(true, fs.exists("/etc/tmp/test"));
    assertEquals(true, fs.isDirectory("/etc/tmp/test"));
  }
  
  @Test
  public void creatingFileWithString() throws Exception
  {
    fs.createTextFile("foo/bar.txt", "some content");

    assertEquals(true, fs.exists("foo/bar.txt"));
    assertEquals(false, fs.isDirectory("foo/bar.txt"));
    assertEquals("some content", fs.readTextFile("foo/bar.txt"));
  }

  @Test
  public void usingDotsInPaths() throws Exception
  {
    assertEquals(true, fs.exists("."));

    fs.createDirectory("foo/bar");
    assertEquals(true, fs.exists("foo/."));
    assertEquals(true, fs.exists("foo/./bar"));
    assertEquals(true, fs.exists("./foo"));
    assertEquals(true, fs.exists("/./foo"));
    assertEquals(true, fs.exists("foo/.."));
    assertEquals(true, fs.exists("foo/bar/.."));
    assertEquals(true, fs.exists("foo/bar/../.."));
    assertEquals(true, fs.exists("foo/bar/../bar"));
    assertEquals(true, fs.exists("foo/bar/../../foo"));
    assertEquals(true, fs.exists("foo/bar/../../foo/bar"));
    assertEquals(true, fs.exists("/foo/../foo"));

    assertEquals(false, fs.exists("/foo/bar/../foo"));
  }

  @Test
  public void absolutePath() throws Exception
  {
    assertEquals("/path", fs.absolutePath("path"));
    assertEquals("/foo", fs.absolutePath("/foo"));

    fs.createDirectory("/work/dir");
    fs.setWorkingDirectory("/work/dir");
    assertEquals("/work/dir/path", fs.absolutePath("path"));
  }
}
