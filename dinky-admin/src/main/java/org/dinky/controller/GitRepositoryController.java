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

package org.dinky.controller;

import org.dinky.data.dto.GitRepositoryCommitDTO;
import org.dinky.data.dto.GitRepositoryInfoDTO;
import org.dinky.data.result.Result;
import org.dinky.service.GitRepositoryService;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.swagger.annotations.Api;

@RestController
@Api(tags = "Flink Task Git Log Controller")
@RequestMapping("/api/git/repository")
@SaCheckLogin
public class GitRepositoryController {

    @Autowired
    private GitRepositoryService gitRepositoryService;

    @GetMapping("/commits/{taskId}")
    public Result<List<GitRepositoryCommitDTO>> getTaskGitCommits(@PathVariable Integer taskId) {
        return Result.data(gitRepositoryService.getGitRepositoryCommits(taskId));
    }

    @GetMapping("/info/{taskId}")
    public Result<GitRepositoryInfoDTO> getTaskGitInfo(@PathVariable Integer taskId) {
        return Result.data(gitRepositoryService.getGitRepositoryInfo(taskId));
    }

    @PostMapping("/commit/{taskId}")
    public Result<Boolean> commitTaskToGitRepository(
            @PathVariable Integer taskId, @RequestBody Map<String, Object> body) {
        return Result.data(gitRepositoryService.commitTaskToGitRepository(
                taskId, body.getOrDefault("message", "").toString()));
    }

    @GetMapping("/hasUncommitted/{taskId}")
    public Result<Boolean> hasUncommittedChanges(@PathVariable Integer taskId) {
        return Result.data(gitRepositoryService.hasUncommittedChanges(taskId));
    }
}
