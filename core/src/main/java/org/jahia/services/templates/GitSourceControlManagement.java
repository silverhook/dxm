package org.jahia.services.templates;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: toto
 * Date: 7/2/12
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class GitSourceControlManagement extends SourceControlManagement {

    private static Logger logger = LoggerFactory.getLogger(GitSourceControlManagement.class);

    private File rootFolder;

    private void executeCommand(List<String> cmdList) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
            processBuilder.directory(rootFolder);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int code = process.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug("Return with code " + code);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    logger.debug(line);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute command " + StringUtils.join(cmdList, ' '), e);
        }
    }

    public GitSourceControlManagement() {
    }

    protected void initWithEmptyFolder(File workingDirectory, String url) throws Exception {
        this.rootFolder = workingDirectory;
        executeCommand(Arrays.asList("git", "init"));
        executeCommand(Arrays.asList("git", "add", "."));
        executeCommand(Arrays.asList("git", "commit", "-a", "-m", "First commit"));
        executeCommand(Arrays.asList("git", "remote", "add", "origin", url));
        executeCommand(Arrays.asList("git", "push", "-u", "origin", "master"));
    }

    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        this.rootFolder = workingDirectory;
    }

    protected void initFromURI(File workingDirectory, String uri) throws Exception {
        this.rootFolder = workingDirectory.getParentFile();
        executeCommand(Arrays.asList("git", "clone", uri, workingDirectory.getName()));
        this.rootFolder = workingDirectory;
    }

    @Override
    public File getRootFolder() {
        return rootFolder;
    }

    public void setModifiedFile(List<File> files) throws Exception {
        if (files.isEmpty()) {
            return;
        }

        String rootPath = rootFolder.getPath();
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("git");
        cmdList.add("add");
        for (File file : files) {
            if (file.getPath().equals(rootPath)) {
                cmdList.add(".");
            } else {
                cmdList.add(file.getPath().substring(rootPath.length() + 1));
            }
        }
        executeCommand(cmdList);
    }

    public void update() {
        executeCommand(Arrays.asList("git", "stash"));
        executeCommand(Arrays.asList("git", "pull", "--rebase"));
        executeCommand(Arrays.asList("git", "stash", "pop"));
    }

    public void commit(String message) {
        executeCommand(Arrays.asList("git", "commit", "-a", "-m", message));
        executeCommand(Arrays.asList("git", "push"));
    }
}
