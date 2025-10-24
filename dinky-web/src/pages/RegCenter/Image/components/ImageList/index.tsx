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

import {
  handlePutData,
  queryDataByParams} from '@/services/BusinessCrud';
import { API_CONSTANTS } from '@/services/endpoints';
import { ImageInfo } from '@/types/RegCenter/data.d';
import { Space, Table, Tag } from 'antd';
import React, { useState } from 'react';
import { useAsyncEffect } from 'ahooks';
import Column from 'antd/es/table/Column';
import { Button, Descriptions, Input, Modal, Tabs } from 'antd/lib';

export default () => {
  /**
   * state
   */
  const [data, setData] = useState<ImageInfo[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ImageInfo | null>(null);
  const [id, setId] = useState(-1);
  const [name, setName] = useState('');
  const [note, setNote] = useState('');
  const [versions, setVersions] = useState<string[]>([]);
  const [expandedRowKeys, setExpandedRowKeys] = useState<React.Key[]>([]);

  const handleExpand = (expanded: boolean, record: ImageInfo) => {
    if (expanded) {
      // 只展开当前行
      setExpandedRowKeys([record.id]);
    } else {
      setExpandedRowKeys([]);
    }
  };

  const queryImageList = async () => {
    const queryData = (await queryDataByParams<ImageInfo[]>(API_CONSTANTS.IMAGE_LIST))!!;
    setData(queryData);
  };

  const openEditModal = (record: ImageInfo) => {
    setEditingRecord(record);
    setId(record.id);
    setName(record.name);
    setNote(record.note);
    setVersions([...record.versions]);
    setIsModalOpen(true);
  };

  const openAddModal = () => {
    setEditingRecord(null);
    setId(-1);
    setName('');
    setNote('');
    setVersions([]);
    setIsModalOpen(true);
  };

  const handleOk = async () => {
    if (editingRecord) {
      await handlePutData(API_CONSTANTS.IMAGE_SAVE, {
        id: id == -1 ? null : id,
        name: name,
        note: note,
        versions: versions
      });
      await queryImageList();
    }
    setIsModalOpen(false);
  };

  const handleCancel = () => {
    setIsModalOpen(false);
  };

  const updateVersion = (index: number, value: string) => {
    const newVersions = [...versions];
    newVersions[index] = value;
    setVersions(newVersions);
  };

  const addVersion = () => {
    setVersions([...versions, '']);
  };

  const removeVersion = (index: number) => {
    const newVersions = versions.filter((_, i) => i !== index);
    setVersions(newVersions);
  };

  /**
   * query  list
   */
  useAsyncEffect(async () => {
    await queryImageList();
  }, []);
  
  /**
   * render
   */
  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
        <Button type="primary" onClick={() => openAddModal()}>添加镜像</Button>
      </div>

      <Table<ImageInfo>
        dataSource={data}
        rowKey="id"
        expandable={{
          expandedRowRender: (record) => (
            <Tabs
              tabPosition="left"
              items={[
                {
                  key: '1',
                  label: '版本',
                  children: (
                    <>
                      {record.versions.map((v, index) => (
                        <Descriptions
                          key={index}
                          title={`版本: ${v}`}
                          column={1}
                          bordered
                          size="small"
                        >
                          <Descriptions.Item label="版本">{v}</Descriptions.Item>
                          <Descriptions.Item label="名称">{record.name}</Descriptions.Item>
                          <Descriptions.Item label="URL">{`${record.name}:${v}`}</Descriptions.Item>
                        </Descriptions>
                      ))}
                    </>
                  ),
                },
              ]}
            />
          ),
          expandedRowKeys,
          onExpand: handleExpand
        }}
      >
        <Column title="name" dataIndex="name" key="name" />
        <Column title="note" dataIndex="note" key="note" />
        <Column
          title="Action"
          key="action"
          render={(_: any, record: ImageInfo) => (
            <Space size="middle">
              <a onClick={() => openEditModal(record)}>编辑</a>
            </Space>
          )}
        />
      </Table>

      <Modal
        title={editingRecord ? '编辑镜像' : '添加镜像'}
        open={isModalOpen}
        onOk={handleOk}
        onCancel={handleCancel}
        width={600}
      >
        <div style={{ marginBottom: 16 }}>
          <label>名称: </label>
          <Input value={name} onChange={(e) => setName(e.target.value)} />
        </div>
        <div style={{ marginBottom: 16 }}>
          <label>说明: </label>
          <Input value={note} onChange={(e) => setNote(e.target.value)} />
        </div>
        <div>
          <label>版本: </label>
          {versions.map((v, index) => (
            <Space key={index} style={{ display: 'flex', marginBottom: 8 }}>
              <Input value={v} onChange={(e) => updateVersion(index, e.target.value)} />
              <Button danger onClick={() => removeVersion(index)}>
                删除
              </Button>
            </Space>
          ))}
          <Button type="dashed" onClick={addVersion} style={{ width: '100%' }}>
            添加版本
          </Button>
        </div>
      </Modal>
    </>
  );
};
