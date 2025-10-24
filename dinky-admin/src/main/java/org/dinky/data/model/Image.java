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

package org.dinky.data.model;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableName;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 任务
 *
 * @since 2021-05-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("dinky_image")
@NoArgsConstructor
@ApiModel(value = "Image", description = "Image Information")
public class Image implements Serializable {

    @ApiModelProperty(value = "Id", dataType = "Long", notes = "Dialect for the task")
    private long id;

    @ApiModelProperty(value = "Name", dataType = "String", notes = "Dialect for the task")
    private String name;

    @ApiModelProperty(value = "Note", dataType = "String", notes = "Dialect for the task")
    private String Note;

    @ApiModelProperty(value = "Versions", dataType = "String", notes = "Dialect for the task")
    private String versions;
}
