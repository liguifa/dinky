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

import org.dinky.data.dto.ImageDTO;
import org.dinky.data.model.Image;
import org.dinky.mapper.ImageMapper;
import org.dinky.mybatis.service.impl.SuperServiceImpl;
import org.dinky.service.ImageService;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * JobHistoryServiceImpl
 *
 * @since 2022/3/2 20:00
 */
@Service
@Slf4j
public class ImageServiceImpl extends SuperServiceImpl<ImageMapper, Image> implements ImageService {
    @Override
    public List<ImageDTO> getImages() {
        log.info("getImages 2");
        List<Image> imges = baseMapper.listAllImages();
        log.info("getImages cnt: " + String.valueOf(imges.size()));
        return imges.stream()
                .map(image -> new ImageDTO(
                        image.getId(), image.getName(), image.getVersions().split(","), image.getNote()))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getTasksByImageId(long imageId) {
        return baseMapper.listAllTasksByImageId(imageId);
    }

    @Override
    public boolean createOrUpdateImage(ImageDTO imageDto) {
        Image image = new Image();
        image.setId(imageDto.getId());
        image.setName(imageDto.getName());
        image.setNote(imageDto.getNote());
        image.setVersions(String.join(",", imageDto.getVersions()));
        return baseMapper.insertOrUpdate(image);
    }
}
