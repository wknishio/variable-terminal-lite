/*
 * Copyright 2017 Guilherme Chaguri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.guichaguri.minimalftp.handler;

import com.guichaguri.minimalftp.FTPConnection;
import com.guichaguri.minimalftp.Utils;
import com.guichaguri.minimalftp.api.IFileSystem;
import com.guichaguri.minimalftp.api.ResponseException;
import com.guichaguri.minimalftp.api.CommandInfo.Command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.UUID;

/**
 * Handles file management commands
 * @author Guilherme Chaguri
 */
@SuppressWarnings("unchecked")
public class FTPFileHandler {

    protected final FTPConnection con;

    protected IFileSystem<Object> fs = null;
    protected Object cwd = null;

    protected Object rnFile = null;
    protected long start = 0;
    
    private StringBuilder response = new StringBuilder();
    //private StringBuilder factsBuffer = new StringBuilder();

    public FTPFileHandler(FTPConnection connection) {
        this.con = connection;
    }

    public IFileSystem<Object> getFileSystem() {
        return fs;
    }

    public void setFileSystem(IFileSystem<Object> fs) {
        this.fs = fs;
        this.cwd = fs.getRoot();
    }

    public void registerCommands() {
        con.registerCommand("CWD", "CWD <file>", cwd()); // Change Working Directory
        con.registerCommand("CDUP", "CDUP", cdup()); // Change to Parent Directory
        con.registerCommand("PWD", "PWD", pwd()); // Retrieve Working Directory
        con.registerCommand("MKD", "MKD <file>", mkd()); // Create Directory
        con.registerCommand("RMD", "RMD <file>", rmd()); // Delete Directory
        con.registerCommand("DELE", "DELE <file>", dele()); // Delete File
        con.registerCommand("LIST", "LIST [file]", list()); // List Files
        con.registerCommand("NLST", "NLST [file]", nlst()); // List File Names
        con.registerCommand("RETR", "RETR <file>", retr()); // Retrieve File
        con.registerCommand("STOR", "STOR <file>", stor()); // Store File
        con.registerCommand("STOU", "STOU [file]", stou()); // Store Random File
        con.registerCommand("APPE", "APPE <file>", appe()); // Append File
        con.registerCommand("REST", "REST <bytes>", rest()); // Restart from a position
        con.registerCommand("ABOR", "ABOR", abor()); // Abort all data transfers
        con.registerCommand("ALLO", "ALLO <size>", allo()); // Allocate Space (Obsolete)
        con.registerCommand("RNFR", "RNFR <file>", rnfr()); // Rename From
        con.registerCommand("RNTO", "RNTO <file>", rnto()); // Rename To
        con.registerCommand("SMNT", "SMNT <file>", smnt()); // Structure Mount (Obsolete)

        con.registerSiteCommand("CHMOD", "CHMOD <perm> <file>", site_chmod()); // Change Permissions

        con.registerCommand("MDTM", "MDTM <file>", mdtm()); // Modification Time (RFC 3659)
        con.registerCommand("SIZE", "SIZE <file>", size()); // File Size (RFC 3659)
        con.registerCommand("MLST", "MLST <file>", mlst()); // File Information (RFC 3659)
        con.registerCommand("MLSD", "MLSD <file>", mlsd()); // List Files Information (RFC 3659)

        con.registerCommand("XCWD", "XCWD <file>", cwd()); // Change Working Directory (RFC 775) (Obsolete)
        con.registerCommand("XCUP", "XCUP", cdup()); // Change to Parent Directory (RFC 775) (Obsolete)
        con.registerCommand("XPWD", "XPWD", pwd()); // Retrieve Working Directory (RFC 775) (Obsolete)
        con.registerCommand("XMKD", "XMKD <file>", mkd()); // Create Directory (RFC 775) (Obsolete)
        con.registerCommand("XRMD", "XRMD <file>", rmd()); // Delete Directory (RFC 775) (Obsolete)

        con.registerCommand("MFMT", "MFMT <time> <file>", mfmt()); // Change Modified Time (draft-somers-ftp-mfxx-04)

        con.registerCommand("MD5", "MD5 <file>", md5()); // MD5 Digest (draft-twine-ftpmd5-00) (Obsolete)
        con.registerCommand("MMD5", "MMD5 <file1, file2, ...>", mmd5()); // MD5 Digest (draft-twine-ftpmd5-00) (Obsolete)

        con.registerCommand("HASH", "HASH <file>", hash()); // Hash Digest (draft-bryan-ftpext-hash-02)

        con.registerFeature("base"); // Base Commands (RFC 5797)
        con.registerFeature("hist"); // Obsolete Commands (RFC 5797)
        con.registerFeature("REST STREAM"); // Restart in stream mode (RFC 3659)
        con.registerFeature("MDTM"); // Modification Time (RFC 3659)
        con.registerFeature("SIZE"); // File Size (RFC 3659)
        con.registerFeature("MLST Type*;Size*;Modify*;Perm*;"); // File Information (RFC 3659)
        con.registerFeature("TVFS"); // TVFS Mechanism (RFC 3659)
        con.registerFeature("MFMT"); // Change Modified Time (draft-somers-ftp-mfxx-04)
        con.registerFeature("MD5"); // MD5 Digest (draft-twine-ftpmd5-00)
        con.registerFeature("HASH MD5;SHA-1;SHA-256"); // Hash Digest (draft-bryan-ftpext-hash-02)

        con.registerOption("MLST", "Type;Size;Modify;Perm;");
        con.registerOption("HASH", "MD5");
    }

