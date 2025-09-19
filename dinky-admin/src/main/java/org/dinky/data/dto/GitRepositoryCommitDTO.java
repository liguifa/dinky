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

package org.dinky.data.dto;

import org.dinky.data.annotations.ProcessId;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 *  Param for debug flink sql and common sql
 */
@Getter
@Setter
@AllArgsConstructor
@ApiModel(value = "GitRepositoryCommitDTO", description = "git repository info")
public class GitRepositoryCommitDTO {

    @ApiModelProperty(value = "id", dataType = "String", example = "http://xxx.com", notes = "git repository commit id")
    @ProcessId
    private String id;

    @ApiModelProperty(value = "author", dataType = "String", example = "xxx", notes = "git repository commit author")
    @ProcessId
    private String author;

    @ApiModelProperty(value = "date", dataType = "Date", example = "xxx", notes = "git commit time")
    @ProcessId
    private Date date;

    @ApiModelProperty(value = "message", dataType = "String", example = "xxx", notes = "git commit message")
    @ProcessId
    private String message;
}
