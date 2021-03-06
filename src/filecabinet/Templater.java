//- Copyright © 2008-2010 8th Light, Inc. All Rights Reserved.
//- Limelight and all included source files are distributed under terms of the GNU LGPL.

package filecabinet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Templater
{
  private static final Pattern TOKEN_PATTERN = Pattern.compile("!-(\\w+)-!");

  private final FileHandler textFileHandler = new TextFileHandler();
  private final FileHandler binaryFileHandler = new BinaryFileHandler();

  private TemplaterLogger logger;
  private FileSystem fs;
  private String destinationRoot;
  private String sourceRoot;
  private Map<String, String> tokens = new HashMap<String, String>();
  private boolean destinationRootVerified;
  private boolean forceful;

  public Templater(String destination, String source)
  {
    logger = new TemplaterLogger();
    fs = FileSystem.instance();
    destinationRoot = destination;
    sourceRoot = source;
  }

  public void setLogger(TemplaterLogger logger)
  {
    this.logger = logger;
  }

  public void setForceful(boolean forceful)
  {
    this.forceful = forceful;
  }

  public boolean isForceful()
  {
    return forceful;
  }

  public void setFs(FileSystem fs)
  {
    this.fs = fs;
  }

  public String getDestinationRoot()
  {
    return destinationRoot;
  }

  public String getSourceRoot()
  {
    return sourceRoot;
  }

  public void directory(String dir)
  {
    verifyDestinationRoot();
    final String fullPath = fs.join(destinationRoot, dir);
    if(fs.exists(fullPath))
      return;

    directory(fs.parentPath(dir));
    creatingDirectory(dir);
    fs.createDirectory(fullPath);
  }

  private void verifyDestinationRoot()
  {
    if(!destinationRootVerified)
    {
      if(!fs.exists(destinationRoot))
        throw new RuntimeException("Templater destination root doesn't exist: " + destinationRoot);
      destinationRootVerified = true;
    }
  }

  public void file(String filePath, String sourcePath)
  {
    doFile(filePath, sourcePath, textFileHandler);
  }

  public void binary(String filePath, String sourcePath)
  {
    doFile(filePath, sourcePath, binaryFileHandler);
  }

  private void doFile(String filePath, String sourcePath, FileHandler handler)
  {
    directory(fs.parentPath(filePath));
    final String source = fs.join(sourceRoot, sourcePath);
    final String destination = fs.join(destinationRoot, filePath);

    if(fs.exists(destination))
      if(isForceful())
      {
        overwritingFile(filePath);
        handler.handle(destination, source);
      }
      else
        fileExists(filePath);
    else
    {
      creatingFile(filePath);
      handler.handle(destination, source);
    }
  }

  public void addToken(String token, String value)
  {
    tokens.put(token, value);
  }

  private String replaceTokens(String content)
  {
    return StringUtil.gsub(content, TOKEN_PATTERN, new StringUtil.Gsuber()
    {
      public String replacementFor(Matcher matcher)
      {
        final String token = matcher.group(1);
        String tokenValue = tokens.get(token);
        if(tokenValue == null)
          tokenValue = "UNKNOWN TOKEN";
        return tokenValue;
      }
    });
  }

  private void creatingFile(String filePath)
  {
    logger.say("\tcreating file:       " + filePath);
  }

  private void fileExists(String filePath)
  {
    logger.say("\tfile already exists: " + filePath);
  }

  private void overwritingFile(String filePath)
  {
    logger.say("\toverwriting file:    " + filePath);
  }

  private void creatingDirectory(String dir)
  {
    logger.say("\tcreating directory:  " + dir);
  }

  public static class TemplaterLogger
  {
    public void say(String message)
    {
      System.out.println(message);
    }
  }

  private static interface FileHandler
  {
    void handle(String destination, String source);
  }

  private class TextFileHandler implements FileHandler
  {
    public void handle(String destination, String source)
    {
      final String templateContent = fs.readTextFile(source);
      fs.createTextFile(destination, replaceTokens(templateContent));
    }
  }

  private class BinaryFileHandler implements FileHandler
  {
    public void handle(String destination, String source)
    {
      final InputStream inputStream = fs.inputStream(source);
      final OutputStream outputStream = fs.outputStream(destination);
      new StreamReader(inputStream).copyBytes(outputStream);
      try
      {
        inputStream.close();
        outputStream.close();
      }
      catch(IOException e)
      {
        throw new RuntimeException(e);
      }
    }
  }
}