    protected Object getFile(String path) throws IOException {
        if(path.equals("...") || path.equals("..")) {
            return fs.getParent(cwd);
        } else if(path.equals("/")) {
            return fs.getRoot();
        } else if(path.startsWith("/")) {
            return fs.findFile(fs.getRoot(), path.substring(1));
        } else {
            return fs.findFile(cwd, path);
        }
    }

    protected Command cwd()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object dir = getFile(path);
          if(fs.isDirectory(dir)) {
              cwd = dir;
              con.sendResponse(250, "The working directory was changed");
          } else {
              con.sendResponse(550, "Not a valid directory");
          }
        }
      };
    }

    protected Command cdup()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          cwd = fs.getParent(cwd);
          con.sendResponse(200, "The working directory was changed");
        }
      };
    }

    protected Command pwd()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = fs.getPath(cwd);
          con.sendResponse(257, '"' + path + '"' + " CWD Name");
        }
      };
    }

    protected Command allo()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          // Obsolete command. Accepts the command but takes no action
          con.sendResponse(200, "There's no need to allocate space");
        }
      };
    }

    protected Command rnfr()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          rnFile = getFile(path);
          con.sendResponse(350, "Rename request received");
        }
      };
    }

    protected Command rnto()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          if(rnFile == null) {
              con.sendResponse(503, "No rename request was received");
              return;
          }
          fs.rename(rnFile, getFile(path));
          rnFile = null;
          con.sendResponse(250, "File successfully renamed");
        }
      };
    }

    protected Command stor()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          OutputStream fileStream = fs.writeFile(file, start);
          con.sendResponse(150, "Receiving a file stream for " + path);
          receiveStream(fileStream);
          start = 0;
        }
      };
    }

    protected Command stou()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          Object file = null;
          String ext = ".tmp";
          if(args.length > 0) {
              file = getFile(args[0]);
              int i = args[0].lastIndexOf('.');
              if(i > 0) ext = args[0].substring(i);
          }
          while(file != null && fs.exists(file)) {
              // Quick way to generate simple random names
              // It's not the "perfect" solution, as it only uses hexadecimal characters
              // But definitely enough for file names
              String name = UUID.randomUUID().toString().replace("-", "");
              file = fs.findFile(cwd, name + ext);
          }
          OutputStream outputStream = fs.writeFile(file, 0);
          con.sendResponse(150, "File: " + fs.getPath(file));
          receiveStream(outputStream);
        }
      };
    }

    protected Command appe()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          OutputStream outputStream = fs.writeFile(file, fs.exists(file) ? fs.getSize(file) : 0);
          con.sendResponse(150, "Receiving a file stream for " + path);
          receiveStream(outputStream);
        }
      };
    }

    protected Command retr()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);

          InputStream inputStream = Utils.readFileSystem(fs, file, start, con.isAsciiMode(), con.getBufferSize());
          con.sendResponse(150, "Sending the file stream for " + path + " (" + fs.getSize(file) + " bytes)");
          sendStream(inputStream);
          start = 0;
        }
      };
    }

    protected Command rest()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String byteStr = parms;
          long bytes = Long.parseLong(byteStr);
          if(bytes >= 0) {
              start = bytes;
              con.sendResponse(350, "Restarting at " + bytes + ". Ready to receive a RETR or STOR command");
          } else {
              con.sendResponse(501, "The number of bytes should be greater or equal to 0");
          }
        }
      };
        
    }

    protected Command abor()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          con.abortDataTransfers();
          con.sendResponse(226, "All transfers were aborted successfully");
        }
      };
    }

    protected Command list()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          con.sendResponse(150, "Sending file list...");
          Object dir = cwd;
          // "-l" is not present in any specification, but chrome uses it
          // TODO remove this when the bug gets fixed
          // https://bugs.chromium.org/p/chromium/issues/detail?id=706905
          for (String arg : args) {
              if (!arg.equals("-l") && !arg.equals("-a")) {
                  dir = getFile(arg);
                  break;
              }
          }
          if(!fs.isDirectory(dir)) {
              con.sendResponse(550, "Not a directory");
              return;
          }
          //StringBuilder data = new StringBuilder();
          response.setLength(0);
          for(Object file : fs.listFiles(dir))
          {
            response.append(Utils.format(fs, file));
          }
          con.sendData(response.toString().getBytes("UTF-8"), false);
          con.sendResponse(226, "The list was sent");
        }
      };
    }

    protected Command nlst()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          con.sendResponse(150, "Sending file list...");
          Object dir = cwd;
          // "-l" is not present in any specification, but chrome uses it
          // TODO remove this when the bug gets fixed
          // https://bugs.chromium.org/p/chromium/issues/detail?id=706905
          for (String arg : args) {
              if (!arg.equals("-l") && !arg.equals("-a")) {
                  dir = getFile(arg);
                  break;
              }
          }
          if(!fs.isDirectory(dir)) {
              con.sendResponse(550, "Not a directory");
              return;
          }
          //StringBuilder data = new StringBuilder();
          response.setLength(0);
          for(Object file : fs.listFiles(dir))
          {
            response.append(fs.getName(file)).append("\r\n");
          }
          con.sendData(response.toString().getBytes("UTF-8"), false);
          con.sendResponse(226, "The list was sent");
        }
      };
    }

    protected Command rmd()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          if(!fs.isDirectory(file)) {
              con.sendResponse(550, "Not a directory");
              return;
          }
          fs.delete(file);
          con.sendResponse(250, '"' + path + '"' + " Directory Deleted");
        }
      };
    }

    protected Command dele()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          if(fs.isDirectory(file)) {
              con.sendResponse(550, "Not a file");
              return;
          }
          fs.delete(file);
          con.sendResponse(250, '"' + path + '"' + " File Deleted");
        }
      };
    }

    protected Command mkd()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          fs.mkdirs(file);
          con.sendResponse(257, '"' + path + '"' + " Directory Created");
        }
      };
    }

    protected Command smnt()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          // Obsolete command. The server should respond with a 502 code
          con.sendResponse(502, "SMNT is not implemented in this server");
        }
      };
    }

    protected Command site_chmod()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] cmd = parms.split("\\s+");
          if(cmd.length <= 1) {
            con.sendResponse(501, "Missing parameters");
            return;
        }
        fs.chmod(getFile(cmd[1]), Utils.fromOctal(cmd[0]));
        con.sendResponse(200, "The file permissions were successfully changed");
        }
      };
    }

    protected Command mdtm()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          con.sendResponse(213, Utils.toMdtmTimestamp(fs.getLastModified(file)));
        }
      };
    }

    protected Command size()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          Object file = getFile(path);
          con.sendResponse(213, Long.toString(fs.getSize(file)));
        }
      };
    }

    protected Command mlst()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          Object file = args.length > 0 ? getFile(args[0]) : cwd;
          if(!fs.exists(file)) {
              con.sendResponse(550, "File not found");
              return;
          }
          String[] options = con.getOption("MLST").split(";");
          response.setLength(0);
          Utils.getFacts(fs, file, options, response);
          con.sendResponse(250, "- Listing " + fs.getName(file) + "\r\n" + response.toString());
          con.sendResponse(250, "End");
        }
      };
    }

    protected Command mlsd()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          Object file = args.length > 0 ? getFile(args[0]) : cwd;
          if(!fs.isDirectory(file)) {
              con.sendResponse(550, "Not a directory");
              return;
          }
          con.sendResponse(150, "Sending file information list...");
          String[] options = con.getOption("MLST").split(";");
          //StringBuilder data = new StringBuilder();
          response.setLength(0);
          for(Object f : fs.listFiles(file))
          {
            Utils.getFacts(fs, f, options, response);
          }
          con.sendData(response.toString().getBytes("UTF-8"), false);
          con.sendResponse(226, "The file list was sent!");
        }
      };
    }

    protected Command mfmt()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String[] args = parms.split("\\s+");
          if(args.length < 2) {
            con.sendResponse(501, "Missing arguments");
            return;
          }
          Object file = getFile(args[1]);
          long time;
          if(!fs.exists(file)) {
              con.sendResponse(550, "File not found");
              return;
          }
          try {
              time = Utils.fromMdtmTimestamp(args[0]);
          } catch(ParseException ex) {
              con.sendResponse(500, "Couldn't parse the time");
              return;
          }
          fs.touch(file, time);
          con.sendResponse(213, "Modify=" + args[0] + "; " + fs.getPath(file));
        }
      };
    }

    protected Command md5()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          String p = path = path.trim();
          if(p.length() > 2 && p.startsWith("\"") && p.endsWith("\"")) {
              // Remove the quotes
              p = p.substring(1, p.length() - 1).trim();
          }
          try {
              Object file = getFile(p);
              byte[] digest = fs.getDigest(file, "MD5", con.getBufferSize());
              String md5 = new BigInteger(1, digest).toString(16);
              con.sendResponse(251, path + " " + md5);
          } catch(NoSuchAlgorithmException ex) {
              // Shouldn't ever happen
              con.sendResponse(504, ex.getMessage());
          }
        }
      };
    }

    protected Command mmd5()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String args = parms;
          String[] paths = args.split(",");
          //StringBuilder response = new StringBuilder();
          response.setLength(0);

          try {
              for(String path : paths) {
                  String p = path = path.trim();

                  if(p.length() > 2 && p.startsWith("\"") && p.endsWith("\"")) {
                      // Remove the quotes
                      p = p.substring(1, p.length() - 1).trim();
                  }

                  Object file = getFile(p);
                  byte[] digest = fs.getDigest(file, "MD5", con.getBufferSize());
                  String md5 = new BigInteger(1, digest).toString(16);

                  if(response.length() > 0) response.append(", ");
                  response.append(path).append(" ").append(md5);
              }

              con.sendResponse(paths.length == 1 ? 251 : 252, response.toString());
          } catch(NoSuchAlgorithmException ex) {
              // Shouldn't ever happen
              con.sendResponse(504, ex.getMessage());
          }
        }
      };
    }

    protected Command hash()
    {
      return new Command()
      {
        public void run(String parms) throws IOException
        {
          String path = parms;
          try {
              Object file = getFile(path);
              String hash = con.getOption("HASH");
              byte[] digest = fs.getDigest(file, hash, con.getBufferSize());
              String hex = new BigInteger(1, digest).toString(16);

              // TODO RANG
              con.sendResponse(213, String.format("%s 0-%s %s %s", hash, fs.getSize(file), hex, fs.getName(file)));
          } catch(NoSuchAlgorithmException ex) {
              con.sendResponse(504, ex.getMessage());
          }
        }
      };
    }

    /**
     * Sends a stream asynchronously, sending a response after it's done
     * @param in The stream
     */
    protected void sendStream(final InputStream in) {
      Thread thread = 
        new Thread(new Runnable() {
          public void run() {
              try {
                  con.sendData(in);
                  con.sendResponse(226, "File sent!");
              } catch(ResponseException ex) {
                  con.sendResponse(ex.getCode(), ex.getMessage());
              } catch(Exception ex) {
                  con.sendResponse(451, ex.getMessage());
              }
          }
        });
      thread.setDaemon(true);
      thread.start();
    }

    /**
     * Receives a stream asynchronously, sending a response after it's done
     * @param out The stream
     */
    protected void receiveStream(final OutputStream out) {
      Thread thread = 
        new Thread(new Runnable() {
          public void run() {
              try {
                  con.receiveData(out);
                  con.sendResponse(226, "File received!");
              } catch(ResponseException ex) {
                  con.sendResponse(ex.getCode(), ex.getMessage());
              } catch(Exception ex) {
                  con.sendResponse(451, ex.getMessage());
              }
          }
        });
      thread.setDaemon(true);
      thread.start();
    }

}
