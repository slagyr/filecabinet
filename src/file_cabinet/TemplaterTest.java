//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package file_cabinet;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class TemplaterTest
{
  private Templater templater;
  private FakeTemplaterLogger log;
  private FakeFileSystem fileSystem;

  @Before
  public void setUp() throws Exception
  {
    templater = new Templater("destination", "source");
    log = new FakeTemplaterLogger();
    templater.setLogger(log);
    fileSystem = new FakeFileSystem();
    templater.setFs(fileSystem);
    fileSystem.createDirectory("destination");
  }

  public static class FakeTemplaterLogger extends Templater.TemplaterLogger
  {
    public LinkedList<String> messages = new LinkedList<String>();

    @Override
    public void say(String message)
    {
      messages.add(message);
    }
  }

  @Test
  public void root() throws Exception
  {
    assertEquals("destination", templater.getDestinationRoot());
    assertEquals("source", templater.getSourceRoot());
  }

  @Test
  public void creatingDirectory() throws Exception
  {
    templater.directory("foo");

    assertEquals(true, fileSystem.exists("destination/foo"));
    assertEquals(true, fileSystem.isDirectory("destination/foo"));
    assertEquals("\tcreating directory:  foo", log.messages.get(0));
  }

  @Test
  public void creatingFile() throws Exception
  {
    fileSystem.createTextFile("source/1.template", "template content");

    templater.file("file.txt", "1.template");

    assertEquals("template content", fileSystem.readTextFile("destination/file.txt"));
    assertEquals("\tcreating file:       file.txt", log.messages.get(0));
  }

  @Test
  public void existingFilesAreSkipped() throws Exception
  {
    fileSystem.createTextFile("source/1.template", "template content");
    fileSystem.createTextFile("destination/file.txt", "original content");

    templater.file("file.txt", "1.template");

    assertEquals("original content", fileSystem.readTextFile("destination/file.txt"));
    assertEquals("\tfile already exists: file.txt", log.messages.get(0));
  }
  
  @Test
  public void creatingFileWithTag() throws Exception
  {
    fileSystem.createTextFile("source/1.template", "!-MY_TAG-! content");
    templater.addToken("MY_TAG", "wicked");

    templater.file("file.txt", "1.template");

    assertEquals("wicked content", fileSystem.readTextFile("destination/file.txt"));
  }

  @Test
  public void creatingFileWithMultipleTokens() throws Exception
  {
    fileSystem.createTextFile("source/1.template", "!-TOKEN0-! !-TOKEN1-! !-TOKEN2-!");
    templater.addToken("TOKEN0", "0");
    templater.addToken("TOKEN1", "1");
    templater.addToken("TOKEN2", "2");

    templater.file("file.txt", "1.template");

    assertEquals("0 1 2", fileSystem.readTextFile("destination/file.txt"));
  }
}
