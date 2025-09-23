/*
 *
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dinky.service.impl;

import org.dinky.data.dto.GitRepositoryCommitDTO;
import org.dinky.data.dto.GitRepositoryInfoDTO;
import org.dinky.data.dto.TaskDTO;
import org.dinky.data.ext.ConfigItem;
import org.dinky.data.model.SystemConfiguration;
import org.dinky.data.model.rbac.User;
import org.dinky.service.GitRepositoryService;
import org.dinky.service.TaskService;
import org.dinky.service.UserService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.ProxySOCKS5;
import com.jcraft.jsch.Session;

import cn.dev33.satoken.stp.StpUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GitRepositoryServiceImpl implements GitRepositoryService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    private static final String GIT_LOCAL_REPOSITORY_DIR = "./tmp/";
    private static final String GIT_TASK_BASE_PATH = "tasks";

    @Override
    public GitRepositoryInfoDTO getGitRepositoryInfo(Integer taskId) {
        return new GitRepositoryInfoDTO(
                SystemConfiguration.getInstances().getGitRepository(),
                SystemConfiguration.getInstances().getGitRepositoryBranch());
    }

    @Override
    public List<GitRepositoryCommitDTO> getGitRepositoryCommits(Integer taskId) {
        if (!isGitSyncEnabled(taskId)) {
            return Collections.emptyList();
        }
        try (Git git = getOrInitLocalRepository(taskId)) {
            if (!isTaskFileExist(git, taskId)) {
                return Collections.emptyList();
            }

            Iterable<RevCommit> commits = git.log()
                    .addPath(String.format("tasks/%s", taskId))
                    .setMaxCount(10)
                    .call();

            List<GitRepositoryCommitDTO> result = new ArrayList<>();
            for (RevCommit commit : commits) {
                result.add(new GitRepositoryCommitDTO(
                        commit.getName(),
                        commit.getAuthorIdent().getName(),
                        commit.getAuthorIdent().getWhen(),
                        commit.getFullMessage()));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("获取 Git 提交记录失败", e);
        }
    }

    private boolean isGitSyncEnabled(Integer taskId) {
        GitRepositoryInfoDTO info = this.getGitRepositoryInfo(taskId);
        return info.getUrl() != null && !info.getUrl().isEmpty();
    }

    /**
     * 如果本地仓库存在就打开，否则初始化并克隆
     */
    private Git getOrInitLocalRepository(Integer taskId) throws Exception {
        GitRepositoryInfoDTO info = this.getGitRepositoryInfo(taskId);
        String localPath = String.format("%s/%s", GIT_LOCAL_REPOSITORY_DIR, this.generateRepositoryKey(info.getUrl()));
        File repoDir = new File(localPath);

        if (hasLocalGitRepository(localPath)) {
            return Git.open(repoDir);
        } else {
            return initLocalGitRepository(info, repoDir);
        }
    }

    /**
     * 克隆仓库
     */
    private Git initLocalGitRepository(GitRepositoryInfoDTO info, File repoDir) throws Exception {
        SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host host, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
                ProxySOCKS5 proxy = new ProxySOCKS5("127.0.0.1", 9999);
                session.setProxy(proxy);
            }

            @Override
            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch jsch = super.createDefaultJSch(fs);
                log.info(
                        "privateKeyBytes: " + SystemConfiguration.getInstances().getGitRepositoryKey());
                byte[] privateKeyBytes =
                        SystemConfiguration.getInstances().getGitRepositoryKey().getBytes(StandardCharsets.UTF_8);
                jsch.addIdentity("git-key", privateKeyBytes, null, null);
                return jsch;
            }
        };
        SshSessionFactory.setInstance(sshSessionFactory);

        return Git.cloneRepository()
                .setURI(info.getUrl())
                .setBranch(info.getBranch())
                .setDirectory(repoDir)
                .call();
    }

    /**
     * 判断本地是否存在仓库
     */
    private boolean hasLocalGitRepository(String path) {
        try {
            Git.open(new File(path));
            return true;
        } catch (RepositoryNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("检查仓库失败: " + e.getMessage(), e);
        }
    }

    public static boolean isTaskFileExist(Git git, Integer taskId) {
        try {
            Repository repository = git.getRepository();
            ObjectId headId = repository.resolve("HEAD");

            if (headId == null) {
                // 仓库为空
                return false;
            }

            try (RevWalk revWalk = new RevWalk(repository)) {
                RevCommit headCommit = revWalk.parseCommit(headId);

                try (TreeWalk treeWalk =
                        TreeWalk.forPath(repository, String.format("tasks/%s", taskId), headCommit.getTree())) {
                    return treeWalk != null;
                }
            }

        } catch (Exception e) {
            System.err.println("检查路径失败: " + e.getMessage());
            return false;
        }
    }

    private String generateRepositoryKey(String url) throws NoSuchAlgorithmException {
        String normalized = url.trim().replaceAll("\\s+", " ").toLowerCase();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(normalized.getBytes(StandardCharsets.UTF_8));
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        return base64.substring(0, 32);
    }

    @Override
    public boolean commitTaskToGitRepository(Integer taskId, String message) {
        if (!isGitSyncEnabled(taskId)) {
            return false;
        }

        try (Git git = getOrInitLocalRepository(taskId)) {
            String taskPath = String.format("tasks/%d", taskId);

            if (!writeTaskContentToLocalRepository(taskId)) {
                // log.warn("任务 [{}] 写入本地仓库失败，跳过提交。", taskId);
                return false;
            }

            git.add().addFilepattern(taskPath).call();
            Status status = git.status().call();
            if (status.isClean()) {
                // log.info("任务 [{}] 无变更，跳过提交。", taskId);
                return true;
            }
            User currentUser = userService.getById(StpUtil.getLoginIdAsString());
            git.commit()
                    .setMessage(String.format("[%s] %s", currentUser.getUsername(), message))
                    .call();
            git.push().setForce(true).call();

            // log.info("任务 [{}] 已提交并推送到远程 Git 仓库。", taskId);
            return true;
        } catch (Exception e) {
            // log.error("任务 [{}] 提交到 Git 仓库失败。", taskId, e);
            return false;
        }
    }

    /**
     * 判断仓库是否存在未提交的变更（新增/修改/删除文件等）
     */
    @Override
    public boolean hasUncommittedChanges(Integer taskId) {
        if (!isGitSyncEnabled(taskId)) {
            return false;
        }

        try (Git git = getOrInitLocalRepository(taskId)) {
            String taskPath = String.format("tasks/%d", taskId);

            if (!writeTaskContentToLocalRepository(taskId)) {
                log.warn("任务 [{}] 写入本地仓库失败，跳过提交。", taskId);
                return false;
            }

            // 获取仓库状态
            Status status = git.status().addPath(taskPath).call();

            // 判断是否有未提交的改动
            boolean hasChanges = !status.isClean()
                    || !status.getAdded().isEmpty()
                    || !status.getChanged().isEmpty()
                    || !status.getRemoved().isEmpty()
                    || !status.getModified().isEmpty()
                    || !status.getUntracked().isEmpty();

            git.checkout().addPath(taskPath).call();

            return hasChanges;
        } catch (Exception e) {
            log.error("任务 [{}] 检查 Git 仓库变更失败。", taskId, e);
            return false;
        }
    }

    private boolean writeTaskContentToLocalRepository(Integer taskId) {
        try {
            TaskDTO task = taskService.getTaskInfoById(taskId);
            String sql = task.getStatement();
            if (Pattern.compile("EXECUTE\\s+JAR\\s+WITH.*").matcher(sql).find()) {
                Pattern pattern = Pattern.compile("'args'='base64@([A-Za-z0-9+/=]+)'");
                Matcher matcher = pattern.matcher(sql);
                if (matcher.find()) {
                    String base64Encoded = matcher.group(1);
                    String decoded = new String(Base64.getDecoder().decode(base64Encoded), StandardCharsets.UTF_8);
                    sql = matcher.replaceFirst("'args'='" + decoded + "'");
                }
            }
            List<ConfigItem> configs = task.getConfigJson().getCustomConfig();
            String confs = configs.stream()
                    .map(conf -> String.format("%s=%s", conf.getKey(), conf.getValue()))
                    .collect(Collectors.joining("\n"));
            GitRepositoryInfoDTO info = this.getGitRepositoryInfo(taskId);
            Path taskDir = Paths.get(String.format(
                    "%s/%s/%s/%s",
                    GIT_LOCAL_REPOSITORY_DIR,
                    this.generateRepositoryKey(info.getUrl()),
                    GIT_TASK_BASE_PATH,
                    String.valueOf(taskId)));
            if (!Files.exists(taskDir)) {
                Files.createDirectories(taskDir);
            }
            Files.write(taskDir.resolve(String.format("%s.sql", task.getName())), sql.getBytes(StandardCharsets.UTF_8));
            Files.write(
                    taskDir.resolve(String.format("%s.conf", task.getName())), confs.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            log.error("任务 [{}] writeTaskContentToLocalRepository失败。", taskId, e);
            return false;
        }
    }
}
